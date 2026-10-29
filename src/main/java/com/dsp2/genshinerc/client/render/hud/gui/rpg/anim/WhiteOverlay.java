package com.dsp2.genshinerc.client.render.hud.gui.rpg.anim;

import com.dsp2.genshinerc.client.render.hud.bar.config.BarConfig;
import net.minecraft.util.Mth;

public final class WhiteOverlay {
    private static final float EXTEND_MS = 350f, OLD_FADE_MS = 150f;

    public interface Sink { void seg(float lo, float hi, float alpha); }

    private boolean active, healing, extending, shrinking;
    private float start, end, maxWidth, base, alpha = 1f;
    private long animStart;

    private boolean oldActive;
    private float oldStart, oldEnd, oldAlpha = 1f;
    private long oldAnimStart;

    public void reset() { active = oldActive = extending = shrinking = false; }

    public boolean busy() { return active || oldActive; }

    private void interrupt(long now) {
        if (!active) return;
        oldStart = Math.min(start, end);
        oldEnd = Math.max(start, end);
        oldActive = true;
        oldAlpha = 1f;
        oldAnimStart = now;
        active = false;
    }

    public void trigger(float visual, float target, long now) {
        interrupt(now);
        maxWidth = Math.abs(target - visual);
        if (maxWidth <= 0.001f) return;
        healing = target > visual;
        base = visual;
        start = visual;
        end = visual;
        animStart = now;
        active = true;
        extending = true;
        shrinking = false;
        alpha = 1f;
    }

    public void update(long now, float visual, BarConfig cfg) {
        if (oldActive) {
            long el = now - oldAnimStart;
            if (el >= OLD_FADE_MS) {
                oldActive = false;
            } else {
                float p = Math.min(1f, el / OLD_FADE_MS);
                oldAlpha = 1f - p;
                oldStart = Mth.lerp(p, oldStart, visual);
                oldEnd = Mth.lerp(p, oldEnd, visual);
            }
        }
        if (!active) return;

        long elapsed = now - animStart;
        if (extending) {
            float p = Math.min(1f, elapsed / EXTEND_MS);
            float e = BarConfig.interpolate(p, BarConfig.EasingType.EASE_OUT_CUBIC);
            if (healing) end = base + maxWidth * e; else start = base - maxWidth * e;
            if (p >= 1f) {
                if (healing) { start = base; end = base + maxWidth; }
                else { start = base - maxWidth; end = base; }
                extending = false;
                shrinking = true;
                animStart = now;
            }
        } else if (shrinking) {
            float durMs = Math.max(0.3f, maxWidth * cfg.playerOverlayShrinkTime) * 1000f;
            if (elapsed >= (long) durMs) {
                active = false;
                shrinking = false;
                return;
            }
            float p = Math.min(1f, elapsed / durMs);
            float e = BarConfig.interpolate(p, cfg.whiteEasing);
            if (healing) {
                float hi = Math.min(base + maxWidth, visual);
                end = hi;
                start = Math.min(Mth.lerp(e, base, hi), hi);
            } else {
                float lo = Math.min(base - maxWidth, visual);
                start = lo;
                end = Math.max(Mth.lerp(e, base, lo), lo);
            }

            alpha = 1.0f - 0.7f * e;
        }
    }

    public void emit(Sink s) {
        if (oldActive && oldAlpha > 0.01f) {
            float lo = Math.min(oldStart, oldEnd), hi = Math.max(oldStart, oldEnd);
            if (hi - lo > 0.001f) s.seg(lo, hi, oldAlpha);
        }
        if (active && alpha > 0.01f) {
            float lo = Math.min(start, end), hi = Math.max(start, end);
            if (hi - lo > 0.001f) s.seg(lo, hi, alpha);
        }
    }
}