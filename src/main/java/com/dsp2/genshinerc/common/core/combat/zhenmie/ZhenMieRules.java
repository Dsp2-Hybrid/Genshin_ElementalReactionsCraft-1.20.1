package com.dsp2.genshinerc.common.core.combat.zhenmie;

import net.minecraft.util.Mth;

public final class ZhenMieRules {
    private ZhenMieRules() {}
    
    public static final float LONG_STAGGER_MIN_POINT = 0.9f;
    public static final float KNOCKDOWN_MIN_POINT = 0.5f;
    public static final float MIE_KEPT_AFTER_CLASH = 0.5f;
    public static final int ZHEN_DECAY_DELAY_TICKS = 60;
    public static final float ZHEN_DECAY_PER_TICK = 1.0f;
    public static final int MIE_DECAY_DELAY_TICKS = 140;
    public static final float MIE_DECAY_PER_TICK = 0.4f;
    public static final int DECAY_SYNC_INTERVAL = 4;

    public static final float SUPPRESSION_MIN = 0.02f;
    public static final float SUPPRESSION_MAX = 8.0f;
    public static final float SUPPRESSION_UP_DIVISOR = 20.0f;
    public static final float SUPPRESSION_DOWN_DIVISOR = 8.0f;

    public static ClashResult classify(float meetingPoint) {
        if (meetingPoint >= LONG_STAGGER_MIN_POINT) return ClashResult.LONG_STAGGER;
        if (meetingPoint >= KNOCKDOWN_MIN_POINT) return ClashResult.KNOCKDOWN;
        return ClashResult.CRUSH;
    }

    public record Delta(float zhen, float mie) { public static final Delta NONE = new Delta(0f, 0f); }
    
    private static Delta d(float zhen, float mie) { return new Delta(zhen, mie); }

    public static float maxCapacity(float maxHealth, int level) {
        return 40.0f * (float)Math.sqrt(maxHealth / 6.0);
    }

    public static float suppression(int attackerLevel, int defenderLevel) {
        int diff = attackerLevel - defenderLevel;
        double s = diff >= 0
                ? Math.pow(2.0, diff / SUPPRESSION_UP_DIVISOR)
                : Math.pow(2.0, diff / SUPPRESSION_DOWN_DIVISOR);
        return (float) Mth.clamp(s, SUPPRESSION_MIN, SUPPRESSION_MAX);
    }

    public static float damageFactor(float damage) {
        return (float) Math.max(0.1, Math.sqrt(damage / 6.0));
    }

    public static float hitFractionLimit(AttackIntensity i) {
        return switch (i) {
            case SILENT, LIGHT -> 0.08f;
            case HEAVY -> 0.20f;
            case ASSAULT -> 0.35f;
            case FINISHER -> 0.60f;
        };
    }

    public static Delta forDefense(AttackIntensity i, DefenseOutcome o) {
        return switch (o) {
            case DODGE -> Delta.NONE;
            case HIT -> switch (i) { case SILENT -> d(0f, 5f); case LIGHT -> d(0f, 8f); case HEAVY -> d(0f, 18f); case ASSAULT -> d(0f, 30f); case FINISHER -> d(0f, 100f); };
            case BLOCK -> switch (i) { case SILENT -> d(6f, 0f); case LIGHT -> d(10f, 0f); case HEAVY -> d(12f, 8f); case ASSAULT -> d(15f, 15f); case FINISHER -> d(0f, 60f); };
            case PARRY -> switch (i) { case SILENT -> d(8f, 0f); case LIGHT -> d(12f, 0f); case HEAVY -> d(5f, 10f); case ASSAULT -> d(5f, 18f); case FINISHER -> d(0f, 40f); };
        };
    }

    public static Delta forDefense(AttackIntensity i, DefenseOutcome o, int attackerLevel, int defenderLevel, float damage, float capacity) {
        Delta base = forDefense(i, o);
        float dmgK = damageFactor(damage);
        float zhen = base.zhen() * dmgK;
        float mie = base.mie() * dmgK;

        float limit = capacity * hitFractionLimit(i);
        float total = zhen + mie;
        if (total > limit && total > 0f) {
            float scale = limit / total;
            zhen *= scale;
            mie *= scale;
        }

        float sup = suppression(attackerLevel, defenderLevel);
        return new Delta(zhen * sup, mie * sup);
    }
}