package com.dsp2.genshinerc.common.level;

public final class WorldLevelScaling {
    private static final double MULT_PER_STEP = 200.0;
    private static final double LEVELS_PER_STEP = 10.0;

    private WorldLevelScaling() {}

    public static double damageMultiplier() {
        return Math.pow(MULT_PER_STEP, WorldLevel.get() / LEVELS_PER_STEP);
    }
}