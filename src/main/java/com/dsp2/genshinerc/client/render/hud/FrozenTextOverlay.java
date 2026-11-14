package com.dsp2.genshinerc.client.render.hud;

import com.dsp2.genshinerc.common.core.element.ElementAuraManager;
import com.dsp2.genshinerc.common.core.element.ElementType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "genshinerc", bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class FrozenTextOverlay {
    private FrozenTextOverlay() {}
    @SubscribeEvent
    public static void onRender(RenderGuiOverlayEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || !ElementAuraManager.hasAura(mc.player, ElementType.FROZEN)) return;
        GuiGraphics g = event.getGuiGraphics();
        int w = mc.getWindow().getGuiScaledWidth(), h = mc.getWindow().getGuiScaledHeight();
        Component msg = Component.literal("Frozen");
        int tw = mc.font.width(msg);
        g.drawString(mc.font, msg, (w - tw) / 2, h / 2 + 20, 0x00FFFF, true);
    }
}