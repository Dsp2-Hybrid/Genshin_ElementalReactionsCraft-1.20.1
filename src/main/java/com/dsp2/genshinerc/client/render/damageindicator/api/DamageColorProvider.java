package com.dsp2.genshinerc.client.render.damageindicator.api;

import com.dsp2.genshinerc.client.render.damageindicator.config.DamageIndicatorConfig;
import com.dsp2.genshinerc.client.render.damageindicator.event.DamageInfo;

@FunctionalInterface
public interface DamageColorProvider {
    CustomColor getColor(DamageInfo info);
    DamageColorProvider DEFAULT = info -> {
        DamageIndicatorConfig cfg = DamageIndicatorConfig.get();
        if (info.absorbDelta) return CustomColor.fromHexString(cfg.absorbColor);
        if (info.isHeal()) return CustomColor.hex(0x32C832);
        DamageType t = DamageTypeRegistry.get(info.typeKey);
        return CustomColor.fromHexString(cfg.colorFor(t.getKey()));
    };
}