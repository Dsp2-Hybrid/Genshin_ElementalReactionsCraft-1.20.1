package com.dsp2.genshinerc.common.core.damage;

import com.dsp2.genshinerc.common.config.ServerConfig;
import com.dsp2.genshinerc.common.core.combat.api.ElementalCombatAPI;
import com.dsp2.genshinerc.common.core.element.FrozenCrit;
import com.dsp2.genshinerc.common.core.reaction.ReactionDamage;
import com.dsp2.genshinerc.server.render.damageindicator.DamageServerEventHandler;
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
        LivingEntity target = event.getEntity();
        if (target.level().isClientSide() || event.getAmount() <= 0f || ReactionDamage.isDealing()
                || !ServerConfig.USE_CRIT_SYSTEM.get() || DamageMapper.classify(event.getSource()).isTrue()
                || !(event.getSource().getEntity() instanceof LivingEntity attacker) || attacker == target) return;
        double rate = FrozenCrit.effectiveCritRate(target, ElementalCombatAPI.getCritRate(attacker));
        if (attacker.getRandom().nextDouble() >= rate) return;
        event.setAmount((float) (event.getAmount() * (1.0 + ElementalCombatAPI.getCritDamage(attacker))));
        DamageServerEventHandler.markCrit(target);
    }
}
