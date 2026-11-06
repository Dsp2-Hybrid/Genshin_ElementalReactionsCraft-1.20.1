package com.dsp2.genshinerc.mixin.mods.shaders.guhao;

import com.dsp2.genshinerc.client.render.vfx.aaapshader.ShaderPackAaaPostProcess;
import com.guhao.vix.client.aaaeffect.AAAEffectPostProcessDispatcher;
import com.mojang.logging.LogUtils;
import mod.chloeprime.aaaparticles.api.client.EffectDefinition;
import mod.chloeprime.aaaparticles.api.client.effekseer.EffekseerManager;
import mod.chloeprime.aaaparticles.api.client.effekseer.ParticleEmitter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = AAAEffectPostProcessDispatcher.class, remap = false)
public abstract class MixinAAAEffectPostProcessDispatcher {

    @Inject(method = "begin", at = @At("TAIL"))
    private static void genshinerc$begin(EffectDefinition definition, ParticleEmitter.Type type, float partial, CallbackInfo ci) {
        ShaderPackAaaPostProcess.begin(definition, type, partial);
    }

    @Inject(method = "beginBatch", at = @At("TAIL"))
    private static void genshinerc$beginBatch(ParticleEmitter.Type type, float partial, CallbackInfo ci) {
        ShaderPackAaaPostProcess.begin(null, type, partial);
    }

    @Inject(method = "end", at = @At("HEAD"))
    private static void genshinerc$end(CallbackInfo ci) {
        ShaderPackAaaPostProcess.end();
    }

    @Inject(
            method = "draw(Lmod/chloeprime/aaaparticles/api/client/effekseer/EffekseerManager;Ljava/util/List;Lcom/guhao/vix/client/aaaeffect/AAAEffectPostProcessDispatcher$DrawOperation;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void genshinerc$draw(EffekseerManager manager, List active, @Coerce Object original, CallbackInfo ci) {
        try {
            if (ShaderPackAaaPostProcess.tryDraw(manager, (List<ParticleEmitter>) active)) {
                ci.cancel();
            }
        } catch (Throwable t) {
            LogUtils.getLogger().error("shader-pack AAA post-process crashed; falling back to VIX default", t);
        }
    }
}