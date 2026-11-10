package com.dsp2.genshinerc.mixin.client.render.vfx;

import com.dsp2.genshinerc.client.render.vfx.feature.light.AaapLightRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = LevelRenderer.class, priority = 2000)
public abstract class MixinAaapCurLight {
    @Inject(method = "renderLevel", require = 1, at = @At("HEAD"))
    private void genshinerc$captureLight(PoseStack pose, float partialTick, long nanos, boolean outline, Camera camera, GameRenderer gr, LightTexture lt, Matrix4f projection, CallbackInfo ci) {
        AaapLightRenderer.capture(pose, camera, projection);
    }
}