package com.dsp2.genshinerc.common.core.combat.zhenmie;

import net.minecraft.util.Mth;

public final class ZhenMieRules {
    private ZhenMieRules() {}

    public static final float LONG_STAGGER_MIN_POINT = 0.9f;
    public static final float KNOCKDOWN_MIN_POINT = 0.5f;
    public static final float MIE_KEPT_AFTER_CLASH = 0.5f;
    public static final int ZHEN_DECAY_DELAY_TICKS = 60;
    public static final int MIE_DECAY_DELAY_TICKS = 140;
    public static final int DECAY_SYNC_INTERVAL = 4;

    public static final float DMG_REF = 6.0f;
    public static final float SCALE_EXP = 0.5f;
    public static final float CAP_BASE = 40.0f;
    public static final float CAP_MIN = 30.0f;
    public static final float CAP_LEVEL_DIVISOR = 80.0f;
    public static final float DMG_FACTOR_MIN = 0.25f;
    public static final float DMG_FACTOR_MAX = 100.0f;
    public static final float SUPPRESSION_MIN = 0.01f;
    public static final float SUPPRESSION_MAX = 64.0f;
    public static final float ZHEN_DRAIN_TICKS = 80.0f;
    public static final float MIE_DRAIN_TICKS = 400.0f;
    public static final float DECAY_LEVEL_DIVISOR = 120.0f;

    public static ClashResult classify(float meetingPoint) {
        if (meetingPoint >= LONG_STAGGER_MIN_POINT) return ClashResult.LONG_STAGGER;
        if (meetingPoint >= KNOCKDOWN_MIN_POINT) return ClashResult.KNOCKDOWN;
        return ClashResult.CRUSH;
    }

    public record Delta(float zhen, float mie) { public static final Delta NONE = new Delta(0f, 0f); }

    private static Delta d(float zhen, float mie) { return new Delta(zhen, mie); }

    public static float maxCapacity(float maxHealth, int defenderLevel) {
        float hpTerm = (float) Math.pow(Math.max(maxHealth, 1.0f) / DMG_REF, SCALE_EXP);
        float levelTerm = 1.0f + Math.max(0, defenderLevel) / CAP_LEVEL_DIVISOR;
        return Math.max(CAP_MIN, CAP_BASE * hpTerm * levelTerm);
    }

    public static float suppression(int attackerLevel, int defenderLevel) {
        int diff = attackerLevel - defenderLevel;
        double s = diff >= 0 ? Math.pow(2.0, diff / 10.0) : Math.pow(2.0, diff / 5.0);
        return (float) Mth.clamp(s, SUPPRESSION_MIN, SUPPRESSION_MAX);
    }

    public static float damageFactor(float damage) {
        if (!(damage > 0.0f)) return DMG_FACTOR_MIN;
        float f = (float) Math.pow(damage / DMG_REF, SCALE_EXP);
        return Mth.clamp(f, DMG_FACTOR_MIN, DMG_FACTOR_MAX);
    }

    public static float zhenDecayPerTick(float capacity, int level) {
        return capacity * (1.0f + Math.max(0, level) / DECAY_LEVEL_DIVISOR) / ZHEN_DRAIN_TICKS;
    }

    public static float mieDecayPerTick(float capacity, int level) {
        return capacity * (1.0f + Math.max(0, level) / DECAY_LEVEL_DIVISOR) / MIE_DRAIN_TICKS;
    }

    public static Delta forDefense(AttackIntensity i, DefenseOutcome o) {
        return switch (o) {
            case DODGE -> Delta.NONE;
            case HIT -> switch (i) { case SILENT -> d(0f, 5f); case LIGHT -> d(0f, 8f); case HEAVY -> d(0f, 18f); case ASSAULT -> d(0f, 30f); case FINISHER -> d(0f, 100f); };
            case BLOCK -> switch (i) { case SILENT -> d(6f, 0f); case LIGHT -> d(10f, 0f); case HEAVY -> d(12f, 8f); case ASSAULT -> d(15f, 15f); case FINISHER -> d(0f, 60f); };
            case PARRY -> switch (i) { case SILENT -> d(8f, 0f); case LIGHT -> d(12f, 0f); case HEAVY -> d(5f, 10f); case ASSAULT -> d(5f, 18f); case FINISHER -> d(0f, 40f); };
        };
    }

    public static Delta forDefense(AttackIntensity i, DefenseOutcome o, int attackerLevel, int defenderLevel, float damage) {
        Delta base = forDefense(i, o);
        float k = damageFactor(damage) * suppression(attackerLevel, defenderLevel);
        return new Delta(base.zhen() * k, base.mie() * k);
    }
}