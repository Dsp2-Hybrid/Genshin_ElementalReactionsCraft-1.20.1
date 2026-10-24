package com.dsp2.genshinerc.client.render.hud;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public final class HudRenderTypes {
    private static final Map<ResourceLocation, RenderType> OVERLAY = new HashMap<>();
    private static ResourceLocation whiteTexture;

    private HudRenderTypes() {}

    public static RenderType overlay(ResourceLocation texture) {
        return OVERLAY.computeIfAbsent(texture, Shards::create);
    }

    public static ResourceLocation whiteTexture() {
        if (whiteTexture == null) {
            NativeImage img = new NativeImage(1, 1, false);
            img.setPixelRGBA(0, 0, 0xFFFFFFFF);
            whiteTexture = Minecraft.getInstance().getTextureManager()
                    .register("genshinerc_hud_white", new DynamicTexture(img));
        }
        return whiteTexture;
    }

    public static void forget(ResourceLocation texture) {
        OVERLAY.remove(texture);
    }

    private static final class Shards extends RenderStateShard {
        private static final ShaderStateShard SHADER = new ShaderStateShard(GameRenderer::getPositionTexColorShader);

        private Shards() { super("genshinerc_hud", () -> {}, () -> {}); }

        static RenderType create(ResourceLocation tex) {
            String name = "genshinerc_hud_" + tex.getNamespace() + "_" + tex.getPath().replaceAll("[^a-z0-9_]", "_");
            return RenderType.create(name,
                    DefaultVertexFormat.POSITION_TEX_COLOR,
                    VertexFormat.Mode.QUADS,
                    256,
                    false,
                    false,
                    RenderType.CompositeState.builder()
                            .setShaderState(SHADER)
                            .setTextureState(new TextureStateShard(tex, false, false))
                            .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                            .setDepthTestState(NO_DEPTH_TEST)
                            .setWriteMaskState(COLOR_WRITE)
                            .setCullState(NO_CULL)
                            .createCompositeState(false));
        }
    }
}