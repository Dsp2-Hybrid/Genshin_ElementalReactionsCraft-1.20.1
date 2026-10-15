package com.dsp2.genshinerc.client.render.damageindicator.api;

import com.dsp2.genshinerc.client.render.damageindicator.config.DamageIndicatorConfig;
import com.dsp2.genshinerc.client.render.damageindicator.event.DamageInfo;

@FunctionalInterface
public interface DamageSizeProvider {
    float getSize(DamageInfo info);

    DamageSizeProvider DEFAULT = info -> {
        DamageIndicatorConfig cfg = DamageIndicatorConfig.get();
        return info.isCrit() ? cfg.critSize : cfg.normalSize;
    };

    DamageSizeProvider ALWAYS_NORMAL = info -> DamageIndicatorConfig.get().normalSize;
}