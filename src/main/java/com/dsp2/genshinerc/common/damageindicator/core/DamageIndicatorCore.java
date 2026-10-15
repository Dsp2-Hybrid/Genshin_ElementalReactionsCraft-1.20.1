package com.dsp2.genshinerc.common.damageindicator.core;

import com.dsp2.genshinerc.client.render.damageindicator.config.DamageIndicatorConfig;
import com.dsp2.genshinerc.common.network.NetworkHandler;
import net.minecraftforge.fml.loading.FMLEnvironment;

public class DamageIndicatorCore {
    public static void init() {
        NetworkHandler.register();
        if (FMLEnvironment.dist.isClient()) {
            DamageIndicatorConfig.load();
            NetworkHandler.currentRange = DamageIndicatorConfig.get().maxDistance;
        }
    }
}