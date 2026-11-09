package com.dsp2.genshinerc.common.core.combat.epicfight;

import com.dsp2.genshinerc.common.core.combat.zhenmie.DefenseOutcome;
import com.dsp2.genshinerc.common.core.combat.zhenmie.ZhenMieManager;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
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
        LivingEntity attacker = null;
        if (event.getSource().getEntity() instanceof LivingEntity livingAttacker) {
            attacker = livingAttacker;
        }
        ZhenMieManager.applyDefense(victim, attacker, EfCombatBridge.classify(event.getSource()), DefenseOutcome.HIT, event.getAmount());
        if (attacker != null) {
            ZhenMieManager.markCombat(attacker);
        }
    }

    public static void onGuard(LivingEntity defender, TakeDamageEvent.Attack event) {
        DefenseOutcome outcome = event.isParried() ? DefenseOutcome.PARRY : DefenseOutcome.BLOCK;
        LivingEntity attacker = null;
        if (event.getDamageSource().getEntity() instanceof LivingEntity livingAttacker) {
            attacker = livingAttacker;
        }
        float dmg = attacker != null ? (float) attacker.getAttributeValue(Attributes.ATTACK_DAMAGE) : 1.0f;
        ZhenMieManager.applyDefense(defender, attacker, EfCombatBridge.classify(event.getDamageSource()), outcome, dmg);
    }
}