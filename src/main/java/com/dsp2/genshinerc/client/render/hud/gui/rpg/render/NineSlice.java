package com.dsp2.genshinerc.client.render.hud.gui.rpg.render;

import com.dsp2.genshinerc.client.render.hud.gui.rpg.RpgHudElement.Box;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public final class NineSlice {
    private NineSlice() {}

    public record Corner(float w, float h, float su, float sv) {}

    public static Corner corner(ResourceLocation tex, int sliceOverride, float cornerOverride, float refHeight) {
        TexScan.Frame f = TexScan.frame(tex);
        int bw = Math.max(1, f.ax1() - f.ax0()), bh = Math.max(1, f.ay1() - f.ay0());
        int sx = sliceOverride > 0 ? Math.min(sliceOverride, Math.max(1, bw / 2)) : f.sliceX();
        int sy = sliceOverride > 0 ? Math.min(sliceOverride, Math.max(1, bh / 2)) : f.sliceY();
        float scale = cornerOverride > 0f ? cornerOverride / Math.max(1, Math.max(sx, sy)) : refHeight / bh;
        return new Corner(sx * scale, sy * scale, sx / (float) f.w(), sy / (float) f.h());
    }

    public static void draw(GuiGraphics g, ResourceLocation tex, Box b, Corner c, int argb) {
        TexScan.Frame f = TexScan.frame(tex);
        float u0 = f.ax0() / (float) f.w(), u3 = f.ax1() / (float) f.w();
        float v0 = f.ay0() / (float) f.h(), v3 = f.ay1() / (float) f.h();
        float cw = Math.min(c.w(), b.w() / 2f), ch = Math.min(c.h(), b.h() / 2f);
        float su = c.w() <= 0f ? 0f : c.su() * cw / c.w(), sv = c.h() <= 0f ? 0f : c.sv() * ch / c.h();

        float[] xs = {b.x(), b.x() + cw, b.x2() - cw, b.x2()};
        float[] ys = {b.y(), b.y() + ch, b.y2() - ch, b.y2()};
        float[] us = {u0, u0 + su, u3 - su, u3};
        float[] vs = {v0, v0 + sv, v3 - sv, v3};
        for (int row = 0; row < 3; row++)
            for (int col = 0; col < 3; col++) {
                if (xs[col + 1] - xs[col] <= 0.001f || ys[row + 1] - ys[row] <= 0.001f) continue;
                GuiQuad.blit(g, tex, xs[col], ys[row], xs[col + 1], ys[row + 1], us[col], vs[row], us[col + 1], vs[row + 1], argb);
            }
    }
}