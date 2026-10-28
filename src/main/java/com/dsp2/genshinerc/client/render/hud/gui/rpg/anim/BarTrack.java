package com.dsp2.genshinerc.client.render.hud.gui.rpg.anim;

import com.dsp2.genshinerc.client.render.hud.bar.CustomHealthBar;
import com.dsp2.genshinerc.client.render.hud.bar.config.BarConfig;
import net.minecraft.util.Mth;

public final class BarTrack {
    public interface Sink { void accept(float lo, float hi, float alpha); }

    private float currentValue = -1f;
    private float currentExtra = -1f;
    private float lastMax = -1f;

    public float main = 1.0f;
    public float extra = 1.0f;
    private float targetMain = 1.0f;
    private float targetExtra = 1.0f;
    private float startMain = 1.0f;
    private float startExtra = 1.0f;

    private long mainAnimStart = 0L;
    private boolean mainAnimating = false;
    private static final long MAIN_DURATION = 900L;

    private final WhiteState mainWhite = new WhiteState();
    private final WhiteState extraWhite = new WhiteState();

    private boolean isSmallChangeActive = false;
    private long lastSmallChangeTime = 0L;
    private long lastChangeTime = 0L;

    private boolean isShaking = false;
    private long shakeStartTime = 0L;
    private float shakeMagnitude = 0f;

    public void reset() {
        currentValue = -1f;
        currentExtra = -1f;
        lastMax = -1f;
        main = 1.0f;
        extra = 1.0f;
        mainAnimating = false;
        mainWhite.active = false;
        mainWhite.oldActive = false;
        extraWhite.active = false;
        extraWhite.oldActive = false;
        isSmallChangeActive = false;
        isShaking = false;
    }

    public float shakeOffset(long now, BarConfig config) {
        if (!isShaking) return 0f;
        long elapsed = now - shakeStartTime;
        float durationMs = config.shakeDuration * 1000f;
        if (elapsed >= durationMs) {
            isShaking = false;
            return 0f;
        }
        float t = elapsed / 1000.0f;
        float startAmplitude = shakeMagnitude * config.shakeAmplitudeFactor;
        float dampingRatio = 0.1f;
        float omega0 = 2 * (float) Math.PI * config.shakeFrequencyFactor;
        float omegaD = omega0 * (float) Math.sqrt(1 - dampingRatio * dampingRatio);
        float amplitude = startAmplitude * (float) Math.exp(-dampingRatio * omega0 * t);
        amplitude *= (float) Math.exp(-config.shakeDecayFactor * t);
        return amplitude * (float) Math.sin(omegaD * t);
    }

