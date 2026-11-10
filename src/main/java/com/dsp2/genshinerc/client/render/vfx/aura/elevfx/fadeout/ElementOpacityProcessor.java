package com.dsp2.genshinerc.client.render.vfx.aura.elevfx.fadeout;

import com.dsp2.genshinerc.client.render.vfx.feature.light.AaapLightRenderer;
import com.guhao.vix.client.aaaeffect.AAAEffectPostProcessContext;
import com.dsp2.genshinerc.client.render.vfx.aaapshader.PerEmitterAaaPostProcessor;
import com.dsp2.genshinerc.client.render.vfx.aaapshader.ShaderPackAaaPostProcess;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EffectInstance;
import net.minecraft.server.packs.resources.ResourceManager;
import org.joml.Matrix4f;
import java.io.IOException;

public final class ElementOpacityProcessor implements PerEmitterAaaPostProcessor {
    private EffectInstance shader;
    public static boolean ENABLED = true;
    public static float currentAlpha = 1.0f;

    @Override
    public boolean isEnabled() { return ENABLED; }

    @Override
    public void processScene(AAAEffectPostProcessContext ctx, RenderTarget before, RenderTarget after, RenderTarget isolated, RenderTarget scene) {
        if (shader == null) {
            try {
                shader = new EffectInstance(Minecraft.getInstance().getResourceManager(), "genshinerc:aaa_particle/aura/element_opacity");
            } catch (IOException e) {
                throw new IllegalStateException("Unable to load genshinerc:aaa_particle/aura/element_opacity", e);
            }
        }

        RenderSystem.disableScissor();
        RenderSystem.colorMask(true, true, true, true);
        scene.bindWrite(false);
        RenderSystem.viewport(0, 0, scene.width, scene.height);
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        shader.setSampler("BeforeSampler", before::getColorTextureId);
        shader.setSampler("AfterSampler", after::getColorTextureId);

        shader.safeGetUniform("ProjMat").set(new Matrix4f().setOrtho(0f, scene.width, 0f, scene.height, 0.1f, 1000f));
        shader.safeGetUniform("OutSize").set((float) scene.width, (float) scene.height);
        shader.safeGetUniform("Alpha").set(ElementEmitterAlpha.resolve(ShaderPackAaaPostProcess.currentEmitters(), currentAlpha));

        shader.apply();

        try {
            BufferBuilder b = Tesselator.getInstance().getBuilder();
            b.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
            b.vertex(0.0, 0.0, 500.0).endVertex();
            b.vertex(scene.width, 0.0, 500.0).endVertex();
            b.vertex(scene.width, scene.height, 500.0).endVertex();
            b.vertex(0.0, scene.height, 500.0).endVertex();
            BufferUploader.draw(b.end());
        } finally {
            shader.clear();
        }
    }

    @Override
    public void onResourceReload(ResourceManager rm) {
        close();
        ShaderPackAaaPostProcess.invalidate();
        AaapLightRenderer.invalidate();
    }

    @Override
    public void close() {
        EffectInstance old = shader;
        shader = null;
        if (old != null) { RenderSystem.recordRenderCall(old::close); }
    }
}