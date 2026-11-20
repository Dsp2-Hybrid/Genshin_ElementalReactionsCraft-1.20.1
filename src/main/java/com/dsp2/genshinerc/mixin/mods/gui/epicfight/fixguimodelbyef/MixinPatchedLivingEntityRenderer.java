package com.dsp2.genshinerc.mixin.mods.gui.epicfight.fixguimodelbyef;

import com.dsp2.genshinerc.client.render.entity.elereaction.overlay.FrozenSurface;
import com.dsp2.genshinerc.client.render.entity.elereaction.overlay.ShieldSurface;

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
    protected abstract void prepareModel(SkinnedMesh mesh, LivingEntity entity, LivingEntityPatch<?> entitypatch, LivingEntityRenderer<?, ?> renderer);

    @Shadow(remap = false)
    protected abstract void renderLayer(LivingEntityRenderer<?, ?> renderer, LivingEntityPatch<?> entitypatch, LivingEntity entity, OpenMatrix4f[] poses, MultiBufferSource buffer, PoseStack poseStack, int packedLight, float partialTicks);

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void genshinerc$renderInGui(
            LivingEntity entity,
            LivingEntityPatch<?> entitypatch,
            LivingEntityRenderer<?, ?> renderer,
            MultiBufferSource buffer,
            PoseStack poseStack,
            int packedLight,
            float partialTicks,
            CallbackInfo ci) {

        if (!GuiRenderContext.isInGui()) return;

        @SuppressWarnings({"rawtypes", "unchecked"})
        PatchedEntityRenderer base = (PatchedEntityRenderer) (Object) this;

        Armature armature = entitypatch.getArmature();

        poseStack.pushPose();
        base.mulPoseStack(poseStack, armature, entity, entitypatch, partialTicks);
        base.setArmaturePose(entitypatch, armature, partialTicks);

        @SuppressWarnings("unchecked")
        SkinnedMesh mesh = (SkinnedMesh) base.getMeshProvider(entitypatch).get();
        this.prepareModel(mesh, entity, entitypatch, renderer);

        @SuppressWarnings({"rawtypes", "unchecked"})
        ResourceLocation texture = ((LivingEntityRenderer) renderer).getTextureLocation(entity);

        RenderSystem.enableDepthTest();
        RenderSystem.setShader(GameRenderer::getPositionTexColorNormalShader);
        RenderSystem.setShaderTexture(0, texture);

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder builder = tesselator.getBuilder();
        builder.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL);

        mesh.drawPosed(
                poseStack,
                builder,
                DrawingFunction.POSITION_TEX_COLOR_NORMAL,
                -1,
                0.9411F, 0.9411F, 0.9411F, 1.0F,
                OverlayTexture.NO_OVERLAY,
                armature,
                armature.getPoseMatrices());

        BufferUploader.drawWithShader(builder.end());

        if (!entity.isSpectator()) {
            this.renderLayer(renderer, entitypatch, entity,
                    armature.getPoseMatrices(), buffer, poseStack, packedLight, partialTicks);
        }

        poseStack.popPose();
        ci.cancel();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Inject(method = "renderLayer", at = @At("HEAD"), remap = false)
    private void genshinerc$iceSurface(LivingEntityRenderer<?, ?> renderer, LivingEntityPatch<?> patch,
                                      LivingEntity entity, OpenMatrix4f[] poses, MultiBufferSource buffer,
                                      PoseStack pose, int light, float partial, CallbackInfo ci) {
        float alpha = FrozenSurface.alpha(entity, partial);
        ShieldSurface.Overlay shield = ShieldSurface.sample(entity);
        if (alpha <= 0f && shield.alpha() <= 0.001f) return;
        PatchedEntityRenderer base = (PatchedEntityRenderer) (Object) this;
        SkinnedMesh mesh = (SkinnedMesh) base.getMeshProvider(patch).get();
        Armature armature = patch.getArmature();

        if (alpha > 0f) {
            float tint = FrozenSurface.tintWeight(entity, partial);
            float flash = FrozenSurface.whiteFlashStrength(entity, partial);
            float red = 0.91f - 0.06f * tint;
            float green = 0.965f - 0.035f * tint;
            red += (1.0f - red) * flash;
            green += (1.0f - green) * flash;
            mesh.draw(pose, buffer, FrozenSurface.EPIC_FIGHT, light,
                    red, green, 1.0f, alpha, OverlayTexture.NO_OVERLAY, armature, poses);
        }

        if (shield.alpha() > 0.001f) {
            mesh.draw(pose, buffer, FrozenSurface.EPIC_FIGHT, light,
                    shield.red(), shield.green(), shield.blue(), shield.alpha(),
                    OverlayTexture.NO_OVERLAY, armature, poses);
        }
    }
}
