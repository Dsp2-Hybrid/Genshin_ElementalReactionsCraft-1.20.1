package com.dsp2.genshinerc.client.render.vfx.weaponshaders.test;

import com.guhao.vix.client.pipeline.PostEffectPipelines;
import com.guhao.vix.client.targets.TargetManager;
import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

public class WeaponEffectPipeline extends PostEffectPipelines.Pipeline {
    private static final ResourceLocation TMP = ResourceLocation.fromNamespaceAndPath("genshinerc", "weapon_tmp");

    public WeaponEffectPipeline(ResourceLocation name) {
        super(name);
        this.priority = 1000;
    }

    @Override
    public void PostEffectHandler() {
        if (WeaponPostPasses.blit == null || WeaponPostPasses.composite == null || this.bufferTarget == null) return;

        RenderTarget main = Minecraft.getInstance().getMainRenderTarget();
        RenderTarget tmp = TargetManager.getTarget(TMP);

        if (tmp == null || tmp == main) return;

        if (tmp.width != main.width || tmp.height != main.height) {
            tmp.resize(main.width, main.height, false);
        }

        try {
            WeaponPostPasses.composite.process(this.bufferTarget, main, tmp);
            WeaponPostPasses.blit.draw(tmp, main);
        } finally {
            TargetManager.ReleaseTarget(TMP);
        }
    }
}