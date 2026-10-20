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
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        renderBuffs(graphics, target, config, screenWidth, screenHeight);
        renderLevel(graphics, target, config, screenWidth, screenHeight);
        renderElements(graphics, target, config, screenWidth, screenHeight);
    }

    private static void renderBuffs(GuiGraphics graphics, LivingEntity target, BarConfig config, int screenW, int screenH) {
        Collection<MobEffectInstance> effects = target.getActiveEffects();
        if (effects.isEmpty()) return;

        int startX = screenW - config.hudOffsetX;
        int startY = screenH - config.hudOffsetY;

        int count = 0;
        for (MobEffectInstance effect : effects) {
            if (count >= config.maxBuffs) break;

            int x = startX - (count * config.buffSpacing) - 24;
            int y = startY - 24;

            graphics.blit(InventoryMenu.BLOCK_ATLAS, x, y, 141, 166, 24, 24);

            var sprite = Minecraft.getInstance().getMobEffectTextures().get(effect.getEffect());
            graphics.blit(x + 3, y + 3, 0, 18, 18, sprite);

            count++;
        }
    }

    private static void renderLevel(GuiGraphics graphics, LivingEntity target, BarConfig config, int screenW, int screenH) {
        int level = EntityLevelManager.getLevel(target);
        if (level <= 0) return;

        Minecraft mc = Minecraft.getInstance();
        String text = "Lv. " + level;

        int baseX = screenW - config.hudOffsetX - 60;
        int x = baseX + config.levelOffsetX;
        int y = screenH - config.hudOffsetY + config.levelOffsetY;

        graphics.drawString(mc.font, text, x, y, 0xFFAA00, true);
    }

    private static void renderElements(GuiGraphics graphics, LivingEntity target, BarConfig config, int screenW, int screenH) {
        List<ResourceLocation> elements = ElementStatusAPI.getElements(target);
        if (elements.isEmpty()) return;

        int count = Math.min(elements.size(), 3);
        int boxSize = config.elementBoxSize;
        int spacing = config.elementSpacing;

        int totalWidth = count * boxSize + (count - 1) * spacing;

        int centerX = screenW - config.hudOffsetX - 40;
        int startX = centerX - (totalWidth / 2);
        int startY = screenH - config.hudOffsetY + config.elementOffsetY;

        for (int i = 0; i < count; i++) {
            int x = startX + i * (boxSize + spacing);

            graphics.fill(x, startY, x + boxSize, startY + boxSize, 0x80000000);

            ResourceLocation icon = elements.get(i);
            RenderSystem.enableBlend();
            graphics.blit(icon, x + 2, startY + 2, 0, 0, boxSize - 4, boxSize - 4, boxSize - 4, boxSize - 4);
        }
    }
}