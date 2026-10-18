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
        if (entity == null) return false;
        if (!entity.isAlive() && entity.deathTime > 20) return false; // 尸体最多存在20ticks(1s)
        if (entity.isInvisibleTo(playerpatch.getOriginal())) return false;

        Minecraft mc = Minecraft.getInstance();
        if (mc.getCameraEntity() != null && entity.distanceToSqr(mc.getCameraEntity()) > config.maxDistance * config.maxDistance) return false;

        Tracker tracker = trackers.computeIfAbsent(entity, k -> new Tracker());
        tracker.update(entity, config);

        return tracker.currentAlpha > 0.01f;
    }

    @Override
    public void draw(LivingEntity entity, LivingEntityPatch<?> entitypatch, LocalPlayerPatch playerpatch, PoseStack poseStack, MultiBufferSource buffers, float partialTicks) {
        BarConfig config = BarConfig.get();
        Tracker tracker = trackers.computeIfAbsent(entity, k -> new Tracker());

        float yOffset = entity.getBbHeight() + config.yOffset + (entity.getBbHeight() * config.yOffsetHeightFactor);
        Matrix4f matrix = super.getModelViewMatrixAlignedToCamera(poseStack, entity, 0.0F, yOffset, 0.0F, true, partialTicks);

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

        float healthEnd, absorptionStart, absorptionEnd;

        if (health + absorption <= maxHealth) {
            // 未溢出上限
            healthEnd = Mth.clamp(health / maxHealth, 0.0F, 1.0F);
            absorptionStart = healthEnd;
            absorptionEnd = Mth.clamp((health + absorption) / maxHealth, 0.0F, 1.0F);
        } else {
            // 溢出上限
            healthEnd = Mth.clamp(health / (health + absorption), 0.0F, 1.0F);
            absorptionStart = healthEnd;
            absorptionEnd = 1.0F;
        }

        float alpha = tracker.currentAlpha;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.depthMask(false);

        //外框背景
        drawTexturedQuad(matrix, buffers, FRAME_TEXTURE, minX, minY, maxX, maxY, 0, 0, 1, 1, 1.0f, 1.0f, 1.0f, alpha);

        // 血条
        if (healthEnd > 0.0f) {
            drawTexturedQuad(matrix, buffers, BAR_TEXTURE, minX, minY, minX + length * healthEnd, maxY, 0, 0, healthEnd, 1, r, g, b, alpha);
        }

        // 黄色护盾条
        if (absorptionEnd > absorptionStart) {
            drawTexturedQuad(matrix, buffers, BAR_TEXTURE, minX + length * absorptionStart, minY, minX + length * absorptionEnd, maxY, absorptionStart, 0, absorptionEnd, 1, 1.0f, 0.9f, 0.2f, alpha);
        }

        // 白色覆盖层
        if (tracker.isAnimatingWhite) {
            long elapsed = System.currentTimeMillis() - tracker.whiteAnimStart;
            float durationMs = config.overlayShrinkTime * 1000f;
            float progress = Math.min(1.0f, elapsed / durationMs);
            float eased = easeOutCubic(progress);

            float whiteAlpha = alpha;
            if (elapsed < 200) {
                whiteAlpha *= (elapsed / 200f);
            } else if (durationMs - elapsed < 200) {
                whiteAlpha *= Math.max(0f, (durationMs - elapsed) / 200f);
            }

            if (whiteAlpha > 0.01f) {
                if (tracker.oldHealthEnd > healthEnd) {
                    float currentEnd = Mth.lerp(eased, tracker.oldHealthEnd, healthEnd);
                    if (currentEnd - healthEnd > 0.001f) {
                        drawTexturedQuad(matrix, buffers, BAR_TEXTURE, minX + length * healthEnd, minY, minX + length * currentEnd, maxY, healthEnd, 0, currentEnd, 1, 1.0f, 1.0f, 1.0f, whiteAlpha);
                    }
                }

                if (tracker.oldAbsorptionEnd > absorptionEnd) {
                    float currentEnd = Mth.lerp(eased, tracker.oldAbsorptionEnd, absorptionEnd);
                    if (currentEnd - absorptionEnd > 0.001f) {
                        drawTexturedQuad(matrix, buffers, BAR_TEXTURE, minX + length * absorptionEnd, minY, minX + length * currentEnd, maxY, absorptionEnd, 0, currentEnd, 1, 1.0f, 1.0f, 1.0f, whiteAlpha);
                    }
                }
            }

            if (progress >= 1.0f) {
                tracker.isAnimatingWhite = false;
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

        private boolean isDead = false;
        private long deathTime = 0;
        private long lastChangeTime = 0;
        private float currentAlpha = 0f;

        private float oldHealthEnd = 0f;
        private float oldAbsorptionEnd = 0f;
        private long whiteAnimStart = 0L;
        private boolean isAnimatingWhite = false;

        public void update(LivingEntity entity, BarConfig config) {
            long now = System.currentTimeMillis();
            float health = entity.getHealth();
            float absorption = entity.getAbsorptionAmount();
            float maxHealth = entity.getMaxHealth();

            if (currentHealth == -1f) {
                currentHealth = health;
                currentAbsorption = absorption;
                oldHealthEnd = getHealthEnd(health, absorption, maxHealth);
                oldAbsorptionEnd = getAbsorptionEnd(health, absorption, maxHealth);
            }

            boolean changed = Math.abs(health - currentHealth) > 0.01f || Math.abs(absorption - currentAbsorption) > 0.01f;

            if (changed) {
                lastChangeTime = now;

                if (health < currentHealth || absorption < currentAbsorption) {
                    oldHealthEnd = getHealthEnd(currentHealth, currentAbsorption, maxHealth);
                    oldAbsorptionEnd = getAbsorptionEnd(currentHealth, currentAbsorption, maxHealth);
                    whiteAnimStart = now;
                    isAnimatingWhite = true;
                } else {
                    isAnimatingWhite = false;
                }

                currentHealth = health;
                currentAbsorption = absorption;
            }

            if (!entity.isAlive() && !isDead) {
                isDead = true;
                deathTime = now;
            }

            // 计算状态机透明度
            BarConfig.PlayerMode pMode = config.playerMode;
            BarConfig.MobMode mMode = config.mobMode;
            boolean isPlayer = entity instanceof Player;

            boolean shouldRenderBase = false;
            if (isPlayer) {
                if (pMode == BarConfig.PlayerMode.ALWAYS) shouldRenderBase = true;
                else if (pMode == BarConfig.PlayerMode.ON_CHANGE && (now - lastChangeTime < config.hurtDisplayTime * 1000f)) shouldRenderBase = true;
            } else {
                if (mMode == BarConfig.MobMode.ALWAYS) shouldRenderBase = true;
                else if (mMode == BarConfig.MobMode.ALWAYS_WHEN_NOT_FULLHP && (health < maxHealth || now - lastChangeTime < config.hurtDisplayTime * 1000f)) shouldRenderBase = true;
                else if (mMode == BarConfig.MobMode.ON_HURT && (now - lastChangeTime < config.hurtDisplayTime * 1000f)) shouldRenderBase = true;
            }

            if (isDead) {
                if (now - deathTime < config.deathFadeDelay * 1000f) {
                    currentAlpha = 1.0f;
                } else {
                    long fadeElapsed = now - deathTime - (long)(config.deathFadeDelay * 1000f);
                    float progress = Math.min(1.0f, fadeElapsed / (config.fadeOutTime * 1000f));
                    currentAlpha = 1.0f - progress;
                }
            } else {
                if (shouldRenderBase) {
                    long timeSinceChange = now - lastChangeTime;
                    if (timeSinceChange < config.fadeInTime * 1000f) {
                        currentAlpha = (float)timeSinceChange / (config.fadeInTime * 1000f);
                    } else if (timeSinceChange > config.hurtDisplayTime * 1000f) {
                        long fadeElapsed = timeSinceChange - (long)(config.hurtDisplayTime * 1000f);
                        currentAlpha = 1.0f - Math.min(1.0f, fadeElapsed / (config.fadeOutTime * 1000f));
                    } else {
                        currentAlpha = 1.0f;
                    }
                } else {
                    currentAlpha = 0f;
                }
            }
        }

        private float getHealthEnd(float h, float a, float max) {
            if (h + a <= max) {
                return Mth.clamp(h / max, 0, 1);
            } else {
                return Mth.clamp(h / (h + a), 0, 1);
            }
        }

        private float getAbsorptionEnd(float h, float a, float max) {
            if (h + a <= max) {
                return Mth.clamp((h + a) / max, 0, 1);
            } else {
                return 1.0f;
            }
        }
    }
}