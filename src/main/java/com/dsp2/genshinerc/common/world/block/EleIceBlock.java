package com.dsp2.genshinerc.common.world.block;

import com.dsp2.genshinerc.common.core.element.ElementGauge;
import com.dsp2.genshinerc.common.registry.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public class EleIceBlock extends HalfTransparentBlock {
    public static final BooleanProperty SOURCE = BooleanProperty.create("source");

    public EleIceBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(SOURCE, false));
    }

    public static void freeze(ServerLevel level, BlockPos pos, boolean source, float units) {
        BlockPos at = pos.immutable();
        level.setBlockAndUpdate(at, ModBlocks.SPECIAL_ICE.get().defaultBlockState().setValue(SOURCE, source));
        level.scheduleTick(at, ModBlocks.SPECIAL_ICE.get(), ElementGauge.durationTicksFromUnits(units) + level.random.nextInt(10));
        level.playSound(null, at, SoundEvents.GLASS_PLACE, SoundSource.BLOCKS, 0.6f, 1.4f);
        level.sendParticles(ParticleTypes.SNOWFLAKE, at.getX() + 0.5, at.getY() + 0.6, at.getZ() + 0.5, 8, 0.3, 0.3, 0.3, 0.01);
    }

    public static void melt(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof EleIceBlock)) return;
        level.setBlockAndUpdate(pos, state.getValue(SOURCE) ? Blocks.WATER.defaultBlockState() : Blocks.AIR.defaultBlockState());
        level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5f, 1.3f);
        level.sendParticles(ParticleTypes.CLOUD, pos.getX() + 0.5, pos.getY() + 0.4, pos.getZ() + 0.5, 4, 0.25, 0.2, 0.25, 0.01);
        level.sendParticles(ParticleTypes.ITEM_SNOWBALL, pos.getX() + 0.5, pos.getY() + 0.4, pos.getZ() + 0.5, 6, 0.25, 0.2, 0.25, 0.04);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) { melt(level, pos); }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState old, boolean moving) {
        if (!level.isClientSide && !level.getBlockTicks().hasScheduledTick(pos, this))
            level.scheduleTick(pos, this, ElementGauge.durationTicksFromUnits(ElementImbueUnits()));
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(8) == 0) level.addParticle(ParticleTypes.SNOWFLAKE, pos.getX() + random.nextDouble(), pos.getY() + 0.8, pos.getZ() + random.nextDouble(), 0, 0.01, 0);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) { builder.add(SOURCE); }

    private static float ElementImbueUnits() { return 2f; }
}
