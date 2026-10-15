package com.dsp2.genshinerc.client.render.damageindicator.api;

import com.dsp2.genshinerc.client.render.damageindicator.event.DamageInfo;
import com.dsp2.genshinerc.client.render.damageindicator.config.DamageIndicatorConfig;

@FunctionalInterface
public interface DamageColorProvider {
    CustomColor getColor(DamageInfo info);

    DamageColorProvider WHITE = info -> CustomColor.hex(0xFFFFFF);

    DamageColorProvider USE_PRESET = info -> info.preset.toColor();

    DamageColorProvider CONFIG_DEFAULT = info -> {
        DamageIndicatorConfig cfg = DamageIndicatorConfig.get();
        if (info.isHeal()) return CustomColor.fromHexString(cfg.healColor);
        return CustomColor.fromHexString(cfg.defaultColor);
    };
}