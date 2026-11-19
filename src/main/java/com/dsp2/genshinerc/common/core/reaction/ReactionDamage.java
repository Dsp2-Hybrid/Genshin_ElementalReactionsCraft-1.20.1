package com.dsp2.genshinerc.common.core.reaction;

import com.dsp2.genshinerc.common.config.ServerConfig;
import com.dsp2.genshinerc.common.core.damage.DamageKind;
import com.dsp2.genshinerc.common.core.damage.ElementalDamage;
import com.dsp2.genshinerc.common.core.damage.ElementalMath;
import com.dsp2.genshinerc.common.core.damage.ElementalStats;
import com.dsp2.genshinerc.common.network.base.damageindicator.DamageIndicatorNetworkHandler;
import com.dsp2.genshinerc.server.render.damageindicator.DamageServerEventHandler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.EnumMap;
import java.util.Map;
import java.util.WeakHashMap;

public final class ReactionDamage {
    private static final int WINDOW_TICKS = 10;
    private static boolean dealing;
    private static String activeIndicatorKey;

    public static String indicatorKey(String fallback) {
        return activeIndicatorKey == null ? fallback : activeIndicatorKey;
    }
    private static final Map<LivingEntity, Map<Reaction, ArrayDeque<Long>>> HITS = new WeakHashMap<>();

    private ReactionDamage() {}

    public static boolean isDealing() { return dealing; }

    public static boolean isOverloadHit() {
        return dealing && "overload".equals(activeIndicatorKey);
    }

    public static void deal(LivingEntity target, @Nullable Entity attacker, DamageKind kind, Reaction reaction,
                            ElementalStats stats) {
        if (ElementReactions.isNatural() && reaction != Reaction.OVERLOAD) return;
        if (!(target.level() instanceof ServerLevel level) || !target.isAlive()) return;
        if (!allow(target, reaction, level.getGameTime())) return;

        float coefficient = switch (reaction) {
            case OVERLOAD -> ServerConfig.OVERLOAD_COEFFICIENT.get().floatValue();
            case SHATTER -> ServerConfig.SHATTER_COEFFICIENT.get().floatValue();
            default -> reaction.coefficient();
        };

        double amount;
        if (reaction == Reaction.SHATTER) {
            double mastery = Math.max(0.0, stats.mastery());
            double multiplier = 1.0 + 16.0 * mastery / (mastery + 2000.0)
                    + ServerConfig.SHATTER_REACTION_BONUS.get();
            amount = coefficient * ElementalMath.levelMultiplier(stats.level()) * multiplier * ServerConfig.SCALE.get();
        } else if (reaction == Reaction.OVERLOAD) {
            amount = attacker == null
                    ? ElementalMath.transformativeNoEM(coefficient, 1)
                    : ElementalMath.transformative(coefficient, stats);
        } else if (stats.mastery() > 0.0) {
            amount = ElementalMath.transformative(coefficient, stats);
        } else {
            amount = ElementalMath.transformativeNoEM(coefficient, stats.level());
        }
        if (reaction == Reaction.SHATTER || reaction == Reaction.OVERLOAD) {
            var attribute = kind.resistanceAttribute();
            var instance = attribute == null ? null : target.getAttribute(attribute);
            if (instance != null) amount *= ElementalMath.resistanceMultiplier(instance.getValue());
        }
        amount = ElementalMath.capReaction(amount, target);
        if (!Double.isFinite(amount) || amount <= 0.0) return;

        Entity cause = (reaction == Reaction.BURNING || reaction == Reaction.ELECTRO_CHARGED) ? null : attacker;
        DamageSource source = ElementalDamage.source(level, kind, attacker, cause);

        float hp = target.getHealth();
        float absorb = target.getAbsorptionAmount();
        int invulnerable = target.invulnerableTime;
        boolean previous = dealing;
        String previousKey = activeIndicatorKey;
        activeIndicatorKey = reaction.indicatorKey(kind);
        dealing = true;
        try {
            target.invulnerableTime = 0;
            target.hurt(source, (float) amount);
        } finally {
            target.invulnerableTime = invulnerable;
            dealing = previous;
            activeIndicatorKey = previousKey;
        }

        float lostHp = Math.max(0f, hp - target.getHealth());
        float lostAbsorb = Math.max(0f, absorb - target.getAbsorptionAmount());
        if (lostHp < 0.001f && lostAbsorb < 0.001f) return;
        DamageServerEventHandler.discount(target, lostHp, lostAbsorb);
        if (ServerConfig.ENABLED.get()) {
            DamageIndicatorNetworkHandler.sendDamage(target, reaction.indicatorKey(kind), lostHp, false,
                    target.getHealth() <= 0.001f, false, lostAbsorb, null, null, false, false);
        }
    }

    private static boolean allow(LivingEntity target, Reaction reaction, long now) {
        ArrayDeque<Long> hits = HITS.computeIfAbsent(target, k -> new EnumMap<>(Reaction.class))
                .computeIfAbsent(reaction, k -> new ArrayDeque<>());
        while (!hits.isEmpty() && now - hits.peekFirst() >= WINDOW_TICKS) hits.pollFirst();
        int max = reaction == Reaction.OVERLOAD ? 1 : 2;
        if (hits.size() >= max) return false;
        hits.addLast(now);
        return true;
    }

    static void reset() { HITS.clear(); }
}