package com.dsp2.genshinerc.client.render.bar;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import org.joml.Matrix4f;
import yesman.epicfight.client.gui.EntityUI;
import yesman.epicfight.client.renderer.EpicFightRenderTypes;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import com.dsp2.genshinerc.client.render.bar.config.BarConfig;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings({"removal", "unused", "deprecation"})
public class CustomBarRenderer {

    public static final ResourceLocation HP_BAR_TEXTURE = new ResourceLocation("genshinerc", "textures/gui/HP_bar.png");

    public static class Tracker {
        public float currentHp;
        public float maxHp;

        public Tracker(LivingEntity entity) {
            this.currentHp = entity.getHealth();
            this.maxHp = entity.getMaxHealth();
        }
        public void update(LivingEntity entity) {
            this.currentHp = entity.getHealth();
            this.maxHp = entity.getMaxHealth();
        }
    }

    private static final Map<LivingEntity, Tracker> TRACKERS = new ConcurrentHashMap<>();

    public static void render(LivingEntity entity, LivingEntityPatch<?> entitypatch, LocalPlayerPatch playerpatch, PoseStack poseStack, MultiBufferSource buffers, float partialTicks) {
        BarConfig config = BarConfig.get();
        if (!config.enabled) return;

        Tracker tracker = TRACKERS.computeIfAbsent(entity, Tracker::new);
        tracker.update(entity);

        EntityUI dummyUI = EntityUI.HEALTH_BAR;
        Matrix4f matrix = dummyUI.getModelViewMatrixAlignedToCamera(
                poseStack, entity, 0.0F, entity.getBbHeight() + config.yOffset, 0.0F, true, partialTicks);

        float scale = config.scaleFactor;
        matrix.scale(scale, scale, scale);

        float halfWidth = 0.5F;
        float halfHeight = 0.05F;

        drawSolidColor(matrix, buffers, -halfWidth, -halfHeight, halfWidth, halfHeight, 0xAA000000);

        float ratio = Mth.clamp(tracker.currentHp / tracker.maxHp, 0.0F, 1.0F);

        if (ratio > 0) {
            float fillEndX = -halfWidth + (halfWidth * 2.0F * ratio);
            int fillColor = getColorForEntity(entity, config);

            float uSpan = config.u1 - config.u0;
            float currentMaxU = config.u0 + (uSpan * ratio);

            drawTexture(matrix, buffers, HP_BAR_TEXTURE,
                    -halfWidth, -halfHeight, fillEndX, halfHeight,
                    config.u0, config.v0, currentMaxU, config.v1,
                    fillColor);
        }
    }

    private static void drawSolidColor(Matrix4f matrix, MultiBufferSource buffer, float minX, float minY, float maxX, float maxY, int color) {
        VertexConsumer vertexConsumer = buffer.getBuffer(EpicFightRenderTypes.entityUIColor());
        vertexConsumer.vertex(matrix, minX, minY, 0.0F).color(color).endVertex();
        vertexConsumer.vertex(matrix, maxX, minY, 0.0F).color(color).endVertex();
        vertexConsumer.vertex(matrix, maxX, maxY, 0.0F).color(color).endVertex();
        vertexConsumer.vertex(matrix, minX, maxY, 0.0F).color(color).endVertex();
    }

    private static void drawTexture(Matrix4f matrix, MultiBufferSource buffer, ResourceLocation texture,
                                    float minX, float minY, float maxX, float maxY,
                                    float minU, float minV, float maxU, float maxV, int color) {
        VertexConsumer vertexConsumer = buffer.getBuffer(EpicFightRenderTypes.entityUITexture(texture));
        vertexConsumer.vertex(matrix, minX, minY, 0.0F).color(color).uv(minU, maxV).endVertex();
        vertexConsumer.vertex(matrix, maxX, minY, 0.0F).color(color).uv(maxU, maxV).endVertex();
        vertexConsumer.vertex(matrix, maxX, maxY, 0.0F).color(color).uv(maxU, minV).endVertex();
        vertexConsumer.vertex(matrix, minX, maxY, 0.0F).color(color).uv(minU, minV).endVertex();
    }

    private static int getColorForEntity(LivingEntity entity, BarConfig config) {
        if (entity instanceof Player) return parseColor(config.allyColor);
        if (entity instanceof Monster) return parseColor(config.enemyColor);
        if (entity instanceof NeutralMob) return parseColor(config.allyColor);
        return parseColor(config.allyColor);
    }

    private static int parseColor(String hex) {
        try {
            return 0xFF000000 | Integer.parseInt(hex.replace("#", ""), 16);
        } catch (Exception e) {
            return 0xFF00FF00;
        }
    }
}