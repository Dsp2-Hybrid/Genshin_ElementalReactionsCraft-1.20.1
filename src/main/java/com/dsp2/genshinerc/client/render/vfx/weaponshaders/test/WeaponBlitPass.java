package com.dsp2.genshinerc.client.render.vfx.weaponshaders.test;

import com.guhao.vix.client.shaderpasses.PostPassBase;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.server.packs.resources.ResourceManager;
import java.io.IOException;

public class WeaponBlitPass extends PostPassBase {
    public WeaponBlitPass(String resourceLocation, ResourceManager rm) throws IOException {
        super(resourceLocation, rm);
    }

    public void draw(RenderTarget in, RenderTarget out) {
        out.bindWrite(false);
        RenderSystem.viewport(0, 0, out.width, out.height);
        this.effect.setSampler("DiffuseSampler", in::getColorTextureId);
        this.effect.safeGetUniform("ProjMat").set(orthographic(out));
        this.effect.safeGetUniform("OutSize").set((float) out.width, (float) out.height);
        this.effect.apply();
        this.pushVertex(in, out);
        this.effect.clear();
        out.unbindWrite();
        in.unbindRead();
    }
}