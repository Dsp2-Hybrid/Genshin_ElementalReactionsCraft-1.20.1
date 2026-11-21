package com.dsp2.genshinerc.common.world.block;

import com.dsp2.genshinerc.common.core.element.ElementGauge;
import com.dsp2.genshinerc.common.registry.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class EleIceBlock extends HalfTransparentBlock {
    public static final BooleanProperty SOURCE = BooleanProperty.create("source");
    public static final IntegerProperty AGE = IntegerProperty.create("age", 0, 4);

    public EleIceBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(SOURCE, false).setValue(AGE, 0));
    }

    public static void freeze(ServerLevel level, BlockPos pos, boolean source, float units) {
        BlockPos at = pos.immutable();
        if (!level.getBlockState(at).is(Blocks.WATER)) return;


        var ice = ModBlocks.SPECIAL_ICE.get();
        int lifetime = Math.max(12, ElementGauge.durationTicksFromUnits(units) + level.random.nextInt(10));
        level.scheduleTick(at, ice, lifetime - 8);
        if (!level.setBlockAndUpdate(at, ice.defaultBlockState().setValue(SOURCE, source).setValue(AGE, 0))) return;

        level.playSound(null, at, SoundEvents.GLASS_PLACE, SoundSource.BLOCKS, 0.6f, 1.4f);
        level.sendParticles(ParticleTypes.SNOWFLAKE, at.getX() + 0.5, at.getY() + 0.72, at.getZ() + 0.5,
                10, 0.28, 0.16, 0.28, 0.025);
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.ICE.defaultBlockState()),
                at.getX() + 0.5, at.getY() + 0.78, at.getZ() + 0.5,
                6, 0.25, 0.14, 0.25, 0.05);
    }

    public static void melt(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof EleIceBlock)) return;
        level.setBlockAndUpdate(pos, state.getValue(SOURCE)
                ? Blocks.WATER.defaultBlockState()
                : Blocks.AIR.defaultBlockState());
        level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5f, 1.3f);
        level.sendParticles(ParticleTypes.CLOUD, pos.getX() + 0.5, pos.getY() + 0.4, pos.getZ() + 0.5,
                4, 0.25, 0.2, 0.25, 0.01);
        level.sendParticles(ParticleTypes.FALLING_WATER, pos.getX() + 0.5, pos.getY() + 0.65, pos.getZ() + 0.5,
                8, 0.25, 0.18, 0.25, 0.02);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int age = state.getValue(AGE);
        if (age >= 4) {
            melt(level, pos);
            return;
        }
        level.setBlock(pos, state.setValue(AGE, age + 1), 3);
        level.scheduleTick(pos, this, 2);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState old, boolean moving) {
        if (!level.isClientSide && !level.getBlockTicks().hasScheduledTick(pos, this)) {
            level.scheduleTick(pos, this, ElementGauge.durationTicksFromUnits(2f));
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(8) == 0) {
            level.addParticle(ParticleTypes.SNOWFLAKE,
                    pos.getX() + random.nextDouble(), pos.getY() + 0.8, pos.getZ() + random.nextDouble(),
                    0, 0.01, 0);
        }
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Block.box(0.0, 14.0, 0.0, 16.0, 16.0, 16.0);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(SOURCE, AGE);
    }
}