package com.dsp2.genshinerc.common.core.reaction;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public final class OverloadVFX {
    private OverloadVFX() {}

    public static void play(LivingEntity center) {
        if (center.level() instanceof ServerLevel level) play(level, center.getBoundingBox().getCenter());
    }

    public static void play(ServerLevel level, Vec3 at) {
        BlockPos pos = BlockPos.containing(at);
        level.playSound(null, pos, SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS,
                0.9f, 0.9f + level.random.nextFloat() * 0.3f);
        level.playSound(null, pos, SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.BLOCKS, 0.35f, 1.45f);
        level.sendParticles(ParticleTypes.EXPLOSION, at.x, at.y, at.z, 2, 0.25, 0.18, 0.25, 0);
        level.sendParticles(ParticleTypes.FLAME, at.x, at.y, at.z, 20, 0.4, 0.25, 0.4, 0.075);
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, at.x, at.y, at.z, 22, 0.45, 0.30, 0.45, 0.12);
        level.sendParticles(ParticleTypes.SMOKE, at.x, at.y, at.z, 8, 0.35, 0.25, 0.35, 0.025);
    }
}
