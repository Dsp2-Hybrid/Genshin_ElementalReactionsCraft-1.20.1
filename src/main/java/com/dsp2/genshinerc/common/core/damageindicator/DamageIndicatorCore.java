package com.dsp2.genshinerc.common.core.damageindicator;

import com.dsp2.genshinerc.client.render.damageindicator.config.DamageIndicatorConfig;
import com.dsp2.genshinerc.client.render.bar.config.BarConfig;
import com.dsp2.genshinerc.common.network.damageindicator.DamageIndicatorNetworkHandler;
import net.minecraftforge.fml.loading.FMLEnvironment;

public class DamageIndicatorCore {
    public static void init() {
        DamageIndicatorNetworkHandler.register();
        if (FMLEnvironment.dist.isClient()) {
            DamageIndicatorConfig.load();
            BarConfig.load();
            DamageIndicatorNetworkHandler.currentRange = DamageIndicatorConfig.get().maxDistance;
        }
    }
}