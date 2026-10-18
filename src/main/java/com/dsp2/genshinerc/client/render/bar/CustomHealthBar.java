package com.dsp2.genshinerc.client.render.bar;

import com.dsp2.genshinerc.client.render.bar.config.BarConfig;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import org.joml.Matrix4f;
import yesman.epicfight.client.gui.EntityUI;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

public class CustomHealthBar extends EntityUI {

    private static final ResourceLocation BAR_TEXTURE = ResourceLocation.fromNamespaceAndPath("genshinerc", "textures/gui/hp_bar.png");
    private static final ResourceLocation FRAME_TEXTURE = ResourceLocation.fromNamespaceAndPath("genshinerc", "textures/gui/hp_bar2.png");

    private final Map<LivingEntity, Tracker> trackers = new HashMap<>();

    public CustomHealthBar() {
        super();
    }

    @Override
    public boolean shouldDraw(LivingEntity entity, LivingEntityPatch<?> entitypatch, LocalPlayerPatch playerpatch, float partialTicks) {
        BarConfig config = BarConfig.get();
        if (!config.enabled) return false;
        if (entity == null || !entity.isAlive()) return false;
        if (entity.isInvisibleTo(playerpatch.getOriginal())) return false;

        Minecraft mc = Minecraft.getInstance();
        if (mc.getCameraEntity() != null && entity.distanceToSqr(mc.getCameraEntity()) > config.maxDistance * config.maxDistance) return false;

        Tracker tracker = trackers.computeIfAbsent(entity, k -> new Tracker());
        tracker.update(entity);

        if (entity instanceof Player) {
            if (config.playerMode == BarConfig.PlayerMode.NONE) return false;
            if (config.playerMode == BarConfig.PlayerMode.ALWAYS) return true;
            return tracker.hurtUntil > System.currentTimeMillis();
        }

        return true;
    }

