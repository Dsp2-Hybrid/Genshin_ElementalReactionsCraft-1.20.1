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
        return switch (m) {
            case WAVE, MIRROR -> 1;
            case TWIST, VORTEX, KALEIDOSCOPE -> 2;
            case PULSE, RIPPLE -> 3;
            case FISHEYE, SQUASH, ZOOM_BLUR, RADIAL_BLUR -> 4;
            case GLITCH_SLICE, PIXELSORT, PIXELATE, CRT -> 5;
            case RGBSPLIT, CHROMATIC -> 6;
            case NONE, TEST_RED -> 0;
            default -> 7;
        };
    }

    public void process(RenderTarget trail, RenderTarget scene, RenderTarget out) {
        out.bindWrite(true);
        RenderSystem.viewport(0, 0, out.width, out.height);
        this.effect.setSampler("DiffuseSampler", scene::getColorTextureId);
        this.effect.setSampler("TrailSampler", trail::getColorTextureId);
        this.effect.safeGetUniform("ProjMat").set(orthographic(out));
        this.effect.safeGetUniform("OutSize").set((float) out.width, (float) out.height);
        this.effect.safeGetUniform("Time").set((System.currentTimeMillis() % 100000L) / 1000f);
        this.effect.safeGetUniform("Mode").set(screenMode(WeaponShaderMode.currentMode));
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