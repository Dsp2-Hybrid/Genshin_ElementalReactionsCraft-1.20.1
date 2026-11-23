package com.dsp2.genshinerc.common.core.reaction;

import com.dsp2.genshinerc.common.config.ServerConfig;
import com.dsp2.genshinerc.common.core.damage.DamageKind;
import com.dsp2.genshinerc.common.core.damage.ElementalStats;
import com.dsp2.genshinerc.common.core.element.ElementType;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class DendroCoreEntity extends Entity {
    @Nullable private UUID owner;
    @Nullable private UUID homingTarget;
    private int ownerLevel = 1;
    private double ownerMastery;
    private boolean exploded;
    private boolean homing;
    private int triggerCount;
    private long lastTriggerTick = Long.MIN_VALUE;
    private int randomSeed;

    public DendroCoreEntity(EntityType<? extends DendroCoreEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    public void setOwner(@Nullable UUID owner, ElementalStats stats) {
        this.owner = owner;
        this.ownerLevel = stats.level();
        this.ownerMastery = Math.max(0.0, stats.mastery());
        this.randomSeed = level().random.nextInt();
    }

    public boolean isHoming() { return homing; }

    public int triggerCount() { return triggerCount; }

    public boolean canTrigger() {
        long now = level().getGameTime();
        return now != lastTriggerTick && triggerCount < 3;
    }

    private void markTriggered() {
        triggerCount++;
        lastTriggerTick = level().getGameTime();
    }

    @Override protected void defineSynchedData() {}

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.hasUUID("Owner")) owner = tag.getUUID("Owner");
        if (tag.hasUUID("HomingTarget")) homingTarget = tag.getUUID("HomingTarget");
        ownerLevel = Math.max(1, tag.getInt("OwnerLevel"));
        ownerMastery = Math.max(0.0, tag.getDouble("OwnerMastery"));
        exploded = tag.getBoolean("Exploded");
        homing = tag.getBoolean("Homing");
        triggerCount = Math.max(0, tag.getInt("TriggerCount"));
        lastTriggerTick = tag.getLong("LastTriggerTick");
        randomSeed = tag.getInt("RandomSeed");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (owner != null) tag.putUUID("Owner", owner);
        if (homingTarget != null) tag.putUUID("HomingTarget", homingTarget);
        tag.putInt("OwnerLevel", ownerLevel);
        tag.putDouble("OwnerMastery", ownerMastery);
        tag.putBoolean("Exploded", exploded);
        tag.putBoolean("Homing", homing);
        tag.putInt("TriggerCount", triggerCount);
        tag.putLong("LastTriggerTick", lastTriggerTick);
        tag.putInt("RandomSeed", randomSeed);
    }

    @Override public boolean isPickable() { return false; }

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide()) {
            if (homing || getDeltaMovement().lengthSqr() > 0.01) {
                Vec3 velocity = getDeltaMovement();
                DustParticleOptions trail = new DustParticleOptions(new Vector3f(0.34f, 0.95f, 0.22f), 1.0f);
                for (int i = 0; i < 9; i++) {
                    double spread = 0.035 + i * 0.014;
                    level().addParticle(trail,
                            getX() + (random.nextDouble() - 0.5) * 0.12,
                            getY() + (random.nextDouble() - 0.5) * 0.12,
                            getZ() + (random.nextDouble() - 0.5) * 0.12,
                            -velocity.x * 0.24 + (random.nextDouble() - 0.5) * spread,
                            -velocity.y * 0.24 + (random.nextDouble() - 0.5) * spread,
                            -velocity.z * 0.24 + (random.nextDouble() - 0.5) * spread);
                }
            }
            return;
        }

        if (homing) {
            tickHoming();
            return;
        }

        var currentState = level().getBlockState(blockPosition());
        var belowState = level().getBlockState(blockPosition().below());
        if (currentState.is(BlockTags.FIRE) || belowState.is(BlockTags.FIRE)
                || currentState.is(Blocks.LAVA) || belowState.is(Blocks.LAVA)
                || currentState.getFluidState().is(Fluids.LAVA)
                || belowState.getFluidState().is(Fluids.LAVA)) {
            detonate(Reaction.BURGEON, null);
            return;
        }

        if (tickCount >= ServerConfig.CORE_FUSE_TICKS.get()) detonate(Reaction.BLOOM, null);
    }

    private void tickHoming() {
        if (!(level() instanceof ServerLevel server) || homingTarget == null) {
            discard();
            return;
        }

        Entity attacker = owner == null ? null : server.getEntity(owner);
        Entity found = server.getEntity(homingTarget);
        LivingEntity target = found instanceof LivingEntity living ? living : null;
        if (target == null || !target.isAlive() || !ReactionTargets.isEnemy(attacker, target)) {
            List<LivingEntity> replacements = ReactionTargets.nearbyEnemies(
                    this, attacker, ServerConfig.HYPERBLOOM_RANGE.get());
            target = replacements.stream()
                    .min(Comparator.comparingDouble(entity -> entity.distanceToSqr(this)))
                    .orElse(null);
            if (target == null) {
                discard();
                return;
            }
            homingTarget = target.getUUID();
        }

        Vec3 destination = target.getBoundingBox().getCenter();
        Vec3 offset = destination.subtract(position());
        if (offset.lengthSqr() <= 0.36) {
            impact(target, attacker, server);
            return;
        }

        Vec3 direction = offset.normalize();
        Vec3 side = direction.cross(new Vec3(0.0, 1.0, 0.0));
        if (side.lengthSqr() < 0.001) side = direction.cross(new Vec3(1.0, 0.0, 0.0));
        side = side.normalize();
        Vec3 secondary = direction.cross(side);
        if (secondary.lengthSqr() > 0.001) secondary = secondary.normalize();
        double phase = tickCount * 0.36 + randomSeed * 0.0007;
        double noise = Math.sin(tickCount * 0.17 + getId() * 0.31 + randomSeed * 0.001) * 0.05;
        Vec3 sway = side.scale(Math.sin(phase) * (0.12 + noise))
                .add(secondary.scale(Math.cos(phase * 0.73) * 0.065))
                .add(0.0, Math.sin(phase * 0.51) * 0.05, 0.0);
        Vec3 velocity = direction.scale(0.50).add(sway);
        setDeltaMovement(velocity);
        setPos(getX() + velocity.x, getY() + velocity.y, getZ() + velocity.z);
        hasImpulse = true;
        if (tickCount > 120) discard();
    }

    private void impact(LivingEntity target, @Nullable Entity attacker, ServerLevel server) {
        ElementalStats stats = new ElementalStats(ownerLevel, ownerMastery);
        double radius = 1.0;
        AABB bounds = getBoundingBox().inflate(radius);
        for (LivingEntity hit : server.getEntitiesOfClass(LivingEntity.class, bounds,
                e -> e == target || (e.getBoundingBox().getCenter().distanceToSqr(target.getBoundingBox().getCenter()) <= radius * radius
                        && (ReactionTargets.isEnemy(attacker, e) || (attacker == e && e instanceof Player))))) {
            ReactionDamage.deal(hit, attacker, DamageKind.DENDRO, Reaction.HYPERBLOOM, stats);
            ReactionText.send(hit, Reaction.HYPERBLOOM, ElementType.ELECTRO, ElementType.DENDRO);
        }
        server.sendParticles(ParticleTypes.HAPPY_VILLAGER, getX(), getY(), getZ(), 32, 0.34, 0.34, 0.34, 0.055);
        server.sendParticles(ParticleTypes.ELECTRIC_SPARK, getX(), getY(), getZ(), 22, 0.28, 0.28, 0.28, 0.10);
        server.sendParticles(ParticleTypes.END_ROD, getX(), getY(), getZ(), 8, 0.20, 0.20, 0.20, 0.035);
        server.playSound(null, blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME,
                SoundSource.HOSTILE, 0.34f, 1.55f + server.getRandom().nextFloat() * 0.25f);
        server.playSound(null, blockPosition(), SoundEvents.NOTE_BLOCK_BELL.value(),
                SoundSource.HOSTILE, 0.18f, 1.70f + server.getRandom().nextFloat() * 0.20f);
        discard();
    }

    public void detonate(Reaction reaction, @Nullable Entity detonator) {
        if (exploded || !(level() instanceof ServerLevel server)) return;
        if (reaction == Reaction.HYPERBLOOM || reaction == Reaction.BURGEON) {
            if (!canTrigger()) return;
            markTriggered();
        }

        if (reaction == Reaction.HYPERBLOOM) {
            List<LivingEntity> candidates = ReactionTargets.nearbyEnemies(
                    this, detonator, ServerConfig.HYPERBLOOM_RANGE.get());
            LivingEntity target = candidates.stream()
                    .min(Comparator.comparingDouble(entity -> entity.distanceToSqr(this)))
                    .orElse(null);
            if (target == null) {
                discard();
                return;
            }

            exploded = true;
            homing = true;
            owner = detonator == null ? null : detonator.getUUID();
            ElementalStats stats = ElementalStats.of(detonator);
            ownerLevel = stats.level();
            ownerMastery = Math.max(0.0, stats.mastery());
            homingTarget = target.getUUID();
            tickCount = 0;
            setDeltaMovement(target.getBoundingBox().getCenter().subtract(position()).normalize().scale(0.5));
            hasImpulse = true;
            return;
        }

        exploded = true;
        Entity attacker;
        ElementalStats stats;
        if (reaction == Reaction.BLOOM) {
            attacker = owner == null ? null : server.getEntity(owner);
            stats = new ElementalStats(ownerLevel, ownerMastery);
        } else {
            attacker = detonator;
            stats = ElementalStats.of(detonator);
        }

        double radius = ServerConfig.CORE_BLAST_RADIUS.get();
        List<LivingEntity> targets = new java.util.ArrayList<>(
                ReactionTargets.nearbyEnemies(this, attacker, radius));
        if (attacker instanceof Player && attacker.distanceToSqr(this) <= radius * radius
                && !targets.contains(attacker)) targets.add((LivingEntity) attacker);

        for (LivingEntity target : targets) {
            ReactionDamage.deal(target, attacker, DamageKind.DENDRO, reaction, stats);
            if (reaction != Reaction.BLOOM) {
                ElementType trigger = reaction == Reaction.HYPERBLOOM ? ElementType.ELECTRO : ElementType.PYRO;
                ReactionText.send(target, reaction, trigger, ElementType.DENDRO);
            }
        }

        if (reaction == Reaction.BURGEON) {
            server.sendParticles(ParticleTypes.FLAME, getX(), getY(), getZ(), 28,
                    0.42, 0.35, 0.42, 0.08);
            server.sendParticles(ParticleTypes.SMOKE, getX(), getY(), getZ(), 18,
                    0.35, 0.30, 0.35, 0.035);
            server.playSound(null, blockPosition(), SoundEvents.GENERIC_EXPLODE,
                    SoundSource.HOSTILE, 0.42f, 1.25f + server.getRandom().nextFloat() * 0.30f);
        } else {
            server.sendParticles(ParticleTypes.HAPPY_VILLAGER, getX(), getY(), getZ(), 32,
                    0.38, 0.34, 0.38, 0.055);
            server.sendParticles(ParticleTypes.COMPOSTER, getX(), getY(), getZ(), 26,
                    0.34, 0.28, 0.34, 0.07);
            server.playSound(null, blockPosition(), SoundEvents.AMETHYST_BLOCK_RESONATE,
                    SoundSource.HOSTILE, 0.48f, 1.05f + server.getRandom().nextFloat() * 0.30f);
        }
        discard();
    }
}