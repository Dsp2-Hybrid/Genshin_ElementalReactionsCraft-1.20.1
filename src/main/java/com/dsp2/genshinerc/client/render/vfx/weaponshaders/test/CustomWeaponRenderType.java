package com.dsp2.genshinerc.client.render.vfx.weaponshaders.test;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;

public class CustomWeaponRenderType implements ParticleRenderType {
    private final ResourceLocation texture;
    private final ShaderInstance shader;

    public CustomWeaponRenderType(ResourceLocation texture, ShaderInstance shader) {
        this.texture = texture;
        this.shader = shader;
    }

    @Override
    public void begin(BufferBuilder builder, TextureManager manager) { // 改了这里：m_6505_ -> begin
        RenderSystem.enableBlend();
        RenderSystem.disableCull();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);

        if (this.shader != null) {
            RenderSystem.setShader(() -> this.shader);
            if (this.shader.safeGetUniform("Time") != null) {
                this.shader.safeGetUniform("Time").set((float) (System.currentTimeMillis() % 100000) / 1000f);
            }
        } else {
            // 改了这里：用官方名字
            RenderSystem.setShader(GameRenderer::getPositionColorTexLightmapShader);
        }

        if (this.texture != null) {
            RenderSystem.setShaderTexture(0, this.texture);
        }

        // 改了这里：m_166779_ -> begin
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP);
    }

    @Override
    public void end(Tesselator tesselator) { // 改了这里：m_6294_ -> end
        tesselator.end(); // 改了这里：m_85914_ -> end()
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableCull();
    }
}