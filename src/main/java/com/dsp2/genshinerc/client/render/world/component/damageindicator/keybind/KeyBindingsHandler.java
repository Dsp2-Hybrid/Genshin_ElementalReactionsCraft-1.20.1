package com.dsp2.genshinerc.client.render.world.component.damageindicator.keybind;

import com.dsp2.genshinerc.common.config.ConfigSelectScreen;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "genshinerc", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class KeyBindingsHandler {
    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key e) {
        if (KeyBindings.OPEN_CONFIG.consumeClick()) {
            Minecraft mc = Minecraft.getInstance();
            mc.setScreen(new ConfigSelectScreen(mc.screen));
        }
    }
}