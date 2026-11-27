package com.dsp2.genshinerc.client.render.entity.elereaction.overlay.shield;

import com.dsp2.genshinerc.client.render.hud.bar.ClientCombatBars;
import com.dsp2.genshinerc.client.render.hud.bar.config.BarConfig;
import com.dsp2.genshinerc.client.render.hud.bar.shield.ClientShieldState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

@SuppressWarnings({"deprecation", "removal"})
@Mod.EventBusSubscriber(modid = "genshinerc", value = Dist.CLIENT)
public final class ElementShieldSphereRenderer {
    private static final ResourceLocation WHITE =
            new ResourceLocation("minecraft", "textures/block/white_wool.png");
    private static final int LATITUDES = 16;
    private static final int LONGITUDES = 28;

    private ElementShieldSphereRenderer() {}

    @SubscribeEvent
    public static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) return;

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.isPaused()) return;

        MultiBufferSource.BufferSource buffers =
                minecraft.renderBuffers().bufferSource();
        RenderType renderType = RenderType.entityTranslucent(WHITE, false);
        VertexConsumer consumer = buffers.getBuffer(renderType);
        Vec3 camera = event.getCamera().getPosition();
        long now = net.minecraft.Util.getMillis();
        float partial = event.getPartialTick();

        for (Entity entity : minecraft.level.entitiesForRendering()) {
            if (!(entity instanceof LivingEntity living)
                    || !living.isAlive()
                    || living.isInvisible()
                    || (living == minecraft.getCameraEntity()
                    && minecraft.options.getCameraType().isFirstPerson())) {
                continue;
            }

            ClientShieldState shield = ClientCombatBars.shield(living.getId());
            if (shield == null || shield.amount() <= 0f) continue;

            float colorAlpha = Mth.clamp(
                    shield.amount() / Math.max(1f, shield.initial()),
                    0.18f,
                    1f);
            float alpha = (0.17f + colorAlpha * 0.20f)
                    * shield.warningAlpha(now);
            if (alpha < 0.015f) continue;

            int rgb = shield.color(BarConfig.get(), living.getMaxHealth(), 0);
            float red = ((rgb >> 16) & 255) / 255f;
            float green = ((rgb >> 8) & 255) / 255f;
            float blue = (rgb & 255) / 255f;

            double x = Mth.lerp(partial, living.xo, living.getX()) - camera.x;
            double y = Mth.lerp(partial, living.yo, living.getY()) - camera.y
                    + living.getBbHeight() * 0.5;
            double z = Mth.lerp(partial, living.zo, living.getZ()) - camera.z;

            float radius = Math.max(
                    0.45f,
                    Math.max(living.getBbWidth() * 0.72f, living.getBbHeight() * 0.42f));

            PoseStack pose = event.getPoseStack();
            pose.pushPose();
            pose.translate(x, y, z);
            Matrix4f matrix = pose.last().pose();

            for (int lat = 0; lat < LATITUDES; lat++) {
                double phi0 = -Math.PI / 2.0 + Math.PI * lat / LATITUDES;
                double phi1 = -Math.PI / 2.0 + Math.PI * (lat + 1) / LATITUDES;

                for (int lon = 0; lon < LONGITUDES; lon++) {
                    double theta0 = Math.PI * 2.0 * lon / LONGITUDES;
                    double theta1 = Math.PI * 2.0 * (lon + 1) / LONGITUDES;

                    emit(consumer, pose, matrix, radius, phi0, theta0, red, green, blue, alpha);
                    emit(consumer, pose, matrix, radius, phi0, theta1, red, green, blue, alpha);
                    emit(consumer, pose, matrix, radius, phi1, theta1, red, green, blue, alpha);
                    emit(consumer, pose, matrix, radius, phi1, theta0, red, green, blue, alpha);
                }
            }

            pose.popPose();
        }

        buffers.endBatch(renderType);
    }

    private static void emit(
            VertexConsumer consumer,
            PoseStack pose,
            Matrix4f matrix,
            float radius,
            double phi,
            double theta,
            float red,
            float green,
            float blue,
            float alpha) {
        float nx = (float) (Math.cos(phi) * Math.cos(theta));
        float ny = (float) Math.sin(phi);
        float nz = (float) (Math.cos(phi) * Math.sin(theta));

        consumer.vertex(matrix, nx * radius, ny * radius * 1.35f, nz * radius)
                .color(red, green, blue, alpha)
                .uv((float) (theta / (Math.PI * 2.0)),
                        (float) ((phi + Math.PI / 2.0) / Math.PI))
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(0x00F000F0)
                .normal(pose.last().normal(), nx, ny, nz)
                .endVertex();
    }
}