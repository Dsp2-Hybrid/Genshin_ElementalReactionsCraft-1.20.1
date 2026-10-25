package com.dsp2.genshinerc.client.render.hud.bar.layout;

import com.dsp2.genshinerc.client.render.hud.HudDraw;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

public final class SlicedBar {
    private SlicedBar() {}

    public static void draw(Matrix4f m, MultiBufferSource buffers, ResourceLocation tex, BarTextureInfo info,
                            float[] q, float clipX1, float clipX2, float z, int rgb, float alpha) {
        if (clipX2 - clipX1 <= 1.0e-4f || alpha <= 0.004f) return;

        float px = (q[3] - q[1]) / info.height;
        float visL = info.visibleLeft(q), visR = info.visibleRight(q);
        float capL = info.capLeft * px, capR = info.capRight * px;
        float span = visR - visL;
        if (capL + capR > span) {
            float s = span / (capL + capR);
            capL *= s;
            capR *= s;
        }

        float[] xs = {
                visL - info.left * px,
                visL + capL,
                visR - capR,
                visR + (info.width - info.right) * px
        };
        float[] us = {
                0f,
                (info.left + info.capLeft) / (float) info.width,
                (info.right - info.capRight) / (float) info.width,
                1f
        };
        for (int i = 0; i < 3; i++) {
            segment(m, buffers, tex, xs[i], xs[i + 1], us[i], us[i + 1], clipX1, clipX2, q[1], q[3], z, rgb, alpha);
        }
    }

    private static void segment(Matrix4f m, MultiBufferSource buffers, ResourceLocation tex,
                                float x1, float x2, float u1, float u2, float clipX1, float clipX2,
                                float y1, float y2, float z, int rgb, float alpha) {
        if (x2 - x1 <= 1.0e-6f) return;
        float a = Math.max(x1, clipX1), b = Math.min(x2, clipX2);
        if (b - a <= 1.0e-5f) return;
        float ua = Mth.lerp((a - x1) / (x2 - x1), u1, u2);
        float ub = Mth.lerp((b - x1) / (x2 - x1), u1, u2);
        HudDraw.quad(m, buffers, tex, a, y1, b, y2, ua, 0f, ub, 1f, z, rgb, alpha);
    }
}