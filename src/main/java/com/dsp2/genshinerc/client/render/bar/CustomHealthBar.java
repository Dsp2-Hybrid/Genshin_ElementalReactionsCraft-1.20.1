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
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.registries.ForgeRegistries;
import org.joml.Matrix4f;
import yesman.epicfight.client.gui.EntityUI;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

import java.awt.Color;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class CustomHealthBar extends EntityUI {

    private static final ResourceLocation BAR_TEXTURE = ResourceLocation.fromNamespaceAndPath("genshinerc", "textures/gui/hp_bar.png");
    private static final ResourceLocation FRAME_TEXTURE = ResourceLocation.fromNamespaceAndPath("genshinerc", "textures/gui/hp_bar2.png");

    private final Map<LivingEntity, Tracker> trackers = new HashMap<>();

    private static long lastShaderCheckTime = 0L;
    private static boolean cachedShaderState = false;

    public CustomHealthBar() {
        super();
    }

    public static boolean isShaderPackEnabled() {
        long now = System.currentTimeMillis();
        if (now - lastShaderCheckTime < 2000L) {
            return cachedShaderState;
        }
        lastShaderCheckTime = now;

        try {
            Class<?> irisApiClass = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
            Method getInstanceMethod = irisApiClass.getMethod("getInstance");
            Object instance = getInstanceMethod.invoke(null);
            Method isShaderPackInUseMethod = irisApiClass.getMethod("isShaderPackInUse");
            cachedShaderState = (Boolean) isShaderPackInUseMethod.invoke(instance);
            return cachedShaderState;
        } catch (Throwable ignored) {}

        try {
            Class<?> optifineConfig = Class.forName("net.optifine.Config");
            Method isShaders = optifineConfig.getMethod("isShaders");
            cachedShaderState = (Boolean) isShaders.invoke(null);
            return cachedShaderState;
        } catch (Throwable ignored) {}

        cachedShaderState = false;
        return false;
    }

    @Override
    public boolean shouldDraw(LivingEntity entity, LivingEntityPatch<?> entitypatch, LocalPlayerPatch playerpatch, float partialTicks) {
        BarConfig config = BarConfig.get();
        if (!config.enabled || entity == null) return false;

        if (entity.getMaxHealth() <= 0.0F) return false;
        if (entity.getBbWidth() <= 0.0F || entity.getBbHeight() <= 0.0F) return false;
        if (entity instanceof ArmorStand) return false;

        if (entitypatch == null && !(entity instanceof Player)) {
            return false;
        }

        MobCategory category = entity.getType().getCategory();
        boolean isValidEntity = entity instanceof Player ||
                category == MobCategory.MONSTER ||
                category == MobCategory.CREATURE ||
                category == MobCategory.WATER_CREATURE ||
                category == MobCategory.AMBIENT ||
                category == MobCategory.MISC;

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

        boolean isPlayer = entity instanceof Player;

        float lengthBonus = isPlayer ? config.playerBarLengthBonus : config.entityBarLengthBonus;
        float widthBonus = isPlayer ? config.playerBarWidthBonus : config.entityBarWidthBonus;
        float yOffsetBonus = isPlayer ? config.playerBarYOffsetBonus : config.entityBarYOffsetBonus;
        float opacityMult = isPlayer ? config.playerBarOpacityMultiplier : config.entityBarOpacityMultiplier;

        float entityHeight = Math.max(entity.getBbHeight(), 1.0f);

        String entityId = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType()).toString();
        float specificOffset = config.getYOffsetForEntity(entityId);

        float yOffset = entityHeight + config.yOffset + (entityHeight * config.yOffsetHeightFactor) + specificOffset + yOffsetBonus;
        Matrix4f matrix = super.getModelViewMatrixAlignedToCamera(poseStack, entity, 0.0F, yOffset, 0.0F, true, partialTicks);

        float shakeX = tracker.getShakeOffset(System.currentTimeMillis(), config);

        float length = config.baseLength + (entityHeight * config.heightLengthFactor) + (entity.getBbWidth() * config.bboxLengthFactor) + lengthBonus;
        float width = config.baseWidth + (entityHeight * config.heightWidthFactor) + (entity.getBbWidth() * config.bboxWidthFactor) + widthBonus;

        float minX = -0.5F * length + shakeX;
        float maxX = 0.5F * length + shakeX;
        float minY = -0.5F * width;
        float maxY = 0.5F * width;

        boolean isAlly = !(entity instanceof Monster);
        Color awtColor = isAlly ? BarConfig.toAwt(config.allyColor) : BarConfig.toAwt(config.enemyColor);
        float r = awtColor.getRed() / 255f;
        float g = awtColor.getGreen() / 255f;
        float b = awtColor.getBlue() / 255f;

        float alpha = tracker.currentAlpha * opacityMult;

        float renderHealthEnd = tracker.visualHealthEnd;
        float renderAbsStart = renderHealthEnd;
        float renderAbsEnd = tracker.visualAbsEnd;

        //BAR MAIN
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        drawTexturedQuad(matrix, buffers, FRAME_TEXTURE, minX, minY, maxX, maxY, 0, 0, 1, 1, 1.0f, 1.0f, 1.0f, alpha);
        float mix = config.hudColorMix;
        float bgR = Mth.lerp(mix, 0.15f, r * 0.3f);
        float bgG = Mth.lerp(mix, 0.15f, g * 0.3f);
        float bgB = Mth.lerp(mix, 0.15f, b * 0.3f);
        drawTexturedQuad(matrix, buffers, BAR_TEXTURE, minX, minY, maxX, maxY, 0, 0, 1, 1, bgR, bgG, bgB, alpha);
        if (renderHealthEnd > 0.001f) {
            drawTexturedQuad(matrix, buffers, BAR_TEXTURE, minX, minY, minX + length * renderHealthEnd, maxY, 0, 0, renderHealthEnd, 1, r, g, b, alpha);
        }
        if (renderAbsEnd > renderAbsStart + 0.001f) {
            drawTexturedQuad(matrix, buffers, BAR_TEXTURE, minX + length * renderAbsStart, minY, minX + length * renderAbsEnd, maxY, renderAbsStart, 0, renderAbsEnd, 1, 1.0f, 0.9f, 0.2f, alpha);
        }
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);


        if (renderHealthEnd > 0.001f) {
            drawTexturedQuad(matrix, buffers, BAR_TEXTURE, minX, minY, minX + length * renderHealthEnd, maxY, 0, 0, renderHealthEnd, 1, r, g, b, alpha);
        }

        if (renderAbsEnd > renderAbsStart + 0.001f) {
            drawTexturedQuad(matrix, buffers, BAR_TEXTURE, minX + length * renderAbsStart, minY, minX + length * renderAbsEnd, maxY, renderAbsStart, 0, renderAbsEnd, 1, 1.0f, 0.9f, 0.2f, alpha);
        }

        if (tracker.hpWhite.oldActive && tracker.hpWhite.oldAlpha > 0.01f) {
            float start = Math.min(tracker.hpWhite.oldStart, tracker.hpWhite.oldEnd);
            float end = Math.max(tracker.hpWhite.oldStart, tracker.hpWhite.oldEnd);
            if (end - start > 0.001f) {
                drawTexturedQuad(matrix, buffers, BAR_TEXTURE, minX + length * start, minY, minX + length * end, maxY, start, 0, end, 1, 1.0f, 1.0f, 1.0f, tracker.hpWhite.oldAlpha * alpha);
            }
        }

        if (tracker.hpWhite.active && tracker.hpWhite.alpha > 0.01f) {
            float start = Math.min(tracker.hpWhite.startOverlay, tracker.hpWhite.endOverlay);
            float end = Math.max(tracker.hpWhite.startOverlay, tracker.hpWhite.endOverlay);
            if (end - start > 0.001f) {
                drawTexturedQuad(matrix, buffers, BAR_TEXTURE, minX + length * start, minY, minX + length * end, maxY, start, 0, end, 1, 1.0f, 1.0f, 1.0f, tracker.hpWhite.alpha * alpha);
            }
        }

        if (tracker.absWhite.oldActive && tracker.absWhite.oldAlpha > 0.01f) {
            float start = Math.min(tracker.absWhite.oldStart, tracker.absWhite.oldEnd);
            float end = Math.max(tracker.absWhite.oldStart, tracker.absWhite.oldEnd);
            if (end - start > 0.001f) {
                drawTexturedQuad(matrix, buffers, BAR_TEXTURE, minX + length * start, minY, minX + length * end, maxY, start, 0, end, 1, 1.0f, 1.0f, 1.0f, tracker.absWhite.oldAlpha * alpha);
            }
        }

        if (tracker.absWhite.active && tracker.absWhite.alpha > 0.01f) {
            float start = Math.min(tracker.absWhite.startOverlay, tracker.absWhite.endOverlay);
            float end = Math.max(tracker.absWhite.startOverlay, tracker.absWhite.endOverlay);
            if (end - start > 0.001f) {
                drawTexturedQuad(matrix, buffers, BAR_TEXTURE, minX + length * start, minY, minX + length * end, maxY, start, 0, end, 1, 1.0f, 1.0f, 1.0f, tracker.absWhite.alpha * alpha);
            }
        }

        if (tracker.isSmallChangeActive) {
            long elapsed = System.currentTimeMillis() - tracker.lastSmallChangeTime;
            float smallAlpha = 0f;
            boolean shouldDraw = false;

            if (isShaderPackEnabled() && config.smallChangeUseShaderMode) {
                float shaderDurationMs = config.smallChangeShaderDuration * 1000f;
                if (elapsed < shaderDurationMs) {
                    smallAlpha = 1.0f;
                    shouldDraw = true;
                }
            } else {
                float totalMs = config.smallChangeDuration * 1000f;
                float fadeInMs = config.smallChangeFadeIn * 1000f;
                float fadeOutMs = config.smallChangeFadeOut * 1000f;

                if (elapsed < totalMs) {
                    shouldDraw = true;
                    if (totalMs < fadeInMs + fadeOutMs) {
                        smallAlpha = 1.0f - (elapsed / totalMs);
                    } else {
                        if (elapsed < fadeInMs) {
                            smallAlpha = fadeInMs > 0 ? (elapsed / fadeInMs) : 1.0f;
                        } else if (elapsed < totalMs - fadeOutMs) {
                            smallAlpha = 1.0f;
                        } else {
                            smallAlpha = fadeOutMs > 0 ? (totalMs - elapsed) / fadeOutMs : 0f;
                        }
                    }
                }
            }

            if (shouldDraw) {
                smallAlpha = Mth.clamp(smallAlpha, 0.0f, 1.0f);
                float maxHealthEnd = Math.max(tracker.smallChangeWidth, 0.001f);
                if (maxHealthEnd > 0.001f) {
                    drawTexturedQuad(matrix, buffers, BAR_TEXTURE, minX, minY, minX + length * maxHealthEnd, maxY, 0, 0, maxHealthEnd, 1, 1.0f, 1.0f, 1.0f, smallAlpha * alpha);
                }
            } else {
                tracker.isSmallChangeActive = false;
            }
        }
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        StatusBar.renderHUD(entity, matrix, poseStack, buffers, config, maxX, minY, alpha);

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

    private static class WhiteState {
        boolean active = false;
        float startOverlay = 0f;
        float endOverlay = 0f;
        float maxWidth = 0f;
        float baseScale = 0f;
        long animStart = 0L;
        boolean isHealing = false;
        boolean extending = false;
        boolean shrinking = false;
        float alpha = 1.0f;

        boolean oldActive = false;
        float oldStart = 0f;
        float oldEnd = 0f;
        float oldAlpha = 1.0f;
        long oldAnimStart = 0L;

        void interrupt(long now) {
            if (active) {
                oldStart = Math.min(startOverlay, endOverlay);
                oldEnd = Math.max(startOverlay, endOverlay);
                oldActive = true;
                oldAlpha = 1.0f;
                oldAnimStart = now;
                active = false;
            }
        }

        void trigger(float currentVisualScale, float exactTargetScale, long now) {
            interrupt(now);
            maxWidth = Math.abs(exactTargetScale - currentVisualScale);
            if (maxWidth > 0.001f) {
                isHealing = exactTargetScale > currentVisualScale;
                baseScale = currentVisualScale;

                startOverlay = baseScale;
                endOverlay = baseScale;

                animStart = now;
                active = true;
                extending = true;
                shrinking = false;
                alpha = 1.0f;
            }
        }

        void update(long now, float currentVisualScale, float baseShrinkTime, float fadeTimeSec, BarConfig.EasingType easing) {
            if (oldActive) {
                long oldElapsed = now - oldAnimStart;
                float oldDur = 150f;
                if (oldElapsed >= oldDur) {
                    oldActive = false;
                } else {
                    float progress = Math.min(1.0f, oldElapsed / oldDur);
                    oldAlpha = 1.0f - progress;

                    oldStart = Mth.lerp(progress, oldStart, currentVisualScale);
                    oldEnd = Mth.lerp(progress, oldEnd, currentVisualScale);
                }
            }

            if (!active) return;
            long elapsed = now - animStart;

            if (extending) {
                float progress = Math.min(1.0f, elapsed / 350f);
                float eased = BarConfig.interpolate(progress, BarConfig.EasingType.EASE_OUT_CUBIC);
                if (isHealing) {
                    endOverlay = baseScale + (maxWidth * eased);
                } else {
                    startOverlay = baseScale - (maxWidth * eased);
                }
                if (progress >= 1.0f) {
                    if (isHealing) {
                        startOverlay = baseScale;
                        endOverlay = baseScale + maxWidth;
                    } else {
                        startOverlay = baseScale - maxWidth;
                        endOverlay = baseScale;
                    }
                    extending = false;
                    shrinking = true;
                    animStart = now;
                }
            } else if (shrinking) {
                float dynamicDuration = Math.max(0.3f, maxWidth * baseShrinkTime) * 1000f;
                long shrinkDurationMs = (long) dynamicDuration;
                long shrinkElapsed = elapsed;

                if (shrinkElapsed >= shrinkDurationMs) {
                    active = false;
                    shrinking = false;
                } else {
                    float progress = Math.min(1.0f, shrinkElapsed / (float) shrinkDurationMs);
                    float eased = BarConfig.interpolate(progress, easing);

                    if (isHealing) {
                        startOverlay = baseScale + (maxWidth * eased);
                    } else {
                        endOverlay = baseScale - (maxWidth * eased);
                    }

                    float fadeStartMs = dynamicDuration - (fadeTimeSec * 1000f);
                    if (shrinkElapsed > fadeStartMs) {
                        alpha = 1.0f - (shrinkElapsed - fadeStartMs) / (fadeTimeSec * 1000f);
                        if (alpha < 0) alpha = 0;
                    } else {
                        alpha = 1.0f;
                    }
                }
            }
        }
    }

    private static class Tracker {
        private float currentHealth = -1f;
        private float currentAbsorption = -1f;
        private float lastMaxHealth = -1f;

        public float visualHealthEnd = 1.0f;
        public float visualAbsEnd = 1.0f;
        private float targetHealthEnd = 1.0f;
        private float targetAbsEnd = 1.0f;
        private float startHealthEnd = 1.0f;
        private float startAbsEnd = 1.0f;

        private long mainBarAnimStartTime = 0L;
        private boolean mainBarAnimating = false;
        private static final long MAIN_BAR_DURATION = 900L;

        public WhiteState hpWhite = new WhiteState();
        public WhiteState absWhite = new WhiteState();

        public boolean isSmallChangeActive = false;
        public long lastSmallChangeTime = 0L;
        public float smallChangeWidth = 1.0f;

        public boolean isShaking = false;
        private long shakeStartTime = 0L;
        private float shakeMagnitude = 0f;

        private boolean isDead = false;
        private long deathTime = 0;
        private long lastHealthChangeTime = 0;

        public enum Phase { IDLE, FADING_IN, HOLDING, FADING_OUT }
        private Phase phase = Phase.IDLE;
        private long phaseStartTime = 0;
        public float currentAlpha = 0f;

        public float getShakeOffset(long now, BarConfig config) {
            if (!isShaking) return 0f;

            long elapsed = now - shakeStartTime;
            float durationMs = config.shakeDuration * 1000f;
            if (elapsed >= durationMs) {
                isShaking = false;
                return 0f;
            }

            float t = elapsed / 1000.0f;
            float startAmplitude = shakeMagnitude * config.shakeAmplitudeFactor;
            float dampingRatio = 0.1f;
            float omega0 = 2 * (float) Math.PI * config.shakeFrequencyFactor;
            float omegaD = omega0 * (float) Math.sqrt(1 - dampingRatio * dampingRatio);

            float amplitude = startAmplitude * (float) Math.exp(-dampingRatio * omega0 * t);
            amplitude *= (float) Math.exp(-config.shakeDecayFactor * t);

            return amplitude * (float) Math.sin(omegaD * t);
        }

        public void update(LivingEntity entity, BarConfig config) {
            long now = System.currentTimeMillis();
            float health = entity.getHealth();
            float absorption = entity.getAbsorptionAmount();
            float maxHealth = entity.getMaxHealth();

            if (entity.isAlive() && isDead) {
                isDead = false;
                phase = Phase.IDLE;
                currentAlpha = 0f;
                isShaking = false;
            }

            if (currentHealth == -1f) {
                currentHealth = health;
                currentAbsorption = absorption;
                lastMaxHealth = maxHealth;

                if (health + absorption <= maxHealth) {
                    visualHealthEnd = Mth.clamp(health / maxHealth, 0.0F, 1.0F);
                    visualAbsEnd = Mth.clamp((health + absorption) / maxHealth, 0.0F, 1.0F);
                } else {
                    visualHealthEnd = Mth.clamp(health / (health + absorption), 0.0F, 1.0F);
                    visualAbsEnd = 1.0F;
                }
                targetHealthEnd = visualHealthEnd;
                targetAbsEnd = visualAbsEnd;
                startHealthEnd = visualHealthEnd;
                startAbsEnd = visualAbsEnd;
            }

            boolean healthChanged = Math.abs(health - currentHealth) > 0.001f;
            boolean absorptionChanged = Math.abs(absorption - currentAbsorption) > 0.001f;
            boolean maxHealthChanged = Math.abs(maxHealth - lastMaxHealth) > 0.001f;

            if (healthChanged || absorptionChanged || maxHealthChanged) {
                lastHealthChangeTime = now;
                boolean isSmallChange = false;

                if (healthChanged || absorptionChanged) {
                    float hChange = Math.abs(health - currentHealth);
                    float aChange = Math.abs(absorption - currentAbsorption);
                    float maxChange = Math.max(hChange, aChange);
                    float changePercent = maxChange / Math.max(1.0f, maxHealth);

                    if (health <= 0 && health < currentHealth) {
                        isShaking = true;
                        shakeStartTime = now;
                        float remainingPercent = currentHealth / maxHealth;
                        float calculatedMagnitude = remainingPercent * config.fatalShakeMultiplier;
                        shakeMagnitude = Mth.clamp(calculatedMagnitude, config.fatalShakeMinPercent, config.fatalShakeMaxPercent);
                    } else if (changePercent < config.smallChangeThreshold && config.smallChangeEnabled) {
                        isSmallChange = true;
                        isSmallChangeActive = true;
                        lastSmallChangeTime = now;
                        smallChangeWidth = Math.max(visualHealthEnd, visualAbsEnd);
                        hpWhite.interrupt(now);
                        absWhite.interrupt(now);
                    } else if (health < currentHealth) {
                        isShaking = true;
                        shakeStartTime = now;
                        shakeMagnitude = Math.min(changePercent, 1.0f);
                    }
                }

                startHealthEnd = visualHealthEnd;
                startAbsEnd = visualAbsEnd;

                if (health + absorption <= maxHealth) {
                    targetHealthEnd = Mth.clamp(health / maxHealth, 0.0F, 1.0F);
                    targetAbsEnd = Mth.clamp((health + absorption) / maxHealth, 0.0F, 1.0F);
                } else {
                    targetHealthEnd = Mth.clamp(health / (health + absorption), 0.0F, 1.0F);
                    targetAbsEnd = 1.0F;
                }

                if (!isSmallChange) {
                    if (Math.abs(targetHealthEnd - visualHealthEnd) > 0.001f) {
                        hpWhite.trigger(visualHealthEnd, targetHealthEnd, now);
                    }

                    if (Math.abs(targetAbsEnd - visualAbsEnd) > 0.001f) {
                        absWhite.trigger(visualAbsEnd, targetAbsEnd, now);
                    }
                }

                mainBarAnimStartTime = now;
                mainBarAnimating = true;

                currentHealth = health;
                currentAbsorption = absorption;
                lastMaxHealth = maxHealth;
            }

            if (mainBarAnimating) {
                long elapsed = now - mainBarAnimStartTime;
                if (elapsed >= MAIN_BAR_DURATION) {
                    visualHealthEnd = targetHealthEnd;
                    visualAbsEnd = targetAbsEnd;
                    mainBarAnimating = false;
                } else {
                    float progress = Math.min(1.0f, elapsed / (float) MAIN_BAR_DURATION);
                    float eased = BarConfig.interpolate(progress, BarConfig.EasingType.EASE_OUT_CUBIC);
                    visualHealthEnd = startHealthEnd + (targetHealthEnd - startHealthEnd) * eased;
                    visualAbsEnd = startAbsEnd + (targetAbsEnd - startAbsEnd) * eased;
                }
            }

            boolean isPlayer = entity instanceof Player;
            float baseShrinkTime = isPlayer ? config.playerOverlayShrinkTime : config.mobOverlayShrinkTime;

            hpWhite.update(now, visualHealthEnd, baseShrinkTime, config.overlayFadeTime, config.whiteEasing);
            absWhite.update(now, visualAbsEnd, baseShrinkTime, config.overlayFadeTime, config.whiteEasing);

            if (!entity.isAlive() && !isDead) {
                isDead = true;
                deathTime = now;
            }

            if (isDead) {
                if (now - deathTime < config.deathFadeDelay * 1000f) {
                    currentAlpha = 1.0f;
                    phase = Phase.IDLE;
                } else {
                    long fadeElapsed = now - deathTime - (long) (config.deathFadeDelay * 1000f);
                    float progress = Math.min(1.0f, fadeElapsed / (config.deathFadeDelay * 1000f));
                    currentAlpha = 1.0f - progress;
                    phase = Phase.IDLE;
                }
                return;
            }

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

            if (healthChanged || absorptionChanged || maxHealthChanged) {
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

            if (phase == Phase.HOLDING && !isAlways) {
                if (hpWhite.active || absWhite.active || mainBarAnimating || hpWhite.oldActive || absWhite.oldActive || isSmallChangeActive) {
                    phaseStartTime = now;
                } else {
                    if (now - lastHealthChangeTime > config.hurtDisplayTime * 1000f) {
                        phase = Phase.FADING_OUT;
                        phaseStartTime = now;
                    }
                }
            }

            long elapsedPhase = now - phaseStartTime;

            switch (phase) {
                case FADING_IN:
                    if (config.fadeInTime <= 0) {
                        currentAlpha = 1.0f;
                        phase = Phase.HOLDING;
                        phaseStartTime = now;
                    } else {
                        currentAlpha = Math.min(1f, (float) elapsedPhase / (config.fadeInTime * 1000f));
                        if (currentAlpha >= 1f) {
                            phase = Phase.HOLDING;
                            phaseStartTime = now;
                        }
                    }
                    break;
                case HOLDING:
                    currentAlpha = 1f;
                    break;
                case FADING_OUT:
                    if (config.fadeOutTime <= 0) {
                        currentAlpha = 0f;
                        phase = Phase.IDLE;
                    } else {
                        currentAlpha = 1f - Math.min(1f, (float) elapsedPhase / (config.fadeOutTime * 1000f));
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
    }
}