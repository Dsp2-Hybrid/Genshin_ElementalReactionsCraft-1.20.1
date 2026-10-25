package com.dsp2.genshinerc.common.core.combat.ef;

import com.dsp2.genshinerc.common.core.combat.zhenmie.DefenseOutcome;
import com.dsp2.genshinerc.common.core.combat.zhenmie.ZhenMieManager;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import yesman.epicfight.world.entity.eventlistener.TakeDamageEvent;

@Mod.EventBusSubscriber(modid = "genshinerc")
public final class ZhenMieCombatEvents {
    private ZhenMieCombatEvents() {}

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onDamage(LivingDamageEvent event) {
        LivingEntity victim = event.getEntity();
        if (victim.level().isClientSide() || event.getSource().getEntity() == null) return;
        ZhenMieManager.applyDefense(victim, EfCombatBridge.classify(event.getSource()), DefenseOutcome.HIT);
        if (event.getSource().getEntity() instanceof LivingEntity attacker) ZhenMieManager.markCombat(attacker);
    }

    public static void onGuard(LivingEntity defender, TakeDamageEvent.Attack event) {
        DefenseOutcome outcome = event.isParried() ? DefenseOutcome.PARRY : DefenseOutcome.BLOCK;
        ZhenMieManager.applyDefense(defender, EfCombatBridge.classify(event.getDamageSource()), outcome);
    }
}