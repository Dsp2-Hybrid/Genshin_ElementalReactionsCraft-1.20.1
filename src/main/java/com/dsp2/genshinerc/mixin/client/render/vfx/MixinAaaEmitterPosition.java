package com.dsp2.genshinerc.mixin.client.render.vfx;

import com.dsp2.genshinerc.client.render.vfx.manager.AaaEntityOcclusion;
import mod.chloeprime.aaaparticles.api.client.effekseer.ParticleEmitter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ParticleEmitter.class, remap = false)
public abstract class MixinAaaEmitterPosition {
    @Inject(method = "setPosition(FFF)V", at = @At("TAIL"))
    private void genshinerc$trackPosition(float x, float y, float z, CallbackInfo ci) {
        AaaEntityOcclusion.position((ParticleEmitter) (Object) this, x, y, z);
    }
}
