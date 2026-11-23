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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

public class EleIceBlock extends HalfTransparentBlock {
    public static final BooleanProperty SOURCE = BooleanProperty.create("source");
    public static final IntegerProperty AGE = IntegerProperty.create("age", 0, 4);

    private static final Map<ServerLevel, Map<BlockPos, Long>> EXPIRES = new WeakHashMap<>();
    private static final Map<ServerLevel, Map<BlockPos, Long>> DURATIONS = new WeakHashMap<>();

    public EleIceBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(SOURCE, false).setValue(AGE, 0));
    }

    public static void freeze(ServerLevel level, BlockPos pos, boolean source, float units) {
        BlockPos at = pos.immutable();
        BlockState previous = level.getBlockState(at);

        if (previous.getBlock() instanceof EleIceBlock) {
            pushIntersectingEntities(level, at);
            return;
        }

        if (!previous.getFluidState().is(net.minecraft.tags.FluidTags.WATER)) return;

        long duration = Math.max(
                60L,
                ElementGauge.durationTicksFromUnits(Math.max(0.25f, units))
                        + level.random.nextInt(10));

        EXPIRES.computeIfAbsent(level, ignored -> new HashMap<>())
                .put(at, level.getGameTime() + duration);
        DURATIONS.computeIfAbsent(level, ignored -> new HashMap<>())
                .put(at, duration);

        BlockState ice = ModBlocks.SPECIAL_ICE.get()
                .defaultBlockState()
                .setValue(SOURCE, source)
                .setValue(AGE, 0);

        if (!level.setBlockAndUpdate(at, ice)) {
            removeTracking(level, at);
            return;
        }

        pushIntersectingEntities(level, at);
        schedule(level, at, duration);

        level.playSound(
                null, at, SoundEvents.GLASS_PLACE,
                SoundSource.BLOCKS, 0.78f,
                1.26f + level.random.nextFloat() * 0.16f);

        level.sendParticles(
                ParticleTypes.SNOWFLAKE,
                at.getX() + 0.5, at.getY() + 0.96, at.getZ() + 0.5,
                30, 0.36, 0.12, 0.36, 0.04);

        level.sendParticles(
                new BlockParticleOption(ParticleTypes.BLOCK, Blocks.ICE.defaultBlockState()),
                at.getX() + 0.5, at.getY() + 0.97, at.getZ() + 0.5,
                22, 0.32, 0.10, 0.32, 0.06);
    }

    private static void schedule(ServerLevel level, BlockPos pos, long delay) {
        level.scheduleTick(
                pos,
                ModBlocks.SPECIAL_ICE.get(),
                (int) Math.max(1L, Math.min(Integer.MAX_VALUE, delay)));
    }

    private static void pushIntersectingEntities(ServerLevel level, BlockPos pos) {
        AABB iceBox = new AABB(pos);

        for (Entity entity : level.getEntitiesOfClass(
                Entity.class,
                iceBox.inflate(0.30),
                candidate -> !candidate.isRemoved()
                        && candidate.getBoundingBox().intersects(iceBox))) {
            AABB box = entity.getBoundingBox();
            double up = pos.getY() + 1.001 - box.minY;

            if (up > 0.0 && up <= 2.0
                    && level.noCollision(entity, box.move(0.0, up, 0.0))) {
                entity.setPos(entity.getX(), entity.getY() + up, entity.getZ());
                entity.setDeltaMovement(
                        entity.getDeltaMovement().x * 0.3,
                        Math.max(0.0, entity.getDeltaMovement().y),
                        entity.getDeltaMovement().z * 0.3);
                entity.hasImpulse = true;
                entity.hurtMarked = true;
                continue;
            }

            double[][] offsets = {
                    {0.75, 0.0},
                    {-0.75, 0.0},
                    {0.0, 0.75},
                    {0.0, -0.75},
                    {0.55, 0.55},
                    {-0.55, 0.55},
                    {0.55, -0.55},
                    {-0.55, -0.55}
            };

            for (double[] offset : offsets) {
                AABB moved = box.move(offset[0], 0.05, offset[1]);
                if (!level.noCollision(entity, moved)) continue;

                entity.setPos(
                        entity.getX() + offset[0],
                        entity.getY() + 0.05,
                        entity.getZ() + offset[1]);
                entity.hasImpulse = true;
                entity.hurtMarked = true;
                break;
            }
        }
    }

    public static void melt(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof EleIceBlock)) return;

        boolean source = state.getValue(SOURCE);
        removeTracking(level, pos);

        level.setBlockAndUpdate(
                pos,
                source ? Blocks.WATER.defaultBlockState() : Blocks.AIR.defaultBlockState());

        level.playSound(
                null, pos, SoundEvents.FIRE_EXTINGUISH,
                SoundSource.BLOCKS, 0.56f,
                1.22f + level.random.nextFloat() * 0.14f);

        level.sendParticles(
                ParticleTypes.CLOUD,
                pos.getX() + 0.5, pos.getY() + 0.4, pos.getZ() + 0.5,
                10, 0.27, 0.22, 0.27, 0.025);

        level.sendParticles(
                ParticleTypes.FALLING_WATER,
                pos.getX() + 0.5, pos.getY() + 0.65, pos.getZ() + 0.5,
                14, 0.28, 0.20, 0.28, 0.03);
    }

    private static void removeTracking(ServerLevel level, BlockPos pos) {
        Map<BlockPos, Long> expiry = EXPIRES.get(level);
        if (expiry != null) {
            expiry.remove(pos);
            if (expiry.isEmpty()) EXPIRES.remove(level);
        }

        Map<BlockPos, Long> duration = DURATIONS.get(level);
        if (duration != null) {
            duration.remove(pos);
            if (duration.isEmpty()) DURATIONS.remove(level);
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        Map<BlockPos, Long> expiries = EXPIRES.get(level);
        long fallback = ElementGauge.durationTicksFromUnits(2f);
        long expires = expiries == null
                ? level.getGameTime() + fallback
                : expiries.getOrDefault(pos, level.getGameTime() + fallback);

        if (level.getGameTime() >= expires) {
            melt(level, pos);
            return;
        }

        pushIntersectingEntities(level, pos);

        Map<BlockPos, Long> durations = DURATIONS.get(level);
        long duration = durations == null
                ? fallback
                : durations.getOrDefault(pos, fallback);
        long elapsed = Math.max(0L, duration - (expires - level.getGameTime()));
        int age = (int) Math.min(4L, elapsed * 5L / Math.max(1L, duration));

        if (state.getValue(AGE) != age) {
            level.setBlock(pos, state.setValue(AGE, age), 3);
        }

        long remaining = expires - level.getGameTime();
        schedule(level, pos, Math.min(10L, Math.max(1L, remaining / 5L)));
    }

    @Override
    public void onPlace(
            BlockState state,
            Level level,
            BlockPos pos,
            BlockState old,
            boolean moving) {
        if (level.isClientSide || !(level instanceof ServerLevel server) || old.getBlock() == this) {
            return;
        }

        Map<BlockPos, Long> expiries = EXPIRES.get(server);
        if (expiries == null || !expiries.containsKey(pos)) {
            long duration = ElementGauge.durationTicksFromUnits(2f);
            EXPIRES.computeIfAbsent(server, ignored -> new HashMap<>())
                    .put(pos.immutable(), server.getGameTime() + duration);
            DURATIONS.computeIfAbsent(server, ignored -> new HashMap<>())
                    .put(pos.immutable(), duration);
            schedule(server, pos, duration);
        }

        pushIntersectingEntities(server, pos);
    }

    @Override
    public void animateTick(
            BlockState state,
            Level level,
            BlockPos pos,
            RandomSource random) {
        float strength = 1.0f - state.getValue(AGE) * 0.10f;

        if (random.nextInt(3) == 0) {
            level.addParticle(
                    ParticleTypes.SNOWFLAKE,
                    pos.getX() + random.nextDouble(),
                    pos.getY() + 0.96,
                    pos.getZ() + random.nextDouble(),
                    (random.nextDouble() - 0.5) * 0.012,
                    0.015 * strength,
                    (random.nextDouble() - 0.5) * 0.012);
        }

        if (random.nextInt(3) == 0) {
            level.addParticle(
                    new BlockParticleOption(ParticleTypes.BLOCK, Blocks.ICE.defaultBlockState()),
                    pos.getX() + random.nextDouble(),
                    pos.getY() + 0.94,
                    pos.getZ() + random.nextDouble(),
                    (random.nextDouble() - 0.5) * 0.008,
                    0.008 * strength,
                    (random.nextDouble() - 0.5) * 0.008);
        }
    }

    @Override
    public VoxelShape getCollisionShape(
            BlockState state,
            BlockGetter level,
            BlockPos pos,
            CollisionContext context) {
        return Shapes.block();
    }

    @Override
    protected void createBlockStateDefinition(
            StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(SOURCE, AGE);
    }
}