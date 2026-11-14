package com.dsp2.genshinerc.client.render.hud.gui.rpg.layer;

import com.dsp2.genshinerc.client.render.hud.bar.shield.AdaptiveColor;
import com.dsp2.genshinerc.client.render.hud.bar.shield.ShieldNumberFormatter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;

public final class ShieldOverlay {
    private float visualValue, targetValue, maxCapacity;
    private boolean animating;
    private long animStart;
    private static final long ANIM_DURATION_MS = 300;

    public void updateValue(float newTarget, long now) {
        if (Math.abs(newTarget - targetValue) > 0.01f) {
            visualValue = current();
            targetValue = newTarget;
            animating = true;
            animStart = now;
        }
    }
    public void setMaxCapacity(float m) { this.maxCapacity = m; }
    private float current() {
        if (!animating) return targetValue;
        long e = System.currentTimeMillis() - animStart;
        if (e >= ANIM_DURATION_MS) { animating = false; visualValue = targetValue; return targetValue; }
        return Mth.lerp(easeOut(e / (float) ANIM_DURATION_MS), visualValue, targetValue);
    }
    private float easeOut(float t) { return 1f - (1f - t) * (1f - t); }

    public void render(GuiGraphics g, int x, int y, int w, int h, int fillColor, float alpha) {
        float val = current();
        float frac = maxCapacity > 0f ? Mth.clamp(val / maxCapacity, 0f, 1f) : 0f;
        g.fill(x, y, x + w, y + h, 0x80000000);
        g.fill(x, y, x + Math.round(w * frac), y + h, ((int) (alpha * 255) << 24) | (fillColor & 0xFFFFFF));
        if (animating && frac < 0.99f) {
            float t = (System.currentTimeMillis() - animStart) / (float) ANIM_DURATION_MS;
            float a = (1f - easeOut(t)) * alpha;
            float of = Math.min(frac, visualValue / Math.max(maxCapacity, 1f));
            g.fill(x, y, x + Math.round(w * of), y + h, (Math.round(a * 255) << 24) | 0xFFFFFF);
        }
        int textColor = AdaptiveColor.contrastAgainst(compositeBg(fillColor, frac));
        String text = ShieldNumberFormatter.format(val);
        var font = Minecraft.getInstance().font;
        int tw = font.width(text);
        g.drawString(font, text, x + (w - tw) / 2, y + (h - font.lineHeight) / 2, textColor, false);
    }
    private int compositeBg(int fill, float frac) {
        if (frac < 0.01f) return 0x404040;
        if (frac > 0.99f) return fill;
        return (lerp(0x40, (fill >> 16) & 0xFF, frac) << 16) | (lerp(0x40, (fill >> 8) & 0xFF, frac) << 8) | lerp(0x40, fill & 0xFF, frac);
    }
    private int lerp(int a, int b, float t) { return Math.round(a + (b - a) * t); }
}