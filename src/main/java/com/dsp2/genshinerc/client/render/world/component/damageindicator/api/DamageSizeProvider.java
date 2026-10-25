package com.dsp2.genshinerc.client.render.world.component.damageindicator.api;

import com.dsp2.genshinerc.client.render.world.component.damageindicator.config.DamageIndicatorConfig;
import com.dsp2.genshinerc.client.render.world.component.damageindicator.event.DamageInfo;

@FunctionalInterface
public interface DamageSizeProvider {
    float getSize(DamageInfo info);
    DamageSizeProvider DEFAULT = info -> {
        DamageIndicatorConfig c = DamageIndicatorConfig.get();
        if (info.kill) return (float) c.killSize;
        return info.crit ? (float) c.critSize : (float) c.normalSize;
    };
}