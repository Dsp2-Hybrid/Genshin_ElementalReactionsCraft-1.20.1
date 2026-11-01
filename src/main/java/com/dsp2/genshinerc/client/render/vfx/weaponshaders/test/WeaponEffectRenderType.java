package com.dsp2.genshinerc.client.render.vfx.weaponshaders.test;

import com.guhao.vix.util.RenderUtils;
import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexSorting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import java.util.HashMap;
import java.util.Map;

public class WeaponEffectRenderType implements ParticleRenderType {
    private static final WeaponEffectPipeline PPL = new WeaponEffectPipeline(new ResourceLocation("genshinerc", "weapon_effect"));
    private record Key(WeaponShaderMode mode, ResourceLocation texture) {}
    private static final Map<Key, WeaponEffectRenderType> CACHE = new HashMap<>();

    private final ResourceLocation texture;
    private final WeaponShaderMode mode;

    private WeaponEffectRenderType(ResourceLocation texture, WeaponShaderMode mode) {
        this.texture = texture;
        this.mode = mode;
    }

    public static WeaponEffectRenderType get(ResourceLocation texture, WeaponShaderMode mode) {
        return CACHE.computeIfAbsent(new Key(mode, texture), k -> new WeaponEffectRenderType(texture, mode));
    }

    @Override
    public void begin(BufferBuilder builder, TextureManager manager) {
        RenderSystem.enableBlend();
        RenderSystem.disableCull();
        RenderSystem.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);

        ShaderInstance found = WeaponShaderManager.getShader(mode);
        final ShaderInstance shader = found != null ? found : GameRenderer.getPositionTexColorShader();
        RenderSystem.setShader(() -> shader);
        if (shader != null) {
            shader.safeGetUniform("Time").set((System.currentTimeMillis() % 100000L) / 1000f);
            shader.safeGetUniform("OutSize").set(
                    (float) Minecraft.getInstance().getWindow().getWidth(),
                    (float) Minecraft.getInstance().getWindow().getHeight());
        }

        if (this.texture != null) {
            RenderUtils.GLSetTexture(this.texture);
        }

        PPL.start();
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
    }

    @Override
    public void end(Tesselator tesselator) {
        tesselator.getBuilder().setQuadSorting(VertexSorting.DISTANCE_TO_ORIGIN);
        tesselator.end();
        PPL.call();
        PPL.suspend();
        RenderSystem.depthMask(false);
        RenderSystem.disableDepthTest();
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableCull();
    }

    @Override
    public String toString() {
        return "genshinerc:weapon_effect_" + mode.name().toLowerCase();
    }
}