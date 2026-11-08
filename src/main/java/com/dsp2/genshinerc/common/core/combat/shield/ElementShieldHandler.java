package com.dsp2.genshinerc.common.core.combat.shield;

import com.dsp2.genshinerc.common.core.damage.DamageKind;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "genshinerc")
public final class ElementShieldHandler {
    @SubscribeEvent
    public static void onHurt(LivingHurtEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide() || event.getAmount() <= 0) return;

        DamageKind kind = DamageKind.PHYSICAL;
        ShieldManager.AbsorbResult res = ShieldManager.absorbElemental(entity, event.getAmount(), kind);

        if (res.blocked() > 0) {
            event.setAmount(event.getAmount() - res.blocked());
        }
    }
}