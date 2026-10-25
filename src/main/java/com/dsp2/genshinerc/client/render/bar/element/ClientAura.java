package com.dsp2.genshinerc.client.render.bar.element;

import com.dsp2.genshinerc.client.render.bar.config.BarConfig;
import com.dsp2.genshinerc.common.core.element.ElementGauge;
import com.dsp2.genshinerc.common.core.element.ElementType;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;

public final class ClientAura {
    private static final long APPEAR_MS = 120L;
    private static final float TWO_PI = (float) (Math.PI * 2.0);

    private final ElementType type;
    private final long createdMs;

    private float serverAmount;
    private float decayPerTick;
    private double syncTicks;

    private float animFrom;
    private long animStartMs;
    private long animDurMs = 1L;

    private long flashStartMs = -1L;
    private boolean ghost;
    private long ghostStartMs;

    private float slot;
    private boolean slotInit;

    private float flickerPhase;
    private long lastFlickerMs;

    ClientAura(ElementType type, long nowMs) {
        this.type = type;
        this.createdMs = nowMs;
    }

    public ElementType type() { return type; }
    public float slot() { return slot; }
    public boolean isGhost() { return ghost; }

    static double nowTicks() {
        Minecraft mc = Minecraft.getInstance();
        return mc.level == null ? 0.0 : mc.level.getGameTime() + mc.getFrameTime();
    }

    private static long ms(float seconds) {
        return (long) (Math.max(0f, seconds) * 1000f);
    }

    void sync(float amount, float decay, boolean flash, boolean isNew, double nowTicks, long nowMs, BarConfig cfg) {
        float shown = isNew ? 0f : display(nowTicks, nowMs);
        serverAmount = amount;
        decayPerTick = decay;
        syncTicks = nowTicks;
        ghost = false;
        startAnim(shown - amount, flash, nowMs, cfg);
        if (flash) flashStartMs = nowMs;
    }

    void becomeGhost(double nowTicks, long nowMs, BarConfig cfg) {
        if (ghost) return;
        float shown = display(nowTicks, nowMs);
        ghost = true;
        ghostStartMs = nowMs;
        flashStartMs = nowMs;
        startAnim(shown, true, nowMs, cfg);
    }

    private void startAnim(float offset, boolean afterFlash, long nowMs, BarConfig cfg) {
        if (Math.abs(offset) < 1.0e-3f) {
            animFrom = 0f;
            return;
        }
        animFrom = offset;
        long delay = afterFlash ? ms(cfg.elementFlashInTime) + ms(cfg.elementFlashHoldTime) : 0L;
        animStartMs = nowMs + delay;
        animDurMs = Math.max(1L, ms(cfg.elementGaugeAnimTime));
    }

    float level(double nowTicks) {
        if (ghost) return 0f;
        return Math.max(0f, serverAmount - decayPerTick * (float) (nowTicks - syncTicks));
    }

    float display(double nowTicks, long nowMs) {
        float offset = 0f;
        if (animFrom != 0f) {
            long el = nowMs - animStartMs;
            if (el < 0) {
                offset = animFrom;
            } else if (el < animDurMs) {
                float t = el / (float) animDurMs;
                offset = animFrom * (1f - BarConfig.interpolate(t, BarConfig.EasingType.EASE_OUT_CUBIC));
            }
        }
        return Mth.clamp(level(nowTicks) + offset, 0f, ElementGauge.MAX_GAUGE);
    }

    boolean isVisible(double nowTicks) {
        return ghost || level(nowTicks) > 1.0e-3f;
    }

    boolean isFlashing(long nowMs, BarConfig cfg) {
        if (flashStartMs < 0) return false;
        long total = ms(cfg.elementFlashInTime) + ms(cfg.elementFlashHoldTime) + ms(cfg.elementFlashOutTime);
        return nowMs - flashStartMs < total;
    }

    float whiteness(long nowMs, BarConfig cfg) {
        if (flashStartMs < 0) return 0f;
        long el = nowMs - flashStartMs;
        long in = ms(cfg.elementFlashInTime);
        long hold = ms(cfg.elementFlashHoldTime);
        long out = ms(cfg.elementFlashOutTime);
        float w;
        if (el < in) w = in <= 0 ? 1f : el / (float) in;
        else if (ghost || el < in + hold) w = 1f;
        else if (el < in + hold + out) w = 1f - (el - in - hold) / (float) out;
        else return 0f;
        w = Mth.clamp(w, 0f, 1f);
        return w * w * (3f - 2f * w);
    }

    float opacity(double nowTicks, long nowMs, float shownGauge, BarConfig cfg) {
        float gaugeAlpha = Mth.lerp(Mth.clamp(shownGauge / ElementGauge.MAX_GAUGE, 0f, 1f), cfg.elementMinOpacity, 1f);
        float appear = Mth.clamp((nowMs - createdMs) / (float) APPEAR_MS, 0f, 1f);
        return gaugeAlpha * appear * flicker(nowTicks, nowMs, cfg) * ghostFade(nowMs, cfg);
    }

    private float flicker(double nowTicks, long nowMs, BarConfig cfg) {
        long dtMs = lastFlickerMs == 0 ? 0 : Math.min(100L, Math.max(0L, nowMs - lastFlickerMs));
        lastFlickerMs = nowMs;

        float threshold = cfg.elementLowGaugeThreshold;
        float gauge = level(nowTicks);
        if (ghost || threshold <= 0f || gauge >= threshold || isFlashing(nowMs, cfg)) {
            flickerPhase = 0f;
            return 1f;
        }
        float t = 1f - gauge / threshold;
        float hz = Mth.lerp(t, cfg.elementFlickerMinHz, cfg.elementFlickerMaxHz);
        flickerPhase = (flickerPhase + TWO_PI * hz * dtMs / 1000f) % (TWO_PI * 1000f);
        float wave = 0.5f + 0.5f * Mth.cos(flickerPhase);
        return Mth.lerp(wave, cfg.elementFlickerMinAlpha, 1f);
    }

    private float ghostFade(long nowMs, BarConfig cfg) {
        if (!ghost) return 1f;
        long start = ghostStartMs + ms(cfg.elementFlashInTime) + ms(cfg.elementFlashHoldTime);
        if (nowMs < start) return 1f;
        long fade = ms(cfg.elementGhostFadeTime);
        return fade <= 0 ? 0f : 1f - Mth.clamp((nowMs - start) / (float) fade, 0f, 1f);
    }

    boolean isGhostDone(long nowMs, BarConfig cfg) {
        return ghost && ghostFade(nowMs, cfg) <= 0f;
    }

    void approachSlot(int target, float k) {
        if (!slotInit) {
            slot = target;
            slotInit = true;
        } else {
            slot += (target - slot) * k;
        }
    }
}