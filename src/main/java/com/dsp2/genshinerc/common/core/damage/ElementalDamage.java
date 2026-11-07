package com.dsp2.genshinerc.common.core.damage;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public final class ElementalDamage {
    private ElementalDamage() {}

    public static DamageSource source(Level level, DamageKind kind, @Nullable Entity direct, @Nullable Entity causing) {
        return new DamageSource(
                level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(kind.typeKey()),
                direct,
                causing);
    }
}
