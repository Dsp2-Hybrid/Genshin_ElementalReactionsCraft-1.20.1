package com.dsp2.genshinerc.client.render.hud.bar.shield;

public final class AdaptiveColor {
    private AdaptiveColor() {}
    public static int contrastAgainst(int bg) {
        int r = (bg >> 16) & 0xFF, g = (bg >> 8) & 0xFF, b = bg & 0xFF;
        float luma = (0.299f * r + 0.587f * g + 0.114f * b) / 255f;
        return luma > 0.5f ? 0x000000 : 0xFFFFFF;
    }
    public static int argbContrast(int bgARGB, float alpha) {
        int rgb = contrastAgainst(bgARGB & 0xFFFFFF);
        return (Math.round(alpha * 255f) << 24) | rgb;
    }
}