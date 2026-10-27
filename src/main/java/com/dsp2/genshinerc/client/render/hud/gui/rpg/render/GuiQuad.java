package com.dsp2.genshinerc.client.render.hud.gui.rpg.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

public final class GuiQuad {
    private GuiQuad() {}

    public static void blit(GuiGraphics g, ResourceLocation tex, float x1, float y1, float x2, float y2,
                            float u1, float v1, float u2, float v2, int argb) {
        int a = argb >>> 24;
        if (a == 0 || x2 <= x1 || y2 <= y1) return;
        int r = (argb >> 16) & 0xFF, gr = (argb >> 8) & 0xFF, b = argb & 0xFF;

        RenderSystem.setShaderTexture(0, tex);
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        Matrix4f m = g.pose().last().pose();
        BufferBuilder buf = Tesselator.getInstance().getBuilder();
        buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        buf.vertex(m, x1, y1, 0f).uv(u1, v1).color(r, gr, b, a).endVertex();
        buf.vertex(m, x1, y2, 0f).uv(u1, v2).color(r, gr, b, a).endVertex();
        buf.vertex(m, x2, y2, 0f).uv(u2, v2).color(r, gr, b, a).endVertex();
        buf.vertex(m, x2, y1, 0f).uv(u2, v1).color(r, gr, b, a).endVertex();
        BufferUploader.drawWithShader(buf.end());
        RenderSystem.disableBlend();
    }
}