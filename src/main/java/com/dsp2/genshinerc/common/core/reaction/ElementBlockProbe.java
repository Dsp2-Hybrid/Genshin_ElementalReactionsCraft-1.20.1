package com.dsp2.genshinerc.common.core.reaction;

import com.dsp2.genshinerc.common.config.ServerConfig;
import com.dsp2.genshinerc.common.core.element.ElementType;
import com.dsp2.genshinerc.common.world.block.EleIceBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public final class ElementBlockProbe {
    private ElementBlockProbe() {}

    public static boolean apply(ServerLevel level, BlockPos pos, BlockState state,
                                @Nullable Entity attacker, ElementType element, float units) {
        boolean fire = state.is(BlockTags.FIRE);
        boolean loose = state.is(Blocks.SNOW) || state.is(Blocks.SNOW_BLOCK);
        LivingEntity living = attacker instanceof LivingEntity l ? l : null;
        boolean handled = false;
        switch (element) {
            case ELECTRO -> handled = true;
            case CRYO, FROZEN -> {
                if (state.is(Blocks.WATER) && ServerConfig.CRYO_FREEZE_BLOCKS.get()) {
                    EleIceBlock.freeze(level, pos, state.getFluidState().isSource(), Math.max(0.1f, units));
                    handled = true;
                } else if (fire && level.removeBlock(pos, false)) {
                    snd(level, pos, SoundEvents.FIRE_EXTINGUISH, 0.55f, 1.25f);
                    level.sendParticles(ParticleTypes.SNOWFLAKE, pos.getX() + 0.5, pos.getY() + 0.4,
                            pos.getZ() + 0.5, 8, 0.22, 0.18, 0.22, 0.03);
                    handled = true;
                }
            }
            case GEO -> {
                if ((fire || loose) && level.removeBlock(pos, false)) {
                    if (living != null) {
                        ElementReactions.probeCrystallize(level, living, Vec3.atCenterOf(pos),
                                fire ? ElementType.PYRO : ElementType.CRYO);
                    }
                    snd(level, pos, SoundEvents.AMETHYST_BLOCK_BREAK, 0.6f, 1.2f);
                    level.sendParticles(ParticleTypes.END_ROD, pos.getX() + 0.5, pos.getY() + 0.45,
                            pos.getZ() + 0.5, 8, 0.25, 0.2, 0.25, 0.03);
                    handled = true;
                }
            }
            case HYDRO -> {
                if (fire && level.removeBlock(pos, false)) {
                    snd(level, pos, SoundEvents.FIRE_EXTINGUISH, 0.6f, 1.2f);
                    level.sendParticles(ParticleTypes.CLOUD, pos.getX() + 0.5, pos.getY() + 0.4,
                            pos.getZ() + 0.5, 7, 0.2, 0.16, 0.2, 0.025);
                    handled = true;
                }
            }
            case PYRO -> {
                if (state.getBlock() instanceof CropBlock || state.is(BlockTags.FLOWERS)) {
                    handled = true;
                }
            }
            case DENDRO -> {
                if (state.getBlock() instanceof CropBlock crop) {
                    crop.randomTick(state, level, pos, level.random);
                    level.sendParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5,
                            pos.getY() + 0.55, pos.getZ() + 0.5, 2, 0.15, 0.15, 0.15, 0.01);
                    handled = true;
                }
            }
            default -> { }
        }
        return handled;
    }

    private static void snd(ServerLevel level, BlockPos pos, net.minecraft.sounds.SoundEvent sound, float volume, float pitch) {
        level.playSound(null, pos, sound, SoundSource.BLOCKS,
                volume * (0.9f + level.random.nextFloat() * 0.2f),
                pitch * (0.93f + level.random.nextFloat() * 0.14f));
    }
}