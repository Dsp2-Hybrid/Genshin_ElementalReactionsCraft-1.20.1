package com.dsp2.genshinerc.client.render.vfx.aaapshader;

import com.guhao.vix.client.aaaeffect.AAAEffectPostProcessContext;
import com.guhao.vix.client.aaaeffect.AAAEffectPostProcessRegistry;
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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.joml.Matrix4f;

import java.io.IOException;

@SuppressWarnings({"deprecation", "removal"})
@Mod.EventBusSubscriber(modid = "genshinerc", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class MurasamaShaderSetup {
    public static boolean ENABLE_THERMAL_SHADER = true;
    private static final ResourceLocation HIT_EFFECT_ID = new ResourceLocation("genshinerc", "hit_fire_c01");

    private MurasamaShaderSetup() {}

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ThermalProcessor thermal = new ThermalProcessor();
            AAAEffectPostProcessRegistry.register(HIT_EFFECT_ID, thermal);
            ShaderPackAaaPostProcess.register(HIT_EFFECT_ID, thermal);
        });
    }

    private static final class ThermalProcessor implements AAAEffectPostProcessor {
        private EffectInstance shader;

        @Override
        public boolean isEnabled() { return ENABLE_THERMAL_SHADER; }

        @Override
        public void processScene(AAAEffectPostProcessContext ctx, RenderTarget before, RenderTarget after, RenderTarget isolated, RenderTarget scene) {
            if (shader == null) {
                try {
                    shader = new EffectInstance(Minecraft.getInstance().getResourceManager(), "genshinerc:aaa_particle/thermal/weapon_thermal");
                } catch (IOException e) {
                    throw new IllegalStateException("Unable to load genshinerc:aaa_particle/thermal/weapon_thermal", e);
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

            shader.setSampler("DiffuseSampler", after::getColorTextureId);
            shader.setSampler("BeforeSampler", before::getColorTextureId);
            
            shader.safeGetUniform("ProjMat").set(new Matrix4f().setOrtho(0f, scene.width, 0f, scene.height, 0.1f, 1000f));
            shader.safeGetUniform("OutSize").set((float) scene.width, (float) scene.height);
            shader.safeGetUniform("Time").set((float)(System.currentTimeMillis() % 100000) / 1000.0f);
            
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
        }

        @Override
        public void close() {
            EffectInstance old = shader;
            shader = null;
            if (old != null) { RenderSystem.recordRenderCall(old::close); }
        }
    }
}