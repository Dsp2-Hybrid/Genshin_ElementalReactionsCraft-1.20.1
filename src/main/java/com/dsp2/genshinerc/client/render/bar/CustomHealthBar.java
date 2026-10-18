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
import net.minecraft.world.entity.MobCategory;
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
        if (!config.enabled || entity == null) return false;

        boolean isValidEntity = entity instanceof Player ||
                entity.getType().getCategory() == MobCategory.MONSTER ||
                entity.getType().getCategory() == MobCategory.CREATURE ||
                entity.getType().getCategory() == MobCategory.WATER_CREATURE ||
                entity.getType().getCategory() == MobCategory.AMBIENT;

        if (!isValidEntity) return false;
        if (entity.isInvisibleTo(playerpatch.getOriginal())) return false;

        Minecraft mc = Minecraft.getInstance();
        if (!(entity instanceof Player) && mc.getCameraEntity() != null && entity.distanceToSqr(mc.getCameraEntity()) > config.maxDistance * config.maxDistance) return false;

        Tracker tracker = trackers.computeIfAbsent(entity, k -> new Tracker());
        tracker.update(entity, config);

        return tracker.currentAlpha > 0.01f;
    }

    @Override
    public void draw(LivingEntity entity, LivingEntityPatch<?> entitypatch, LocalPlayerPatch playerpatch, PoseStack poseStack, MultiBufferSource buffers, float partialTicks) {
        BarConfig config = BarConfig.get();
        Tracker tracker = trackers.computeIfAbsent(entity, k -> new Tracker());

        float entityHeight = Math.max(entity.getBbHeight(), 1.0f);
        float yOffset = entityHeight + config.yOffset + (entityHeight * config.yOffsetHeightFactor);
        Matrix4f matrix = super.getModelViewMatrixAlignedToCamera(poseStack, entity, 0.0F, yOffset, 0.0F, true, partialTicks);

        float length = config.baseLength + (entityHeight * config.heightLengthFactor) + (entity.getBbWidth() * config.bboxLengthFactor);
        float width = config.baseWidth + (entityHeight * config.heightWidthFactor) + (entity.getBbWidth() * config.bboxWidthFactor);

        float minX = -0.5F * length;
        float maxX = 0.5F * length;
        float minY = -0.5F * width;
        float maxY = 0.5F * width;

        boolean isAlly = !(entity instanceof Monster);
        Color awtColor = isAlly ? BarConfig.toAwt(config.allyColor) : BarConfig.toAwt(config.enemyColor);
        float r = awtColor.getRed() / 255f;
        float g = awtColor.getGreen() / 255f;
        float b = awtColor.getBlue() / 255f;

        float alpha = tracker.currentAlpha;
        float healthEnd = tracker.visualHealthEnd;
        float absorptionStart = healthEnd;
        float absorptionEnd = tracker.visualAbsorptionEnd;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);

        drawTexturedQuad(matrix, buffers, FRAME_TEXTURE, minX, minY, maxX, maxY, 0, 0, 1, 1, 1.0f, 1.0f, 1.0f, alpha);

        if (healthEnd > 0.0f) {
            drawTexturedQuad(matrix, buffers, BAR_TEXTURE, minX, minY, minX + length * healthEnd, maxY, 0, 0, healthEnd, 1, r, g, b, alpha);
        }

        if (absorptionEnd > absorptionStart) {
            drawTexturedQuad(matrix, buffers, BAR_TEXTURE, minX + length * absorptionStart, minY, minX + length * absorptionEnd, maxY, absorptionStart, 0, absorptionEnd, 1, 1.0f, 0.9f, 0.2f, alpha);
        }

        if (tracker.whiteOverlayVisible) {
            float actualStart = Math.min(tracker.whiteOverlayStart, tracker.whiteOverlayEnd);
            float actualEnd = Math.max(tracker.whiteOverlayStart, tracker.whiteOverlayEnd);
            if (actualEnd - actualStart > 0.001f) {
                drawTexturedQuad(matrix, buffers, BAR_TEXTURE, minX + length * actualStart, minY, minX + length * actualEnd, maxY, actualStart, 0, actualEnd, 1, 1.0f, 1.0f, 1.0f, alpha);
            }
        }

        long now = System.currentTimeMillis();
        float oldWhiteAlpha = tracker.getOldWhiteAlpha(now);
        if (oldWhiteAlpha > 0.01f) {
            float actualStart = Math.min(tracker.oldWhiteOverlayStart, tracker.oldWhiteOverlayEnd);
            float actualEnd = Math.max(tracker.oldWhiteOverlayStart, tracker.oldWhiteOverlayEnd);
            if (actualEnd - actualStart > 0.001f) {
                drawTexturedQuad(matrix, buffers, BAR_TEXTURE, minX + length * actualStart, minY, minX + length * actualEnd, maxY, actualStart, 0, actualEnd, 1, 1.0f, 1.0f, 1.0f, oldWhiteAlpha * alpha);
            }
        }

        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
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

    private static float easeOutCubic(float x) {
        float f = 1.0f - x;
        return 1.0f - (f * f * f);
    }

    private static class Tracker {
        private float currentHealth = -1f;
        private float currentAbsorption = -1f;

        private float visualHealthEnd = 0f;
        private float visualAbsorptionEnd = 0f;
        private float startHealthEnd = 0f;
        private float startAbsorptionEnd = 0f;
        private float targetHealthEnd = 0f;
        private float targetAbsorptionEnd = 0f;

        private long mainAnimStartTime = 0L;
        private boolean isAnimatingMain = false;
        private static final long MAIN_BAR_DURATION = 500L;

        private boolean whiteOverlayVisible = false;
        private float whiteOverlayStart = 0f;
        private float whiteOverlayEnd = 0f;
        private float whiteOverlayMaxWidth = 0f;
        private long whiteOverlayAnimStartTime = 0L;
        private boolean isHealing = false;
        private boolean whiteOverlayExtending = false;
        private boolean whiteOverlayShrinking = false;

        private static final long WHITE_EXTEND_DURATION = 350L;
        private static final long WHITE_SHRINK_DURATION = 300L;
        private static final long WHITE_FADE_OUT_DURATION = 200L;

        private boolean oldWhiteOverlayVisible = false;
        private float oldWhiteOverlayStart = 0f;
        private float oldWhiteOverlayEnd = 0f;
        private long oldWhiteOverlayFadeStartTime = 0L;
        private boolean oldWhiteOverlayFading = false;

        private float previousTotalEnd = -1f;

        private boolean isDead = false;
        private long deathTime = 0;

        public enum Phase { IDLE, FADING_IN, HOLDING, FADING_OUT }
        private Phase phase = Phase.IDLE;
        private long phaseStartTime = 0;
        public float currentAlpha = 0f;

        public void update(LivingEntity entity, BarConfig config) {
            long now = System.currentTimeMillis();
            float health = entity.getHealth();
            float absorption = entity.getAbsorptionAmount();
            float maxHealth = entity.getMaxHealth();

            if (entity.isAlive() && isDead) {
                isDead = false;
                phase = Phase.IDLE;
                currentAlpha = 0f;
            }

            float newHealthEnd, newAbsorptionEnd;
            if (health + absorption <= maxHealth) {
                newHealthEnd = Mth.clamp(health / maxHealth, 0.0F, 1.0F);
                newAbsorptionEnd = Mth.clamp((health + absorption) / maxHealth, 0.0F, 1.0F);
            } else {
                newHealthEnd = Mth.clamp(health / (health + absorption), 0.0F, 1.0F);
                newAbsorptionEnd = 1.0F;
            }

            if (currentHealth == -1f) {
                currentHealth = health;
                currentAbsorption = absorption;
                visualHealthEnd = newHealthEnd;
                visualAbsorptionEnd = newAbsorptionEnd;
                startHealthEnd = newHealthEnd;
                startAbsorptionEnd = newAbsorptionEnd;
                targetHealthEnd = newHealthEnd;
                targetAbsorptionEnd = newAbsorptionEnd;
                previousTotalEnd = newAbsorptionEnd;
            }

            boolean changed = Math.abs(health - currentHealth) > 0.01f || Math.abs(absorption - currentAbsorption) > 0.01f;

            if (changed) {
                startHealthEnd = visualHealthEnd;
                startAbsorptionEnd = visualAbsorptionEnd;
                targetHealthEnd = newHealthEnd;
                targetAbsorptionEnd = newAbsorptionEnd;

                mainAnimStartTime = now;
                isAnimatingMain = true;

                float exactTargetScale = newAbsorptionEnd;
                float oldTotalEnd = previousTotalEnd;
                boolean currentIsHealing = exactTargetScale > oldTotalEnd;

                boolean isDirectionChanged = false;
                if (whiteOverlayVisible && (whiteOverlayExtending || whiteOverlayShrinking)) {
                    isDirectionChanged = (currentIsHealing != isHealing);
                }

                if (isDirectionChanged) {
                    if (whiteOverlayVisible) {
                        float actualStart = Math.min(whiteOverlayStart, whiteOverlayEnd);
                        float actualEnd = Math.max(whiteOverlayStart, whiteOverlayEnd);
                        oldWhiteOverlayStart = actualStart;
                        oldWhiteOverlayEnd = actualEnd;
                        oldWhiteOverlayFadeStartTime = now;
                        oldWhiteOverlayFading = true;
                        oldWhiteOverlayVisible = true;

                        whiteOverlayVisible = false;
                        whiteOverlayExtending = false;
                        whiteOverlayShrinking = false;
                    }
                } else {
                    if (oldWhiteOverlayVisible) {
                        oldWhiteOverlayFading = true;
                    }
                }

                isHealing = currentIsHealing;
                whiteOverlayMaxWidth = Math.abs(exactTargetScale - oldTotalEnd);

                if (whiteOverlayMaxWidth > 0.001f) {
                    if (isHealing) {
                        whiteOverlayStart = oldTotalEnd;
                        whiteOverlayEnd = oldTotalEnd;
                    } else {
                        whiteOverlayStart = oldTotalEnd;
                        whiteOverlayEnd = oldTotalEnd;
                    }
                    whiteOverlayAnimStartTime = now;
                    whiteOverlayVisible = true;
                    whiteOverlayExtending = true;
                    whiteOverlayShrinking = false;
                } else {
                    whiteOverlayVisible = false;
                }

                previousTotalEnd = exactTargetScale;
                currentHealth = health;
                currentAbsorption = absorption;
            }

            if (isAnimatingMain) {
                long elapsed = now - mainAnimStartTime;
                if (elapsed >= MAIN_BAR_DURATION) {
                    visualHealthEnd = targetHealthEnd;
                    visualAbsorptionEnd = targetAbsorptionEnd;
                    isAnimatingMain = false;
                } else {
                    float progress = Math.min(1.0f, elapsed / (float) MAIN_BAR_DURATION);
                    float eased = easeOutCubic(progress);
                    visualHealthEnd = startHealthEnd + (targetHealthEnd - startHealthEnd) * eased;
                    visualAbsorptionEnd = startAbsorptionEnd + (targetAbsorptionEnd - startAbsorptionEnd) * eased;
                }
            }

            if (whiteOverlayVisible) {
                long elapsed = now - whiteOverlayAnimStartTime;
                if (whiteOverlayExtending) {
                    float progress = Math.min(1.0f, elapsed / (float) WHITE_EXTEND_DURATION);
                    float eased = easeOutCubic(progress);
                    if (isHealing) {
                        whiteOverlayEnd = previousTotalEnd - whiteOverlayMaxWidth + (whiteOverlayMaxWidth * eased);
                    } else {
                        whiteOverlayStart = previousTotalEnd + whiteOverlayMaxWidth - (whiteOverlayMaxWidth * eased);
                    }
                    if (progress >= 1.0f) {
                        if (isHealing) {
                            whiteOverlayStart = previousTotalEnd - whiteOverlayMaxWidth;
                            whiteOverlayEnd = previousTotalEnd;
                        } else {
                            whiteOverlayStart = previousTotalEnd;
                            whiteOverlayEnd = previousTotalEnd + whiteOverlayMaxWidth;
                        }
                        whiteOverlayExtending = false;
                        whiteOverlayShrinking = true;
                        whiteOverlayAnimStartTime = now;
                    }
                } else if (whiteOverlayShrinking) {
                    float progress = Math.min(1.0f, elapsed / (float) WHITE_SHRINK_DURATION);
                    float eased = easeOutCubic(progress);
                    if (isHealing) {
                        whiteOverlayStart = previousTotalEnd - whiteOverlayMaxWidth + (whiteOverlayMaxWidth * eased);
                    } else {
                        whiteOverlayEnd = previousTotalEnd + whiteOverlayMaxWidth - (whiteOverlayMaxWidth * eased);
                    }
                    if (progress >= 1.0f) {
                        whiteOverlayVisible = false;
                        whiteOverlayShrinking = false;
                    }
                }
            }

            if (!entity.isAlive() && !isDead) {
                isDead = true;
                deathTime = now;
            }

            if (isDead) {
                if (now - deathTime < config.deathFadeDelay * 1000f) {
                    currentAlpha = 1.0f;
                    phase = Phase.IDLE;
                } else {
                    long fadeElapsed = now - deathTime - (long)(config.deathFadeDelay * 1000f);
                    float progress = Math.min(1.0f, fadeElapsed / (config.fadeOutTime * 1000f));
                    currentAlpha = 1.0f - progress;
                    phase = Phase.IDLE;
                }
                return;
            }

            boolean isPlayer = entity instanceof Player;
            BarConfig.PlayerMode pMode = config.playerMode;
            BarConfig.MobMode mMode = config.mobMode;
            boolean isAlways = false;

            if (isPlayer) {
                isAlways = (pMode == BarConfig.PlayerMode.ALWAYS);
            } else {
                if (mMode == BarConfig.MobMode.ALWAYS) {
                    isAlways = true;
                } else if (mMode == BarConfig.MobMode.ALWAYS_WHEN_NOT_FULLHP && health < maxHealth) {
                    isAlways = true;
                }
            }

            if (changed) {
                if (phase == Phase.IDLE) {
                    phase = Phase.FADING_IN;
                    phaseStartTime = now;
                } else {
                    phase = Phase.HOLDING;
                    phaseStartTime = now;
                }
            }

            if (!isPlayer && mMode == BarConfig.MobMode.ALWAYS_WHEN_NOT_FULLHP && health < maxHealth && phase == Phase.IDLE) {
                phase = Phase.FADING_IN;
                phaseStartTime = now;
            }

            if (isAlways && phase == Phase.IDLE) {
                phase = Phase.FADING_IN;
                phaseStartTime = now;
            }

            long elapsedPhase = now - phaseStartTime;

            switch (phase) {
                case FADING_IN:
                    if (config.fadeInTime <= 0) {
                        currentAlpha = 1.0f;
                        phase = Phase.HOLDING;
                        phaseStartTime = now;
                    } else {
                        currentAlpha = Math.min(1f, (float)elapsedPhase / (config.fadeInTime * 1000f));
                        if (currentAlpha >= 1f) {
                            phase = Phase.HOLDING;
                            phaseStartTime = now;
                        }
                    }
                    break;
                case HOLDING:
                    currentAlpha = 1f;
                    if (!isAlways && elapsedPhase > config.hurtDisplayTime * 1000f) {
                        phase = Phase.FADING_OUT;
                        phaseStartTime = now;
                    }
                    break;
                case FADING_OUT:
                    if (config.fadeOutTime <= 0) {
                        currentAlpha = 0f;
                        phase = Phase.IDLE;
                    } else {
                        currentAlpha = 1f - Math.min(1f, (float)elapsedPhase / (config.fadeOutTime * 1000f));
                        if (currentAlpha <= 0f) {
                            phase = Phase.IDLE;
                            currentAlpha = 0f;
                        }
                    }
                    break;
                case IDLE:
                default:
                    currentAlpha = 0f;
                    break;
            }
        }

        public float getOldWhiteAlpha(long now) {
            if (!oldWhiteOverlayVisible || !oldWhiteOverlayFading) return 0f;
            long elapsed = now - oldWhiteOverlayFadeStartTime;
            if (elapsed >= WHITE_FADE_OUT_DURATION) {
                oldWhiteOverlayVisible = false;
                oldWhiteOverlayFading = false;
                return 0f;
            }
            return 1.0f - (float)elapsed / WHITE_FADE_OUT_DURATION;
        }
    }
}