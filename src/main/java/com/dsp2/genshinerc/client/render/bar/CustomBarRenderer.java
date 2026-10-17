package com.dsp2.genshinerc.client.render.bar;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
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

    public static final ResourceLocation BG_TEXTURE = new ResourceLocation("genshinerc", "textures/gui/HP_bar.png");
    public static final ResourceLocation FILL_TEXTURE = new ResourceLocation("genshinerc", "textures/gui/HP_bar2.png");

    public static class Tracker {
        public float currentHp;
        public float displayHp;
        public float maxHp;

        public Tracker(LivingEntity entity) {
            this.currentHp = entity.getHealth();
            this.displayHp = entity.getHealth();
            this.maxHp = entity.getMaxHealth();
        }

        public void update(LivingEntity entity) {
            float actual = entity.getHealth();
            if (actual < currentHp) {
                displayHp = currentHp;
            }
            currentHp = actual;
            maxHp = entity.getMaxHealth();

            if (displayHp > currentHp) {
                displayHp = Mth.lerp(0.05f, displayHp, currentHp);
                if (displayHp - currentHp < 0.01f) displayHp = currentHp;
            } else {
                displayHp = currentHp;
            }
        }
    }

    private static final Map<LivingEntity, Tracker> TRACKERS = new ConcurrentHashMap<>();

    public static void render(LivingEntity entity, LivingEntityPatch<?> entitypatch, LocalPlayerPatch playerpatch, PoseStack poseStack, MultiBufferSource buffers, float partialTicks) {
        if (!(entity instanceof LivingEntity)) return;

        BarConfig config = BarConfig.get();
        if (!config.enabled) return;

        if (entity.distanceToSqr(Minecraft.getInstance().getCameraEntity()) > config.maxDistance * config.maxDistance) return;

        Tracker tracker = TRACKERS.computeIfAbsent(entity, Tracker::new);
        tracker.update(entity);

        float actualHeight = entity.getBbHeight() + config.yOffset + (entity.getBbHeight() * config.heightScaleFactor);
        float actualSize = config.baseSize * (1.0f + (entity.getBbWidth() * config.bboxSizeFactor));

        EntityUI dummyUI = EntityUI.HEALTH_BAR;
        Matrix4f matrix = dummyUI.getModelViewMatrixAlignedToCamera(
                poseStack, entity, 0.0F, actualHeight, 0.0F, true, partialTicks);

        matrix.scale(actualSize, actualSize, actualSize);

        float healthBarHeight = 0.048828125F;
        float innerHealthBarHeight = 0.029296875F;
        float maxHealth = tracker.maxHp;

        drawTexture(matrix, buffers, BG_TEXTURE,
                -0.5F, -healthBarHeight, 0.5F, healthBarHeight,
                0.0F, config.bgV0, 1.0F, config.bgV1,
                0xFFFFFFFF);

        float displayRatio = Mth.clamp(tracker.displayHp / maxHealth, 0.0F, 1.0F);
        if (displayRatio > 0) {
            float displayEndX = Math.max(-0.5F + displayRatio, -0.46875F);
            drawTexture(matrix, buffers, FILL_TEXTURE,
                    -0.5F, -innerHealthBarHeight, displayEndX, innerHealthBarHeight,
                    0.0F, config.fillV0, displayRatio, config.fillV1,
                    0xFFFFFFFF);
        }

        float ratio = Mth.clamp(tracker.currentHp / maxHealth, 0.0F, 1.0F);
        if (ratio > 0) {
            float fillEndX = Math.max(-0.5F + ratio, -0.46875F);
            int fillColor = getColorForEntity(entity, config);

            drawTexture(matrix, buffers, FILL_TEXTURE,
                    -0.5F, -innerHealthBarHeight, fillEndX, innerHealthBarHeight,
                    0.0F, config.fillV0, ratio, config.fillV1,
                    fillColor);
        }
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