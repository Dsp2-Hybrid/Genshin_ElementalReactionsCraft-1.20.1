package com.dsp2.genshinerc.client.render.gui.rpg;

import com.dsp2.genshinerc.client.render.bar.config.BarConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Set;

public final class RpgHudClientEvents {
    private static final Set<ResourceLocation> HIDDEN = Set.of(
            VanillaGuiOverlay.PLAYER_HEALTH.id(),
            VanillaGuiOverlay.FOOD_LEVEL.id(),
            VanillaGuiOverlay.AIR_LEVEL.id(),
            VanillaGuiOverlay.EXPERIENCE_BAR.id());

    private RpgHudClientEvents() {}

    @Mod.EventBusSubscriber(modid = "genshinerc", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class ModBus {
        @SubscribeEvent
        public static void onRegisterOverlays(RegisterGuiOverlaysEvent event) {
            event.registerAbove(VanillaGuiOverlay.HOTBAR.id(), "rpg_hud", RpgHudOverlay.INSTANCE);
        }
    }

    @Mod.EventBusSubscriber(modid = "genshinerc", value = Dist.CLIENT)
    public static final class ForgeBus {
        @SubscribeEvent
        public static void onOverlay(RenderGuiOverlayEvent.Pre event) {
            if (BarConfig.get().rpgHudEnabled && HIDDEN.contains(event.getOverlay().id())) event.setCanceled(true);
        }
    }
}