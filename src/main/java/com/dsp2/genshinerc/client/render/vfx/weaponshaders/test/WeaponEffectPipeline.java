package com.dsp2.genshinerc.client.render.vfx.weaponshaders.test;

import com.guhao.vix.client.pipeline.PostEffectPipelines;
import com.guhao.vix.client.targets.TargetManager;
import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;


@SuppressWarnings({"deprecation", "removal"})
public class WeaponEffectPipeline extends PostEffectPipelines.Pipeline {
    private static final ResourceLocation TMP = new ResourceLocation("genshinerc", "weapon_tmp");

    public WeaponEffectPipeline(ResourceLocation name) {
        super(name);
        this.priority = 1000;
    }

    @Override
    public void PostEffectHandler() {
        if (WeaponPostPasses.blit == null || this.bufferTarget == null) return;

        RenderTarget main = Minecraft.getInstance().getMainRenderTarget();
        RenderTarget tmp = TargetManager.getTarget(TMP);
        try {
            WeaponPostPasses.composite.process(this.bufferTarget, main, tmp);
            WeaponPostPasses.blit.process(tmp, main);
        } finally {
            TargetManager.ReleaseTarget(TMP);
        }
    }
}