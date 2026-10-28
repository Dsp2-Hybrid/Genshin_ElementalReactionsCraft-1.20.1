package com.dsp2.genshinerc.client.render.hud.gui.rpg.anim;

import net.minecraft.util.Mth;

public final class NumberFx {
    private static final float IDLE_ALPHA = 0.8f, POP = 0.18f, POP_HZ = 9f;

    private final long countMs, holdMs, fadeMs;
    private boolean init, counting, blinkOn;
    private float from, to, shown, dir, alpha = IDLE_ALPHA;
    private long start, settled = -1L, last, lastBlink;

    public NumberFx(long countMs, long holdMs, long fadeMs) {
        this.countMs = Math.max(1L, countMs);
        this.holdMs = holdMs;
        this.fadeMs = Math.max(1L, fadeMs);
    }

    public void reset() { init = false; counting = false; blinkOn = false; alpha = IDLE_ALPHA; settled = -1L; }

    public void set(float v, long now) {
        if (!init) { init = true; from = to = shown = v; last = now; return; }
        if (Math.abs(v - to) > 1.0e-4f) {
            from = shown;
            to = v;
            dir = Math.signum(to - from);
            start = now;
            counting = true;
            settled = -1L;
        }
    }

    public void update(long now) {
        float dt = Mth.clamp((now - last) / 1000f, 0f, 0.1f);
        last = now;
        if (counting) {
            float t = (now - start) / (float) countMs;
            if (t >= 1f) { shown = to; counting = false; settled = now; }
            else shown = Mth.lerp(t, from, to);
        }
        boolean lit = counting || (settled >= 0L && now - settled < holdMs);
        float target = lit ? 1f : IDLE_ALPHA;
        float step = dt * (1f - IDLE_ALPHA) / (fadeMs / 1000f);
        alpha = alpha < target ? Math.min(target, alpha + step) : Math.max(target, alpha - step);
    }

    public float shown() { return shown; }
    public float alpha() { return alpha; }

    public float scale(long now) {
        if (!counting) return 1f;
        float t = Mth.clamp((now - start) / (float) countMs, 0f, 1f);
        return 1f + dir * POP * (1f - t) * Math.abs(Mth.sin((now - start) / 1000f * POP_HZ * Mth.PI));
    }

    public boolean blink(long now, float frac, float threshold) {
        if (frac >= threshold) { blinkOn = false; return false; }
        float n = Mth.clamp((threshold - frac) / threshold, 0f, 1f);
        long interval = (long) ((7f - 5f * n) * 50f);
        if (now - lastBlink >= interval) { blinkOn = !blinkOn; lastBlink = now; }
        return blinkOn;
    }
}