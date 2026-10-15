package com.dsp2.genshinerc.client.render.damageindicator.api;

import com.dsp2.genshinerc.client.render.damageindicator.config.DamageIndicatorConfig;
import com.dsp2.genshinerc.client.render.damageindicator.event.DamageInfo;

@FunctionalInterface
public interface DamageSizeProvider {
    float getSize(DamageInfo info);
    DamageSizeProvider DEFAULT = info -> {
        DamageIndicatorConfig c = DamageIndicatorConfig.get();
        if (info.kill) return c.killSize;
        return info.crit ? c.critSize : c.normalSize;
    };
}