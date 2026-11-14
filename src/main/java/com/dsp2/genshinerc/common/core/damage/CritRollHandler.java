package com.dsp2.genshinerc.common.core.damage;

import com.dsp2.genshinerc.common.core.combat.api.ElementalCombatAPI;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "genshinerc")
public final class CritRollHandler {
    private CritRollHandler() {}
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onHurt(LivingHurtEvent event) {
        if (event.getSource().getEntity() instanceof LivingEntity a && ElementalCombatAPI.rollCrit(a)) {
            event.setAmount((float) (event.getAmount() * (1.0 + ElementalCombatAPI.getCritDamage(a))));
        }
    }
}