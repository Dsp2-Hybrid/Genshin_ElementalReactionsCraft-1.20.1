package com.dsp2.genshinerc.common.core.reaction;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;

public final class OverloadVFX {
    private OverloadVFX() {}
    public static void play(LivingEntity center) {
        if (!(center.level() instanceof ServerLevel sl)) return;
        sl.playSound(null, center.blockPosition(), SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 1.0f, 0.8f + sl.getRandom().nextFloat() * 0.4f);
        sl.playSound(null, center.blockPosition(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.HOSTILE, 0.6f, 1.2f);
        sl.sendParticles(ParticleTypes.FLAME, center.getX(), center.getY() + 0.5, center.getZ(), 20, 0.5, 0.5, 0.5, 0.05);
        sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, center.getX(), center.getY() + 0.5, center.getZ(), 15, 0.5, 0.5, 0.5, 0.1);
    }
}