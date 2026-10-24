package com.dsp2.genshinerc.client.render.hud;

import com.dsp2.genshinerc.client.render.element.ElementTextures;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "genshinerc", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class HudClientModEvents {
    private HudClientModEvents() {}

    @SubscribeEvent
    public static void onRegisterReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener((ResourceManagerReloadListener) manager -> {
            WhiteMaskTextures.clear();
            ElementTextures.clear();
        });
    }
}