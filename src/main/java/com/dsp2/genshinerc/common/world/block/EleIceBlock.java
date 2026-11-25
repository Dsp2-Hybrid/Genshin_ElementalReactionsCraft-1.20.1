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

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

public class EleIceBlock extends BaseEntityBlock {
    public static final BooleanProperty SOURCE = BooleanProperty.create("source");
    public static final IntegerProperty AGE = IntegerProperty.create("age", 0, 40);
    private static final Map<ServerLevel, Map<BlockPos, Long>> EXPIRES = new WeakHashMap<>();
    private static final Map<ServerLevel, Map<BlockPos, Long>> DURATIONS = new WeakHashMap<>();
    private static final Map<ServerLevel, Map<BlockPos, Long>> LAST_TICK = new WeakHashMap<>();

    public EleIceBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(SOURCE, false).setValue(AGE, 0));
    }

    public static void freeze(ServerLevel level, BlockPos pos, boolean source, float units) {
        BlockPos at = pos.immutable();
        BlockState previous = level.getBlockState(at);
        if (previous.getBlock() instanceof EleIceBlock) {
            IceEntityResolver.resolveAround(level, at);
            return;
        }

        FluidState fluid = previous.getFluidState();
        if (!fluid.is(net.minecraft.tags.FluidTags.WATER)) return;

        boolean isSource = source || fluid.isSource();
        int waterLevel = isSource ? 0 : Math.max(1, Math.min(7, 8 - fluid.getAmount()));
        long duration = Math.max(60L,
                ElementGauge.durationTicksFromUnits(Math.max(0.25f, units)) + level.random.nextInt(10));
        long start = level.getGameTime();
        long expires = start + duration;
        int densityTier = Math.max(0, (int) Math.floor(Math.max(0f, units) / 0.5f));

        EXPIRES.computeIfAbsent(level, ignored -> new HashMap<>()).put(at, expires);
        DURATIONS.computeIfAbsent(level, ignored -> new HashMap<>()).put(at, duration);
        LAST_TICK.computeIfAbsent(level, ignored -> new HashMap<>()).put(at, start);

        BlockState ice = ModBlocks.SPECIAL_ICE.get().defaultBlockState()
                .setValue(SOURCE, isSource).setValue(AGE, 0);
        if (!level.setBlockAndUpdate(at, ice)) {
            removeTracking(level, at);
            return;
        }

        if (level.getBlockEntity(at) instanceof SpecialIceBlockEntity be) {
            be.setFreezeData(start, expires, level.random.nextLong(), densityTier, waterLevel);
        }

        IceEntityResolver.resolveAround(level, at);
        schedule(level, at, duration);
        level.playSound(null, at, SoundEvents.GLASS_PLACE, SoundSource.BLOCKS,
                0.78f, 1.26f + level.random.nextFloat() * 0.16f);
        level.sendParticles(ParticleTypes.SNOWFLAKE, at.getX() + 0.5, at.getY() + 0.96, at.getZ() + 0.5,
                30, 0.36, 0.12, 0.36, 0.04);
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.ICE.defaultBlockState()),
                at.getX() + 0.5, at.getY() + 0.97, at.getZ() + 0.5, 22, 0.32, 0.10, 0.32, 0.06);
    }

    private static void schedule(ServerLevel level, BlockPos pos, long delay) {
        level.scheduleTick(pos, ModBlocks.SPECIAL_ICE.get(),
                (int) Math.max(1L, Math.min(Integer.MAX_VALUE, delay)));
    }

    public static void melt(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof EleIceBlock)) return;

        boolean source = state.getValue(SOURCE);
        SpecialIceBlockEntity be = level.getBlockEntity(pos) instanceof SpecialIceBlockEntity blockEntity
                ? blockEntity : null;
        int waterLevel = be == null ? (source ? 0 : 1) : be.getWaterLevel();
        removeTracking(level, pos);

        BlockState water = source
                ? Blocks.WATER.defaultBlockState()
                : Blocks.WATER.defaultBlockState().setValue(LiquidBlock.LEVEL, Math.max(1, waterLevel));
        level.setBlockAndUpdate(pos, water);
        level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS,
                0.56f, 1.22f + level.random.nextFloat() * 0.14f);
        level.sendParticles(ParticleTypes.CLOUD, pos.getX() + 0.5, pos.getY() + 0.4, pos.getZ() + 0.5,
                10, 0.27, 0.22, 0.27, 0.025);
        level.sendParticles(ParticleTypes.FALLING_WATER, pos.getX() + 0.5, pos.getY() + 0.65, pos.getZ() + 0.5,
                14, 0.28, 0.20, 0.28, 0.03);
    }

    private static void removeTracking(ServerLevel level, BlockPos pos) {
        remove(level, pos, EXPIRES);
        remove(level, pos, DURATIONS);
        remove(level, pos, LAST_TICK);
    }

    private static void remove(ServerLevel level, BlockPos pos, Map<ServerLevel, Map<BlockPos, Long>> tracking) {
        Map<BlockPos, Long> values = tracking.get(level);
        if (values == null) return;
        values.remove(pos);
        if (values.isEmpty()) tracking.remove(level);
    }

    private static boolean isFire(ServerLevel level, BlockPos pos) {
        return level.hasChunkAt(pos) && level.getBlockState(pos).is(net.minecraft.tags.BlockTags.FIRE);
    }

    private static boolean isLava(ServerLevel level, BlockPos pos) {
        if (!level.hasChunkAt(pos)) return false;
        return level.getFluidState(pos).is(Fluids.LAVA) || level.getBlockState(pos).is(Blocks.LAVA);
    }

    private static boolean hasAdjacentLava(ServerLevel level, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            if (isLava(level, pos.relative(direction))) return true;
        }
        return false;
    }

    private static boolean hasHeatWithinTwo(ServerLevel level, BlockPos pos) {
        for (BlockPos test : BlockPos.withinManhattan(pos, 2, 2, 2)) {
            if (pos.distManhattan(test) == 2 && (isLava(level, test) || isFire(level, test))) return true;
        }
        return false;
    }

    private static boolean hasAdjacentFire(ServerLevel level, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            if (isFire(level, pos.relative(direction))) return true;
        }
        return false;
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        SpecialIceBlockEntity be = level.getBlockEntity(pos) instanceof SpecialIceBlockEntity blockEntity
                ? blockEntity : null;
        long now = level.getGameTime();
        long fallback = ElementGauge.durationTicksFromUnits(2f);
        long expires = be != null && be.getExpiresAt() > 0L
                ? be.getExpiresAt()
                : EXPIRES.getOrDefault(level, Map.of()).getOrDefault(pos, now + fallback);

        if (isFire(level, pos.above())) {
            level.removeBlock(pos.above(), false);
            melt(level, pos);
            return;
        }
        if (hasAdjacentLava(level, pos)) {
            melt(level, pos);
            return;
        }

        long last = LAST_TICK.computeIfAbsent(level, ignored -> new HashMap<>()).getOrDefault(pos, now);
        long elapsed = Math.max(0L, now - last);
        long multiplier = hasHeatWithinTwo(level, pos) || hasAdjacentFire(level, pos) ? 2L : 1L;
        expires -= elapsed * multiplier;
        LAST_TICK.get(level).put(pos.immutable(), now);
        EXPIRES.computeIfAbsent(level, ignored -> new HashMap<>()).put(pos.immutable(), expires);
        if (be != null) be.setExpiresAt(expires);

        if (now >= expires) {
            melt(level, pos);
            return;
        }

        IceEntityResolver.resolveAround(level, pos);
        long duration = be != null && be.getDuration() > 0L
                ? be.getDuration()
                : DURATIONS.getOrDefault(level, Map.of()).getOrDefault(pos, fallback);
        long elapsedLife = Math.max(0L, duration - Math.max(0L, expires - now));
        int age = (int) Math.min(40L, elapsedLife * 40L / Math.max(1L, duration));
        if (state.getValue(AGE) != age) level.setBlock(pos, state.setValue(AGE, age), 3);

        long remaining = expires - now;
        schedule(level, pos, Math.min(4L, Math.max(1L, remaining / Math.max(1L, 41L - age))));
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState old, boolean moving) {
        if (level.isClientSide || !(level instanceof ServerLevel server) || old.getBlock() == this) return;

        long duration = ElementGauge.durationTicksFromUnits(2f);
        long start = server.getGameTime();
        long expires = start + duration;
        Map<BlockPos, Long> expiries = EXPIRES.computeIfAbsent(server, ignored -> new HashMap<>());
        Map<BlockPos, Long> durations = DURATIONS.computeIfAbsent(server, ignored -> new HashMap<>());
        LAST_TICK.computeIfAbsent(server, ignored -> new HashMap<>()).put(pos.immutable(), start);

        if (!expiries.containsKey(pos)) {
            expiries.put(pos.immutable(), expires);
            durations.put(pos.immutable(), duration);
            schedule(server, pos, duration);
        } else {
            expires = expiries.get(pos);
            duration = durations.getOrDefault(pos, duration);
            start = expires - duration;
        }

        if (server.getBlockEntity(pos) instanceof SpecialIceBlockEntity be) {
            if (be.getExpiresAt() <= 0L) {
                be.setFreezeData(start, expires, server.random.nextLong(), 4, 0);
            } else {
                be.setLifetime(start, expires);
            }
        }
        IceEntityResolver.resolveAround(server, pos);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        float strength = 1.0f - state.getValue(AGE) * 0.10f;
        if (random.nextInt(3) == 0) {
            level.addParticle(ParticleTypes.SNOWFLAKE, pos.getX() + random.nextDouble(), pos.getY() + 0.96,
                    pos.getZ() + random.nextDouble(), (random.nextDouble() - 0.5) * 0.012,
                    0.015 * strength, (random.nextDouble() - 0.5) * 0.012);
        }
        if (random.nextInt(3) == 0) {
            level.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.ICE.defaultBlockState()),
                    pos.getX() + random.nextDouble(), pos.getY() + 0.94, pos.getZ() + random.nextDouble(),
                    (random.nextDouble() - 0.5) * 0.008, 0.008 * strength,
                    (random.nextDouble() - 0.5) * 0.008);
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
        return 1.0f;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
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
        builder.add(SOURCE, AGE);
    }
}
