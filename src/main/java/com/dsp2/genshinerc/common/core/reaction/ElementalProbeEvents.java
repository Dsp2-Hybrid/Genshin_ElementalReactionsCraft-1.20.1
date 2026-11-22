package com.dsp2.genshinerc.common.core.reaction;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "genshinerc")
public final class ElementalProbeEvents {
    private static final double EXPLOSION_RADIUS = 3.5;
    private static final float EXPLOSION_UNITS = 1.5f;
    private static final double LIGHTNING_RADIUS = 2.5;
    private static final float LIGHTNING_CENTER_UNITS = 1.0f;
    private static final float LIGHTNING_NEAR_UNITS = 0.6f;

    private ElementalProbeEvents() {}

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onExplosion(ExplosionEvent.Detonate event) {
        if (!(event.getLevel() instanceof ServerLevel)) {
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
                    com.dsp2.genshinerc.common.core.element.ElementType.PYRO,
                    EXPLOSION_UNITS);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLightning(LivingAttackEvent event) {
        if (!(event.getSource().getDirectEntity() instanceof LightningBolt)) {
            return;
        }

        if (!(event.getEntity().level() instanceof ServerLevel)) {
            return;
        }

        LivingEntity target = event.getEntity();

        ElementReactions.trigger(
                target,
                null,
                com.dsp2.genshinerc.common.core.element.ElementType.ELECTRO,
                LIGHTNING_CENTER_UNITS);

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
                    com.dsp2.genshinerc.common.core.element.ElementType.ELECTRO,
                    LIGHTNING_NEAR_UNITS);
        }
    }
}