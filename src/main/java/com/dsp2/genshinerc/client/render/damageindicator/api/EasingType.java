package com.dsp2.genshinerc.client.render.damageindicator.api;

public enum EasingType {
    LINEAR, EASE_IN, EASE_OUT, EASE_IN_OUT;

    public float apply(float t) {
        t = Math.max(0f, Math.min(1f, t));
        return switch (this) {
            case LINEAR -> t;
            case EASE_IN -> t * t;
            case EASE_OUT -> 1f - (1f - t) * (1f - t);
            case EASE_IN_OUT -> t < 0.5f ? 2f * t * t : 1f - (float) Math.pow(-2f * t + 2f, 2f) / 2f;
        };
    }
    public static EasingType fromName(String s) {
        if (s == null) return EASE_IN_OUT;
        try { return valueOf(s.toUpperCase()); } catch (Exception e) { return EASE_IN_OUT; }
    }
}