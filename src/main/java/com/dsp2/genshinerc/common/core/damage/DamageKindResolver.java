package com.dsp2.genshinerc.common.core.damage;

import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;

public final class DamageKindResolver {
    private DamageKindResolver() {}

    public static DamageKind of(DamageSource source) {
        for (DamageKind k : DamageKind.values()) {
            if (source.is(k.typeKey())) return k;
        }
        if (source.is(DamageTypes.DROWN)) return DamageKind.HYDRO; 
        if (source.is(DamageTypeTags.IS_FIRE)) return DamageKind.PYRO;
        if (source.is(DamageTypeTags.IS_FREEZING)) return DamageKind.CRYO;
        if (source.is(DamageTypes.LIGHTNING_BOLT)) return DamageKind.ELECTRO;
        return DamageKind.PHYSICAL;
    }
}