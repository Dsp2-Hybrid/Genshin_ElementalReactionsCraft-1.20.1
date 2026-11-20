package com.dsp2.genshinerc.common.core.reaction;

import com.dsp2.genshinerc.common.config.ServerConfig;
import com.dsp2.genshinerc.common.core.damage.DamageKind;
import com.dsp2.genshinerc.common.core.damage.ElementalStats;
import com.dsp2.genshinerc.common.core.element.ElementType;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
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

    public DendroCoreEntity(EntityType<? extends DendroCoreEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    public void setOwner(@Nullable UUID owner, ElementalStats stats) {
        this.owner = owner;
        this.ownerLevel = stats.level();
        this.ownerMastery = Math.max(0.0, stats.mastery());
    }

    public boolean isHoming() { return homing; }

    @Override protected void defineSynchedData() {}
    @Override protected void readAdditionalSaveData(CompoundTag tag) {}
    @Override protected void addAdditionalSaveData(CompoundTag tag) {}
    @Override public boolean isPickable() { return false; }

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide()) {
            if (homing || getDeltaMovement().lengthSqr() > 0.01) {
                Vec3 velocity = getDeltaMovement();
                DustParticleOptions trail = new DustParticleOptions(new Vector3f(0.34f, 0.95f, 0.22f), 1.0f);
                for (int i = 0; i < 3; i++) {
                    level().addParticle(trail, getX(), getY(), getZ(),
                            -velocity.x * 0.22 + (random.nextDouble() - 0.5) * 0.08,
                            -velocity.y * 0.22 + (random.nextDouble() - 0.5) * 0.08,
                            -velocity.z * 0.22 + (random.nextDouble() - 0.5) * 0.08);
                }
            }
            return;
        }

        if (homing) {
            tickHoming();
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
        side = side.normalize().scale(Math.sin(tickCount * 0.36) * 0.10);
        Vec3 velocity = direction.scale(0.50).add(side);
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
        server.sendParticles(ParticleTypes.HAPPY_VILLAGER, getX(), getY(), getZ(), 12, 0.3, 0.3, 0.3, 0.03);
        discard();
    }

    public void detonate(Reaction reaction, @Nullable Entity detonator) {
        if (exploded || !(level() instanceof ServerLevel server)) return;

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

        server.sendParticles(ParticleTypes.HAPPY_VILLAGER, getX(), getY(), getZ(), 18, 0.35, 0.3, 0.35, 0.03);
        server.sendParticles(ParticleTypes.COMPOSTER, getX(), getY(), getZ(), 12, 0.3, 0.24, 0.3, 0.05);
        discard();
    }
}