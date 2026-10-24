package com.dsp2.genshinerc.common.element;

public final class ElementGauge {
    public static final float MAX_GAUGE = 4.0f;
    public static final float AURA_TAX = 0.8f;

    private ElementGauge() {}

    public static float gaugeFromUnits(float units) {
        return Math.min(MAX_GAUGE, units * AURA_TAX);
    }

    public static int durationTicksFromUnits(float units) {
        return Math.max(1, Math.round((2.5f * units + 7f) * 20f));
    }
}