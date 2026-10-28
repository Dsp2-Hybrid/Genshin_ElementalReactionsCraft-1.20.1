package com.dsp2.genshinerc.client.render.hud.gui.rpg.render;

import com.dsp2.genshinerc.client.render.hud.gui.rpg.RpgHudElement.Box;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

@SuppressWarnings({"deprecation", "removal"})
public final class HudGeom {
    public static final ResourceLocation ICONS = new ResourceLocation("minecraft", "textures/gui/icons.png");
    private static final int GLOW_SEGMENTS = 20;

    private HudGeom() {}

    public static int argb(float a, int rgb) { return (Mth.clamp(Math.round(a * 255f), 0, 255) << 24) | (rgb & 0xFFFFFF); }

    public static void rect(GuiGraphics g, float x0, float y0, float x1, float y1, int argb) {
        if (x1 - x0 <= 0.001f || y1 - y0 <= 0.001f) return;
        GuiQuad.blit(g, RpgTextures.pixel(), x0, y0, x1, y1, 0f, 0f, 1f, 1f, argb);
    }

    public static void icon(GuiGraphics g, ResourceLocation atlas, int u, int v, Box b, int argb) {
        GuiQuad.blit(g, atlas, b.x(), b.y(), b.x2(), b.y2(), u / 256f, v / 256f, (u + 9) / 256f, (v + 9) / 256f, argb);
    }

    public static void glow(GuiGraphics g, float cx, float cy, float radius, int rgb, float alpha) {
        int a = Mth.clamp(Math.round(alpha * 255f), 0, 255);
        if (a == 0 || radius <= 0.01f) return;
        int r = (rgb >> 16) & 0xFF, gr = (rgb >> 8) & 0xFF, b = rgb & 0xFF;
        Matrix4f m = g.pose().last().pose();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        boolean toggleCull = !HudMirror.active();
        if (toggleCull) RenderSystem.disableCull();
        BufferBuilder buf = Tesselator.getInstance().getBuilder();
        buf.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
        buf.vertex(m, cx, cy, 0f).color(r, gr, b, a).endVertex();
        for (int i = 0; i <= GLOW_SEGMENTS; i++) {
            float t = i / (float) GLOW_SEGMENTS * Mth.TWO_PI;
            buf.vertex(m, cx + Mth.cos(t) * radius, cy + Mth.sin(t) * radius, 0f).color(r, gr, b, 0).endVertex();
        }
        BufferUploader.drawWithShader(buf.end());
        if (toggleCull) RenderSystem.enableCull();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }
}