package com.dsp2.genshinerc.common.core.element;

public enum GaugeTier {
    WEAK(1.0f), MEDIUM(1.5f), STRONG(2.0f), SUPER(4.0f);

    private final float units;

    GaugeTier(float units) { this.units = units; }

    public float units() { return units; }
}
