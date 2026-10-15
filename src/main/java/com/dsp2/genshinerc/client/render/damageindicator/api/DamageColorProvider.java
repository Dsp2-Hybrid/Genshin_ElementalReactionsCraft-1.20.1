package com.dsp2.genshinerc.client.render.damageindicator.api;

import com.dsp2.genshinerc.client.render.damageindicator.config.DamageIndicatorConfig;
import com.dsp2.genshinerc.client.render.damageindicator.event.DamageInfo;

@FunctionalInterface
public interface DamageColorProvider {
    CustomColor getColor(DamageInfo info);
    DamageColorProvider DEFAULT = info -> {
        if (info.isHeal()) return CustomColor.fromHexString(DamageIndicatorConfig.get().absorbColor);
        DamageType t = DamageTypeRegistry.get(info.typeKey);
        return CustomColor.fromHexString(DamageIndicatorConfig.get().colorFor(t.getKey()));
    };
}