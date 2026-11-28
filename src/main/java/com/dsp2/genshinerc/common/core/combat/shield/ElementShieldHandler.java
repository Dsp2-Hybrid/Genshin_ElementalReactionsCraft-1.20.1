package com.dsp2.genshinerc.common.core.combat.shield;

import com.dsp2.genshinerc.common.config.ServerConfig;
import com.dsp2.genshinerc.common.core.damage.DamageKind;
import com.dsp2.genshinerc.common.core.damage.DamageMapper;
import com.dsp2.genshinerc.server.render.damageindicator.DamageServerEventHandler;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "genshinerc")
public final class ElementShieldHandler {
    private ElementShieldHandler() {}

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onHurt(LivingHurtEvent event) {
        LivingEntity entity = event.getEntity();
        float amount = event.getAmount();
        if (entity.level().isClientSide() || amount <= 0f || !ServerConfig.SHIELD_ENABLED.get()) return;

        DamageKind kind = DamageMapper.classify(event.getSource());
        if (kind.isTrue()) return;

        float rockCost = com.dsp2.genshinerc.common.core.reaction.ReactionDamage.overloadShieldCost(entity, event.getSource());
        ShieldManager.AbsorbResult res = ShieldManager.absorbElemental(entity, amount, kind, rockCost);
        if (res.blocked() <= 0f) return;

        if (res.shieldSpent() > 0f) {
            DamageServerEventHandler.shieldAbsorb(entity, res.shieldSpent(), kind.indicatorKey());
            com.dsp2.genshinerc.common.core.reaction.ReactionSoundEvents.playShieldHit(entity);
            com.dsp2.genshinerc.common.core.reaction.ReactionParticles.spawnShieldHit(entity, res.shieldElement());
        }
        
        float left = amount - res.blocked();
        if (left <= 0.001f) {
            event.setAmount(0f);
            event.setCanceled(true);
        } else {
            event.setAmount(left);
        }
    }
}