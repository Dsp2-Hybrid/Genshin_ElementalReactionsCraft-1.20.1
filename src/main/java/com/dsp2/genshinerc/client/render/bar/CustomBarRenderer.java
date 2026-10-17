package com.dsp2.genshinerc.client.render.bar;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import org.joml.Matrix4f;
import yesman.epicfight.client.gui.EntityUI;
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
        BarConfig config = BarConfig.get();
        if (!config.enabled) return;

        Tracker tracker = TRACKERS.computeIfAbsent(entity, Tracker::new);
        tracker.update(entity);

        float actualHeight = entity.getBbHeight() + config.yOffset + (entity.getBbHeight() * config.heightScaleFactor);
        float actualSize = config.baseSize * (1.0f + (entity.getBbWidth() * config.bboxSizeFactor));

        EntityUI dummyUI = EntityUI.HEALTH_BAR;
        Matrix4f matrix = dummyUI.getModelViewMatrixAlignedToCamera(
                poseStack, entity, 0.0F, actualHeight, 0.0F, true, partialTicks);

        matrix.scale(actualSize, actualSize, actualSize);

        // 修正高度：你的贴图宽273，高16，宽高比 1.0 : (16/273) ≈ 0.0586
        float healthBarHeight = 16.0F / 273.0F / 2.0F;
        // 内部填充稍微矮一点
        float innerHealthBarHeight = healthBarHeight * 0.7F;

        float maxHealth = tracker.maxHp;

        float bgV0F = (float)config.bgV0 / config.uvSize;
        float bgV1F = (float)config.bgV1 / config.uvSize;
        float fillV0F = (float)config.fillV0 / config.uvSize;
        float fillV1F = (float)config.fillV1 / config.uvSize;

        drawTexture(matrix, buffers, BG_TEXTURE,
                -0.5F, -healthBarHeight, 0.5F, healthBarHeight,
                0.0F, bgV0F, 1.0F, bgV1F,
                0xFFFFFFFF);

        float displayRatio = Mth.clamp(tracker.displayHp / maxHealth, 0.0F, 1.0F);
        if (displayRatio > 0) {
            float displayEndX = Math.max(-0.5F + displayRatio, -0.46875F);
            drawTexture(matrix, buffers, FILL_TEXTURE,
                    -0.5F, -innerHealthBarHeight, displayEndX, innerHealthBarHeight,
                    0.0F, fillV0F, displayRatio, fillV1F,
                    0xFFFFFFFF);
        }

        float ratio = Mth.clamp(tracker.currentHp / maxHealth, 0.0F, 1.0F);
        if (ratio > 0) {
            float fillEndX = Math.max(-0.5F + ratio, -0.46875F);
            int fillColor = getColorForEntity(entity, config);

            drawTexture(matrix, buffers, FILL_TEXTURE,
                    -0.5F, -innerHealthBarHeight, fillEndX, innerHealthBarHeight,
                    0.0F, fillV0F, ratio, fillV1F,
                    fillColor);
        }
    }

    private static void drawTexture(Matrix4f matrix, MultiBufferSource buffer, ResourceLocation texture,
                                    float minX, float minY, float maxX, float maxY,
                                    float minU, float minV, float maxU, float maxV, int color) {
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.text(texture));
        // uv2(0x00F000F0) 代表全亮光照
        vertexConsumer.vertex(matrix, minX, minY, 0.0F).color(color).uv(minU, maxV).uv2(0x00F000F0).endVertex();
        vertexConsumer.vertex(matrix, maxX, minY, 0.0F).color(color).uv(maxU, maxV).uv2(0x00F000F0).endVertex();
        vertexConsumer.vertex(matrix, maxX, maxY, 0.0F).color(color).uv(maxU, minV).uv2(0x00F000F0).endVertex();
        vertexConsumer.vertex(matrix, minX, maxY, 0.0F).color(color).uv(minU, minV).uv2(0x00F000F0).endVertex();
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