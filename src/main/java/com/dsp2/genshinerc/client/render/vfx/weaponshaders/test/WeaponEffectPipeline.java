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
            String effectKey = getScreenEffectKey(WeaponShaderMode.currentMode);
            WeaponCompositePass screenPass = WeaponPostPasses.screenEffects.get(effectKey);

            if (screenPass != null) {
                screenPass.process(this.bufferTarget, main, tmp);
                WeaponPostPasses.blit.process(tmp, main);
            } else {
                WeaponPostPasses.composite.process(this.bufferTarget, main, tmp);
                WeaponPostPasses.blit.process(tmp, main);
            }
        } finally {
            TargetManager.ReleaseTarget(TMP);
        }
    }

    private String getScreenEffectKey(WeaponShaderMode mode) {
        switch (mode) {
            case DISTORT_WAVE: return "distort_wave";
            case DISTORT_RADIAL: return "distort_radial";
            case RGB_SPLIT: return "rgbsplit";
            case PIXELATE: return "pixelate";
            case SCANLINE: return "scanline";
            case KALEIDO: return "kaleido";
            case GLITCH: return "glitch";
            case FISHEYE_SCREEN: return "fisheye_screen";
            case INVERT: return "invert";
            case DISTORT_TWIST: return "twist";
            case DISTORT_MIRROR: return "mirror";
            default: return "none";
        }
    }
}