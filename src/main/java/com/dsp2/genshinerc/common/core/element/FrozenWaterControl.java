package com.dsp2.genshinerc.common.core.element;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "genshinerc")
public final class FrozenWaterControl {
    private static final String PREVIOUS_GRAVITY = "GenshinERC_FrozenWaterPreviousGravity";

    private FrozenWaterControl() {}

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide()) return;

        boolean frozenInWater = FrozenControl.active(entity)
                && (entity.isInWater() || entity.isUnderWater());

        if (frozenInWater) {
            if (!entity.getPersistentData().contains(PREVIOUS_GRAVITY)) {
                entity.getPersistentData().putBoolean(PREVIOUS_GRAVITY, entity.isNoGravity());
            }

            entity.setNoGravity(true);

            Vec3 velocity = entity.getDeltaMovement();
            entity.setDeltaMovement(velocity.x * 0.22, 0.0, velocity.z * 0.22);
            entity.hasImpulse = true;
            entity.hurtMarked = true;
            return;
        }

        if (entity.getPersistentData().contains(PREVIOUS_GRAVITY)) {
            boolean noGravity = entity.getPersistentData().getBoolean(PREVIOUS_GRAVITY);
            entity.getPersistentData().remove(PREVIOUS_GRAVITY);
            entity.setNoGravity(noGravity);
        }
    }
}