    public void update(long now, float value, float extraVal, float max, BarConfig cfg, boolean isPlayer, boolean isAbs) {
        if (currentValue == -1f) {
            currentValue = value;
            currentExtra = extraVal;
            lastMax = max;
            main = Mth.clamp(value / max, 0f, 1f);
            extra = Mth.clamp((value + extraVal) / max, 0f, 1f);
            targetMain = main;
            targetExtra = extra;
            startMain = main;
            startExtra = extra;
        }

        boolean changed = Math.abs(value - currentValue) > 0.001f || Math.abs(extraVal - currentExtra) > 0.001f || Math.abs(max - lastMax) > 0.001f;

        if (changed) {
            long oldLastChange = lastChangeTime;
            lastChangeTime = now;
            boolean isSmall = false;

            if (value < currentValue) {
                float changePercent = Math.abs(value - currentValue) / Math.max(1f, max);
                boolean isUltra = (now - oldLastChange < 100L);
                if (isUltra) {
                    isSmall = true;
                    isSmallChangeActive = true;
                    lastSmallChangeTime = now;
                } else if (value <= 0 && value < currentValue) {
                    isShaking = true;
                    shakeStartTime = now;
                    float rem = currentValue / max;
                    shakeMagnitude = Mth.clamp(rem * cfg.fatalShakeMultiplier, cfg.fatalShakeMinPercent, cfg.fatalShakeMaxPercent);
                } else if (changePercent < cfg.smallChangeThreshold && cfg.smallChangeEnabled) {
                    isSmall = true;
                    isSmallChangeActive = true;
                    lastSmallChangeTime = now;
                } else {
                    isShaking = true;
                    shakeStartTime = now;
                    shakeMagnitude = Math.min(changePercent, 1.0f);
                }
            }

            startMain = main;
            startExtra = extra;
            targetMain = Mth.clamp(value / max, 0f, 1f);
            targetExtra = Mth.clamp((value + extraVal) / max, 0f, 1f);

            if (!isSmall) {
                if (Math.abs(targetMain - main) > 0.001f) {
                    mainWhite.trigger(main, targetMain, now);
                }
                if (Math.abs(targetExtra - extra) > 0.001f) {
                    extraWhite.trigger(extra, targetExtra, now);
                }
            }

            mainAnimStart = now;
            mainAnimating = true;

            currentValue = value;
            currentExtra = extraVal;
            lastMax = max;
        }

        if (mainAnimating) {
            long elapsed = now - mainAnimStart;
            if (elapsed >= MAIN_DURATION) {
                main = targetMain;
                extra = targetExtra;
                mainAnimating = false;
            } else {
                float progress = Math.min(1.0f, elapsed / (float) MAIN_DURATION);
                float eased = BarConfig.interpolate(progress, BarConfig.EasingType.EASE_OUT_CUBIC);
                main = startMain + (targetMain - startMain) * eased;
                extra = startExtra + (targetExtra - startExtra) * eased;
            }
        }

        float baseShrink = isPlayer ? cfg.playerOverlayShrinkTime : cfg.mobOverlayShrinkTime;
        mainWhite.update(now, main, baseShrink, cfg.overlayFadeTime, cfg.whiteEasing);
        extraWhite.update(now, extra, baseShrink, cfg.overlayFadeTime, cfg.whiteEasing);
    }

    public boolean busy() {
        return mainAnimating || mainWhite.active || mainWhite.oldActive || extraWhite.active || extraWhite.oldActive || isSmallChangeActive;
    }

    public void forEachWhite(Sink sink) {
        mainWhite.emit(sink);
        extraWhite.emit(sink);
    }

    public float smallAlpha(long now, BarConfig cfg) {
        if (!isSmallChangeActive) return 0f;
        long timeSinceLastChange = now - lastChangeTime;
        boolean isStillUltraHighFreq = (timeSinceLastChange < 100L);

        float smallAlpha = 0f;
        boolean shouldDraw = false;

        if (isStillUltraHighFreq) {
            smallAlpha = 1.0f;
            shouldDraw = true;
        } else {
            long elapsed = now - lastSmallChangeTime;
            if (cfg.smallChangeUseShaderMode && CustomHealthBar.isShaderPackEnabled()) {
                float shaderDurationMs = cfg.smallChangeShaderDuration * 1000f;
                float fadeOutMs = cfg.smallChangeFadeOut * 1000f;
                float totalMs = shaderDurationMs + fadeOutMs;
                if (elapsed < totalMs) {
                    shouldDraw = true;
                    if (elapsed > shaderDurationMs) {
                        smallAlpha = 1.0f - ((float)(elapsed - shaderDurationMs) / fadeOutMs);
                    } else {
                        smallAlpha = 1.0f;
                    }
                }
            } else {
                float totalMs = cfg.smallChangeDuration * 1000f;
                float fadeOutMs = cfg.smallChangeFadeOut * 1000f;
                if (elapsed < totalMs) {
                    shouldDraw = true;
                    if (totalMs < fadeOutMs) {
                        smallAlpha = 1.0f - ((float) elapsed / totalMs);
                    } else if (elapsed > totalMs - fadeOutMs) {
                        smallAlpha = (totalMs - elapsed) / fadeOutMs;
                    } else {
                        smallAlpha = 1.0f;
                    }
                }
            }
        }

        if (shouldDraw) {
            return Mth.clamp(smallAlpha, 0.0f, 1.0f);
        } else {
            isSmallChangeActive = false;
            return 0f;
        }
    }

    private static class WhiteState {
        boolean active = false;
        float startOverlay = 0f;
        float endOverlay = 0f;
        float maxWidth = 0f;
        float baseScale = 0f;
        long animStart = 0L;
        boolean isHealing = false;
        boolean extending = false;
        boolean shrinking = false;
        float alpha = 1.0f;

