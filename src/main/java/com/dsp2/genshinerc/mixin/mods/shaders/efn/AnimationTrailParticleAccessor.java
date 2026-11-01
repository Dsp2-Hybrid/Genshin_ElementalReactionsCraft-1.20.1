package com.dsp2.genshinerc.mixin.mods.shaders.efn;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import yesman.epicfight.client.particle.AnimationTrailParticle;
import yesman.epicfight.api.client.animation.property.TrailInfo;

@Mixin(AnimationTrailParticle.class)
public interface AnimationTrailParticleAccessor {
    @Accessor("trailInfo")
    TrailInfo getTrailInfo();
}