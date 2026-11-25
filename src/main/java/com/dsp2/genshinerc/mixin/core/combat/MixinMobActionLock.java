package com.dsp2.genshinerc.mixin.core.combat;

import com.dsp2.genshinerc.common.core.element.FrozenControl;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mob.class)
public abstract class MixinMobActionLock {
    @Inject(method = "serverAiStep", at = @At("HEAD"), cancellable = true)
    private void genshinerc$lockAi(CallbackInfo ci) {
        Mob mob = (Mob) (Object) this;
        if (FrozenControl.locked(mob)) {
            mob.getNavigation().stop();
            ci.cancel();
        }
    }
}