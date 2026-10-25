package com.dsp2.genshinerc.client.render.world.component.damageindicator.config;

import com.dsp2.genshinerc.common.config.ConfigSelectScreen;
import dev.isxander.yacl3.gui.YACLScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "genshinerc", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ConfigScreenOpeningHandler {

    @SubscribeEvent
    public static void onScreenOpening(ScreenEvent.Opening event) {
        if (!(event.getNewScreen() instanceof ConfigSelectScreen)) return;
        if (!(event.getCurrentScreen() instanceof YACLScreen)) return;

        String action = DamageIndicatorConfigYaclActionTracker.consume();

        if ("cancel".equals(action) && ConfigSelectScreen.lastModListParent != null) {
            event.setNewScreen(ConfigSelectScreen.lastModListParent);
        }
    }
}