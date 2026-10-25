package com.dsp2.genshinerc.client.render.hud.gui.rpg;

import com.dsp2.genshinerc.client.render.hud.bar.config.BarConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Quaternionf;

public final class PlayerModelPanel {
    private static boolean rendering;

    private PlayerModelPanel() {}

    public static boolean isRendering() { return rendering; }

    static void render(GuiGraphics g, LivingEntity entity, int x1, int y1, int x2, int y2,
                       BarConfig cfg, float ox, float oy, float scale) {
        g.enableScissor((int) Math.floor(ox + x1 * scale), (int) Math.floor(oy + y1 * scale),
                (int) Math.ceil(ox + x2 * scale), (int) Math.ceil(oy + y2 * scale));

        int panelH = y2 - y1;
        int modelScale = Math.max(1, Math.round(panelH * 0.55f * cfg.rpgModelZoom));
        int cx = (x1 + x2) / 2;
        int feetY = y1 + 3 + Math.round(1.85f * modelScale);

        Quaternionf pose = new Quaternionf().rotateZ(Mth.PI);
        Quaternionf camera = new Quaternionf().rotateX(RpgHudLayout.MODEL_PITCH * Mth.DEG_TO_RAD);
        pose.mul(camera);

        float bodyRot = entity.yBodyRot, yRot = entity.getYRot(), xRot = entity.getXRot();
        float headRot = entity.yHeadRot, headRotO = entity.yHeadRotO;
        entity.yBodyRot = 180f + cfg.rpgModelYaw;
        entity.setYRot(180f + cfg.rpgModelYaw);
        entity.setXRot(0f);
        entity.yHeadRot = entity.getYRot();
        entity.yHeadRotO = entity.getYRot();

        rendering = true;
        try {
            InventoryScreen.renderEntityInInventory(g, cx, feetY, modelScale, pose, camera, entity);
        } finally {
            rendering = false;
            entity.yBodyRot = bodyRot;
            entity.setYRot(yRot);
            entity.setXRot(xRot);
            entity.yHeadRot = headRot;
            entity.yHeadRotO = headRotO;
            g.disableScissor();
        }
    }
}