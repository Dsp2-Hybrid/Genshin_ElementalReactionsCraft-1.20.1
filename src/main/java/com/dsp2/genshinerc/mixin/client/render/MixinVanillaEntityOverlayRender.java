package com.dsp2.genshinerc.mixin.client.render;

import com.dsp2.genshinerc.client.render.entity.elereaction.overlay.FrozenSurface;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LivingEntityRenderer.class)
public abstract class MixinVanillaEntityOverlayRender {
    @Redirect(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/EntityModel;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;IIFFFF)V"))
    private void genshinerc$frozenSurface(EntityModel<?> model, PoseStack pose, VertexConsumer vertices,
                                          int light, int overlay, float red, float green, float blue, float alpha,
                                          LivingEntity entity, float yaw, float partial, PoseStack renderPose,
                                          MultiBufferSource buffers, int packedLight) {
        model.renderToBuffer(pose, vertices, light, overlay, red, green, blue, alpha);
        float coating = FrozenSurface.alpha(entity, partial);
        if (coating > 0f) {
            model.renderToBuffer(pose, buffers.getBuffer(FrozenSurface.layer(entity, false)),
                    light, OverlayTexture.NO_OVERLAY, 1f, 1f, 1f, coating);
        }
    }
}
