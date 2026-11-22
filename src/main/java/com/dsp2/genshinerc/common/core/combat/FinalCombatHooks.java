package com.dsp2.genshinerc.common.core.combat;

import com.dsp2.genshinerc.common.core.combat.shield.ShieldManager;
import com.dsp2.genshinerc.common.core.element.ElementReactions;
import com.dsp2.genshinerc.common.core.element.ElementType;
import com.dsp2.genshinerc.common.core.element.FrozenControl;
import com.dsp2.genshinerc.common.core.reaction.ReactionDamage;
import com.dsp2.genshinerc.common.level.EntityLevelManager;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "genshinerc")
public final class FinalCombatHooks {
    private static final float STATUS_LEVEL_STEP = 0.035f;
    private static final float STATUS_LEVEL_LIMIT = 6.0f;
    private static final double FROZEN_WATER_Y_LOCK = 0.0;
    private static final double FROZEN_WATER_HORIZONTAL_DAMP = 0.42;
    private static final double LIGHTNING_RADIUS = 2.5;
    private static final float LIGHTNING_CENTER_UNITS = 1.0f;
    private static final float LIGHTNING_NEAR_UNITS = 0.6f;

    private FinalCombatHooks() {}

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onExplosion(ExplosionEvent.Detonate event) {
        if (event.getLevel().isClientSide()) return;

        int size = event.getAffectedEntities().size();
        float units = Math.max(1.0f, Math.min(4.0f, 1.0f + size * 0.12f));

        for (Entity entity : event.getAffectedEntities()) {
            if (!(entity instanceof LivingEntity living)) continue;
            if (!living.isAlive() || living.isSpectator()) continue;
            ElementReactions.trigger(living, null, ElementType.PYRO, units);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLightningHit(LivingAttackEvent event) {
        if (!(event.getSource().getDirectEntity() instanceof LightningBolt)) return;
        if (event.getEntity().level().isClientSide()) return;

        LivingEntity target = event.getEntity();
        ElementReactions.trigger(target, null, ElementType.ELECTRO, LIGHTNING_CENTER_UNITS);

        Vec3 center = target.getBoundingBox().getCenter();
        for (LivingEntity nearby : target.level().getEntitiesOfClass(
                LivingEntity.class,
                target.getBoundingBox().inflate(LIGHTNING_RADIUS),
                entity -> entity != target
                        && entity.isAlive()
                        && !entity.isSpectator()
                        && entity.getBoundingBox().getCenter().distanceToSqr(center)
                        <= LIGHTNING_RADIUS * LIGHTNING_RADIUS)) {
            ElementReactions.trigger(nearby, null, ElementType.ELECTRO, LIGHTNING_NEAR_UNITS);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onKnockBack(LivingKnockBackEvent event) {
        LivingEntity entity = event.getEntity();
        if (!ShieldManager.protectsFromImpact(entity)) return;

        event.setCanceled(true);
        Vec3 velocity = entity.getDeltaMovement();
        entity.setDeltaMovement(velocity.x * 0.08, velocity.y, velocity.z * 0.08);
        entity.hurtMarked = true;
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onFall(LivingFallEvent event) {
        LivingEntity entity = event.getEntity();
        if (!FrozenControl.active(entity)) return;

        float threshold = Math.max(4.0f, entity.getMaxHealth() * 0.5f);
        if (event.getDistance() < threshold) return;

        ElementReactions.blunt(entity, null);
        event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onStatusDamage(LivingHurtEvent event) {
        LivingEntity target = event.getEntity();
        if (target.level().isClientSide()) return;
        if (event.getAmount() <= 0.0f) return;
        if (ReactionDamage.isDealing()) return;
        if (!isStatusDamage(event.getSource())) return;
        if (!(event.getSource().getEntity() instanceof LivingEntity attacker)) return;

        int level = Math.max(0, Math.min(120, EntityLevelManager.getLevel(attacker)));
        float multiplier = Math.min(
                STATUS_LEVEL_LIMIT,
                1.0f + level * STATUS_LEVEL_STEP);

        event.setAmount(event.getAmount() * multiplier);
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();

        if (entity.level().isClientSide()) return;
        if (!FrozenControl.active(entity)) return;
        if (!entity.isInWater() && !entity.isUnderWater()) return;

        Vec3 velocity = entity.getDeltaMovement();
        double horizontal = Math.sqrt(
                velocity.x * velocity.x
                        + velocity.z * velocity.z);

        double damping = horizontal > 0.06
                ? FROZEN_WATER_HORIZONTAL_DAMP
                : FROZEN_WATER_HORIZONTAL_DAMP * 0.5;

        entity.setDeltaMovement(
                velocity.x * damping,
                FROZEN_WATER_Y_LOCK,
                velocity.z * damping);
        entity.hasImpulse = true;
        entity.hurtMarked = true;
    }

    private static boolean isStatusDamage(net.minecraft.world.damagesource.DamageSource source) {
        return source.is(DamageTypes.MAGIC)
                || source.is(DamageTypes.WITHER)
                || source.is(DamageTypes.DROWN)
                || source.is(DamageTypeTags.IS_FIRE)
                || source.is(DamageTypeTags.IS_LIGHTNING);
    }
}