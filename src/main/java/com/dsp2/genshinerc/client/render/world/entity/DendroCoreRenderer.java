package com.dsp2.genshinerc.client.render.world.entity;

import com.dsp2.genshinerc.GenshinERC;
import com.dsp2.genshinerc.common.core.reaction.DendroCoreEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class DendroCoreRenderer extends EntityRenderer<DendroCoreEntity> {
    private static final ResourceLocation CUSTOM =
            ResourceLocation.fromNamespaceAndPath(GenshinERC.MODID, "textures/entity/dendro_core.png");
    private static final ResourceLocation PLACEHOLDER =
            ResourceLocation.fromNamespaceAndPath("minecraft", "textures/entity/experience_orb.png");
    private static final int FULL_BRIGHT = 15728880;

    private final boolean custom;

    public DendroCoreRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0f;
        this.custom = context.getResourceManager().getResource(CUSTOM).isPresent();
    }

    @Override
    public ResourceLocation getTextureLocation(DendroCoreEntity entity) {
        return custom ? CUSTOM : PLACEHOLDER;
    }

    @Override
    public void render(DendroCoreEntity entity, float yaw, float partialTick, PoseStack stack,
                       MultiBufferSource buffers, int light) {
        float u0 = custom ? 0f : 48f / 64f, u1 = 1f;
        float v0 = 0f, v1 = custom ? 1f : 16f / 64f;
        int r = custom ? 255 : 85, g = custom ? 255 : 166, b = custom ? 255 : 1;

        stack.pushPose();
        stack.translate(0.0, 0.2 + 0.05 * Mth.sin((entity.tickCount + partialTick) * 0.2f), 0.0);
        stack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        stack.mulPose(Axis.YP.rotationDegrees(180f));
        stack.scale(0.5f, 0.5f, 0.5f);
        VertexConsumer vc = buffers.getBuffer(RenderType.entityCutoutNoCull(getTextureLocation(entity)));
        PoseStack.Pose pose = stack.last();
        vertex(vc, pose.pose(), pose.normal(), -0.5f, -0.5f, u0, v1, r, g, b);
        vertex(vc, pose.pose(), pose.normal(), 0.5f, -0.5f, u1, v1, r, g, b);
        vertex(vc, pose.pose(), pose.normal(), 0.5f, 0.5f, u1, v0, r, g, b);
        vertex(vc, pose.pose(), pose.normal(), -0.5f, 0.5f, u0, v0, r, g, b);
        stack.popPose();
        super.render(entity, yaw, partialTick, stack, buffers, light);
    }

    private static void vertex(VertexConsumer vc, Matrix4f pose, Matrix3f normal, float x, float y,
                               float u, float v, int r, int g, int b) {
        vc.vertex(pose, x, y, 0f).color(r, g, b, 255).uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(FULL_BRIGHT).normal(normal, 0f, 1f, 0f).endVertex();
    }
}
