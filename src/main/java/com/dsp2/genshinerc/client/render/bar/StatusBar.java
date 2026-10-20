package com.dsp2.genshinerc.client.render.bar;

import com.dsp2.genshinerc.client.render.bar.api.ElementStatusAPI;
import com.dsp2.genshinerc.client.render.bar.config.BarConfig;
import com.dsp2.genshinerc.common.level.EntityLevelManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Collection;
import java.util.List;

public class StatusBar {

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;

        LivingEntity target = null;
        if (mc.hitResult != null && mc.hitResult.getType() == HitResult.Type.ENTITY) {
            target = (LivingEntity) ((EntityHitResult) mc.hitResult).getEntity();
        } else {
            target = mc.player;
        }

        if (target == null) return;

        BarConfig config = BarConfig.get();
        GuiGraphics graphics = event.getGuiGraphics();
        int screenW = mc.getWindow().getGuiScaledWidth();
        int screenH = mc.getWindow().getGuiScaledHeight();

        boolean isLeftToRight = config.buffDirection == BarConfig.BuffDirection.LEFT_TO_RIGHT;

        int baseX = isLeftToRight ? config.hudOffsetX : (screenW - config.hudOffsetX);
        int baseY = screenH - config.hudOffsetY;

        renderBuffs(graphics, target, config, baseX, baseY, isLeftToRight);
        renderLevelAndElements(graphics, target, config, baseX, baseY, isLeftToRight);
    }

    private static void renderBuffs(GuiGraphics graphics, LivingEntity target, BarConfig config, int baseX, int baseY, boolean isLeftToRight) {
        Collection<MobEffectInstance> effects = target.getActiveEffects();
        if (effects.isEmpty()) return;

        int size = 24;
        int count = 0;

        for (MobEffectInstance effect : effects) {
            if (count >= config.maxBuffs) break;

            int x = isLeftToRight
                    ? baseX + (count * config.buffSpacing)
                    : baseX - size - (count * config.buffSpacing);
            int y = baseY - size;

            graphics.blit(InventoryMenu.BLOCK_ATLAS, x, y, 141, 166, size, size);

            var sprite = Minecraft.getInstance().getMobEffectTextures().get(effect.getEffect());
            graphics.blit(x + 3, y + 3, 0, 18, 18, sprite);

            count++;
        }
    }

    private static void renderLevelAndElements(GuiGraphics graphics, LivingEntity target, BarConfig config, int baseX, int baseY, boolean isLeftToRight) {
        Minecraft mc = Minecraft.getInstance();

        int maxWidth = config.maxBuffs * config.buffSpacing;
        int centerX = isLeftToRight ? baseX + (maxWidth / 2) : baseX - (maxWidth / 2);

        int level = EntityLevelManager.getLevel(target);
        if (level > 0) {
            String text = "Lv. " + level;
            int x = centerX + config.levelOffsetX - (mc.font.width(text) / 2);
            int y = baseY + config.levelOffsetY;
            graphics.drawString(mc.font, text, x, y, 0xFFAA00, true);
        }

        List<ResourceLocation> elements = ElementStatusAPI.getElements(target);
        if (elements.isEmpty()) return;

        int count = Math.min(elements.size(), 3);
        int boxSize = config.elementBoxSize;
        int spacing = config.elementSpacing;

        int totalWidth = count * boxSize + (count - 1) * spacing;
        int startX = centerX - (totalWidth / 2);
        int startY = baseY + config.elementOffsetY;

        for (int i = 0; i < count; i++) {
            int x = startX + i * (boxSize + spacing);

            graphics.fill(x, startY, x + boxSize, startY + boxSize, 0x80000000);

            ResourceLocation icon = elements.get(i);
            RenderSystem.enableBlend();
            graphics.blit(icon, x, startY, 0, 0, boxSize, boxSize, boxSize, boxSize);
        }
    }
}