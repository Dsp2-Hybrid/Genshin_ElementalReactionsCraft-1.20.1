package com.dsp2.genshinerc.client.render.hud.bar;

import com.dsp2.genshinerc.client.render.hud.bar.config.BarConfig;
import com.dsp2.genshinerc.client.render.hud.bar.element.ElementHudRenderer;
import com.dsp2.genshinerc.client.render.hud.HudDraw;
import com.dsp2.genshinerc.common.level.EntityLevelManager;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.registries.ForgeRegistries;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public class StatusBar {

    private static final ResourceLocation INVENTORY_TEXTURE = ResourceLocation.fromNamespaceAndPath("minecraft", "textures/gui/container/inventory.png");

    public static void renderHUD(LivingEntity entity, Matrix4f matrix, PoseStack poseStack, MultiBufferSource buffers, BarConfig config, float maxX, float minY, float globalAlpha) {
        float hudX = config.hudBuffOffsetX / 100.0f;
        float hudY = config.hudBuffOffsetY / 100.0f;

        float rightX = maxX + hudX;
        float bottomY = minY - hudY;

        Collection<MobEffectInstance> activeEffects = entity.getActiveEffects();
        List<MobEffectInstance> sortedEffects = new ArrayList<>(activeEffects);

        sortedEffects.sort(Comparator.comparingInt(MobEffectInstance::getDuration).reversed());
        int buffCount = Math.min(sortedEffects.size(), config.maxBuffs);

        float singleScale = config.buffSingleScale;
        float listScale = config.buffListScale;
        float baseSize = 0.35f * singleScale;
        float baseSpacing = (config.buffSpacing / 100.0f) * singleScale;
        float totalWidth = buffCount * baseSpacing;
        float renderSize = baseSize * listScale;
        float renderSpacing = baseSpacing * listScale;
        float renderWidth = totalWidth * listScale;

        boolean isLeftToRight = config.buffDirection == BarConfig.BuffDirection.LEFT_TO_RIGHT;

        for (int i = 0; i < buffCount; i++) {
            MobEffectInstance effect = sortedEffects.get(i);

            float x;
            if (isLeftToRight) {
                x = rightX - renderWidth + (i * renderSpacing);
            } else {
                x = rightX - renderSize - (buffCount - 1 - i) * renderSpacing;
            }
            float y = bottomY - renderSize;

            float iconAlpha = globalAlpha * config.buffOpacity;
            float bgAlpha = globalAlpha * config.buffBackgroundAlpha;

            int remaining = effect.getDuration();
            if (remaining < 100) {
                float elapsed = 100f - remaining;
                float phase = elapsed * config.buffBlinkRate * 0.05f * (1.0f + elapsed / 100f);
                float blinkFactor = 0.5f + 0.5f * (float)Math.cos(phase);
                iconAlpha *= blinkFactor;
                bgAlpha *= blinkFactor;
            }

            if (config.buffRenderBackground) {
                HudDraw.quad(matrix, buffers, INVENTORY_TEXTURE, x, y, x + renderSize, y + renderSize,
                        141f/256f, 166f/256f, 165f/256f, 190f/256f, config.buffOffsetZ + 0.1f, 0xFFFFFF, bgAlpha);
            }

            TextureAtlasSprite sprite = Minecraft.getInstance().getMobEffectTextures().get(effect.getEffect());
            if (sprite != null) {
                float pad = renderSize * 0.1f;
                HudDraw.quad(matrix, buffers, sprite.atlasLocation(), x + pad, y + pad, x + renderSize - pad, y + renderSize - pad,
                        sprite.getU0(), sprite.getV0(), sprite.getU1(), sprite.getV1(), config.buffOffsetZ + 0.1f, 0xFFFFFF, iconAlpha);
            }
        }

        Minecraft mc = Minecraft.getInstance();

        if (!(entity instanceof Player)) {
            int level = EntityLevelManager.getLevel(entity);
            int playerLevel = mc.player != null ? EntityLevelManager.getPlayerLevel(mc.player) : 0;
            int diff = level - playerLevel;
            String hex = config.levelColorWhite;
            if (diff <= -5) hex = config.levelColorWhite;
            else if (diff <= 3) hex = config.levelColorGreen;
            else if (diff <= 8) hex = config.levelColorYellow;
            else if (diff <= 12) hex = config.levelColorOrange;
            else if (diff <= 17) hex = config.levelColorRed;
            else hex = config.levelColorDarkRed;

            Color mainColor = BarConfig.toAwt(hex);
            String text = "Lv. " + level;
            float scale = config.levelScale;

            int mainAlpha = (int)(globalAlpha * 255);
            int shadowAlpha = (int)(mainAlpha * config.levelShadowAlpha);

            float shadowMultiplier = 1.0f - config.levelShadowBrightness;
            int shadowR = (int)(mainColor.getRed() * shadowMultiplier);
            int shadowG = (int)(mainColor.getGreen() * shadowMultiplier);
            int shadowB = (int)(mainColor.getBlue() * shadowMultiplier);
            int shadowColorInt = (shadowAlpha << 24) | (shadowR << 16) | (shadowG << 8) | shadowB;

            int mainColorInt = (mainAlpha << 24) | (mainColor.getRGB() & 0xFFFFFF);

            float entityHeight = Math.max(entity.getBbHeight(), 1.0f);
            String entityId = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType()).toString();
            float specificOffset = config.getYOffsetForEntity(entityId);
            float yOffset = entityHeight + config.yOffset + (entityHeight * config.yOffsetHeightFactor) + specificOffset + config.entityBarYOffsetBonus;

            poseStack.pushPose();
            poseStack.translate(0.0f, yOffset + minY, 0.0f);

            Quaternionf cameraRotation = mc.gameRenderer.getMainCamera().rotation();
            poseStack.mulPose(cameraRotation);

            poseStack.translate(config.levelOffsetX / 100.0f, config.levelOffsetY / 100.0f, config.levelOffsetZ);
            poseStack.scale(-scale, -scale, scale);

            if (config.levelShadowEnabled) {
                mc.font.drawInBatch(text, -mc.font.width(text) / 2.0f + config.levelShadowOffsetX, config.levelShadowOffsetY,
                        shadowColorInt, false, poseStack.last().pose(), buffers, Font.DisplayMode.SEE_THROUGH, 0, LightTexture.FULL_BRIGHT);
            }

            mc.font.drawInBatch(text, -mc.font.width(text) / 2.0f, 0,
                    mainColorInt, false, poseStack.last().pose(), buffers, Font.DisplayMode.SEE_THROUGH, 0, LightTexture.FULL_BRIGHT);

            poseStack.popPose();
        }

        ElementHudRenderer.render(entity, matrix, buffers, config, hudX, hudY, globalAlpha);
    }
}