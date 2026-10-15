package com.dsp2.genshinerc.client.render.damageindicator.api;

import com.dsp2.genshinerc.client.render.damageindicator.event.DamageInfo;

@FunctionalInterface
public interface DamageColorProvider {
    CustomColor getColor(DamageInfo info);
    DamageColorProvider DEFAULT = info -> DamageTypeRegistry.get(info.typeKey).getDefaultColor();
}