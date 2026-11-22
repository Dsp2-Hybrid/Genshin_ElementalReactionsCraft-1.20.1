package com.dsp2.genshinerc.common.core.combat.zhenmie;

import net.minecraft.util.Mth;

public final class ZhenMieRules {
    public static final float LONG_STAGGER_MIN_POINT = 0.90f;
    public static final float KNOCKDOWN_MIN_POINT = 0.50f;
    public static final float KNOCKUP_MIN_POINT = 0.75f;
    public static final float MIE_KEPT_AFTER_CLASH = 0.50f;

    public static final int ZHEN_DECAY_DELAY_TICKS = 60;
    public static final float ZHEN_DECAY_PER_TICK = 1.0f;
    public static final int MIE_DECAY_DELAY_TICKS = 140;
    public static final float MIE_DECAY_PER_TICK = 0.4f;
    public static final int DECAY_SYNC_INTERVAL = 4;

    public static final int STAGGER_MEMORY_STEP = 120;
    public static final int DIRECT_IMPACT_IMMUNITY_TICKS = 12;
    public static final int COMMON_IMMUNITY_TICKS = 80;
    public static final int COMMON_IMMUNITY_FADE_TICKS = 100;

    public static final float STAGGER_VULNERABILITY_MIN = 0.50f;
    public static final float STAGGER_VULNERABILITY_MAX = 0.50f;

    public static final float SUPPRESSION_MIN = 0.02f;
    public static final float SUPPRESSION_MAX = 8.0f;
    public static final float SUPPRESSION_UP_DIVISOR = 20.0f;
    public static final float SUPPRESSION_DOWN_DIVISOR = 8.0f;

    private ZhenMieRules() {}

    public static float vulnerability(float meetingPoint) {
        float point = Mth.clamp(meetingPoint, 0f, 1f);
        float weight = Mth.clamp(1f - point * 0.35f, 0.65f, 1f);
        return Mth.lerp(weight, STAGGER_VULNERABILITY_MIN, STAGGER_VULNERABILITY_MAX);
    }

    public static float resistanceMultiplier(double resistance) {
        float value = Mth.clamp((float) resistance, 0f, 1f);
        if (value <= 0.75f) return 1f - value;
        return 0.25f / (1f + (value - 0.75f) * 4f);
    }

    public static float powerMultiplier(double power) {
        if (!Double.isFinite(power) || power <= 0.0) return 1f;
        double soft = Math.sqrt(Math.max(0.0, power));
        double hard = Math.log1p(Math.max(0.0, power)) * 0.18;
        return (float) Mth.clamp(1.0 + soft * 0.72 + hard, 1.0, 6.0);
    }

    public static float resolveMultiplier(double resolve, double force) {
        float resolveValue = Mth.clamp((float) resolve, 0f, 1f);
        float forceValue = Mth.clamp((float) force, 0f, 1f);
        return resistanceMultiplier(resolveValue + forceValue * 0.25f);
    }

    public static float critForceMultiplier(double force) {
        if (!Double.isFinite(force) || force <= 0.0) return 1f;
        double value = Math.sqrt(force);
        return (float) Mth.clamp(1.0 + value * 0.85, 1.0, 5.0);
    }

    public static float impactImmunityMultiplier(long now, long directUntil, long commonUntil) {
        if (now < directUntil) return 0f;
        if (now >= commonUntil) return 1f;

        long duration = Math.max(1L, commonUntil - directUntil);
        float progress = Mth.clamp((now - directUntil) / (float) duration, 0f, 1f);
        return progress * progress;
    }

    public static float commonImmunityMultiplier(long now, long commonUntil, long fadeUntil) {
        if (now < commonUntil) return 0f;
        if (now >= fadeUntil) return 1f;

        long duration = Math.max(1L, fadeUntil - commonUntil);
        float progress = Mth.clamp((now - commonUntil) / (float) duration, 0f, 1f);
        return progress * progress;
    }

    public static int staggerTicks(ClashResult result, int recentClashes) {
        int base = Math.round(result.stunSeconds() * 20f);
        float repeated = (float) Math.pow(0.60, Math.max(0, recentClashes));
        return Math.max(7, Math.round(base * repeated));
    }

