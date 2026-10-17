package com.dsp2.genshinerc.mixin.patch.gui.epicfight.bar;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yesman.epicfight.client.gui.HealthBar;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import com.dsp2.genshinerc.client.render.bar.CustomBarRenderer;
import com.dsp2.genshinerc.client.render.bar.config.BarConfig;

@Mixin(value = HealthBar.class, remap = false)
public class MixinHealthBar {

    @Inject(method = "shouldDraw", at = @At("HEAD"), cancellable = true)
    private void genshinerc$shouldDraw(LivingEntity entity, LivingEntityPatch<?> entitypatch, LocalPlayerPatch playerpatch, float partialTicks, CallbackInfoReturnable<Boolean> cir) {
        if (!BarConfig.get().enabled) {
            cir.setReturnValue(false);
            return;
        }
        cir.setReturnValue(entity.isAlive() && !entity.isInvisible());
    }

    @Inject(method = "draw", at = @At("HEAD"), cancellable = true)
    private void genshinerc$draw(LivingEntity entity, LivingEntityPatch<?> entitypatch, LocalPlayerPatch playerpatch, PoseStack poseStack, MultiBufferSource buffers, float partialTicks, CallbackInfo ci) {
        BarConfig config = BarConfig.get();
        if (config.enabled) {
            CustomBarRenderer.render(entity, entitypatch, playerpatch, poseStack, buffers, partialTicks);
            ci.cancel();
        }
    }
}