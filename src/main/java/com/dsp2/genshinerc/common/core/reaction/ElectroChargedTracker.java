package com.dsp2.genshinerc.common.core.reaction;

import com.dsp2.genshinerc.common.config.ServerConfig;
import com.dsp2.genshinerc.common.core.damage.DamageKind;
import com.dsp2.genshinerc.common.core.damage.ElementalStats;
import com.dsp2.genshinerc.common.core.element.ElementAuraManager;
import com.dsp2.genshinerc.common.core.element.ElementType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

final class ElectroChargedTracker {
    private static final float STEP = 0.4f;
    private static final int PERIOD = 20;

    private static final class State {
        UUID source;
        ElementalStats stats = ElementalStats.NONE;
        boolean natural = true;
        long nextTick;
    }

    private static final Map<LivingEntity, State> ACTIVE = new WeakHashMap<>();

    private ElectroChargedTracker() {}

    static void start(LivingEntity target, @Nullable Entity attacker, boolean natural) {
        if (!natural && !coexist(target)) return;
        State state = ACTIVE.computeIfAbsent(target, ignored -> new State());
        if (!natural && attacker != null) {
            state.source = attacker.getUUID();
            state.stats = ElementalStats.of(attacker);
            state.natural = false;
        }
    }

    static void tick() {
        for (LivingEntity target : new ArrayList<>(ACTIVE.keySet())) {
            State state = ACTIVE.get(target);
            if (state == null) continue;
            if (!(target.level() instanceof ServerLevel level) || !target.isAlive()
                    || target.isRemoved() || !coexist(target)) {
                ACTIVE.remove(target);
                continue;
            }

            long now = level.getGameTime();
            if (now < state.nextTick) continue;
            state.nextTick = now + PERIOD;

            Entity owner = state.source == null ? null : level.getEntity(state.source);
            ElementalStats stats = owner == null ? state.stats : ElementalStats.of(owner);
            double radius = ServerConfig.EC_RADIUS.get();
            var center = target.getBoundingBox().getCenter();

            ElementAuraManager.consume(target, ElementType.HYDRO, STEP, false);
            ElementAuraManager.consume(target, ElementType.ELECTRO, STEP, false);
            ReactionParticles.spawnElectroArea(target, radius);
            ReactionSoundEvents.playElectroCharged(target);
            ReactionDamage.dealElectroCharged(target, owner, stats, state.natural);

            if (radius <= 0.0) continue;
            for (LivingEntity near : level.getEntitiesOfClass(LivingEntity.class,
                    target.getBoundingBox().inflate(radius), entity -> entity != target
                            && entity.isAlive() && !entity.isSpectator() && !(entity instanceof ArmorStand)
                            && entity.getBoundingBox().getCenter().distanceToSqr(center) <= radius * radius
                            && ElementAuraManager.hasAura(entity, ElementType.HYDRO)
                            && !coexist(entity))) {
                ReactionParticles.spawnElectroCharged(near);
                ReactionDamage.dealElectroCharged(near, owner, stats, state.natural);
            }
        }
    }

    static void reset() { ACTIVE.clear(); }

    private static boolean coexist(LivingEntity t) {
        return ElementAuraManager.hasAura(t, ElementType.HYDRO) && ElementAuraManager.hasAura(t, ElementType.ELECTRO);
    }

}