    public static ClashResult classify(float meetingPoint) {
        float point = Mth.clamp(meetingPoint, 0f, 1f);
        if (point >= LONG_STAGGER_MIN_POINT) return ClashResult.LONG_STAGGER;
        if (point >= KNOCKDOWN_MIN_POINT) return ClashResult.KNOCKDOWN;
        return ClashResult.CRUSH;
    }

    public record Delta(float zhen, float mie) {
        public static final Delta NONE = new Delta(0f, 0f);
    }

    private static Delta delta(float zhen, float mie) {
        return new Delta(Math.max(0f, zhen), Math.max(0f, mie));
    }

    public static float maxCapacity(float maxHealth, int level) {
        float health = Math.max(1f, maxHealth);
        float levelFactor = 1f + Math.max(0, Math.min(120, level)) * 0.0125f;
        return 40.0f * (float) Math.sqrt(health / 6.0) * levelFactor;
    }

    public static float suppression(int attackerLevel, int defenderLevel) {
        int diff = attackerLevel - defenderLevel;
        double value = diff >= 0
                ? Math.pow(2.0, diff / SUPPRESSION_UP_DIVISOR)
                : Math.pow(2.0, diff / SUPPRESSION_DOWN_DIVISOR);
        return (float) Mth.clamp(value, SUPPRESSION_MIN, SUPPRESSION_MAX);
    }

    public static float damageFactor(float damage) {
        if (!Float.isFinite(damage) || damage <= 0f) return 0.1f;
        return (float) Math.max(0.1, Math.sqrt(damage / 6.0));
    }

    public static float hitFractionLimit(AttackIntensity intensity) {
        return switch (intensity) {
            case SILENT, LIGHT -> 0.08f;
            case HEAVY -> 0.20f;
            case ASSAULT -> 0.35f;
            case FINISHER -> 0.60f;
        };
    }

    public static Delta forDefense(AttackIntensity intensity, DefenseOutcome outcome) {
        return switch (outcome) {
            case DODGE -> Delta.NONE;
            case HIT -> switch (intensity) {
                case SILENT -> delta(0f, 5f);
                case LIGHT -> delta(0f, 8f);
                case HEAVY -> delta(0f, 18f);
                case ASSAULT -> delta(0f, 30f);
                case FINISHER -> delta(0f, 100f);
            };
            case BLOCK -> switch (intensity) {
                case SILENT -> delta(6f, 0f);
                case LIGHT -> delta(10f, 0f);
                case HEAVY -> delta(12f, 8f);
                case ASSAULT -> delta(15f, 15f);
                case FINISHER -> delta(0f, 60f);
            };
            case PARRY -> switch (intensity) {
                case SILENT -> delta(8f, 0f);
                case LIGHT -> delta(12f, 0f);
                case HEAVY -> delta(5f, 10f);
                case ASSAULT -> delta(5f, 18f);
                case FINISHER -> delta(0f, 40f);
            };
        };
    }

    public static Delta forDefense(
            AttackIntensity intensity,
            DefenseOutcome outcome,
            int attackerLevel,
            int defenderLevel,
            float damage,
            float capacity) {
        Delta base = forDefense(intensity, outcome);
        float damageFactor = damageFactor(damage);

        float zhen = base.zhen() * damageFactor;
        float mie = base.mie() * damageFactor;

        float limit = Math.max(0f, capacity) * hitFractionLimit(intensity);
        float total = zhen + mie;

        if (limit > 0f && total > limit) {
            float scale = limit / total;
            zhen *= scale;
            mie *= scale;
        }

        float levelSuppression = suppression(attackerLevel, defenderLevel);
        return new Delta(zhen * levelSuppression, mie * levelSuppression);
    }

    public static float knockupHeight(float meetingPoint, double force) {
        float point = Mth.clamp(meetingPoint, 0.75f, 1f);
        float proximity = (point - 0.75f) / 0.25f;
        return (float) (0.28 + proximity * 0.42 * critForceMultiplier(force));
    }

    public static float knockupHorizontal(float meetingPoint, double force) {
        float point = Mth.clamp(meetingPoint, 0.75f, 1f);
        float proximity = (point - 0.75f) / 0.25f;
        return (float) (0.12 + proximity * 0.24 * critForceMultiplier(force));
    }
}