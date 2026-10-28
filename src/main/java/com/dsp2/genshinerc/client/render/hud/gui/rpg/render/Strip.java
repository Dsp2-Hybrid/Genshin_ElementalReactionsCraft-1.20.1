package com.dsp2.genshinerc.client.render.hud.gui.rpg.render;

import com.dsp2.genshinerc.client.render.hud.gui.rpg.RpgHudElement.Box;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public final class Strip {
    public final RpgTextures.Info i;
    private final float u0, u1, v0, v1;

    private Strip(ResourceLocation tex) {
        i = RpgTextures.info(tex);
        u0 = i.ax0() / (float) i.w();
        u1 = i.ax1() / (float) i.w();
        v0 = i.vTop();
        v1 = i.vBottom();
    }

    public static Strip of(ResourceLocation tex) { return new Strip(tex); }

    public float xAt(Box b, float u) { return b.x() + b.w() * (u - u0) / Math.max(1.0e-6f, u1 - u0); }

    public void full(GuiGraphics g, ResourceLocation layer, Box b, int argb) {
        GuiQuad.blit(g, layer, b.x(), b.y(), b.x2(), b.y2(), u0, v0, u1, v1, argb);
    }

    public void seg(GuiGraphics g, ResourceLocation layer, Box b, float s0, float s1, int argb) {
        s0 = Mth.clamp(s0, 0f, 1f);
        s1 = Mth.clamp(s1, 0f, 1f);
        if (s1 - s0 <= 0.001f) return;
        float a0 = i.u(s0), a1 = i.u(s1);
        GuiQuad.blit(g, layer, xAt(b, a0), b.y(), xAt(b, a1), b.y2(), a0, v0, a1, v1, argb);
    }

    public void background(GuiGraphics g, Box b, int rgb, float alpha) { full(g, i.tint(), b, HudGeom.argb(alpha, rgb)); }

    public void outline(GuiGraphics g, Box b, float alpha) { full(g, i.outline(), b, HudGeom.argb(alpha, 0xFFFFFF)); }
}