package com.dsp2.genshinerc.client.render.entity.elereaction.overlay;

import com.dsp2.genshinerc.common.core.element.ElementAuraManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@SuppressWarnings({"deprecation", "removal"})
@Mod.EventBusSubscriber(modid = "genshinerc", bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class FrozenOverlay {
    private static final ResourceLocation TEX = new ResourceLocation("minecraft", "textures/block/ice.png");
    private static float alpha;
    private static long last;

    @SubscribeEvent
    public static void onRender(RenderGuiOverlayEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        boolean frozen = ElementAuraManager.hasAura(mc.player, com.dsp2.genshinerc.common.core.element.ElementType.FROZEN);
        alpha = frozen ? Math.min(1f, alpha + 0.06f) : Math.max(0f, alpha - 0.05f);
        if (alpha <= 0f) return;
        long now = System.currentTimeMillis();
        float pulse = 0.7f + 0.3f * (float) Math.sin((now - last) / 500.0);
        last = now;
        GuiGraphics g = event.getGuiGraphics();
        int w = mc.getWindow().getGuiScaledWidth(), h = mc.getWindow().getGuiScaledHeight();
        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1f, 1f, 1f, alpha * pulse * 0.4f);
        g.blit(TEX, 0, 0, 0, 0, w, h, w, h);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.disableBlend();
    }
}