package com.dsp2.genshinerc.client.render.damageindicator.keybind;

import com.dsp2.genshinerc.client.render.damageindicator.config.DamageIndicatorConfig;
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
            Minecraft.getInstance().setScreen(DamageIndicatorConfig.createScreen());
        }
    }
}