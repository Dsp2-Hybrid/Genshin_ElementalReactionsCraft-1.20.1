package com.dsp2.genshinerc.client.render.vfx.weaponshaders.test;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class CustomWeaponRenderType implements ParticleRenderType {
    private record Key(WeaponShaderMode mode, ResourceLocation texture) {}
    private static final Map<Key, CustomWeaponRenderType> CACHE = new HashMap<>();

    public static CustomWeaponRenderType get(WeaponShaderMode mode, ResourceLocation texture) {
        return CACHE.computeIfAbsent(new Key(mode, texture), k -> new CustomWeaponRenderType(mode, texture));
    }

    private final WeaponShaderMode mode;
    private final ResourceLocation texture;

    private CustomWeaponRenderType(WeaponShaderMode mode, ResourceLocation texture) {
        this.mode = mode;
        this.texture = texture;
    }

    @Override
    public void begin(BufferBuilder builder, TextureManager manager) {
        RenderSystem.enableBlend();
        RenderSystem.disableCull();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);

        ShaderInstance shader = WeaponShaderManager.getShader(mode);
        if (shader != null) {
            RenderSystem.setShader(() -> shader);
            if (shader.safeGetUniform("Time") != null) {
                shader.safeGetUniform("Time").set((System.currentTimeMillis() % 100000L) / 1000f);
            }
            if (shader.safeGetUniform("OutSize") != null) {
                shader.safeGetUniform("OutSize").set((float)Minecraft.getInstance().getWindow().getWidth(), (float)Minecraft.getInstance().getWindow().getHeight());
            }
        } else {
            RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        }

        if (this.texture != null) {
            manager.getTexture(this.texture).bind();
        }

        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
    }

    @Override
    public void end(Tesselator tesselator) {
        tesselator.end();
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableCull();
    }
}