package com.dsp2.genshinerc.client.render.hud.gui.rpg.anim;

import com.dsp2.genshinerc.client.render.hud.bar.config.BarConfig;
import net.minecraft.util.Mth;

public final class OxygenAnim {
    public enum Phase { IDLE, DRAIN, RETRACT, GAIN, SETTLE }
    private static final float EPS = 1.0e-4f;

    private Phase phase = Phase.IDLE;
    private float last = -1f, bar, ghostTop, ghostAlpha, gainTop, from;
    private long phaseStart, lastRise;

    public void update(long now, float air, BarConfig cfg) {
        air = Mth.clamp(air, 0f, 1f);
        if (last < 0f) last = bar = ghostTop = gainTop = air;
        boolean falling = air < last - EPS, rising = air > last + EPS;
        if (rising) lastRise = now;

        if (falling && phase != Phase.DRAIN) {
            ghostTop = Math.max(last, phase == Phase.RETRACT ? ghostTop : 0f);
            ghostAlpha = 1f;
            enter(Phase.DRAIN, now);
        } else if (rising && phase == Phase.DRAIN) {
            from = ghostTop;
            enter(Phase.RETRACT, now);
        } else if (rising && phase == Phase.IDLE) {
            enter(Phase.GAIN, now);
        }
        last = air;

        switch (phase) {
            case DRAIN -> { bar = air; gainTop = air; }
            case RETRACT -> {
                float t = progress(now, cfg.rpgOxygenRetractTime);
                ghostTop = Mth.lerp(ease(t), from, bar);
                ghostAlpha = 1f - t;
                gainTop = air;
                if (t >= 1f) { ghostAlpha = 0f; enter(Phase.GAIN, now); }
            }
            case GAIN -> {
                gainTop = air;
                if (air >= 1f - EPS || now - lastRise > cfg.rpgOxygenGainIdleTime * 1000f) {
                    from = bar;
                    enter(Phase.SETTLE, now);
                }
            }
            case SETTLE -> {
                gainTop = air;
                float t = progress(now, cfg.rpgOxygenSettleTime);
                bar = Mth.lerp(ease(t), from, air);
                if (t >= 1f) { bar = air; enter(Phase.IDLE, now); }
            }
            case IDLE -> { bar = air; gainTop = air; }
        }
    }

    public float bar() { return bar; }
    public float ghostTop() { return ghostTop; }
    public float ghostAlpha() { return phase == Phase.DRAIN || phase == Phase.RETRACT ? ghostAlpha : 0f; }
    public float gainTop() { return gainTop; }
    public boolean visible(boolean underwater) { return underwater || phase != Phase.IDLE || bar < 1f - EPS; }

    private void enter(Phase p, long now) { phase = p; phaseStart = now; }
    private float progress(long now, float seconds) { return seconds <= 0f ? 1f : Mth.clamp((now - phaseStart) / (seconds * 1000f), 0f, 1f); }
    private static float ease(float t) { return BarConfig.interpolate(t, BarConfig.EasingType.EASE_OUT_CUBIC); }
}