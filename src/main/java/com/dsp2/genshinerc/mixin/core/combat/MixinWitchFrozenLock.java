package com.dsp2.genshinerc.mixin.core.combat;

import com.dsp2.genshinerc.common.core.element.FrozenControl;
import net.minecraft.world.entity.monster.Witch;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Witch.class)
public abstract class MixinWitchFrozenLock {
    @Redirect(method = "aiStep",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/Witch;isAlive()Z"),
            require = 0)
    private boolean genshinerc$frozenNoPotion(Witch witch) {
        return witch.isAlive() && !FrozenControl.locked(witch);
    }
}