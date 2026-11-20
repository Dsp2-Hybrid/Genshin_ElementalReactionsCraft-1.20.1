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
    private static String activePalette;

    public static String indicatorPalette() {
        return activePalette;
    }
    private record ShieldImpact(LivingEntity target, DamageSource source, float cost) {}
    private static ShieldImpact shieldImpact;

    public static float overloadShieldCost(LivingEntity target, DamageSource source) {
        ShieldImpact hit = shieldImpact;
        return hit != null && hit.target() == target && hit.source() == source ? hit.cost() : 1f;
    }

    static void dealOverload(LivingEntity target, @Nullable Entity attacker, ElementalStats stats,
                             net.minecraft.world.phys.Vec3 center) {
        double radius = Math.max(0.01, ServerConfig.OVERLOAD_RADIUS.get());
        double ratio = Math.min(1.0, target.getBoundingBox().getCenter().distanceTo(center) / radius);
        double proximity = 0.28 + 0.72 * Math.pow(1.0 - ratio, 1.25);
        int world = Math.max(0, com.dsp2.genshinerc.common.level.WorldLevel.get());
        int defender = ElementalStats.of(target).level();
        int strength = attacker == null ? 1 + 8 * world : stats.level();
        double levels = net.minecraft.util.Mth.clamp((strength - defender) / 90.0, -0.35, 0.35);
        double mastery = attacker == null || !Double.isFinite(stats.mastery()) ? 0.0 : Math.max(0.0, stats.mastery());
        double factor = (attacker == null ? 0.78 : 1.0)
                * (1.0 + 0.035 * world) * (1.0 + levels) * (1.0 + 0.25 * mastery / (mastery + 700.0));
        float cost = (float) (1.0 + Math.min(1.5, proximity * factor));
        deal(target, attacker, DamageKind.PYRO, Reaction.OVERLOAD, stats, cost, false);
    }

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
        deal(target, attacker, kind, reaction, stats, 1f, false);
    }

    static void dealElectroCharged(LivingEntity target, @Nullable Entity attacker,
                                   ElementalStats stats, boolean natural) {
        deal(target, attacker, DamageKind.ELECTRO, Reaction.ELECTRO_CHARGED, stats, 1f, natural);
    }

    private static void deal(LivingEntity target, @Nullable Entity attacker, DamageKind kind, Reaction reaction,
                             ElementalStats stats, float shieldCost, boolean naturalElectroCharged) {
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
        } else if (reaction == Reaction.SWIRL || reaction == Reaction.ELECTRO_CHARGED) {
            if (naturalElectroCharged) {
                amount = ElementalMath.transformativeNoEM(coefficient, 1);
            } else {
                double mastery = Double.isFinite(stats.mastery()) ? Math.max(0.0, stats.mastery()) : 0.0;
                amount = ElementalMath.transformative(coefficient, new ElementalStats(stats.level(), mastery));
            }
        } else if (stats.mastery() > 0.0) {
            amount = ElementalMath.transformative(coefficient, stats);
        } else {
            amount = ElementalMath.transformativeNoEM(coefficient, stats.level());
        }
        if (reaction == Reaction.SHATTER || reaction == Reaction.OVERLOAD
                || reaction == Reaction.SWIRL || reaction == Reaction.ELECTRO_CHARGED) {
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
        ShieldImpact previousImpact = shieldImpact;
        shieldImpact = shieldCost > 1f ? new ShieldImpact(target, source, shieldCost) : null;
        String previousKey = activeIndicatorKey;
        String previousPalette = activePalette;
        String palette = reaction.damagePalette(kind);
        activePalette = palette;
        activeIndicatorKey = reaction.indicatorKey(kind);
        dealing = true;
        try {
            target.invulnerableTime = 0;
            target.hurt(source, (float) amount);
        } finally {
            target.invulnerableTime = invulnerable;
            dealing = previous;
            activeIndicatorKey = previousKey;
            activePalette = previousPalette;
            shieldImpact = previousImpact;
        }

        float lostHp = Math.max(0f, hp - target.getHealth());
        float lostAbsorb = Math.max(0f, absorb - target.getAbsorptionAmount());
        if (lostHp < 0.001f && lostAbsorb < 0.001f) return;
        DamageServerEventHandler.discount(target, lostHp, lostAbsorb);
        if (ServerConfig.ENABLED.get()) {
            DamageIndicatorNetworkHandler.sendDamage(target, reaction.indicatorKey(kind), lostHp, false,
                    target.getHealth() <= 0.001f, false, lostAbsorb, palette, null, false, false);
        }
    }

    private static boolean allow(LivingEntity target, Reaction reaction, long now) {
        ArrayDeque<Long> hits = HITS.computeIfAbsent(target, k -> new EnumMap<>(Reaction.class))
                .computeIfAbsent(reaction, k -> new ArrayDeque<>());
        int window = reaction == Reaction.ELECTRO_CHARGED ? 20 : WINDOW_TICKS;
        while (!hits.isEmpty() && now - hits.peekFirst() >= window) hits.pollFirst();
        int max = reaction == Reaction.OVERLOAD || reaction == Reaction.ELECTRO_CHARGED ? 1 : 2;
        if (hits.size() >= max) return false;
        hits.addLast(now);
        return true;
    }

    static void reset() { HITS.clear(); }
}