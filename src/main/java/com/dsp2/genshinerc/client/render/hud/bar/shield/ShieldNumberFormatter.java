package com.dsp2.genshinerc.client.render.hud.bar.shield;

public final class ShieldNumberFormatter {
    private ShieldNumberFormatter() {}
    public static String format(float v) {
        if (v < 0.001f) return "0";
        if (v >= 1000f) return String.format("%d", Math.round(v));
        return String.format("%.1f", v);
    }
}