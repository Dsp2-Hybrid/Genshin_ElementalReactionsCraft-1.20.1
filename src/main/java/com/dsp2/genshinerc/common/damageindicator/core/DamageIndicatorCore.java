package com.dsp2.genshinerc.common.damageindicator.core;

import com.dsp2.genshinerc.client.render.damageindicator.config.DamageIndicatorConfig;
import net.minecraftforge.fml.loading.FMLEnvironment;

public class DamageIndicatorCore {
    public static void init() {
        if (FMLEnvironment.dist.isClient()) {
            DamageIndicatorConfig.load();
        }
    }
}