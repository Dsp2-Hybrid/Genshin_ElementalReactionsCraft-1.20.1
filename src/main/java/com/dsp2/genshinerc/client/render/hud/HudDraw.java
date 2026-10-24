package com.dsp2.genshinerc.client.render.hud;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

public final class HudDraw {
    private HudDraw() {}

    public static void quad(Matrix4f m, MultiBufferSource buffers, ResourceLocation tex,
                            float x1, float y1, float x2, float y2,
                            float u1, float v1, float u2, float v2,
                            float z, int rgb, float alpha) {
        int a = Mth.clamp((int) (alpha * 255f), 0, 255);
        if (a == 0) return;
        int r = (rgb >> 16) & 0xFF, g = (rgb >> 8) & 0xFF, b = rgb & 0xFF;
        VertexConsumer vc = buffers.getBuffer(HudRenderTypes.overlay(tex));
        vc.vertex(m, x1, y1, z).uv(u1, v2).color(r, g, b, a).endVertex();
        vc.vertex(m, x2, y1, z).uv(u2, v2).color(r, g, b, a).endVertex();
        vc.vertex(m, x2, y2, z).uv(u2, v1).color(r, g, b, a).endVertex();
        vc.vertex(m, x1, y2, z).uv(u1, v1).color(r, g, b, a).endVertex();
    }

    public static void rect(Matrix4f m, MultiBufferSource buffers,
                            float x1, float y1, float x2, float y2, float z, int rgb, float alpha) {
        quad(m, buffers, HudRenderTypes.whiteTexture(), x1, y1, x2, y2, 0, 0, 1, 1, z, rgb, alpha);
    }

    public static int lerpRgb(int from, int to, float t) {
        t = Mth.clamp(t, 0f, 1f);
        int r = (int) Mth.lerp(t, (from >> 16) & 0xFF, (to >> 16) & 0xFF);
        int g = (int) Mth.lerp(t, (from >> 8) & 0xFF, (to >> 8) & 0xFF);
        int b = (int) Mth.lerp(t, from & 0xFF, to & 0xFF);
        return (r << 16) | (g << 8) | b;
    }

    public static int scaleRgb(int rgb, float f) {
        int r = Mth.clamp((int) (((rgb >> 16) & 0xFF) * f), 0, 255);
        int g = Mth.clamp((int) (((rgb >> 8) & 0xFF) * f), 0, 255);
        int b = Mth.clamp((int) ((rgb & 0xFF) * f), 0, 255);
        return (r << 16) | (g << 8) | b;
    }
}