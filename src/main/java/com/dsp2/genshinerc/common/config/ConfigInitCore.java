package com.dsp2.genshinerc.common.config;

import com.dsp2.genshinerc.client.render.bar.config.BarConfig;
import com.dsp2.genshinerc.client.render.damageindicator.config.DamageIndicatorConfig;
import com.dsp2.genshinerc.common.network.base.damageindicator.DamageIndicatorNetworkHandler;
import net.minecraftforge.fml.loading.FMLEnvironment;

public class ConfigInitCore {
    public static void init() {
        DamageIndicatorNetworkHandler.register();
        if (FMLEnvironment.dist.isClient()) {
            load();
        }
    }

    public static void load() {
        DamageIndicatorConfig.load();
        BarConfig.load();
    }
}