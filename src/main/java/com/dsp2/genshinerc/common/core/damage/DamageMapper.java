package com.dsp2.genshinerc.common.core.damage;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;

public final class DamageMapper {
    private DamageMapper() {}

    public static DamageKind classify(DamageSource source) {
        if (source.is(DamageTypes.IN_FIRE)
                || source.is(DamageTypes.ON_FIRE)
                || source.is(DamageTypes.LAVA)
                || source.is(DamageTypes.HOT_FLOOR)) {
            return DamageKind.PYRO;
        }
        if (source.is(DamageTypes.DROWN)) return DamageKind.HYDRO;
        if (source.is(DamageTypes.LIGHTNING_BOLT)) return DamageKind.ELECTRO;
        if (source.is(DamageTypes.FREEZE)) return DamageKind.CRYO;
        if (source.is(DamageTypes.MAGIC)
                || source.is(DamageTypes.INDIRECT_MAGIC)
                || source.is(DamageTypes.WITHER)
                || source.is(DamageTypes.DRAGON_BREATH)
                || source.is(DamageTypes.SONIC_BOOM)) {
            return DamageKind.DENDRO;
        }
        if (source.is(DamageTypes.FELL_OUT_OF_WORLD) || source.is(DamageTypes.GENERIC_KILL)) {
            return DamageKind.TRUE;
        }
        return DamageKind.PHYSICAL;
    }
}