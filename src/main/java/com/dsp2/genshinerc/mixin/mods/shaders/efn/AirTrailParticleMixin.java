package com.dsp2.genshinerc.mixin.mods.shaders.efn;

import com.dsp2.genshinerc.client.render.vfx.weaponshaders.test.WeaponEffectRenderType;
import com.dsp2.genshinerc.client.render.vfx.weaponshaders.test.WeaponShaderMode;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yesman.epicfight.api.animation.Joint;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.client.animation.property.TrailInfo;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

@Mixin(value = com.hm.efn.client.particle.AirTrailParticle.class, remap = true)
public abstract class AirTrailParticleMixin {

    @Unique
    private ResourceLocation genshinerc$trailTexture;

    @Inject(method = "<init>", at = @At("RETURN"), require = 1, remap = false)
    private void genshinerc$onInit(ClientLevel level, LivingEntityPatch<?> owner, Joint joint, AssetAccessor<? extends StaticAnimation> animation, TrailInfo trailInfo, CallbackInfo ci) {
        if (trailInfo != null) {
            this.genshinerc$trailTexture = trailInfo.texturePath();
        }
    }

    @Inject(method = "getRenderType", at = @At("HEAD"), cancellable = true, require = 1)
    private void genshinerc$renderType(CallbackInfoReturnable<ParticleRenderType> cir) {
        WeaponShaderMode mode = WeaponShaderMode.currentMode;
        if (mode != WeaponShaderMode.NONE) {
            cir.setReturnValue(WeaponEffectRenderType.get(this.genshinerc$trailTexture, mode));
        }
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lcom/guhao/vix/client/pipeline/PostEffectPipelines;isActive()Z", remap = false), require = 1)
    private boolean genshinerc$skipEfnPipeline() {
        if (WeaponShaderMode.currentMode != WeaponShaderMode.NONE) {
            return false;
        }
        return com.guhao.vix.client.pipeline.PostEffectPipelines.isActive();
    }
}