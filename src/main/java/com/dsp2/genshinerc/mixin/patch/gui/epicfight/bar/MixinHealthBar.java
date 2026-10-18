package com.dsp2.genshinerc.mixin.patch.gui.epicfight.bar;

import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yesman.epicfight.client.gui.HealthBar;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import com.dsp2.genshinerc.client.render.bar.CustomHealthBar;

@Mixin(HealthBar.class)
public class MixinHealthBar {

    static {
        new CustomHealthBar();
    }

    @Inject(method = "shouldDraw", at = @At("HEAD"), cancellable = true, remap = false)
    private void onShouldDraw(LivingEntity entity, LivingEntityPatch<?> entitypatch, LocalPlayerPatch playerpatch, float partialTicks, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }
}