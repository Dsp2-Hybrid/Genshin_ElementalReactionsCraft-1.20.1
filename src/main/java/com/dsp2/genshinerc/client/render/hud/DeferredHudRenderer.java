package com.dsp2.genshinerc.client.render.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexSorting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Mod.EventBusSubscriber(modid = "genshinerc", value = Dist.CLIENT)
public final class DeferredHudRenderer {
    private static final List<Consumer<MultiBufferSource>> QUEUE = new ArrayList<>();
    private static final List<Consumer<MultiBufferSource>> POST_QUEUE = new ArrayList<>();
    private static Matrix4f capturedProjection;

    private DeferredHudRenderer() {}

    public static void submit(Consumer<MultiBufferSource> task) {
        QUEUE.add(task);
    }

    public static void submitPost(Consumer<MultiBufferSource> task) {
        POST_QUEUE.add(task);
    }

    @SubscribeEvent
    public static void onRenderStage(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_SKY) {
            QUEUE.clear();
            POST_QUEUE.clear();
            capturedProjection = null;
            return;
        }
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_LEVEL) {
            capturedProjection = new Matrix4f(event.getProjectionMatrix());
        }
    }

    @SubscribeEvent
    public static void onRenderGuiPre(RenderGuiEvent.Pre event) {
        if (capturedProjection == null) return;
        if (QUEUE.isEmpty() && POST_QUEUE.isEmpty()) return;

        Matrix4f prevProjection = new Matrix4f(RenderSystem.getProjectionMatrix());
        VertexSorting prevSorting = RenderSystem.getVertexSorting();
        boolean prevDepthTest = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
        boolean prevDepthMask = GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK);
        boolean prevBlend = GL11.glIsEnabled(GL11.GL_BLEND);
        boolean prevCull = GL11.glIsEnabled(GL11.GL_CULL_FACE);

        RenderSystem.setProjectionMatrix(capturedProjection, VertexSorting.DISTANCE_TO_ORIGIN);

        PoseStack modelView = RenderSystem.getModelViewStack();
        modelView.pushPose();
        modelView.setIdentity();
        RenderSystem.applyModelViewMatrix();

        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();

        MultiBufferSource.BufferSource buffers = Minecraft.getInstance().renderBuffers().bufferSource();
        try {
            for (Consumer<MultiBufferSource> task : QUEUE) task.accept(buffers);
            buffers.endBatch();

            for (Consumer<MultiBufferSource> task : POST_QUEUE) task.accept(buffers);
            buffers.endBatch();
        } finally {
            QUEUE.clear();
            POST_QUEUE.clear();
            capturedProjection = null;

            if (prevDepthTest) RenderSystem.enableDepthTest();
            else RenderSystem.disableDepthTest();
            RenderSystem.depthMask(prevDepthMask);
            if (prevBlend) RenderSystem.enableBlend();
            else RenderSystem.disableBlend();
            if (prevCull) RenderSystem.enableCull();
            else RenderSystem.disableCull();

            modelView.popPose();
            RenderSystem.applyModelViewMatrix();
            RenderSystem.setProjectionMatrix(prevProjection, prevSorting);
        }
    }
}