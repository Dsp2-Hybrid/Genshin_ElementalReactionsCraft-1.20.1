package com.dsp2.genshinerc.client.render.bar;

import com.dsp2.genshinerc.client.render.bar.api.ElementManager;
import com.dsp2.genshinerc.client.render.bar.api.ElementType;
import com.dsp2.genshinerc.client.render.bar.config.BarConfig;
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
import net.minecraft.world.inventory.InventoryMenu;
import org.joml.Matrix4f;

import java.awt.Color;
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

            float x = isLeftToRight ? rightX - hudWidth + (index * buffSpacing) : rightX - buffSize - (index * buffSpacing);
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
            int playerLevel = Minecraft.getInstance().player != null ? EntityLevelManager.getPlayerLevel(Minecraft.getInstance().player) : 0;

            int diff = level - playerLevel;
            String hex = config.levelColorWhite;
            if (diff <= -5) hex = config.levelColorWhite;
            else if (diff <= 3) hex = config.levelColorGreen;
            else if (diff <= 8) hex = config.levelColorYellow;
            else if (diff <= 12) hex = config.levelColorOrange;
            else if (diff <= 17) hex = config.levelColorRed;
            else hex = config.levelColorDarkRed;

            Color mainColor = BarConfig.toAwt(hex);
            Color shadowColor = BarConfig.toAwt(config.levelShadowColor);

            String text = "Lv. " + level;
            Minecraft mc = Minecraft.getInstance();

            float scale = config.levelScale;
            Matrix4f textMatrix = new Matrix4f(matrix);

            float baseX = centerX + (config.levelOffsetX / 100.0f);
            float baseY = bottomY - buffSize - 0.15f + (config.levelOffsetY / 100.0f);
            float baseZ = config.levelOffsetZ;

            float textX = baseX - (mc.font.width(text) * scale / 2.0f);
            float textY = baseY + (mc.font.lineHeight * scale / 2.0f);

            int mainAlpha = (int)(globalAlpha * 255);
            int shadowAlpha = (int)(mainAlpha * config.levelShadowAlpha);
            int mainColorInt = (mainAlpha << 24) | mainColor.getRGB() & 0xFFFFFF;
            int shadowColorInt = (shadowAlpha << 24) | shadowColor.getRGB() & 0xFFFFFF;

            PoseStack shadowPose = new PoseStack();
            shadowPose.last().pose().set(textMatrix);
            shadowPose.translate(textX + config.levelShadowOffsetX, textY + config.levelShadowOffsetY, baseZ + config.levelShadowOffsetZ);
            shadowPose.scale(-scale, -scale, scale);
            mc.font.drawInBatch(text, 0, 0, shadowColorInt, false, shadowPose.last().pose(), buffers, Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);

            PoseStack mainPose = new PoseStack();
            mainPose.last().pose().set(textMatrix);
            mainPose.translate(textX, textY, baseZ);
            mainPose.scale(-scale, -scale, scale);
            mc.font.drawInBatch(text, 0, 0, mainColorInt, false, mainPose.last().pose(), buffers, Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
        }

        List<ElementType> elements = ElementManager.getElements(entity);
        if (!elements.isEmpty()) {
            int count = elements.size();
            float boxSize = (config.elementBoxSize / 100.0f) * config.elementScale;
            float spacing = (config.elementSpacing / 100.0f) * config.elementScale;
            float totalWidth = count * boxSize + (count - 1) * spacing;

            float startX = centerX + (config.elementOffsetX / 100.0f) - (totalWidth / 2.0f);
            float startY = bottomY + (config.elementOffsetY / 100.0f);

            for (int i = 0; i < count; i++) {
                float x = startX + i * (boxSize + spacing);
                ResourceLocation icon = ElementManager.getCurrentFrame(elements.get(i));
                drawTexturedQuad(matrix, buffers, icon, x, startY, x + boxSize, startY + boxSize, 0, 0, 1, 1, 1f, 1f, 1f, globalAlpha);
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