    @Override
    public void draw(LivingEntity entity, LivingEntityPatch<?> entitypatch, LocalPlayerPatch playerpatch, PoseStack poseStack, MultiBufferSource buffers, float partialTicks) {
        BarConfig config = BarConfig.get();
        Tracker tracker = trackers.computeIfAbsent(entity, k -> new Tracker());

        float yOffset = entity.getBbHeight() + config.yOffset;
        Matrix4f matrix = super.getModelViewMatrixAlignedToCamera(poseStack, entity, 0.0F, yOffset, 0.0F, true, partialTicks);

        // 严格按照 6 个配置计算长度与宽度
        float length = config.baseLength + (entity.getBbHeight() * config.heightLengthFactor) + (entity.getBbWidth() * config.bboxLengthFactor);
        float width = config.baseWidth + (entity.getBbHeight() * config.heightWidthFactor) + (entity.getBbWidth() * config.bboxWidthFactor);

        float minX = -0.5F * length;
        float maxX = 0.5F * length;
        float minY = -0.5F * width;
        float maxY = 0.5F * width;

        boolean isAlly = !(entity instanceof Monster);
        Color awtColor = isAlly ? BarConfig.toAwt(config.allyColor) : BarConfig.toAwt(config.enemyColor);
        float r = awtColor.getRed() / 255f;
        float g = awtColor.getGreen() / 255f;
        float b = awtColor.getBlue() / 255f;

        float health = entity.getHealth();
        float absorption = entity.getAbsorptionAmount();
        float maxHealth = entity.getMaxHealth();

        // 计算原版血量/护盾分布
        boolean isOverMax = health + absorption > maxHealth;
        float healthEnd = isOverMax ? Mth.clamp(health / (health + absorption), 0.0F, 1.0F) : Mth.clamp(health / maxHealth, 0.0F, 1.0F);
        float absorptionStart = healthEnd;
        float absorptionEnd = isOverMax ? 1.0F : Mth.clamp((health + absorption) / maxHealth, 0.0F, 1.0F);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.depthMask(false);

        // 1. 最底层：外框/背景（解决遮挡导致看不见血条的问题）
        drawTexturedQuad(matrix, buffers, FRAME_TEXTURE, minX, minY, maxX, maxY, 0, 0, 1, 1, 1.0f, 1.0f, 1.0f, 1.0f);

        // 2. 中层：红/绿血条
        if (healthEnd > 0.0f) {
            drawTexturedQuad(matrix, buffers, BAR_TEXTURE, minX, minY, minX + length * healthEnd, maxY, 0, 0, healthEnd, 1, r, g, b, 1.0f);
        }

        // 3. 护盾黄条
        if (absorptionEnd > absorptionStart) {
            drawTexturedQuad(matrix, buffers, BAR_TEXTURE, minX + length * absorptionStart, minY, minX + length * absorptionEnd, maxY, absorptionStart, 0, absorptionEnd, 1, 1.0f, 0.9f, 0.2f, 1.0f);
        }

        // 4. 顶层：白条动画 (带 4 ticks 淡入淡出)
        if (tracker.isAnimating) {
            long elapsed = System.currentTimeMillis() - tracker.animStartTime;
            float durationMs = config.overlayShrinkTime * 1000f;
            float progress = Math.min(1.0f, elapsed / durationMs);
            float eased = easeOutCubic(progress);

            // 4 ticks = 200ms 的淡入淡出
            float alpha = 1.0f;
            if (elapsed < 200) {
                alpha = elapsed / 200f;
            } else if (durationMs - elapsed < 200) {
                alpha = Math.max(0f, (durationMs - elapsed) / 200f);
            }

            // 血量消耗白条
            if (tracker.oldHealthEnd > healthEnd) {
                float currentEnd = Mth.lerp(eased, tracker.oldHealthEnd, healthEnd);
                if (currentEnd - healthEnd > 0.001f) {
                    drawTexturedQuad(matrix, buffers, BAR_TEXTURE, minX + length * healthEnd, minY, minX + length * currentEnd, maxY, healthEnd, 0, currentEnd, 1, 1.0f, 1.0f, 1.0f, alpha);
                }
            }

            // 护盾消耗白条
            if (tracker.oldAbsorptionEnd > absorptionEnd) {
                float currentEnd = Mth.lerp(eased, tracker.oldAbsorptionEnd, absorptionEnd);
                if (currentEnd - absorptionEnd > 0.001f) {
                    drawTexturedQuad(matrix, buffers, BAR_TEXTURE, minX + length * absorptionEnd, minY, minX + length * currentEnd, maxY, absorptionEnd, 0, currentEnd, 1, 1.0f, 1.0f, 1.0f, alpha);
                }
            }

            if (progress >= 1.0f) {
                tracker.isAnimating = false;
            }
        }

        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    private static void drawTexturedQuad(Matrix4f matrix, MultiBufferSource buffers, ResourceLocation tex, float x1, float y1, float x2, float y2, float u1, float v1, float u2, float v2, float r, float g, float b, float a) {
        VertexConsumer vc = buffers.getBuffer(RenderType.entityTranslucent(tex));
        int rInt = (int)(r * 255);
        int gInt = (int)(g * 255);
        int bInt = (int)(b * 255);
        int aInt = (int)(a * 255);

        vc.vertex(matrix, x1, y1, 0.0F).color(rInt, gInt, bInt, aInt).uv(u1, v2).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(0, 0, 1).endVertex();
        vc.vertex(matrix, x2, y1, 0.0F).color(rInt, gInt, bInt, aInt).uv(u2, v2).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(0, 0, 1).endVertex();
        vc.vertex(matrix, x2, y2, 0.0F).color(rInt, gInt, bInt, aInt).uv(u2, v1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(0, 0, 1).endVertex();
        vc.vertex(matrix, x1, y2, 0.0F).color(rInt, gInt, bInt, aInt).uv(u1, v1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(0, 0, 1).endVertex();
    }

    private float easeOutCubic(float x) {
        float f = 1.0f - x;
        return 1.0f - (f * f * f);
    }

    private static class Tracker {
        private float currentHealth = -1f;
        private float currentAbsorption = -1f;
        private long hurtUntil = 0;

        private float oldHealthEnd = 0f;
        private float oldAbsorptionEnd = 0f;
        private long animStartTime = 0L;
        private boolean isAnimating = false;

        public void update(LivingEntity entity) {
            float health = entity.getHealth();
            float absorption = entity.getAbsorptionAmount();
            float maxHealth = entity.getMaxHealth();

            if (currentHealth == -1f) {
                currentHealth = health;
                currentAbsorption = absorption;
                oldHealthEnd = getHealthEnd(health, absorption, maxHealth);
                oldAbsorptionEnd = getAbsorptionEnd(health, absorption, maxHealth);
                return;
            }

            if (Math.abs(health - currentHealth) > 0.01f || Math.abs(absorption - currentAbsorption) > 0.01f) {
                hurtUntil = System.currentTimeMillis() + 2000;

                oldHealthEnd = getHealthEnd(currentHealth, currentAbsorption, maxHealth);
                oldAbsorptionEnd = getAbsorptionEnd(currentHealth, currentAbsorption, maxHealth);

                animStartTime = System.currentTimeMillis();
                isAnimating = true;

                currentHealth = health;
                currentAbsorption = absorption;
            }
        }

        private float getHealthEnd(float h, float a, float max) {
            boolean isOver = h + a > max;
            return isOver ? Mth.clamp(h / (h + a), 0, 1) : Mth.clamp(h / max, 0, 1);
        }

        private float getAbsorptionEnd(float h, float a, float max) {
            boolean isOver = h + a > max;
            return isOver ? 1.0f : Mth.clamp((h + a) / max, 0, 1);
        }
    }
}