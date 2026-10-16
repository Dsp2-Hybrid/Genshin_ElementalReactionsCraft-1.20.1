package com.dsp2.genshinerc.mixin.patch.gui.epicfight;

import com.dsp2.genshinerc.patch.gui.render.GuiRenderHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void onRender(Entity entity, double x, double y, double z, float yaw, float tickDelta,
                          PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                          CallbackInfo ci) {
        if (isGuiRender()) {
            EntityRenderer<Entity> vanilla = GuiRenderHelper.getVanillaRenderer(entity);
            if (vanilla != null) {
                vanilla.render(entity, yaw, tickDelta, poseStack, buffer, packedLight);
                ci.cancel();
            }
        }
    }

    private static boolean isGuiRender() {
        for (StackTraceElement e : Thread.currentThread().getStackTrace()) {
            if (e.getClassName().startsWith("net.minecraft.client.gui")) {
                return true;
            }
        }
        return false;
    }
}