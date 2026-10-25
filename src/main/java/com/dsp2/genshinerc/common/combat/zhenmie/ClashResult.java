package com.dsp2.genshinerc.common.combat.zhenmie;

import yesman.epicfight.world.damagesource.StunType;

public enum ClashResult {
    LONG_STAGGER(StunType.LONG,      1.0f,   0.6f),
    KNOCKDOWN   (StunType.KNOCKDOWN, 2.0f,   1.0f),
    CRUSH       (StunType.KNOCKDOWN, 3.0f,   1.6f);

    private final StunType stunType;
    private final float stunSeconds;
    private final float visualSeconds;

    ClashResult(StunType stunType, float stunSeconds, float visualSeconds) {
        this.stunType = stunType;
        this.stunSeconds = stunSeconds;
        this.visualSeconds = visualSeconds;
    }

    public StunType stunType() { return stunType; }
    public float stunSeconds() { return stunSeconds; }
    public float visualSeconds() { return visualSeconds; }
}