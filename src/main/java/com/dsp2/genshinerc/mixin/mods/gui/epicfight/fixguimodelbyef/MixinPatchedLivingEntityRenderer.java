package com.dsp2.genshinerc.mixin.mods.gui.epicfight.fixguimodelbyef;

import com.dsp2.genshinerc.client.render.entity.elereaction.overlay.FrozenSurface;
import com.dsp2.genshinerc.patch.core.gui.render.GuiRenderContext;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yesman.epicfight.api.client.model.Mesh.DrawingFunction;
import yesman.epicfight.api.client.model.SkinnedMesh;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.client.renderer.patched.entity.PatchedEntityRenderer;
import yesman.epicfight.client.renderer.patched.entity.PatchedLivingEntityRenderer;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

@Mixin(value = PatchedLivingEntityRenderer.class, remap = false)
public abstract class MixinPatchedLivingEntityRenderer {
    @Shadow(remap = false)
    protected abstract void prepareModel(SkinnedMesh mesh, LivingEntity entity, LivingEntityPatch<?> patch,
                                         LivingEntityRenderer<?, ?> renderer);

    @Shadow(remap = false)
    protected abstract void renderLayer(LivingEntityRenderer<?, ?> renderer, LivingEntityPatch<?> patch,
                                        LivingEntity entity, OpenMatrix4f[] poses, MultiBufferSource buffer,
                                        PoseStack pose, int light, float partial);

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void genshinerc$renderInGui(LivingEntity entity, LivingEntityPatch<?> patch,
                                        LivingEntityRenderer<?, ?> renderer, MultiBufferSource buffer,
                                        PoseStack pose, int light, float partial, CallbackInfo ci) {
        if (!GuiRenderContext.isInGui()) return;

        PatchedEntityRenderer base = (PatchedEntityRenderer) (Object) this;
        Armature armature = patch.getArmature();
        pose.pushPose();
        try {
            base.mulPoseStack(pose, armature, entity, patch, partial);
            base.setArmaturePose(patch, armature, partial);
            SkinnedMesh mesh = (SkinnedMesh) base.getMeshProvider(patch).get();
            prepareModel(mesh, entity, patch, renderer);

            ResourceLocation texture = ((LivingEntityRenderer) renderer).getTextureLocation(entity);
            RenderSystem.enableDepthTest();
            RenderSystem.setShader(GameRenderer::getPositionTexColorNormalShader);
            RenderSystem.setShaderTexture(0, texture);
            BufferBuilder builder = Tesselator.getInstance().getBuilder();
            builder.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL);
            mesh.drawPosed(pose, builder, DrawingFunction.POSITION_TEX_COLOR_NORMAL,
                    -1, 0.9411f, 0.9411f, 0.9411f, 1f, OverlayTexture.NO_OVERLAY,
                    armature, armature.getPoseMatrices());
            BufferUploader.drawWithShader(builder.end());

            if (!entity.isSpectator()) {
                renderLayer(renderer, patch, entity, armature.getPoseMatrices(), buffer, pose, light, partial);
            }
        } finally {
            pose.popPose();
        }
        ci.cancel();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Inject(method = "renderLayer", at = @At("HEAD"), remap = false)
    private void genshinerc$iceSurface(LivingEntityRenderer<?, ?> renderer, LivingEntityPatch<?> patch,
                                       LivingEntity entity, OpenMatrix4f[] poses, MultiBufferSource buffer,
                                       PoseStack pose, int light, float partial, CallbackInfo ci) {
        float coating = FrozenSurface.alpha(entity, partial);
        if (coating <= 0f) return;

        PatchedEntityRenderer base = (PatchedEntityRenderer) (Object) this;
        SkinnedMesh mesh = (SkinnedMesh) base.getMeshProvider(patch).get();
        mesh.drawPosed(pose, buffer.getBuffer(FrozenSurface.layer(entity, true)), DrawingFunction.NEW_ENTITY,
                light, 1f, 1f, 1f, coating, OverlayTexture.NO_OVERLAY, patch.getArmature(), poses);
    }
}
