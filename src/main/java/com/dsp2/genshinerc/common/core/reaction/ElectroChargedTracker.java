package com.dsp2.genshinerc.common.core.reaction;

import com.dsp2.genshinerc.common.config.ServerConfig;
import com.dsp2.genshinerc.common.core.damage.DamageKind;
import com.dsp2.genshinerc.common.core.damage.ElementalStats;
import com.dsp2.genshinerc.common.core.element.ElementAuraManager;
import com.dsp2.genshinerc.common.core.element.ElementType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

final class ElectroChargedTracker {
    private static final float STEP = 0.4f;
    private static final int COOLDOWN = 20;

    private static final class State {
        UUID source;
        int cooldown;
    }

    private static final Map<LivingEntity, State> ACTIVE = new WeakHashMap<>();

    private ElectroChargedTracker() {}

    static void start(LivingEntity target, @Nullable Entity attacker) {
        if (!coexist(target)) return;
        State state = ACTIVE.computeIfAbsent(target, k -> new State());
        if (attacker != null) state.source = attacker.getUUID();
    }

    static void tick() {
        for (LivingEntity target : new ArrayList<>(ACTIVE.keySet())) {
            State state = ACTIVE.get(target);
            if (state == null) continue;
            if (target.isRemoved() || !target.isAlive() || !coexist(target)) {
                ACTIVE.remove(target);
                continue;
            }
            if (state.cooldown > 0) { state.cooldown--; continue; }
            state.cooldown = COOLDOWN;
            ElementAuraManager.consume(target, ElementType.HYDRO, STEP, false);
            ElementAuraManager.consume(target, ElementType.ELECTRO, STEP, false);
            Entity source = pickSource(target, state);
            ReactionDamage.deal(target, source, DamageKind.ELECTRO, Reaction.ELECTRO_CHARGED, ElementalStats.of(source));
        }
    }

    static void reset() { ACTIVE.clear(); }

    private static boolean coexist(LivingEntity t) {
        return ElementAuraManager.hasAura(t, ElementType.HYDRO) && ElementAuraManager.hasAura(t, ElementType.ELECTRO);
    }

    @Nullable
    private static Entity pickSource(LivingEntity target, State state) {
        double radius = ServerConfig.EC_RADIUS.get();
        double r2 = radius * radius;
        List<Player> players = target.level().getEntitiesOfClass(Player.class, target.getBoundingBox().inflate(radius),
                p -> p.isAlive() && !p.isSpectator() && p != target && p.distanceToSqr(target) <= r2);
        if (!players.isEmpty()) {
            double[] weights = new double[players.size()];
            double total = 0;
            for (int i = 0; i < weights.length; i++) {
                weights[i] = 1.0 / (1.0 + players.get(i).distanceToSqr(target));
                total += weights[i];
            }
            double roll = target.level().getRandom().nextDouble() * total;
            for (int i = 0; i < weights.length; i++) {
                roll -= weights[i];
                if (roll <= 0) return players.get(i);
            }
            return players.get(players.size() - 1);
        }
        if (state.source != null && target.level() instanceof ServerLevel level) return level.getEntity(state.source);
        return null;
    }
}
