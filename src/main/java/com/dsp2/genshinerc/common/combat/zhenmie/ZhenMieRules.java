package com.dsp2.genshinerc.common.combat.zhenmie;

public final class ZhenMieRules {
    private ZhenMieRules() {}

    public static final float LONG_STAGGER_MIN_POINT = 0.9f;
    public static final float KNOCKDOWN_MIN_POINT = 0.5f;
    public static final float MIE_KEPT_AFTER_CLASH = 0.5f;

    public static final int ZHEN_DECAY_DELAY_TICKS = 60;
    public static final float ZHEN_DECAY_PER_TICK = 0.01f;
    public static final int MIE_DECAY_DELAY_TICKS = 140;
    public static final float MIE_DECAY_PER_TICK = 0.004f;
    public static final int DECAY_SYNC_INTERVAL = 4;

    public static ClashResult classify(float meetingPoint) {
        if (meetingPoint >= LONG_STAGGER_MIN_POINT) return ClashResult.LONG_STAGGER;
        if (meetingPoint >= KNOCKDOWN_MIN_POINT) return ClashResult.KNOCKDOWN;
        return ClashResult.CRUSH;
    }

    public record Delta(float zhen, float mie) {
        public static final Delta NONE = new Delta(0f, 0f);
    }

    private static Delta d(float zhen, float mie) {
        return new Delta(zhen, mie);
    }

    public static Delta forDefense(AttackIntensity i, DefenseOutcome o) {
        return switch (o) {
            case DODGE -> Delta.NONE;
            case HIT -> switch (i) {
                case SILENT   -> d(0f, 0.05f);
                case LIGHT    -> d(0f, 0.08f);
                case HEAVY    -> d(0f, 0.18f);
                case ASSAULT  -> d(0f, 0.30f);
                case FINISHER -> d(0f, 1.00f);
            };
            case BLOCK -> switch (i) {
                case SILENT   -> d(0.06f, 0f);
                case LIGHT    -> d(0.10f, 0f);
                case HEAVY    -> d(0.12f, 0.08f);
                case ASSAULT  -> d(0.15f, 0.15f);
                case FINISHER -> d(0f, 0.60f);
            };
            case PARRY -> switch (i) {
                case SILENT   -> d(0.08f, 0f);
                case LIGHT    -> d(0.12f, 0f);
                case HEAVY    -> d(0.05f, 0.10f);
                case ASSAULT  -> d(0.05f, 0.18f);
                case FINISHER -> d(0f, 0.40f);
            };
        };
    }
}