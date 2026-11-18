package com.dsp2.genshinerc.mixin.core.damage;

import net.minecraft.core.Holder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(DamageSource.class)
public interface DamageSourceTypeAccessor {
    @Mutable
    @Accessor("type")
    void genshinerc$setType(Holder<DamageType> type);
}
