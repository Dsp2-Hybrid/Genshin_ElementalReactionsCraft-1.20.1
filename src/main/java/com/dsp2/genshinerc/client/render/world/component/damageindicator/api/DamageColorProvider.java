package com.dsp2.genshinerc.client.render.world.component.damageindicator.api;

import com.dsp2.genshinerc.client.render.world.component.damageindicator.config.DamageIndicatorConfig;
import com.dsp2.genshinerc.client.render.world.component.damageindicator.event.DamageInfo;

@FunctionalInterface
public interface DamageColorProvider {
    CustomColor getColor(DamageInfo info);
    DamageColorProvider DEFAULT = info -> {
        DamageIndicatorConfig cfg = DamageIndicatorConfig.get();
        if (info.absorbDelta) return CustomColor.fromHexString(cfg.absorbColor);
        if (info.isHeal()) return CustomColor.hex(0x00FF00);
        if ("true".equalsIgnoreCase(info.typeKey) || "wither".equalsIgnoreCase(info.typeKey)) {
            return CustomColor.hex(0x101010);
        }
        DamageType t = DamageTypeRegistry.get(info.typeKey);
        return CustomColor.fromHexString(cfg.colorFor(t.getKey()));
    };
}