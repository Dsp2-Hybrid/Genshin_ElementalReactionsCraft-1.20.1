package com.dsp2.genshinerc.client.render.entity.elereaction.specice;

import com.dsp2.genshinerc.GenshinERC;
import com.dsp2.genshinerc.common.registry.block.ModBlockEntities;
import com.dsp2.genshinerc.common.world.block.EleIceBlock;
import com.dsp2.genshinerc.common.world.block.entity.SpecialIceBlockEntity;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.LinkedHashSet;
import java.util.Set;

@Mod.EventBusSubscriber(modid = GenshinERC.MODID, value = Dist.CLIENT)
public final class SpecialIceBlockRenderer implements BlockEntityRenderer<SpecialIceBlockEntity> {
    private static final Set<SpecialIceBlockEntity> VISIBLE = new LinkedHashSet<>();
    private static final MultiBufferSource.BufferSource BUFFER =
            MultiBufferSource.immediate(new BufferBuilder(65536));
    private static final RenderType BASE = RenderType.entityTranslucentCull(TextureAtlas.LOCATION_BLOCKS);
    private static final RenderType SURFACE = new IceSurface();

    public SpecialIceBlockRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(SpecialIceBlockEntity ice, float partialTick, PoseStack stack,
                       MultiBufferSource buffers, int light, int overlay) {
        VISIBLE.add(ice);
    }

    @SubscribeEvent
    public static void draw(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_SKY) {
            VISIBLE.clear();
            return;
        }
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS || VISIBLE.isEmpty()) return;

        var mc = Minecraft.getInstance();
        var level = mc.level;
        var stack = event.getPoseStack();
        var camera = event.getCamera().getPosition();
        try {
            if (level == null) return;
            VertexConsumer vertices = BUFFER.getBuffer(SURFACE);
            RandomSource random = RandomSource.create(42L);
            for (var ice : VISIBLE) {
                if (ice.isRemoved() || ice.getLevel() != level) continue;
                var pos = ice.getBlockPos();
                var state = level.getBlockState(pos);
                if (!(state.getBlock() instanceof EleIceBlock)) continue;

                float alpha = ice.getDuration() > 0 && ice.getExpiresAt() > 0
                        ? (float) (0.20 + 0.65 * Math.min(1.0,
                                ice.remaining(level.getGameTime() + (double) event.getPartialTick())
                                        / ice.getDuration()))
                        : 0.85f;

                var model = mc.getBlockRenderer().getBlockModel(state);
                stack.pushPose();
                try {
                    stack.translate(pos.getX() - camera.x, pos.getY() - camera.y, pos.getZ() - camera.z);
                    for (Direction face : Direction.values()) {
                        var adjacent = pos.relative(face);
                        if (!Block.shouldRenderFace(state, level, pos, face, adjacent)) continue;
                        int light = LevelRenderer.getLightColor(level, adjacent);
                        float shade = level.getShade(face, true);
                        random.setSeed(42L);
                        for (var quad : model.getQuads(state, face, random)) {
                            emit(vertices, stack.last(), quad, alpha, shade, light);
                        }
                    }
                } finally {
                    stack.popPose();
                }
            }
            BUFFER.endBatch(SURFACE);
        } finally {
            VISIBLE.clear();
        }
    }

    private static void emit(VertexConsumer out, PoseStack.Pose pose, BakedQuad quad,
                             float alpha, float shade, int light) {
        int[] data = quad.getVertices();
        int stride = data.length / 4;
        Direction face = quad.getDirection();
        for (int vertex = 0; vertex < 4; vertex++) {
            int i = vertex * stride;
            out.vertex(pose.pose(), Float.intBitsToFloat(data[i]),
                            Float.intBitsToFloat(data[i + 1]), Float.intBitsToFloat(data[i + 2]))
                    .color(shade, shade, shade, alpha)
                    .uv(Float.intBitsToFloat(data[i + 4]), Float.intBitsToFloat(data[i + 5]))
                    .overlayCoords(OverlayTexture.NO_OVERLAY)
                    .uv2(light)
                    .normal(pose.normal(), face.getStepX(), face.getStepY(), face.getStepZ())
                    .endVertex();
        }
    }

    private static final class IceSurface extends RenderType {
        IceSurface() {
            super("genshinerc_special_ice_surface", BASE.format(), BASE.mode(), 65536, false, true,
                    () -> {
                        BASE.setupRenderState();
                        TRANSLUCENT_TARGET.setupRenderState();
                        RenderSystem.depthMask(false);
                    },
                    () -> {
                        RenderSystem.depthMask(true);
                        TRANSLUCENT_TARGET.clearRenderState();
                        BASE.clearRenderState();
                    });
        }
    }

    @Mod.EventBusSubscriber(modid = GenshinERC.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static final class Registration {
        @SubscribeEvent
        public static void register(EntityRenderersEvent.RegisterRenderers event) {
            event.registerBlockEntityRenderer(ModBlockEntities.SPECIAL_ICE.get(), SpecialIceBlockRenderer::new);
        }
    }
}
