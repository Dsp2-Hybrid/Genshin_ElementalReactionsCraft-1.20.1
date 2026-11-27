package com.dsp2.genshinerc.common.world.block;

import com.dsp2.genshinerc.common.core.element.ElementGauge;
import com.dsp2.genshinerc.common.registry.block.ModBlocks;
import com.dsp2.genshinerc.common.world.block.base.IceEntityResolver;
import com.dsp2.genshinerc.common.world.block.entity.SpecialIceBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class EleIceBlock extends BaseEntityBlock {
    public static final BooleanProperty SOURCE = BooleanProperty.create("source");
    public static final IntegerProperty AGE = IntegerProperty.create("age", 0, 40);
    public static final IntegerProperty WATER_LEVEL = IntegerProperty.create("water_level", 0, 7);

    public EleIceBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(SOURCE, false)
                .setValue(AGE, 0).setValue(WATER_LEVEL, 0));
    }

    public static BlockState originalWater(BlockState ice) {
        int waterLevel = ice.getValue(SOURCE) ? 0 : Math.max(1, ice.getValue(WATER_LEVEL));
        return Blocks.WATER.defaultBlockState().setValue(LiquidBlock.LEVEL, waterLevel);
    }

    public static void freeze(ServerLevel level, BlockPos pos, boolean source, float units) {
        BlockState previous = level.getBlockState(pos);
        if (previous.getBlock() instanceof EleIceBlock) {
            IceEntityResolver.resolveAround(level, pos);
            return;
        }

        FluidState fluid = previous.getFluidState();
        if (!fluid.is(FluidTags.WATER)) return;

        boolean waterSource = source || fluid.isSource();
        BlockState legacyWater = fluid.createLegacyBlock();
        int waterLevel = waterSource ? 0 : legacyWater.hasProperty(LiquidBlock.LEVEL)
                ? Math.max(1, Math.min(7, legacyWater.getValue(LiquidBlock.LEVEL)))
                : Math.max(1, Math.min(7, 8 - fluid.getAmount()));

        long now = level.getGameTime();
        long duration = Math.max(60L,
                ElementGauge.durationTicksFromUnits(Math.max(0.25f, units)) + level.random.nextInt(10));
        BlockState ice = ModBlocks.SPECIAL_ICE.get().defaultBlockState()
                .setValue(SOURCE, waterSource).setValue(WATER_LEVEL, waterLevel).setValue(AGE, 0);

        if (!level.setBlockAndUpdate(pos, ice)) return;
        if (level.getBlockEntity(pos) instanceof SpecialIceBlockEntity be) {
            be.setFreezeData(now, now + duration, level.random.nextLong(),
                    Math.max(0, (int) Math.floor(Math.max(0f, units) * 2f)), waterLevel);
        }

        IceEntityResolver.resolveAround(level, pos);
        level.scheduleTick(pos, ModBlocks.SPECIAL_ICE.get(), 1);
        level.playSound(null, pos, SoundEvents.GLASS_PLACE, SoundSource.BLOCKS,
                0.78f, 1.26f + level.random.nextFloat() * 0.16f);
        level.sendParticles(ParticleTypes.SNOWFLAKE, pos.getX() + 0.5, pos.getY() + 0.96,
                pos.getZ() + 0.5, 30, 0.36, 0.12, 0.36, 0.04);
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.ICE.defaultBlockState()),
                pos.getX() + 0.5, pos.getY() + 0.97, pos.getZ() + 0.5,
                22, 0.32, 0.10, 0.32, 0.06);
    }

    public static void melt(ServerLevel level, BlockPos pos) {
        BlockState ice = level.getBlockState(pos);
        if (!(ice.getBlock() instanceof EleIceBlock)) return;

        BlockState water = originalWater(ice);
        if (level.getBlockEntity(pos) instanceof SpecialIceBlockEntity be
                && !ice.getValue(SOURCE) && be.getWaterLevel() > 0) {
            water = water.setValue(LiquidBlock.LEVEL, be.getWaterLevel());
        }

        level.setBlockAndUpdate(pos, water);
        FluidState restoredFluid = level.getFluidState(pos);
        if (restoredFluid.is(FluidTags.WATER)) {
            level.scheduleTick(pos, restoredFluid.getType(), restoredFluid.getType().getTickDelay(level));
        }

        level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS,
                0.78f, 1.05f + level.random.nextFloat() * 0.20f);
        level.sendParticles(ParticleTypes.CLOUD, pos.getX() + 0.5, pos.getY() + 0.5,
                pos.getZ() + 0.5, 14, 0.34, 0.30, 0.34, 0.04);
        level.sendParticles(ParticleTypes.FALLING_WATER, pos.getX() + 0.5, pos.getY() + 0.65,
                pos.getZ() + 0.5, 20, 0.32, 0.28, 0.32, 0.045);
    }

    private static boolean isFire(BlockState state) {
        return state.is(Blocks.FIRE) || state.is(Blocks.SOUL_FIRE);
    }

    private static boolean isLava(ServerLevel level, BlockPos pos) {
        if (!level.hasChunkAt(pos)) return false;
        BlockState state = level.getBlockState(pos);
        FluidState fluid = state.getFluidState();
        return state.is(Blocks.LAVA) || fluid.is(FluidTags.LAVA)
                || fluid.getType() == Fluids.LAVA;
    }

    private static int heatRate(ServerLevel level, BlockPos origin) {
        if (isFire(level.getBlockState(origin.above()))) return 0;

        boolean firstShellFire = false;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int radius = 1; radius <= 2; radius++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dy = -radius; dy <= radius; dy++) {
                    for (int dz = -radius; dz <= radius; dz++) {
                        if (Math.max(Math.abs(dx), Math.max(Math.abs(dy), Math.abs(dz))) != radius) continue;
                        cursor.setWithOffset(origin, dx, dy, dz);
                        if (!level.hasChunkAt(cursor)) continue;

                        BlockState neighbor = level.getBlockState(cursor);
                        if (isLava(level, cursor)) return radius == 1 ? 0 : 2;
                        if (radius == 1 && !(dx == 0 && dy == 1 && dz == 0) && isFire(neighbor)) {
                            firstShellFire = true;
                        }
                    }
                }
            }
            if (radius == 1 && firstShellFire) return 2;
        }
        return 1;
    }

    private static void removeFireAbove(ServerLevel level, BlockPos pos) {
        BlockPos above = pos.above();
        if (!isFire(level.getBlockState(above))) return;
        level.removeBlock(above, false);
        level.playSound(null, above, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS,
                0.8f, 1f + level.random.nextFloat() * 0.2f);
        level.sendParticles(ParticleTypes.SMOKE, above.getX() + 0.5, above.getY() + 0.5,
                above.getZ() + 0.5, 8, 0.18, 0.18, 0.18, 0.01);
    }

    private static void checkHeat(ServerLevel level, BlockPos pos) {
        if (heatRate(level, pos) != 0) return;
        removeFireAbove(level, pos);
        melt(level, pos);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighbor,
                                BlockPos neighborPos, boolean moving) {
        super.neighborChanged(state, level, pos, neighbor, neighborPos, moving);
        if (level instanceof ServerLevel server) {
            checkHeat(server, pos);
            if (server.getBlockState(pos).getBlock() instanceof EleIceBlock) {
                server.scheduleTick(pos, this, 1);
            }
        }
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                  net.minecraft.world.level.LevelAccessor level, BlockPos pos,
                                  BlockPos neighborPos) {
        if (level instanceof ServerLevel server) {
            checkHeat(server, pos);
            if (server.getBlockState(pos).getBlock() instanceof EleIceBlock) {
                server.scheduleTick(pos, this, 1);
            }
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!(level.getBlockEntity(pos) instanceof SpecialIceBlockEntity be)) return;

        int rate = heatRate(level, pos);
        if (rate == 0) {
            removeFireAbove(level, pos);
            melt(level, pos);
            return;
        }

        long now = level.getGameTime();
        if (be.getExpiresAt() <= 0L) {
            long duration = Math.max(1L, ElementGauge.durationTicksFromUnits(2f));
            be.setFreezeData(now, now + duration, random.nextLong(), 4, state.getValue(WATER_LEVEL));
        }
        be.setRate(now, rate);
        if (be.remaining(now) <= 0.0) {
            melt(level, pos);
            return;
        }

        int age = (int) Math.min(40L, Math.max(0L,
                (long) (40.0 * (1.0 - be.remaining(now) / be.getDuration()))));
        if (state.getValue(AGE) != age) {
            level.setBlock(pos, state.setValue(AGE, age), Block.UPDATE_CLIENTS);
        }
        IceEntityResolver.resolveAround(level, pos);
        level.scheduleTick(pos, this, 1);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean moving) {
        super.onPlace(state, level, pos, oldState, moving);
        if (level instanceof ServerLevel server && !oldState.is(this)) {
            server.scheduleTick(pos, this, 1);
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        float strength = 1f - state.getValue(AGE) / 40f;
        if (random.nextInt(3) == 0) {
            level.addParticle(ParticleTypes.SNOWFLAKE, pos.getX() + random.nextDouble(),
                    pos.getY() + 0.96, pos.getZ() + random.nextDouble(),
                    (random.nextDouble() - 0.5) * 0.012, 0.015 * strength,
                    (random.nextDouble() - 0.5) * 0.012);
        }
        if (random.nextInt(3) == 0) {
            level.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.ICE.defaultBlockState()),
                    pos.getX() + random.nextDouble(), pos.getY() + 0.94,
                    pos.getZ() + random.nextDouble(), (random.nextDouble() - 0.5) * 0.008,
                    0.008 * strength, (random.nextDouble() - 0.5) * 0.008);
        }
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SpecialIceBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public boolean skipRendering(BlockState state, BlockState adjacent, Direction direction) {
        return adjacent.is(this) || super.skipRendering(state, adjacent, direction);
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }

    @Override
    public float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 1f;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos,
                                        CollisionContext context) {
        if (context instanceof EntityCollisionContext entityContext) {
            Entity entity = entityContext.getEntity();
            if (entity != null && entity.getBoundingBox().deflate(1.0E-4).intersects(new AABB(pos))) {
                return Shapes.empty();
            }
        }
        return Shapes.block();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(SOURCE, AGE, WATER_LEVEL);
    }
}