        boolean oldActive = false;
        float oldStart = 0f;
        float oldEnd = 0f;
        float oldAlpha = 1.0f;
        long oldAnimStart = 0L;

        void interrupt(long now) {
            if (active) {
                oldStart = Math.min(startOverlay, endOverlay);
                oldEnd = Math.max(startOverlay, endOverlay);
                oldActive = true;
                oldAlpha = 1.0f;
                oldAnimStart = now;
                active = false;
            }
        }

        void trigger(float currentVisualScale, float exactTargetScale, long now) {
            interrupt(now);
            maxWidth = Math.abs(exactTargetScale - currentVisualScale);
            if (maxWidth > 0.001f) {
                isHealing = exactTargetScale > currentVisualScale;
                baseScale = currentVisualScale;
                startOverlay = baseScale;
                endOverlay = baseScale;
                animStart = now;
                active = true;
                extending = true;
                shrinking = false;
                alpha = 1.0f;
            }
        }

        void update(long now, float currentVisualScale, float baseShrinkTime, float fadeTimeSec, BarConfig.EasingType easing) {
            if (oldActive) {
                long oldElapsed = now - oldAnimStart;
                float oldDur = 150f;
                if (oldElapsed >= oldDur) {
                    oldActive = false;
                } else {
                    float progress = Math.min(1.0f, oldElapsed / oldDur);
                    oldAlpha = 1.0f - progress;
                    oldStart = Mth.lerp(progress, oldStart, currentVisualScale);
                    oldEnd = Mth.lerp(progress, oldEnd, currentVisualScale);
                }
            }

            if (!active) return;
            long elapsed = now - animStart;

            if (extending) {
                float progress = Math.min(1.0f, elapsed / 350f);
                float eased = BarConfig.interpolate(progress, BarConfig.EasingType.EASE_OUT_CUBIC);
                if (isHealing) {
                    endOverlay = baseScale + (maxWidth * eased);
                } else {
                    startOverlay = baseScale - (maxWidth * eased);
                }
                if (progress >= 1.0f) {
                    if (isHealing) {
                        startOverlay = baseScale;
                        endOverlay = baseScale + maxWidth;
                    } else {
                        startOverlay = baseScale - maxWidth;
                        endOverlay = baseScale;
                    }
                    extending = false;
                    shrinking = true;
                    animStart = now;
                }
            } else if (shrinking) {
                float dynamicDuration = Math.max(0.3f, maxWidth * baseShrinkTime) * 1000f;
                long shrinkDurationMs = (long) dynamicDuration;
                long shrinkElapsed = elapsed;

                if (shrinkElapsed >= shrinkDurationMs) {
                    active = false;
                    shrinking = false;
                } else {
                    float progress = Math.min(1.0f, shrinkElapsed / (float) shrinkDurationMs);
                    float eased = BarConfig.interpolate(progress, easing);

                    if (isHealing) {
                        startOverlay = baseScale + (maxWidth * eased);
                    } else {
                        endOverlay = baseScale - (maxWidth * eased);
                    }

                    float actualFadeTimeMs = Math.max(1f, fadeTimeSec * 1000f);
                    float fadeStartMs = dynamicDuration - actualFadeTimeMs;

                    if (shrinkElapsed > fadeStartMs) {
                        alpha = 1.0f - (shrinkElapsed - fadeStartMs) / actualFadeTimeMs;
                        if (alpha < 0) alpha = 0;
                    } else {
                        alpha = 1.0f;
                    }
                }
            }
        }

        void emit(Sink sink) {
            if (oldActive && oldAlpha > 0.01f) {
                float s = Math.min(oldStart, oldEnd), e = Math.max(oldStart, oldEnd);
                if (e - s > 0.001f) sink.accept(s, e, oldAlpha);
            }
            if (active && alpha > 0.01f) {
                float s = Math.min(startOverlay, endOverlay), e = Math.max(startOverlay, endOverlay);
                if (e - s > 0.001f) sink.accept(s, e, alpha);
            }
        }
    }
}