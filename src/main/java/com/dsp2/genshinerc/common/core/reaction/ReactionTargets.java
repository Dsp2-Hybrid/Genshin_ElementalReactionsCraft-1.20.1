package com.dsp2.genshinerc.common.core.reaction;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class ReactionTargets {
    private ReactionTargets() {}

    public static boolean isEnemy(@Nullable Entity attacker, LivingEntity target) {
        if (!target.isAlive() || target.isSpectator() || target instanceof ArmorStand) return false;
        if (attacker == null) return !(target instanceof Player);
        if (target == attacker || attacker.isAlliedTo(target)) return false;
        if (attacker instanceof Player && target instanceof Player) return false;
        return !(target instanceof TamableAnimal pet && attacker instanceof LivingEntity owner && pet.isOwnedBy(owner));
    }

    public static List<LivingEntity> nearbyEnemies(Entity center, @Nullable Entity attacker, double radius) {
        double r2 = radius * radius;
        return center.level().getEntitiesOfClass(LivingEntity.class, center.getBoundingBox().inflate(radius),
                e -> e != center && e.distanceToSqr(center) <= r2 && isEnemy(attacker, e));
    }
}
