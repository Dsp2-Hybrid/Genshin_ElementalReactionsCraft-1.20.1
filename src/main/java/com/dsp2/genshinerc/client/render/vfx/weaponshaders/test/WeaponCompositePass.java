package com.dsp2.genshinerc.client.render.vfx.weaponshaders.test;

import com.guhao.vix.client.shaderpasses.PostPassBase;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.server.packs.resources.ResourceManager;
import java.io.IOException;

public class WeaponCompositePass extends PostPassBase {
    public WeaponCompositePass(String resourceLocation, ResourceManager rm) throws IOException {
        super(resourceLocation, rm);
    }

    private static int screenMode(WeaponShaderMode m) {
        if (!WeaponShaderParams.has(m, "distort")) return 0;
        return switch (m) {
            case WAVE -> 1;
            case MIRROR -> 2;
            case TWIST, VORTEX -> 3;
            case KALEIDOSCOPE -> 4;
            case PULSE, RIPPLE -> 5;
            case FISHEYE -> 6;
            case SQUASH -> 7;
            case ZOOM_BLUR, RADIAL_BLUR, PIXELSORT -> 8;
            case GLITCH_SLICE, CRT -> 9;
            case PIXELATE -> 10;
            case RGBSPLIT, CHROMATIC -> 11;
            default -> 12;
        };
    }

    public void process(RenderTarget trail, RenderTarget scene, RenderTarget out) {
        WeaponShaderMode m = WeaponShaderMode.currentMode;
        boolean hasDistort = WeaponShaderParams.has(m, "distort");
        float distort = hasDistort ? WeaponShaderParams.get(m, "distort") : 0f;
        float distortFreq = hasDistort ? WeaponShaderParams.get(m, "distortFreq") : 1f;
        float glow = WeaponShaderParams.get(m, "glow");

        out.bindWrite(true);
        RenderSystem.viewport(0, 0, out.width, out.height);
        this.effect.setSampler("DiffuseSampler", scene::getColorTextureId);
        this.effect.setSampler("TrailSampler", trail::getColorTextureId);
        this.effect.safeGetUniform("ProjMat").set(orthographic(out));
        this.effect.safeGetUniform("OutSize").set((float) out.width, (float) out.height);
        this.effect.safeGetUniform("Time").set((System.currentTimeMillis() % 100000L) / 1000f);
        this.effect.safeGetUniform("Mode").set(screenMode(m));
        this.effect.safeGetUniform("DistortStrength").set(distort);
        this.effect.safeGetUniform("DistortFreq").set(distortFreq);
        this.effect.safeGetUniform("GlowStrength").set(glow);
        this.effect.apply();
        try {
            this.pushVertex(scene, out);
        } finally {
            this.effect.clear();
            out.unbindWrite();
            scene.unbindRead();
            trail.unbindRead();
        }
    }
}