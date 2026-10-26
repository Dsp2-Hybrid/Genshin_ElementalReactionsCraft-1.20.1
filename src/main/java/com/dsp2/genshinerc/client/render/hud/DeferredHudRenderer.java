package com.dsp2.genshinerc.client.render.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexSorting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Mod.EventBusSubscriber(modid = "genshinerc", value = Dist.CLIENT)
public final class DeferredHudRenderer {
    private static final List<Consumer<MultiBufferSource>> QUEUE = new ArrayList<>();

    private DeferredHudRenderer() {}

    public static void submit(Consumer<MultiBufferSource> task) {
        QUEUE.add(task);
    }

    @SubscribeEvent
    public static void onRenderStage(RenderLevelStageEvent event) {
        RenderLevelStageEvent.Stage stage = event.getStage();
        if (stage == RenderLevelStageEvent.Stage.AFTER_SKY) {
            QUEUE.clear();
            return;
        }
        if (stage != RenderLevelStageEvent.Stage.AFTER_LEVEL || QUEUE.isEmpty()) return;

        Matrix4f prevProjection = new Matrix4f(RenderSystem.getProjectionMatrix());
        VertexSorting prevSorting = RenderSystem.getVertexSorting();
        RenderSystem.setProjectionMatrix(event.getProjectionMatrix(), VertexSorting.DISTANCE_TO_ORIGIN);

        PoseStack modelView = RenderSystem.getModelViewStack();
        modelView.pushPose();
        modelView.setIdentity();
        RenderSystem.applyModelViewMatrix();

        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        MultiBufferSource.BufferSource buffers = Minecraft.getInstance().renderBuffers().bufferSource();
        try {
            for (Consumer<MultiBufferSource> task : QUEUE) task.accept(buffers);
            buffers.endBatch();
        } finally {
            QUEUE.clear();
            modelView.popPose();
            RenderSystem.applyModelViewMatrix();
            RenderSystem.setProjectionMatrix(prevProjection, prevSorting);
        }
    }
}