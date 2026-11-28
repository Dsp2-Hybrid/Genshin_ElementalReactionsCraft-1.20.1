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
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.state.BlockState;
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

    private static void snd(ServerLevel level, BlockPos pos, SoundEvent sound, SoundSource source, float volume, float pitch) {
        level.playSound(null, pos, sound, source,
                volume * (0.9f + level.random.nextFloat() * 0.2f),
                pitch * (0.93f + level.random.nextFloat() * 0.14f));
    }

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
                emitAmbient(level, zone, now);
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

                snd(level, zone.origin, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.25f, 1.42f);
                snd(level, zone.origin, SoundEvents.NOTE_BLOCK_HAT.value(), SoundSource.PLAYERS, 0.14f, 1.72f);
                DustParticleOptions electro = dust(0xCC66FF, 0.95f);
                DustParticleOptions hydro = dust(0x27C8FF, 0.75f);
                for (int i = 0; i < 12; i++) {
                    double angle = level.random.nextDouble() * Math.PI * 2.0;
                    double distance = 0.35 + level.random.nextDouble() * WATER_RADIUS;
                    double x = center.x + Math.cos(angle) * distance;
                    double z = center.z + Math.sin(angle) * distance;
                    double y = center.y + level.random.nextDouble() * 0.7;
                    level.sendParticles(i % 2 == 0 ? electro : hydro, x, y, z, 1, 0.06, 0.05, 0.06, 0.015);
                    if (i % 3 == 0) {
                        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, x, y, z, 1, 0.02, 0.05, 0.02, 0.02);
                    }
                }
            }

            if (zones.isEmpty()) worlds.remove();
        }
    }

    private static void emitAmbient(ServerLevel level, WaterZone zone, long now) {
        long remaining = Math.max(0L, zone.expires - now);
        double fade = Math.min(1.0, remaining / 16.0);
        if (fade <= 0.0) return;

        Vec3 center = Vec3.atCenterOf(zone.origin);
        int count = Math.max(1, (int) Math.ceil(10.0 * fade));
        DustParticleOptions electro = dust(0xCC66FF, 0.78f);
        DustParticleOptions hydro = dust(0x27C8FF, 0.66f);

        if (level.random.nextInt(7) == 0) {
            snd(level, zone.origin,
                    level.random.nextBoolean() ? SoundEvents.CONDUIT_AMBIENT_SHORT : SoundEvents.REDSTONE_TORCH_BURNOUT,
                    SoundSource.PLAYERS, 0.22f, 1.5f);
        }

        for (int i = 0; i < count; i++) {
            double angle = level.random.nextDouble() * Math.PI * 2.0;
            double radius = Math.sqrt(level.random.nextDouble()) * WATER_RADIUS;
            double x = center.x + Math.cos(angle) * radius;
            double z = center.z + Math.sin(angle) * radius;
            double y = center.y + 0.12 + level.random.nextDouble() * 0.45;

            level.sendParticles(i % 2 == 0 ? electro : hydro, x, y, z, 1, 0.025, 0.018, 0.025, 0.006);
            if (i % 3 == 0) {
                level.sendParticles(ParticleTypes.ELECTRIC_SPARK, x, y, z, 1, 0.015, 0.04, 0.015, 0.018);
            }
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

    public static void applyLightningToWorld(ServerLevel level, Vec3 at, double radius, @Nullable Entity cause) {
        LivingEntity living = cause instanceof LivingEntity l ? l : null;
        int reach = (int) Math.ceil(radius);
        BlockPos origin = BlockPos.containing(at);
        boolean superconducted = false;
        boolean overloaded = false;
        for (BlockPos cursor : BlockPos.betweenClosed(
                origin.offset(-reach, -reach, -reach), origin.offset(reach, reach, reach))) {
            BlockPos pos = cursor.immutable();
            if (!level.isLoaded(pos) || Vec3.atCenterOf(pos).distanceToSqr(at) > radius * radius) continue;
            BlockState state = level.getBlockState(pos);
            if (state.isAir()) continue;
            boolean fire = state.is(BlockTags.FIRE);
            if (fire || state.getFluidState().is(FluidTags.LAVA)) {
                if (!overloaded) {
                    overloaded = true;
                    ElementReactions.overloadAt(level, living, Vec3.atCenterOf(pos));
                }
                if (fire) level.removeBlock(pos, false);
                continue;
            }
            if (electro(level, pos, state, living, !superconducted)) superconducted = true;
        }
    }

    @Nullable
    private static ElementType sourceElement(BlockState state) {
        if (state.is(BlockTags.FIRE)) return ElementType.PYRO;
        if (state.getFluidState().is(FluidTags.LAVA)) return ElementType.PYRO;
        if (state.getFluidState().is(FluidTags.WATER)) return ElementType.HYDRO;
        if (state.is(Blocks.POWDER_SNOW) || state.is(Blocks.SNOW) || state.is(Blocks.SNOW_BLOCK)) return ElementType.CRYO;
        return null;
    }

    private static boolean electro(ServerLevel level, BlockPos pos, BlockState state,
                                   @Nullable LivingEntity attacker, boolean allowSuperconduct) {
        boolean reacted = false;
        if (state.getFluidState().is(FluidTags.WATER)) startElectroWaterZone(level, pos, attacker);
        boolean powder = state.is(Blocks.POWDER_SNOW);
        boolean loose = state.is(Blocks.SNOW) || state.is(Blocks.SNOW_BLOCK);
        if ((powder || loose) && allowSuperconduct) {
            Vec3 at = Vec3.atCenterOf(pos);
            ElementReactions.probeSuperconduct(level, attacker, at);
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, at.x, at.y, at.z, 14, 0.3, 0.28, 0.3, 0.08);
            level.sendParticles(ParticleTypes.SNOWFLAKE, at.x, at.y, at.z, 10, 0.3, 0.25, 0.3, 0.05);
            snd(level, pos, SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.BLOCKS, 0.32f, 1.85f);
            reacted = true;
        }
        if (loose) level.removeBlock(pos, false);
        return reacted;
    }

    private static boolean swirlBlock(ServerLevel level, BlockPos pos, BlockState state,
                                      @Nullable Entity attacker, float units) {
        ElementType swirled = sourceElement(state);
        if (swirled == null) return false;
        boolean fire = state.is(BlockTags.FIRE);
        boolean destructible = fire || state.is(Blocks.SNOW) || state.is(Blocks.SNOW_BLOCK);
        if (fire) spreadFire(level, pos, units);
        if (destructible) level.removeBlock(pos, false);

        double radius = Math.min(3.0, ServerConfig.SWIRL_RADIUS.get());
        float spread = Math.max(0.5f, Math.min(ServerConfig.SWIRL_SPREAD_UNITS.get().floatValue(), units));
        Vec3 at = Vec3.atCenterOf(pos);
        ReactionParticles.spawnSwirlAt(level, at, swirled, radius);
        LivingEntity livingAttacker = attacker instanceof LivingEntity la ? la : null;
        if (livingAttacker != null) {
            ReactionSoundEvents.playSwirl(livingAttacker, swirled);
        } else {
            snd(level, pos, SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.BLOCKS, 0.5f, 1.3f);
        }

        int reach = (int) Math.ceil(radius);
        for (BlockPos cursor : BlockPos.betweenClosed(
                pos.offset(-reach, -reach, -reach), pos.offset(reach, reach, reach))) {
            BlockPos p = cursor.immutable();
            if (p.equals(pos) || !level.isLoaded(p) || Vec3.atCenterOf(p).distanceToSqr(at) > radius * radius) continue;
            BlockState other = level.getBlockState(p);
            if (other.isAir()) continue;
            ElementBlockProbe.apply(level, p, other, attacker, swirled, spread);
        }

        double radiusSq = radius * radius;
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class,
                new AABB(at, at).inflate(radius),
                e -> e.isAlive() && !e.isSpectator()
                        && e.getBoundingBox().getCenter().distanceToSqr(at) <= radiusSq)) {
            boolean isAttacker = target == attacker;
            if (!isAttacker && !ReactionTargets.isEnemy(attacker, target)) continue;
            double distance = target.getBoundingBox().getCenter().distanceTo(at);
            float falloff = (float) Math.max(0.5, 1.0 - 0.5 * Math.min(1.0, distance / Math.max(0.01, radius)));
            float appliedUnits = spread * falloff;
            if (isAttacker) {
                com.dsp2.genshinerc.common.core.element.ElementAuraManager.applyUnits(target, swirled, appliedUnits * 0.5f);
                ReactionParticles.spawnSwirl(target, swirled, Math.min(1.0, radius * 0.35));
                continue;
            }
            ElementReactions.triggerProbeFalloff(target, attacker, swirled, appliedUnits, falloff);
        }
        return true;
    }

    private static void applyToBlock(ServerLevel level, BlockPos pos, BlockState state,
                                     @Nullable Entity attacker, ElementType element, float units) {
        if (element == ElementType.ANEMO) {
            if (!swirlBlock(level, pos, state, attacker, units)) {
                ElementType fallback = sourceElement(state);
                if (fallback == null) ElementBlockProbe.apply(level, pos, state, attacker, element, units);
            }
            return;
        }
        ElementBlockProbe.apply(level, pos, state, attacker, element, units);
        if (element == ElementType.ELECTRO) electro(level, pos, state, attacker instanceof LivingEntity l ? l : null, true);
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

    private static DustParticleOptions dust(int rgb, float scale) {
        return new DustParticleOptions(new Vector3f(
                ((rgb >>> 16) & 255) / 255f,
                ((rgb >>> 8) & 255) / 255f,
                (rgb & 255) / 255f), scale);
    }
}