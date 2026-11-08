package com.dsp2.genshinerc.common.core.combat.zhenmie;

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

    public static ClashResult classify(float meetingPoint) {
        if (meetingPoint >= LONG_STAGGER_MIN_POINT) return ClashResult.LONG_STAGGER;
        if (meetingPoint >= KNOCKDOWN_MIN_POINT) return ClashResult.KNOCKDOWN;
        return ClashResult.CRUSH;
    }

    public record Delta(float zhen, float mie) { public static final Delta NONE = new Delta(0f, 0f); }
    
    private static Delta d(float zhen, float mie) { return new Delta(zhen, mie); }

    public static float maxCapacity(float maxHealth, int defenderLevel) {
        float baseCap = maxHealth * 15.0f;
        return Math.max(100.0f, baseCap * (float)Math.pow(1.05, defenderLevel));
    }

    public static float suppression(int attackerLevel, int defenderLevel) {
        return (float)Math.pow(200.0, (attackerLevel - defenderLevel) / 100.0);
    }

    public static float attackerScale(int attackerLevel) {
        return (float)Math.pow(200.0, attackerLevel / 100.0);
    }

    public static Delta forDefense(AttackIntensity i, DefenseOutcome o) {
        return switch (o) {
            case DODGE -> Delta.NONE;
            case HIT -> switch (i) { case SILENT -> d(0f, 5f); case LIGHT -> d(0f, 8f); case HEAVY -> d(0f, 18f); case ASSAULT -> d(0f, 30f); case FINISHER -> d(0f, 100f); };
            case BLOCK -> switch (i) { case SILENT -> d(6f, 0f); case LIGHT -> d(10f, 0f); case HEAVY -> d(12f, 8f); case ASSAULT -> d(15f, 15f); case FINISHER -> d(0f, 60f); };
            case PARRY -> switch (i) { case SILENT -> d(8f, 0f); case LIGHT -> d(12f, 0f); case HEAVY -> d(5f, 10f); case ASSAULT -> d(5f, 18f); case FINISHER -> d(0f, 40f); };
        };
    }

    public static Delta forDefense(AttackIntensity i, DefenseOutcome o, int attackerLevel, int defenderLevel) {
        Delta base = forDefense(i, o);
        float sup = suppression(attackerLevel, defenderLevel);
        float scale = attackerScale(attackerLevel);
        float finalMultiplier = sup * scale;
        return new Delta(base.zhen() * finalMultiplier, base.mie() * finalMultiplier);
    }
}