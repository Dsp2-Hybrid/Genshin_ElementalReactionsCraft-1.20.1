package com.dsp2.genshinerc.client.render.entity.elereaction.overlay.specice;

import com.dsp2.genshinerc.GenshinERC;
import com.dsp2.genshinerc.common.registry.block.ModBlockEntities;
import com.dsp2.genshinerc.common.world.block.EleIceBlock;
import com.dsp2.genshinerc.common.world.block.entity.SpecialIceBlockEntity;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.opengl.GL11;

import java.util.List;

@SuppressWarnings({"deprecation", "removal"})
public final class SpecialIceBlockRenderer implements BlockEntityRenderer<SpecialIceBlockEntity> {
    private static final RenderType ICE_SURFACE = new IceSurfaceRenderType();

    public SpecialIceBlockRenderer(BlockEntityRendererProvider.Context context) {}

    private static final class IceSurfaceRenderType extends RenderType {
        private IceSurfaceRenderType() {
            super("genshinerc_special_ice_surface", DefaultVertexFormat.BLOCK,
                    VertexFormat.Mode.QUADS, 2097152, true, true,
                    () -> {
                        RenderSystem.enableBlend();
                        RenderSystem.defaultBlendFunc();
                        RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_BLOCKS);
                        RenderSystem.enableDepthTest();
                        RenderSystem.depthFunc(GL11.GL_LEQUAL);
                        RenderSystem.depthMask(false);
                        RenderSystem.enablePolygonOffset();
                        RenderSystem.polygonOffset(-1.0f, -1.0f);
                    },
                    () -> {
                        RenderSystem.disablePolygonOffset();
                        RenderSystem.polygonOffset(0.0f, 0.0f);
                        RenderSystem.depthMask(true);
                        RenderSystem.depthFunc(GL11.GL_LEQUAL);
                        RenderSystem.disableBlend();
                    });
        }
    }

    @Override
    public void render(SpecialIceBlockEntity be, float partialTick, PoseStack stack,
                       MultiBufferSource buffers, int light, int overlay) {
        if (be.getLevel() == null || !(be.getBlockState().getBlock() instanceof EleIceBlock)) return;

        float alpha = be.opacity(partialTick);
        if (alpha <= 0f) return;

        var level = be.getLevel();
        var pos = be.getBlockPos();
        var state = be.getBlockState();
        var model = Minecraft.getInstance().getBlockRenderer().getBlockModel(state);
        VertexConsumer out = buffers.getBuffer(ICE_SURFACE);
        RandomSource random = RandomSource.create(pos.asLong());
        PoseStack.Pose pose = stack.last();

        for (Direction face : Direction.values()) {
            if (!Block.shouldRenderFace(state, level, pos, face, pos.relative(face))) continue;
            random.setSeed(pos.asLong() ^ face.ordinal());
            List<BakedQuad> quads = model.getQuads(state, face, random);
            for (BakedQuad quad : quads) {
                emit(out, pose, quad, face, alpha, light, overlay);
            }
        }

        random.setSeed(pos.asLong());
        for (BakedQuad quad : model.getQuads(state, null, random)) {
            emit(out, pose, quad, quad.getDirection(), alpha, light, overlay);
        }
    }

    private static void emit(VertexConsumer out, PoseStack.Pose pose, BakedQuad quad, Direction face,
                             float alpha, int light, int overlay) {
        int[] data = quad.getVertices();
        int stride = data.length / 4;

        for (int vertex = 0; vertex < 4; vertex++) {
            int index = vertex * stride;
            out.vertex(pose.pose(), Float.intBitsToFloat(data[index]),
                            Float.intBitsToFloat(data[index + 1]),
                            Float.intBitsToFloat(data[index + 2]))
                    .color(1f, 1f, 1f, alpha)
                    .uv(Float.intBitsToFloat(data[index + 4]), Float.intBitsToFloat(data[index + 5]))
                    .overlayCoords(overlay)
                    .uv2(light)
                    .normal(pose.normal(), face.getStepX(), face.getStepY(), face.getStepZ())
                    .endVertex();
        }
    }

    @Mod.EventBusSubscriber(modid = GenshinERC.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static final class Registration {
        private Registration() {}

        @SubscribeEvent
        public static void register(EntityRenderersEvent.RegisterRenderers event) {
            event.registerBlockEntityRenderer(ModBlockEntities.SPECIAL_ICE.get(), SpecialIceBlockRenderer::new);
        }
    }
}
