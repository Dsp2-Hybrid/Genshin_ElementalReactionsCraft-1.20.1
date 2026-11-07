package com.dsp2.genshinerc.mixin.core.damage;

import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntityArmor {
    @Shadow
    protected abstract void hurtArmor(DamageSource source, float amount);

    @Inject(method = "getDamageAfterArmorAbsorb", at = @At("HEAD"), cancellable = true)
    private void genshinerc$skipVanillaArmor(DamageSource source, float amount, CallbackInfoReturnable<Float> cir) {
        if (!source.is(DamageTypeTags.BYPASSES_ARMOR)) {
            this.hurtArmor(source, amount);
        }
        cir.setReturnValue(amount);
    }
}
