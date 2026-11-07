package com.dsp2.genshinerc.common.core.reaction;

import com.dsp2.genshinerc.common.config.ServerConfig;
import com.dsp2.genshinerc.common.core.damage.DamageKind;
import com.dsp2.genshinerc.common.core.damage.ElementalStats;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class DendroCoreEntity extends Entity {
    @Nullable private UUID owner;
    private int ownerLevel = 1;
    private double ownerMastery;
    private boolean exploded;

    public DendroCoreEntity(EntityType<? extends DendroCoreEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    public void setOwner(@Nullable UUID owner, ElementalStats stats) {
        this.owner = owner;
        this.ownerLevel = stats.level();
        this.ownerMastery = stats.mastery();
    }

    @Override protected void defineSynchedData() {}
    @Override protected void readAdditionalSaveData(CompoundTag tag) {}
    @Override protected void addAdditionalSaveData(CompoundTag tag) {}
    @Override public boolean isPickable() { return false; }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide()) return;
        if (tickCount >= ServerConfig.CORE_FUSE_TICKS.get()) detonate(Reaction.BLOOM, null);
    }

    public void detonate(Reaction reaction, @Nullable Entity detonator) {
        if (exploded || !(level() instanceof ServerLevel server)) return;
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

        List<LivingEntity> targets;
        if (reaction == Reaction.HYPERBLOOM) {
            targets = ReactionTargets.nearbyEnemies(this, attacker, ServerConfig.HYPERBLOOM_RANGE.get()).stream()
                    .min(Comparator.comparingDouble(e -> e.distanceToSqr(this))).map(List::of).orElse(List.of());
        } else {
            targets = ReactionTargets.nearbyEnemies(this, attacker, ServerConfig.CORE_BLAST_RADIUS.get());
        }
        for (LivingEntity target : targets) {
            ReactionDamage.deal(target, attacker, DamageKind.DENDRO, reaction, stats);
            if (reaction != Reaction.BLOOM) ReactionText.send(target, reaction, null, null);
        }

        server.sendParticles(ParticleTypes.HAPPY_VILLAGER, getX(), getY(), getZ(), 12, 0.3, 0.3, 0.3, 0.02);
        discard();
    }
}
