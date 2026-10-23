package com.dsp2.genshinerc.client.render.bar;

import com.dsp2.genshinerc.client.render.bar.api.ElementManager;
import com.dsp2.genshinerc.client.render.bar.api.ElementType;
import com.dsp2.genshinerc.client.render.bar.config.BarConfig;
import com.dsp2.genshinerc.common.level.EntityLevelManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.joml.Matrix4f;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public class StatusBar {

    private static final ResourceLocation INVENTORY_TEXTURE = ResourceLocation.fromNamespaceAndPath("minecraft", "textures/gui/container/inventory.png");

    public static void renderHUD(LivingEntity entity, Matrix4f matrix, PoseStack poseStack, MultiBufferSource buffers, BarConfig config, float maxX, float minY, float yOffset, float globalAlpha) {
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
                drawTexturedQuad(matrix, buffers, INVENTORY_TEXTURE, x, y, x + renderSize, y + renderSize,
                        141f/256f, 166f/256f, 165f/256f, 190f/256f, 1f, 1f, 1f, bgAlpha, config.buffOffsetZ + 0.1f);
            }

            TextureAtlasSprite sprite = Minecraft.getInstance().getMobEffectTextures().get(effect.getEffect());
            if (sprite != null) {
                float pad = renderSize * 0.1f;
                drawTexturedQuad(matrix, buffers, sprite.atlasLocation(), x + pad, y + pad, x + renderSize - pad, y + renderSize - pad,
                        sprite.getU0(), sprite.getV0(), sprite.getU1(), sprite.getV1(), 1f, 1f, 1f, iconAlpha, config.buffOffsetZ + 0.1f);
            }
        }

        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.disableCull();

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

            float textX = hudX + (config.levelOffsetX / 100.0f);
            float textY = hudY + (config.levelOffsetY / 100.0f);
            float textZ = config.levelOffsetZ + 0.2f;

            poseStack.pushPose();
            poseStack.translate(textX, textY, textZ);
            poseStack.scale(-scale, -scale, scale);

            if (config.levelShadowEnabled) {
                mc.font.drawInBatch(text, -mc.font.width(text) / 2.0f + config.levelShadowOffsetX, config.levelShadowOffsetY,
                        shadowColorInt, false, poseStack.last().pose(), buffers, Font.DisplayMode.SEE_THROUGH, 0, LightTexture.FULL_BRIGHT);
            }

            mc.font.drawInBatch(text, -mc.font.width(text) / 2.0f, 0,
                    mainColorInt, false, poseStack.last().pose(), buffers, Font.DisplayMode.SEE_THROUGH, 0, LightTexture.FULL_BRIGHT);

            poseStack.popPose();
        }

        List<ElementType> elements = ElementManager.getElements(entity);
        if (!elements.isEmpty()) {
            int count = elements.size();
            float boxSize = (config.elementBoxSize / 100.0f) * config.elementScale;
            float spacing = (config.elementSpacing / 100.0f) * config.elementScale;
            float elementTotalWidth = count * boxSize + (count - 1) * spacing;

            float startX = hudX + (config.elementOffsetX / 100.0f) - (elementTotalWidth / 2.0f);
            float startY = hudY + (config.elementOffsetY / 100.0f);

            for (int i = 0; i < count; i++) {
                float x = startX + i * (boxSize + spacing);
                float iconCenterX = x + boxSize / 2.0f;
                float iconCenterY = startY + boxSize / 2.0f;

                ResourceLocation icon = ElementManager.getCurrentFrame(elements.get(i));

                Matrix4f iconMatrix = new Matrix4f(matrix);
                iconMatrix.translate(iconCenterX, iconCenterY, config.elementOffsetZ + 0.2f);

                float halfSize = boxSize / 2.0f;
                drawTexturedQuad(iconMatrix, buffers, icon,
                        -halfSize, -halfSize, halfSize, halfSize,
                        0, 0, 1, 1, 1f, 1f, 1f, globalAlpha, 0);
            }
        }

        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
    }

    private static void drawTexturedQuad(Matrix4f matrix, MultiBufferSource buffers, ResourceLocation tex, float x1, float y1, float x2, float y2, float u1, float v1, float u2, float v2, float r, float g, float b, float a, float z) {
        VertexConsumer vc = buffers.getBuffer(RenderType.entityTranslucent(tex));
        int rInt = (int)(r * 255), gInt = (int)(g * 255), bInt = (int)(b * 255), aInt = (int)(a * 255);
        vc.vertex(matrix, x1, y1, z).color(rInt, gInt, bInt, aInt).uv(u1, v2).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(0, 0, 1).endVertex();
        vc.vertex(matrix, x2, y1, z).color(rInt, gInt, bInt, aInt).uv(u2, v2).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(0, 0, 1).endVertex();
        vc.vertex(matrix, x2, y2, z).color(rInt, gInt, bInt, aInt).uv(u2, v1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(0, 0, 1).endVertex();
        vc.vertex(matrix, x1, y2, z).color(rInt, gInt, bInt, aInt).uv(u1, v1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(0, 0, 1).endVertex();
    }
}