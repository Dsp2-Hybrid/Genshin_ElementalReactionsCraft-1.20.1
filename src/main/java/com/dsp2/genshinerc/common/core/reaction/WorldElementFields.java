package com.dsp2.genshinerc.common.core.reaction;

import com.dsp2.genshinerc.common.config.ServerConfig;
import com.dsp2.genshinerc.common.core.damage.ElementalStats;
import com.dsp2.genshinerc.common.core.element.ElementAuraManager;
import com.dsp2.genshinerc.common.core.element.ElementType;
import com.dsp2.genshinerc.common.world.block.EleIceBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public final class WorldElementFields {
    private static final double WATER_RADIUS = 2.5;
    private static final long WATER_DURATION = 60L;
    private static final long WATER_PERIOD = 20L;
    private static final Map<ServerLevel, List<WaterZone>> WATER_ZONES = new WeakHashMap<>();

    private static final class WaterZone {
        final BlockPos origin;
        @Nullable Entity owner;
        ElementalStats stats;
        long expires;
        long nextPulse;

        WaterZone(BlockPos origin, @Nullable Entity owner, long now) {
            this.origin = origin.immutable();
            this.owner = owner;
            this.stats = ElementalStats.of(owner);
            this.expires = now + WATER_DURATION;
            this.nextPulse = now;
        }
    }

    private WorldElementFields() {}

    public static void startElectroWaterZone(ServerLevel level, BlockPos pos, @Nullable Entity attacker) {
        long now = level.getGameTime();
        List<WaterZone> zones = WATER_ZONES.computeIfAbsent(level, ignored -> new ArrayList<>());
        for (WaterZone zone : zones) {
            if (zone.origin.distSqr(pos) <= 2.25) {
                if (attacker != null) {
                    zone.owner = attacker;
                    zone.stats = ElementalStats.of(attacker);
                }
                zone.expires = now + WATER_DURATION;
                return;
            }
        }
        zones.add(new WaterZone(pos, attacker, now));
    }

    public static void tick() {
        Iterator<Map.Entry<ServerLevel, List<WaterZone>>> worlds = WATER_ZONES.entrySet().iterator();
        while (worlds.hasNext()) {
            Map.Entry<ServerLevel, List<WaterZone>> entry = worlds.next();
            ServerLevel level = entry.getKey();
            List<WaterZone> zones = entry.getValue();
            long now = level.getGameTime();
            Iterator<WaterZone> iterator = zones.iterator();

            while (iterator.hasNext()) {
                WaterZone zone = iterator.next();
                if (now >= zone.expires) {
                    iterator.remove();
                    continue;
                }
                if (now < zone.nextPulse) continue;
                zone.nextPulse = now + WATER_PERIOD;

                Entity owner = zone.owner;
                if (owner != null && (owner.isRemoved() || owner.level() != level)) owner = null;
                ElementalStats stats = owner == null ? zone.stats : ElementalStats.of(owner);
                Vec3 center = Vec3.atCenterOf(zone.origin);
                double radiusSquared = WATER_RADIUS * WATER_RADIUS;
                AABB bounds = new AABB(center, center).inflate(WATER_RADIUS);

                for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, bounds,
                        entity -> entity.isAlive() && !entity.isSpectator()
                                && entity.getBoundingBox().getCenter().distanceToSqr(center) <= radiusSquared
                                && (entity.isInWater() || entity.isUnderWater()
                                || ElementAuraManager.hasAura(entity, ElementType.HYDRO)))) {
                    ReactionParticles.spawnElectroArea(target, WATER_RADIUS);
                    ReactionSoundEvents.playElectroCharged(target);
                    ReactionDamage.dealElectroCharged(target, owner, stats, owner == null);
                }

                level.playSound(null, zone.origin, SoundEvents.AMETHYST_BLOCK_CHIME,
                        SoundSource.PLAYERS, 0.25f, 1.42f + level.random.nextFloat() * 0.24f);
                level.playSound(null, zone.origin, SoundEvents.NOTE_BLOCK_HAT,
                        SoundSource.PLAYERS, 0.14f, 1.72f + level.random.nextFloat() * 0.22f);
                DustParticleOptions electro = dust(0xCC66FF, 0.95f);
                DustParticleOptions hydro = dust(0x27C8FF, 0.75f);
                for (int i = 0; i < 12; i++) {
                    double angle = level.random.nextDouble() * Math.PI * 2.0;
                    double distance = 0.35 + level.random.nextDouble() * WATER_RADIUS;
                    double x = center.x + Math.cos(angle) * distance;
                    double z = center.z + Math.sin(angle) * distance;
                    double y = center.y + level.random.nextDouble() * 0.7;
                    level.sendParticles(i % 2 == 0 ? electro : hydro, x, y, z,
                            1, 0.06, 0.05, 0.06, 0.015);
                    if (i % 3 == 0) {
                        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, x, y, z, 1,
                                0.02, 0.05, 0.02, 0.02);
                    }
                }
            }

            if (zones.isEmpty()) worlds.remove();
        }
    }

    public static void applySwirlToWorld(LivingEntity center, @Nullable Entity attacker,
                                         ElementType element, float units, double radius) {
        if (!(center.level() instanceof ServerLevel level) || radius <= 0.0) return;
        int reach = Math.max(1, (int) Math.ceil(Math.min(radius, 6.0)));
        BlockPos origin = center.blockPosition();
        double radiusSquared = radius * radius;

        for (BlockPos cursor : BlockPos.betweenClosed(
                origin.offset(-reach, -reach, -reach), origin.offset(reach, reach, reach))) {
            BlockPos pos = cursor.immutable();
            if (!level.isLoaded(pos)
                    || Vec3.atCenterOf(pos).distanceToSqr(center.getBoundingBox().getCenter()) > radiusSquared) continue;
            applyToBlock(level, pos, level.getBlockState(pos), attacker, element, units);
        }
    }

    public static void applyProbeToBlock(ServerLevel level, BlockPos pos, @Nullable Entity attacker,
                                         ElementType element, float units) {
        if (level.isLoaded(pos)) applyToBlock(level, pos.immutable(), level.getBlockState(pos), attacker, element, units);
    }

    private static void applyToBlock(ServerLevel level, BlockPos pos, BlockState state,
                                     @Nullable Entity attacker, ElementType element, float units) {
        boolean fire = state.is(net.minecraft.tags.BlockTags.FIRE);
        switch (element) {
            case ELECTRO -> {
                if (state.getFluidState().is(Fluids.WATER)) startElectroWaterZone(level, pos, attacker);
            }
            case CRYO, FROZEN -> {
                if (state.is(Blocks.WATER) && ServerConfig.CRYO_FREEZE_BLOCKS.get()) {
                    EleIceBlock.freeze(level, pos, state.getFluidState().isSource(), Math.max(0.1f, units));
                }
            }
            case GEO -> {
                if (fire && level.removeBlock(pos, false)) {
                    if (attacker instanceof LivingEntity living) {
                        ElementReactions.probeCrystallize(level, living, Vec3.atCenterOf(pos), ElementType.PYRO);
                    }
                    level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.55f, 1.15f);
                    level.sendParticles(ParticleTypes.END_ROD, pos.getX() + 0.5, pos.getY() + 0.45,
                            pos.getZ() + 0.5, 8, 0.25, 0.2, 0.25, 0.03);
                }
            }
            case HYDRO -> {
                if (fire && level.removeBlock(pos, false)) {
                    level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.6f, 1.2f);
                    level.sendParticles(ParticleTypes.CLOUD, pos.getX() + 0.5, pos.getY() + 0.4,
                            pos.getZ() + 0.5, 7, 0.2, 0.16, 0.2, 0.025);
                }
            }
            case PYRO -> {
                if (state.getBlock() instanceof CropBlock || state.is(net.minecraft.tags.BlockTags.FLOWERS)) {
                    ignite(level, pos, state);
                }
            }
            case ANEMO -> {
                if (fire) spreadFire(level, pos, units);
            }
            case DENDRO -> {
                if (state.getBlock() instanceof CropBlock crop) {
                    crop.randomTick(state, level, pos, level.random);
                    level.sendParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5,
                            pos.getY() + 0.55, pos.getZ() + 0.5, 2, 0.15, 0.15, 0.15, 0.01);
                }
            }
            default -> { }
        }
    }

    private static void spreadFire(ServerLevel level, BlockPos source, float units) {
        level.sendParticles(ParticleTypes.SWEEP_ATTACK, source.getX() + 0.5, source.getY() + 0.5,
                source.getZ() + 0.5, 2, 0.15, 0.12, 0.15, 0);
        int placed = 0;
        int limit = Math.min(3, Math.max(1, (int) Math.ceil(units)));
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            if (placed >= limit) break;
            BlockPos target = source.relative(direction);
            if (!level.isLoaded(target) || !level.getBlockState(target).isAir()) continue;
            BlockState support = level.getBlockState(target.below());
            if (((FireBlock) Blocks.FIRE).getBurnOdds(support) <= 0) continue;
            BlockState fire = BaseFireBlock.getState(level, target);
            if (fire.canSurvive(level, target)) {
                level.setBlockAndUpdate(target, fire);
                placed++;
            }
        }
    }

    private static void ignite(ServerLevel level, BlockPos pos, BlockState state) {
        BlockPos target = state.canBeReplaced() ? pos : pos.above();
        if (!level.isLoaded(target) || !level.getBlockState(target).isAir()) return;
        BlockState fire = BaseFireBlock.getState(level, target);
        if (fire.canSurvive(level, target)) level.setBlockAndUpdate(target, fire);
    }

    private static DustParticleOptions dust(int rgb, float scale) {
        return new DustParticleOptions(new Vector3f(
                ((rgb >>> 16) & 255) / 255f,
                ((rgb >>> 8) & 255) / 255f,
                (rgb & 255) / 255f), scale);
    }
}