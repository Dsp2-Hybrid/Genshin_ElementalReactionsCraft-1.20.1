package com.dsp2.genshinerc.common.core.reaction;

import com.dsp2.genshinerc.common.config.ServerConfig;
import com.dsp2.genshinerc.common.core.damage.ElementalStats;
import com.dsp2.genshinerc.common.core.element.ElementType;
import com.dsp2.genshinerc.common.registry.ModEntities;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;

public final class DendroCores {
    private DendroCores() {}

    static void spawn(LivingEntity target, @Nullable Entity owner, ElementalStats stats) {
        if (!(target.level() instanceof ServerLevel level)) return;
        List<DendroCoreEntity> near = nearby(level, target.position(), ServerConfig.CORE_LIMIT_RADIUS.get());
        near.sort(Comparator.comparingInt((DendroCoreEntity c) -> c.tickCount).reversed());
        int max = ServerConfig.CORE_MAX.get();
        while (near.size() >= max) near.remove(0).detonate(Reaction.BLOOM, null);

        DendroCoreEntity core = new DendroCoreEntity(ModEntities.DENDRO_CORE.get(), level);
        core.setPos(target.getX() + (level.random.nextDouble() - 0.5) * 0.6,
                target.getY() + target.getBbHeight() * 0.5,
                target.getZ() + (level.random.nextDouble() - 0.5) * 0.6);
        core.setOwner(owner == null ? null : owner.getUUID(), stats);
        level.addFreshEntity(core);
    }

    static void detonateNear(LivingEntity target, @Nullable Entity attacker, ElementType element) {
        if (!(target.level() instanceof ServerLevel level)) return;
        Reaction reaction = element == ElementType.ELECTRO ? Reaction.HYPERBLOOM : Reaction.BURGEON;
        for (DendroCoreEntity core : nearby(level, target.position(), ServerConfig.CORE_TRIGGER_RADIUS.get())) {
            core.detonate(reaction, attacker);
        }
    }

    private static List<DendroCoreEntity> nearby(ServerLevel level, Vec3 pos, double radius) {
        double r2 = radius * radius;
        return level.getEntitiesOfClass(DendroCoreEntity.class, new AABB(pos, pos).inflate(radius),
                c -> !c.isRemoved() && c.position().distanceToSqr(pos) <= r2);
    }
}
