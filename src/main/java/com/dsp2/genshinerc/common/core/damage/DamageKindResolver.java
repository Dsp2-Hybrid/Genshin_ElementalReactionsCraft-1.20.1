package com.dsp2.genshinerc.common.core.damage;

import net.minecraft.world.damagesource.DamageSource;

public final class DamageKindResolver {
    private DamageKindResolver() {}

    public static DamageKind of(DamageSource source) {
        return DamageMapper.classify(source);
    }
}