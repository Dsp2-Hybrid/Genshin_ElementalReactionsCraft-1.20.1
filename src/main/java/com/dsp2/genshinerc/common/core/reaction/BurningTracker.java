package com.dsp2.genshinerc.common.core.reaction;

import com.dsp2.genshinerc.common.core.damage.DamageKind;
import com.dsp2.genshinerc.common.core.damage.ElementalStats;
import com.dsp2.genshinerc.common.core.element.ElementAuraManager;
import com.dsp2.genshinerc.common.core.element.ElementType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

final class BurningTracker {
    private static final float FUEL_PER_TICK = 0.1f;
    private static final int INTERVAL = 5;
    private static final float EPS = 1.0e-4f;

    private static final class State {
        UUID owner;
        int cooldown;
    }

    private static final Map<LivingEntity, State> ACTIVE = new WeakHashMap<>();

    private BurningTracker() {}

    static void start(LivingEntity target, @Nullable Entity attacker) {
        if (ACTIVE.containsKey(target) || !ElementAuraManager.hasAura(target, ElementType.PYRO) || fuel(target) <= EPS) return;
        ElementAuraManager.startBurning(target);
        State state = new State();
        state.owner = attacker == null ? null : attacker.getUUID();
        ACTIVE.put(target, state);
    }

    static void tick() {
        for (LivingEntity target : new ArrayList<>(ACTIVE.keySet())) {
            State state = ACTIVE.get(target);
            if (state == null) continue;
            if (target.isRemoved() || !target.isAlive() || !ElementAuraManager.hasAura(target, ElementType.BURNING)) {
                ACTIVE.remove(target);
                continue;
            }
            if (state.cooldown > 0) { state.cooldown--; continue; }
            state.cooldown = INTERVAL - 1;

            Entity owner = state.owner != null && target.level() instanceof ServerLevel level ? level.getEntity(state.owner) : null;
            consumeFuel(target, FUEL_PER_TICK);
            ReactionDamage.deal(target, owner, DamageKind.PYRO, Reaction.BURNING, ElementalStats.of(owner));
            if (fuel(target) <= EPS) {
                ElementAuraManager.remove(target, ElementType.BURNING, false);
                ACTIVE.remove(target);
            }
        }
    }

    static void reset() { ACTIVE.clear(); }

    private static float fuel(LivingEntity t) {
        return ElementAuraManager.amount(t, ElementType.DENDRO) + ElementAuraManager.amount(t, ElementType.QUICKEN);
    }

    private static void consumeFuel(LivingEntity t, float amount) {
        float used = ElementAuraManager.consume(t, ElementType.DENDRO, amount, false);
        if (used < amount) ElementAuraManager.consume(t, ElementType.QUICKEN, amount - used, false);
    }
}
