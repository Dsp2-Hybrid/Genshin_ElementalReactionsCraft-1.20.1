package com.dsp2.genshinerc.common.core.combat;

import com.dsp2.genshinerc.common.core.combat.shield.ShieldManager;
import com.dsp2.genshinerc.common.core.element.ElementType;
import com.dsp2.genshinerc.common.core.element.FrozenControl;
import com.dsp2.genshinerc.common.core.reaction.ElementReactions;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "genshinerc")
public final class FinalCombatHooks {
    private static final double LIGHTNING_RADIUS = 2.5;

    private FinalCombatHooks() {}

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onExplosion(ExplosionEvent.Detonate event) {
        if (event.getLevel().isClientSide()) {
            return;
        }

        for (Entity entity : event.getAffectedEntities()) {
            if (!(entity instanceof LivingEntity living)) {
                continue;
            }

            if (!living.isAlive() || living.isSpectator()) {
                continue;
            }

            ElementReactions.trigger(
                    living,
                    null,
                    ElementType.PYRO,
                    1.5f);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLightning(LivingAttackEvent event) {
        if (!(event.getSource().getDirectEntity() instanceof LightningBolt)) {
            return;
        }

        if (event.getEntity().level().isClientSide()) {
            return;
        }

        LivingEntity target = event.getEntity();

        ElementReactions.trigger(
                target,
                null,
                ElementType.ELECTRO,
                1.0f);

        Vec3 center = target.getBoundingBox().getCenter();

        for (LivingEntity nearby : target.level().getEntitiesOfClass(
                LivingEntity.class,
                target.getBoundingBox().inflate(LIGHTNING_RADIUS),
                entity -> entity != target
                        && entity.isAlive()
                        && !entity.isSpectator()
                        && entity.getBoundingBox().getCenter().distanceToSqr(center)
                        <= LIGHTNING_RADIUS * LIGHTNING_RADIUS)) {
            ElementReactions.trigger(
                    nearby,
                    null,
                    ElementType.ELECTRO,
                    0.6f);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onKnockBack(LivingKnockBackEvent event) {
        LivingEntity entity = event.getEntity();

        if (!ShieldManager.protectsFromImpact(entity)) {
            return;
        }

        event.setCanceled(true);

        Vec3 velocity = entity.getDeltaMovement();
        entity.setDeltaMovement(
                velocity.x * 0.08,
                velocity.y,
                velocity.z * 0.08);
        entity.hurtMarked = true;
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onFall(LivingFallEvent event) {
        LivingEntity entity = event.getEntity();

        if (!FrozenControl.active(entity)) {
            return;
        }

        float threshold = Math.max(
                4.0f,
                entity.getMaxHealth() * 0.5f);

        if (event.getDistance() < threshold) {
            return;
        }

        ElementReactions.blunt(entity, null);
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onFrozenWaterTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();

        if (entity.level().isClientSide()) {
            return;
        }

        if (!FrozenControl.active(entity)) {
            return;
        }

        if (!entity.isInWater() && !entity.isUnderWater()) {
            return;
        }

        Vec3 velocity = entity.getDeltaMovement();

        entity.setDeltaMovement(
                velocity.x * 0.30,
                0.0,
                velocity.z * 0.30);
        entity.hasImpulse = true;
        entity.hurtMarked = true;
    }
}