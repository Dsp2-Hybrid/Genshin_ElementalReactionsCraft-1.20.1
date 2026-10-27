package com.dsp2.genshinerc.client.render.hud.gui.rpg.anim;

import com.dsp2.genshinerc.client.render.hud.bar.CustomHealthBar;
import com.dsp2.genshinerc.client.render.hud.bar.config.BarConfig;
import net.minecraft.util.Mth;

public final class BarTrack {
    private static final long MAIN_DURATION_MS = 900L;
    private static final long RAPID_CHANGE_MS = 100L;
    private static final float EPS = 0.001f;

    public interface Sink { void accept(float start, float end, float alpha); }

    private boolean init;
    private float value, extra, max;
    private float visMain, visExtra, fromMain, fromExtra, toMain, toExtra;
    private long animStart;
    private boolean animating;
    private long lastChange, smallStart;
    private boolean small;
    private boolean shaking;
    private long shakeStart;
    private float shakeMag;
    private final White mainWhite = new White(), extraWhite = new White();

    public void reset() { init = false; small = false; shaking = false; mainWhite.clear(); extraWhite.clear(); }

    public void update(long now, float v, float e, float m, BarConfig cfg, boolean shake, boolean fatal) {
        m = Math.max(m, EPS);
        v = Math.max(0f, v);
        e = Math.max(0f, e);
        if (!init) {
            init = true;
            value = v; extra = e; max = m;
            visMain = fromMain = toMain = mainEnd(v, e, m);
            visExtra = fromExtra = toExtra = extraEnd(v, e, m);
        }

        if (Math.abs(v - value) > EPS || Math.abs(e - extra) > EPS || Math.abs(m - max) > EPS) {
            long prev = lastChange;
            lastChange = now;
            boolean isSmall = false;
            float delta = Math.max(Math.abs(v - value), Math.abs(e - extra));
            if (delta > EPS) {
                float pct = delta / Math.max(1f, m);
                if (now - prev < RAPID_CHANGE_MS) isSmall = true;
                else if (fatal && v <= 0f && v < value)
                    startShake(now, Mth.clamp(value / m * cfg.fatalShakeMultiplier, cfg.fatalShakeMinPercent, cfg.fatalShakeMaxPercent));
                else if (cfg.smallChangeEnabled && pct < cfg.smallChangeThreshold) isSmall = true;
                else if (shake && v < value) startShake(now, Math.min(pct, 1f));
                if (isSmall) { small = true; smallStart = now; }
            }
            fromMain = visMain;
            fromExtra = visExtra;
            toMain = mainEnd(v, e, m);
            toExtra = extraEnd(v, e, m);
            if (!isSmall) {
                if (Math.abs(toMain - visMain) > EPS) mainWhite.trigger(visMain, toMain, now);
                if (Math.abs(toExtra - visExtra) > EPS) extraWhite.trigger(visExtra, toExtra, now);
            }
            animStart = now;
            animating = true;
            value = v; extra = e; max = m;
        }

        if (animating) {
            long el = now - animStart;
            if (el >= MAIN_DURATION_MS) {
                visMain = toMain; visExtra = toExtra; animating = false;
            } else {
                float k = BarConfig.interpolate(el / (float) MAIN_DURATION_MS, BarConfig.EasingType.EASE_OUT_CUBIC);
                visMain = fromMain + (toMain - fromMain) * k;
                visExtra = fromExtra + (toExtra - fromExtra) * k;
            }
        }
        mainWhite.update(now, visMain, cfg.playerOverlayShrinkTime, cfg.overlayFadeTime, cfg.whiteEasing);
        extraWhite.update(now, visExtra, cfg.playerOverlayShrinkTime, cfg.overlayFadeTime, cfg.whiteEasing);
    }

    public float main() { return visMain; }
    public float extra() { return visExtra; }
    public boolean busy() { return animating || small || mainWhite.busy() || extraWhite.busy(); }

    public void forEachWhite(Sink sink) { mainWhite.emit(sink); extraWhite.emit(sink); }

