package com.dsp2.genshinerc.client.render.hud.gui.rpg;

import com.dsp2.genshinerc.common.network.sync.PlayerHudSync;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "genshinerc", value = Dist.CLIENT)
public final class PlayerHudClientEvents {
    private PlayerHudClientEvents() {}

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) { PlayerHudSync.clientReset(); }
}