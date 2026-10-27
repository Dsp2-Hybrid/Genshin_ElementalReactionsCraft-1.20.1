package com.dsp2.genshinerc.client.render.hud.gui.rpg.render;

import com.dsp2.genshinerc.client.render.hud.bar.config.BarConfig;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

@SuppressWarnings({"deprecation", "removal"})
public final class DiamondModelView {
    public static final ResourceLocation BG = new ResourceLocation("genshinerc", "textures/hud/rpg/rpg_bg.png");
    public static final ResourceLocation BORDER = new ResourceLocation("genshinerc", "textures/hud/rpg/rpg_border.png");

    private static final float BASE_SCALE = 0.55f;
    private static final float FOCAL = 100f;

    private static boolean rendering;

    private DiamondModelView() {}

    public static boolean isRendering() { return rendering; }

    public static void render(GuiGraphics g, LivingEntity entity, float x, float y, float size, BarConfig cfg) {
        float x2 = x + size, y2 = y + size;
        GuiQuad.blit(g, BG, x, y, x2, y2, 0f, 0f, 1f, 1f, argb(cfg.rpgDiamondBgAlpha, 0xFFFFFF));

        int[] bounds = screenBounds(g, x, y, x2, y2);
        boolean stencil = cfg.rpgModelStencil && GuiStencil.ensureEnabled();
        if (stencil) {
            GuiStencil.beginMask(g);
            GuiQuad.blit(g, BG, x, y, x2, y2, 0f, 0f, 1f, 1f, 0xFFFFFFFF);
            GuiStencil.useMask(g);
        } else {
            g.enableScissor(bounds[0], bounds[1], bounds[2], bounds[3]);
        }
        try {
            drawModel(g, entity, x + size / 2f, y + size / 2f, size, cfg);
        } finally {
            if (stencil) GuiStencil.end(g);
            else g.disableScissor();
        }

        g.flush();
        g.enableScissor(bounds[0], bounds[1], bounds[2], bounds[3]);
        RenderSystem.clear(GL11.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);
        g.disableScissor();

        GuiQuad.blit(g, BORDER, x, y, x2, y2, 0f, 0f, 1f, 1f, 0xFFFFFFFF);
    }

    private static void drawModel(GuiGraphics g, LivingEntity e, float cx, float cy, float size, BarConfig cfg) {
        int modelScale = Math.max(1, Math.round(size * BASE_SCALE * cfg.rpgModelZoom));
        float dolly = FOCAL / Math.max(10f, FOCAL - cfg.rpgCamZ);
        int pivotY = Math.round(e.getBbHeight() * modelScale * cfg.rpgModelPivot);

        PoseStack pose = g.pose();
        pose.pushPose();
        pose.translate(cx - cfg.rpgCamX, cy - cfg.rpgCamY, 0f);
        pose.scale(dolly, dolly, dolly);
        pose.mulPose(Axis.ZP.rotationDegrees(-cfg.rpgCamRoll));
        pose.mulPose(Axis.XP.rotationDegrees(-cfg.rpgCamPitch));
        pose.mulPose(Axis.YP.rotationDegrees(-cfg.rpgCamYaw));

        float bodyRot = e.yBodyRot, yRot = e.getYRot(), xRot = e.getXRot();
        float headRot = e.yHeadRot, headRotO = e.yHeadRotO;
        float facing = 180f + cfg.rpgModelYaw;
        e.yBodyRot = facing;
        e.setYRot(facing);
        e.setXRot(0f);
        e.yHeadRot = facing;
        e.yHeadRotO = facing;

        rendering = true;
        try {
            InventoryScreen.renderEntityInInventory(g, 0, pivotY, modelScale,
                    new Quaternionf().rotateZ(Mth.PI),
                    new Quaternionf().rotateX(-cfg.rpgCamPitch * Mth.DEG_TO_RAD), e);
        } finally {
            rendering = false;
            e.yBodyRot = bodyRot;
            e.setYRot(yRot);
            e.setXRot(xRot);
            e.yHeadRot = headRot;
            e.yHeadRotO = headRotO;
            pose.popPose();
        }
    }

    private static int[] screenBounds(GuiGraphics g, float x1, float y1, float x2, float y2) {
        Matrix4f m = g.pose().last().pose();
        Vector3f v = new Vector3f();
        float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE, maxX = -Float.MAX_VALUE, maxY = -Float.MAX_VALUE;
        float[] xs = {x1, x2, x1, x2}, ys = {y1, y1, y2, y2};
        for (int i = 0; i < 4; i++) {
            m.transformPosition(xs[i], ys[i], 0f, v);
            minX = Math.min(minX, v.x); maxX = Math.max(maxX, v.x);
            minY = Math.min(minY, v.y); maxY = Math.max(maxY, v.y);
        }
        return new int[]{(int) Math.floor(minX), (int) Math.floor(minY), (int) Math.ceil(maxX), (int) Math.ceil(maxY)};
    }

    private static int argb(float alpha, int rgb) {
        return (Mth.clamp(Math.round(alpha * 255f), 0, 255) << 24) | (rgb & 0xFFFFFF);
    }
}