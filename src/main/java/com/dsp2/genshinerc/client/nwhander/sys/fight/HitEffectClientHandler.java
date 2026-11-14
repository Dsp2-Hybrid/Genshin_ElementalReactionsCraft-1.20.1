package com.dsp2.genshinerc.client.nwhander.sys.fight;

import com.dsp2.genshinerc.client.render.vfx.feature.light.AaapTransientLights;
import mod.chloeprime.aaaparticles.api.common.AAALevel;
import mod.chloeprime.aaaparticles.api.common.ParticleEmitterInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;

public final class HitEffectClientHandler {
    private HitEffectClientHandler() {}
    public static void spawn(double x, double y, double z, ResourceLocation effectId,
                             float scale, float lightSec, float fadeSec) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return;
        ParticleEmitterInfo info = ParticleEmitterInfo.create(level, effectId).position(x, y, z).scale(scale);
        AAALevel.addParticle(level, true, info);
        if (lightSec > 0f) AaapTransientLights.spawnCustom(effectId, x, y, z, scale, lightSec, fadeSec);
    }
}