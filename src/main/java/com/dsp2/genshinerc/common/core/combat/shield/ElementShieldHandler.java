package com.dsp2.genshinerc.common.core.combat.shield;

import com.dsp2.genshinerc.common.core.damage.DamageKind;
import com.dsp2.genshinerc.common.core.damage.DamageKindResolver;
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
        if (entity.level().isClientSide() || amount <= 0f) return;

        DamageKind kind = DamageKindResolver.of(event.getSource());
        ShieldManager.AbsorbResult res = ShieldManager.absorbElemental(entity, amount, kind);
        if (res.blocked() <= 0f) return;

        event.setAmount(Math.max(0f, amount - res.blocked()));
        DamageServerEventHandler.shieldAbsorb(entity, res.blocked(), kind.indicatorKey());
    }
}