package com.dsp2.genshinerc.mixin.core.reaction;

import com.dsp2.genshinerc.common.core.element.FrozenControl;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LivingEntity.class)
public abstract class MixinLivingActionLock {
    @ModifyVariable(method = "travel", at = @At("HEAD"), argsOnly = true)
    private Vec3 genshinerc$lockTravelInput(Vec3 input) {
        return FrozenControl.locked((LivingEntity) (Object) this) ? Vec3.ZERO : input;
    }
}