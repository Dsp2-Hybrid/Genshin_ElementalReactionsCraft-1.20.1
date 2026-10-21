package com.dsp2.genshinerc.client.render.bar;

import com.dsp2.genshinerc.client.render.bar.config.BarConfig;
import com.dsp2.genshinerc.client.render.bar.api.ElementStatusAPI;
import com.dsp2.genshinerc.common.level.EntityLevelManager;
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

import java.util.Collection;
import java.util.List;

public class StatusBar {

    private static final ResourceLocation FILL_TEXTURE = ResourceLocation.fromNamespaceAndPath("genshinerc", "textures/gui/hp_bar.png");
    private static final ResourceLocation INVENTORY_TEXTURE = ResourceLocation.fromNamespaceAndPath("minecraft", "textures/gui/container/inventory.png");

    public static void renderHUD(LivingEntity entity, Matrix4f matrix, PoseStack poseStack, MultiBufferSource buffers, BarConfig config, float maxX, float minY, float globalAlpha) {
        float rightX = maxX + (config.hudOffsetX / 100.0f);
        float bottomY = minY - (config.hudOffsetY / 100.0f);

        Collection<MobEffectInstance> effects = entity.getActiveEffects();
        int buffCount = Math.min(effects.size(), config.maxBuffs);

        float buffSize = 0.35f;
        float buffSpacing = config.buffSpacing / 100.0f;
        float hudWidth = buffCount * buffSpacing;

        boolean isLeftToRight = config.buffDirection == BarConfig.BuffDirection.LEFT_TO_RIGHT;

        int index = 0;
        for (MobEffectInstance effect : effects) {
            if (index >= config.maxBuffs) break;

            float x = isLeftToRight ?
                    rightX - hudWidth + (index * buffSpacing) :
                    rightX - buffSize - (index * buffSpacing);
            float y = bottomY - buffSize;

            float iconAlpha = globalAlpha * config.buffOpacity;
            float bgAlpha = globalAlpha * config.buffBackgroundAlpha;

            boolean blink = effect.endsWithin(200) && (effect.getDuration() % config.buffBlinkRate < config.buffBlinkRate / 2);
            if (blink) {
                iconAlpha *= 0.5f;
                bgAlpha *= 0.5f;
            }

            if (config.buffRenderBackground) {
                drawTexturedQuad(matrix, buffers, INVENTORY_TEXTURE, x, y, x + buffSize, y + buffSize,
                        141f/256f, 166f/256f, 165f/256f, 190f/256f, 1f, 1f, 1f, bgAlpha);
            }

            TextureAtlasSprite sprite = Minecraft.getInstance().getMobEffectTextures().get(effect.getEffect());
            if (sprite != null) {
                drawTexturedQuad(matrix, buffers, sprite.atlasLocation(), x + 0.04f, y + 0.04f, x + buffSize - 0.04f, y + buffSize - 0.04f,
                        sprite.getU0(), sprite.getV0(), sprite.getU1(), sprite.getV1(), 1f, 1f, 1f, iconAlpha);
            }
            index++;
        }

        float centerX = rightX - (hudWidth / 2.0f);

        if (!(entity instanceof Player)) {
            int level = EntityLevelManager.getLevel(entity);
            String text = "Lv. " + level;
            Minecraft mc = Minecraft.getInstance();

            float scale = config.levelScale;

            Matrix4f textMatrix = new Matrix4f(matrix);

            float textX = centerX + (config.levelOffsetX / 100.0f) - (mc.font.width(text) * scale / 2.0f);
            float textY = bottomY - buffSize - 0.2f + (config.levelOffsetY / 100.0f);

            textMatrix.translate(textX, textY, 0);
            textMatrix.scale(scale, -scale, scale); // 适配 MC 文字向下的坐标系

            int alphaInt = (int)(globalAlpha * 255);
            int shadowColor = (alphaInt << 24) | 0x000000;
            int color = (alphaInt << 24) | 0xFFAA00;

            // 使用矩阵直接绘制
            mc.font.drawInBatch(text, 1, 1, shadowColor, false, textMatrix, buffers, Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
            mc.font.drawInBatch(text, 0, 0, color, false, textMatrix, buffers, Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
        }

        List<ResourceLocation> elements = ElementStatusAPI.getElements(entity);
        if (!elements.isEmpty()) {
            int count = Math.min(elements.size(), 3);
            float boxSize = config.elementBoxSize / 100.0f;
            float spacing = config.elementSpacing / 100.0f;
            float totalWidth = count * boxSize + (count - 1) * spacing;

            float startX = centerX - (totalWidth / 2.0f);
            float startY = bottomY + (config.elementOffsetY / 100.0f);

            for (int i = 0; i < count; i++) {
                float x = startX + i * (boxSize + spacing);

                drawTexturedQuad(matrix, buffers, FILL_TEXTURE, x, startY, x + boxSize, startY + boxSize, 0, 0, 1, 1, 0f, 0f, 0f, globalAlpha * 0.5f);

                ResourceLocation icon = elements.get(i);
                drawTexturedQuad(matrix, buffers, icon, x + 0.01f, startY + 0.01f, x + boxSize - 0.01f, startY + boxSize - 0.01f, 0, 0, 1, 1, 1f, 1f, 1f, globalAlpha);
            }
        }
    }

    private static void drawTexturedQuad(Matrix4f matrix, MultiBufferSource buffers, ResourceLocation tex, float x1, float y1, float x2, float y2, float u1, float v1, float u2, float v2, float r, float g, float b, float a) {
        VertexConsumer vc = buffers.getBuffer(RenderType.entityTranslucent(tex));
        int rInt = (int)(r * 255), gInt = (int)(g * 255), bInt = (int)(b * 255), aInt = (int)(a * 255);
        vc.vertex(matrix, x1, y1, 0.0F).color(rInt, gInt, bInt, aInt).uv(u1, v2).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(0, 0, 1).endVertex();
        vc.vertex(matrix, x2, y1, 0.0F).color(rInt, gInt, bInt, aInt).uv(u2, v2).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(0, 0, 1).endVertex();
        vc.vertex(matrix, x2, y2, 0.0F).color(rInt, gInt, bInt, aInt).uv(u2, v1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(0, 0, 1).endVertex();
        vc.vertex(matrix, x1, y2, 0.0F).color(rInt, gInt, bInt, aInt).uv(u1, v1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(0, 0, 1).endVertex();
    }
}