    public float smallAlpha(long now, BarConfig cfg) {
        if (!small) return 0f;
        if (now - lastChange < RAPID_CHANGE_MS) return 1f;
        float el = now - smallStart, fade = cfg.smallChangeFadeOut * 1000f, a;
        if (cfg.smallChangeUseShaderMode && CustomHealthBar.isShaderPackEnabled()) {
            float hold = cfg.smallChangeShaderDuration * 1000f;
            if (el >= hold + fade) { small = false; return 0f; }
            a = el > hold ? 1f - (el - hold) / fade : 1f;
        } else {
            float total = cfg.smallChangeDuration * 1000f;
            if (el >= total) { small = false; return 0f; }
            if (total < fade) a = 1f - el / total;
            else if (el > total - fade) a = (total - el) / fade;
            else a = 1f;
        }
        return Mth.clamp(a, 0f, 1f);
    }

    public float shakeOffset(long now, BarConfig cfg) {
        if (!shaking) return 0f;
        long el = now - shakeStart;
        if (el >= cfg.shakeDuration * 1000f) { shaking = false; return 0f; }
        float t = el / 1000f, zeta = 0.1f;
        float w0 = Mth.TWO_PI * cfg.shakeFrequencyFactor, wd = w0 * Mth.sqrt(1f - zeta * zeta);
        float amp = shakeMag * cfg.shakeAmplitudeFactor * (float) Math.exp(-zeta * w0 * t) * (float) Math.exp(-cfg.shakeDecayFactor * t);
        return amp * Mth.sin(wd * t);
    }

    private void startShake(long now, float magnitude) { shaking = true; shakeStart = now; shakeMag = magnitude; }

    private static float mainEnd(float v, float e, float m) { return Mth.clamp(v + e <= m ? v / m : v / (v + e), 0f, 1f); }
    private static float extraEnd(float v, float e, float m) { return v + e <= m ? Mth.clamp((v + e) / m, 0f, 1f) : 1f; }

    private static final class White {
        private static final float OLD_FADE_MS = 150f, EXTEND_MS = 350f;
        boolean active, extending, shrinking, healing;
        float start, end, maxWidth, base, alpha = 1f;
        long animStart;
        boolean oldActive;
        float oldStart, oldEnd, oldAlpha;
        long oldStartMs;

        void clear() { active = false; oldActive = false; }
        boolean busy() { return active || oldActive; }

        void trigger(float current, float target, long now) {
            if (active) {
                oldStart = Math.min(start, end); oldEnd = Math.max(start, end);
                oldActive = true; oldAlpha = 1f; oldStartMs = now; active = false;
            }
            maxWidth = Math.abs(target - current);
            if (maxWidth <= 0.001f) return;
            healing = target > current;
            base = start = end = current;
            animStart = now;
            active = extending = true;
            shrinking = false;
            alpha = 1f;
        }

        void update(long now, float current, float shrinkTime, float fadeSec, BarConfig.EasingType easing) {
            if (oldActive) {
                float p = (now - oldStartMs) / OLD_FADE_MS;
                if (p >= 1f) oldActive = false;
                else {
                    oldAlpha = 1f - p;
                    oldStart = Mth.lerp(p, oldStart, current);
                    oldEnd = Mth.lerp(p, oldEnd, current);
                }
            }
            if (!active) return;
            long el = now - animStart;
            if (extending) {
                float p = Math.min(1f, el / EXTEND_MS);
                float k = BarConfig.interpolate(p, BarConfig.EasingType.EASE_OUT_CUBIC);
                if (healing) end = base + maxWidth * k; else start = base - maxWidth * k;
                if (p >= 1f) {
                    start = healing ? base : base - maxWidth;
                    end = healing ? base + maxWidth : base;
                    extending = false; shrinking = true; animStart = now;
                }
            } else if (shrinking) {
                float dur = Math.max(0.3f, maxWidth * shrinkTime) * 1000f;
                if (el >= dur) { active = shrinking = false; return; }
                float k = BarConfig.interpolate(el / dur, easing);
                if (healing) start = base + maxWidth * k; else end = base - maxWidth * k;
                float fadeMs = Math.max(1f, fadeSec * 1000f), fadeStart = dur - fadeMs;
                alpha = el > fadeStart ? Math.max(0f, 1f - (el - fadeStart) / fadeMs) : 1f;
            }
        }

        void emit(Sink sink) {
            if (oldActive && oldAlpha > 0.01f) sink.accept(Math.min(oldStart, oldEnd), Math.max(oldStart, oldEnd), oldAlpha);
            if (active && alpha > 0.01f) sink.accept(Math.min(start, end), Math.max(start, end), alpha);
        }
    }
}