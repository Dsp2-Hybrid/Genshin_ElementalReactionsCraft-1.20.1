package com.dsp2.genshinerc.client.render.vfx.element.elevfx.fadeout;

import com.guhao.vix.client.aaaeffect.AAAEffectPostProcessContext;
import com.guhao.vix.client.aaaeffect.AAAEffectPostProcessor;
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

public final class ElementOpacityProcessor implements AAAEffectPostProcessor {
    private EffectInstance shader;
    public static boolean ENABLED = true;
    public static float currentAlpha = 1.0f;

    @Override
    public boolean isEnabled() { return ENABLED; }

    @Override
    public void processScene(AAAEffectPostProcessContext ctx, RenderTarget before, RenderTarget after, RenderTarget isolated, RenderTarget scene) {
        if (shader == null) {
            try {
                shader = new EffectInstance(Minecraft.getInstance().getResourceManager(), "genshinerc:element_opacity");
            } catch (IOException e) {
                throw new IllegalStateException("Unable to load genshinerc:element_opacity", e);
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
        shader.setSampler("IsolatedSampler", isolated::getColorTextureId);

        shader.safeGetUniform("ProjMat").set(new Matrix4f().setOrtho(0f, scene.width, 0f, scene.height, 0.1f, 1000f));
        shader.safeGetUniform("OutSize").set((float) scene.width, (float) scene.height);
        shader.safeGetUniform("Alpha").set(currentAlpha);

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
        com.dsp2.genshinerc.client.render.vfx.aaapshader.ShaderPackAaaPostProcess.invalidate();
    }

    @Override
    public void close() {
        EffectInstance old = shader;
        shader = null;
        if (old != null) { RenderSystem.recordRenderCall(old::close); }
    }
}