package com.dsp2.genshinerc.mixin.mods.shaders.efn;

import com.dsp2.genshinerc.client.render.vfx.weaponshaders.test.CustomWeaponRenderType;
import com.dsp2.genshinerc.client.render.vfx.weaponshaders.test.WeaponShaderManager;
import com.dsp2.genshinerc.client.render.vfx.weaponshaders.test.WeaponShaderMode;
import com.hm.efn.client.particle.AirTrailParticle;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yesman.epicfight.api.client.animation.property.TrailInfo;

@Mixin(value = AirTrailParticle.class, remap = false)
public abstract class AirTrailParticleMixin {

    // 拦截 getRenderType
    @Inject(method = "getRenderType", at = @At("HEAD"), cancellable = true)
    public void overrideRenderType(CallbackInfoReturnable<ParticleRenderType> cir) {
        WeaponShaderMode mode = WeaponShaderMode.currentMode;
        if (mode == WeaponShaderMode.NONE) return;

        ShaderInstance customShader = WeaponShaderManager.getShader(mode);
        if (customShader != null) {
            // 用 Accessor 接口获取父类的贴图
            TrailInfo info = ((AnimationTrailParticleAccessor)(Object)this).getTrailInfo();
            if (info != null) {
                ResourceLocation originalTexture = info.texturePath();
                cir.setReturnValue(new CustomWeaponRenderType(originalTexture, customShader));
            }
        }
    }

    // 拦截 render，取消原本的管道
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    public void cancelOriginalPipeline(VertexConsumer pBuffer, Camera pRenderInfo, float pPartialTicks, CallbackInfo ci) {
        if (WeaponShaderMode.currentMode != WeaponShaderMode.NONE) {
            ci.cancel();
        }
    }
}