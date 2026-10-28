package com.dsp2.genshinerc.client.render.hud.gui.rpg.anim;

import com.dsp2.genshinerc.client.render.hud.bar.config.BarConfig;
import net.minecraft.util.Mth;

public final class PointAnim {
    public enum Style { PLAIN, CLASSIC, LAYERED }
    public interface Sink { void accept(float lo, float hi, float alpha); }

    private static final long LAYER_GAP_MS = 150L, CLASSIC_EXTEND_MS = 200L, CLASSIC_SHRINK_MS = 200L;
    private static final float EXTEND = 0.35f, HOLD = 0.30f, EPS = 1.0e-3f;

    private final Style style;
    private final long baseMs;
    private boolean init, animating, whiteOn;
    private float from, to, shown, wFrom, wTo;
    private long start, dur, wStart, wDur;

    public PointAnim(Style style, long baseMs) {
        this.style = style;
        this.baseMs = baseMs;
    }

    public void reset() { init = false; animating = false; whiteOn = false; }

    public void update(long now, float v, float layerSize) {
        if (!init) { init = true; from = to = shown = v; return; }
        if (Math.abs(v - to) > EPS) {
            from = shown;
            to = v;
            start = now;
            int layers = style == Style.LAYERED ? layersSpanned(from, to, layerSize) : 1;
            dur = baseMs * layers + LAYER_GAP_MS * (layers - 1);
            animating = true;
            if (style != Style.PLAIN) {
                wFrom = from;
                wTo = to;
                wStart = now;
                wDur = style == Style.CLASSIC ? CLASSIC_EXTEND_MS + CLASSIC_SHRINK_MS : dur;
                whiteOn = true;
            }
        }
        if (animating) {
            float t = Mth.clamp((now - start) / (float) Math.max(1L, dur), 0f, 1f);
            float k = BarConfig.interpolate(t, style == Style.LAYERED
                    ? BarConfig.EasingType.EASE_IN_OUT_CUBIC : BarConfig.EasingType.EASE_OUT_CUBIC);
            shown = Mth.lerp(k, from, to);
            if (t >= 1f) { shown = to; animating = false; }
        }
        if (whiteOn && now - wStart >= wDur) whiteOn = false;
    }

    public float shown() { return shown; }
    public float target() { return to; }
    public boolean busy() { return animating || whiteOn; }

    public void emitWhite(long now, Sink sink) {
        if (!whiteOn) return;
        float lo = Math.min(wFrom, wTo), hi = Math.max(wFrom, wTo), span = hi - lo;
        if (span <= EPS) return;
        boolean gain = wTo > wFrom;
        long el = now - wStart;
        float s = lo, e = hi, alpha = 1f;
        if (style == Style.CLASSIC) {
            if (el < CLASSIC_EXTEND_MS) {
                float k = ease(el / (float) CLASSIC_EXTEND_MS);
                if (gain) { s = lo; e = lo + span * k; } else { s = hi - span * k; e = hi; }
            } else {
                float p = Mth.clamp((el - CLASSIC_EXTEND_MS) / (float) CLASSIC_SHRINK_MS, 0f, 1f), k = ease(p);
                if (gain) { s = lo + span * k; e = hi; } else { s = lo; e = hi - span * k; }
                if (p > 0.7f) alpha = 1f - (p - 0.7f) / 0.3f;
            }
        } else {
            float p = Mth.clamp(el / (float) Math.max(1L, wDur), 0f, 1f);
            if (p < EXTEND) {
                float k = ease(p / EXTEND);
                if (gain) { s = lo; e = lo + span * k; } else { s = hi - span * k; e = hi; }
            } else if (p >= EXTEND + HOLD) {
                float k = ease((p - EXTEND - HOLD) / (1f - EXTEND - HOLD));
                if (gain) { s = lo + span * k; e = hi; } else { s = lo; e = hi - span * k; }
                alpha = 1f - k;
            }
        }
        if (e - s > EPS && alpha > 0.01f) sink.accept(s, e, alpha);
    }

    private static float ease(float t) { return BarConfig.interpolate(t, BarConfig.EasingType.EASE_OUT_CUBIC); }

    private static int layersSpanned(float a, float b, float size) {
        if (size <= 0f) return 1;
        float lo = Math.min(a, b), hi = Math.max(a, b);
        int l0 = (int) Math.floor(lo / size), l1 = (int) Math.ceil(hi / size) - 1;
        return Math.max(1, l1 - l0 + 1);
    }
}