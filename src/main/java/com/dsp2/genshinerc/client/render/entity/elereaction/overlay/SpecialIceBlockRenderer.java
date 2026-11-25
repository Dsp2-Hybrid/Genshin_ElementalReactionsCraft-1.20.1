package com.dsp2.genshinerc.client.render.entity.elereaction.overlay;

import com.dsp2.genshinerc.GenshinERC;
import com.dsp2.genshinerc.common.registry.block.ModBlockEntities;
import com.dsp2.genshinerc.common.world.block.entity.SpecialIceBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@SuppressWarnings({"deprecation", "removal"})
public final class SpecialIceBlockRenderer implements BlockEntityRenderer<SpecialIceBlockEntity> {
    public SpecialIceBlockRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(SpecialIceBlockEntity be, float partialTick, PoseStack stack,
                       MultiBufferSource buffers, int light, int overlay) {
        if (be.getLevel() == null) return;

        var level = be.getLevel();
        var pos = be.getBlockPos();
        BlockState state = be.getBlockState();
        var model = Minecraft.getInstance().getBlockRenderer().getBlockModel(state);
        PoseStack.Pose pose = stack.last();
        RandomSource random = RandomSource.create(be.getTextureSeed());
        float alpha = be.opacity(partialTick);
        int tint = Minecraft.getInstance().getBlockColors().getColor(state, level, pos, 0);
        float depth = Math.min(0.09f, be.getDensityTier() * 0.006f);
        float red = Math.max(0f, ((tint >> 16) & 255) / 255f - depth);
        float green = Math.max(0f, ((tint >> 8) & 255) / 255f - depth);
        float blue = Math.max(0f, (tint & 255) / 255f - depth * 0.45f);
        VertexConsumer consumer = buffers.getBuffer(RenderType.translucent());

        for (Direction direction : Direction.values()) {
            random.setSeed(be.getTextureSeed() ^ direction.ordinal());
            for (BakedQuad quad : model.getQuads(state, direction, random)) {
                putQuad(consumer, pose, quad, random, be.getDensityTier(),
                        red, green, blue, alpha, light, overlay);
            }
        }

        random.setSeed(be.getTextureSeed() ^ 0x5DEECE66DL);
        for (BakedQuad quad : model.getQuads(state, null, random)) {
            putQuad(consumer, pose, quad, random, be.getDensityTier(),
                    red, green, blue, alpha, light, overlay);
        }
    }

    private static void putQuad(VertexConsumer consumer, PoseStack.Pose pose, BakedQuad quad,
                                RandomSource random, int densityTier,
                                float red, float green, float blue, float alpha,
                                int light, int overlay) {
        int[] vertices = quad.getVertices();
        float[][] positions = new float[4][3];
        for (int i = 0; i < 4; i++) {
            int index = i * 8;
            positions[i][0] = Float.intBitsToFloat(vertices[index]);
            positions[i][1] = Float.intBitsToFloat(vertices[index + 1]);
            positions[i][2] = Float.intBitsToFloat(vertices[index + 2]);
        }

        Direction face = quad.getDirection();
        float shade = face == Direction.DOWN ? 0.5f : face == Direction.UP ? 1f : 0.8f;
        float nx = face.getStepX();
        float ny = face.getStepY();
        float nz = face.getStepZ();
        TextureAtlasSprite sprite = quad.getSprite();
        float spriteU0 = sprite.getU0();
        float spriteU1 = sprite.getU1();
        float spriteV0 = sprite.getV0();
        float spriteV1 = sprite.getV1();

        int divisions = Math.min(8, 1 + Math.max(0, densityTier) / 2);
        float patchFraction = 1f / divisions;

        for (int row = 0; row < divisions; row++) {
            for (int column = 0; column < divisions; column++) {
                float uStart = spriteU0 + random.nextFloat() * (1f - patchFraction) * (spriteU1 - spriteU0);
                float vStart = spriteV0 + random.nextFloat() * (1f - patchFraction) * (spriteV1 - spriteV0);
                float uEnd = uStart + patchFraction * (spriteU1 - spriteU0);
                float vEnd = vStart + patchFraction * (spriteV1 - spriteV0);
                float[][] corners = {
                        interpolate(positions, (float) column / divisions, (float) row / divisions),
                        interpolate(positions, (float) (column + 1) / divisions, (float) row / divisions),
                        interpolate(positions, (float) (column + 1) / divisions, (float) (row + 1) / divisions),
                        interpolate(positions, (float) column / divisions, (float) (row + 1) / divisions)
                };
                float[][] uvs = {{uStart, vStart}, {uEnd, vStart}, {uEnd, vEnd}, {uStart, vEnd}};

                for (int i = 0; i < 4; i++) {
                    consumer.vertex(pose.pose(), corners[i][0], corners[i][1], corners[i][2])
                            .color(red * shade, green * shade, blue * shade, alpha)
                            .uv(uvs[i][0], uvs[i][1])
                            .overlayCoords(overlay == 0 ? OverlayTexture.NO_OVERLAY : overlay)
                            .uv2(light)
                            .normal(pose.normal(), nx, ny, nz)
                            .endVertex();
                }
            }
        }
    }

    private static float[] interpolate(float[][] vertices, float u, float v) {
        float[] bottom = new float[3];
        float[] top = new float[3];
        for (int axis = 0; axis < 3; axis++) {
            bottom[axis] = vertices[0][axis] + (vertices[1][axis] - vertices[0][axis]) * u;
            top[axis] = vertices[3][axis] + (vertices[2][axis] - vertices[3][axis]) * u;
        }
        return new float[] {
                bottom[0] + (top[0] - bottom[0]) * v,
                bottom[1] + (top[1] - bottom[1]) * v,
                bottom[2] + (top[2] - bottom[2]) * v
        };
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
