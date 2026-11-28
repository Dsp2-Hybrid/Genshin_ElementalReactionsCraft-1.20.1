package com.dsp2.genshinerc.common.core.combat;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Witch;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "genshinerc")
public final class LevelScaledHealing {
    private LevelScaledHealing() {}

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onHeal(LivingHealEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide() || event.getAmount() <= 0f) return;

        if (entity.getType() == EntityType.IRON_GOLEM
                && Math.abs(event.getAmount() - 25f) < 0.01f) {
            event.setAmount(entity.getMaxHealth() * 0.25f);
            return;
        }

        if (entity instanceof Witch
                && event.getAmount() >= 3.5f
                && event.getAmount() <= 4.5f) {
            event.setAmount(entity.getMaxHealth() / 6.0f);
        }
    }
}