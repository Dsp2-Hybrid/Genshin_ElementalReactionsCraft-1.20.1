package com.dsp2.genshinerc.client.render.damageindicator.config;

import dev.isxander.yacl3.gui.YACLScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "genshinerc", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ConfigScreenOpeningHandler {

    private static volatile long escPressedTime = 0L;

    @SubscribeEvent
    public static void onKeyPressed(ScreenEvent.KeyPressed.Pre event) {
        if (!(event.getScreen() instanceof YACLScreen)) return;
        if (event.getKeyCode() != 256) return;
        escPressedTime = System.currentTimeMillis();
    }

    @SubscribeEvent
    public static void onScreenOpening(ScreenEvent.Opening event) {
        if (!(event.getNewScreen() instanceof ConfigSelectScreen)) return;
        if (!(event.getCurrentScreen() instanceof YACLScreen)) return;

        boolean wasEsc = System.currentTimeMillis() - escPressedTime < 500;
        escPressedTime = 0;

        if (!wasEsc && ConfigSelectScreen.lastModListParent != null) {
            event.setNewScreen(ConfigSelectScreen.lastModListParent);
        }
    }
}