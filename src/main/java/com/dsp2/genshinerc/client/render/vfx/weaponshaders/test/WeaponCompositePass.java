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

    public void process(RenderTarget trail, RenderTarget scene, RenderTarget out) {
        this.prevProcess(scene, out);
        scene.unbindWrite();
        RenderSystem.viewport(0, 0, out.width, out.height);
        this.effect.setSampler("DiffuseSampler", scene::getColorTextureId);
        this.effect.setSampler("TrailSampler", trail::getColorTextureId);
        this.effect.safeGetUniform("ProjMat").set(orthographic(out));
        this.effect.safeGetUniform("OutSize").set((float) out.width, (float) out.height);
        var time = this.effect.safeGetUniform("Time");
        if (time != null) time.set((System.currentTimeMillis() % 100000L) / 1000f);
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