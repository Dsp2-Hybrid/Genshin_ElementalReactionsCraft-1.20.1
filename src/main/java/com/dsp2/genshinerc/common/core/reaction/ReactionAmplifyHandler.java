package com.dsp2.genshinerc.common.core.reaction;

import com.dsp2.genshinerc.common.core.combat.epicfight.EfElementalHit;

import com.dsp2.genshinerc.common.core.element.ElementAuraManager;
import com.dsp2.genshinerc.common.core.element.ElementType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.tags.DamageTypeTags;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "genshinerc")
public final class ReactionAmplifyHandler {
    private ReactionAmplifyHandler() {}

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onHurt(LivingHurtEvent event) {
        if (event.getEntity().level().isClientSide() || event.getAmount() <= 0 || ReactionDamage.isDealing()) return;
        if (EfElementalHit.converted(event.getSource()) || event.getSource().is(DamageTypeTags.IS_EXPLOSION)) return;
        LivingEntity entity = event.getEntity();
        
        ElementType trigger = null;
        if (event.getSource().is(DamageTypeTags.IS_FIRE)) {
            trigger = ElementType.PYRO;
        } else if (event.getSource().is(DamageTypeTags.IS_FREEZING)) {
            trigger = ElementType.CRYO;
        }

        if (trigger == null) return;

        ElementType aura = null;
        float maxAmount = 0;
        for (ElementType t : ElementType.values()) {
            if (ElementReactions.isAmpReaction(trigger, t)) {
                float amt = ElementAuraManager.amount(entity, t);
                if (amt > maxAmount) {
                    maxAmount = amt;
                    aura = t;
                }
            }
        }

        if (aura != null && maxAmount > 1.0e-4f) {
            float mult = ElementReactions.calcAmplify(trigger, aura);
            event.setAmount(event.getAmount() * mult);
            ElementAuraManager.consume(entity, aura, 1.0f, true);
            
            Reaction r = null;
            if (trigger == ElementType.PYRO) {
                if (aura == ElementType.HYDRO) r = Reaction.VAPORIZE;
                else if (aura == ElementType.CRYO || aura == ElementType.FROZEN) r = Reaction.MELT;
            } else if (trigger == ElementType.CRYO) {
                if (aura == ElementType.PYRO) r = Reaction.MELT;
            }
            if (r != null) {
                if (r == Reaction.MELT) {
                    ReactionSoundEvents.playMelt(entity);
                    ReactionParticles.spawnMelt(entity);
                }
                if (r == Reaction.VAPORIZE) {
                    ReactionSoundEvents.playVaporize(entity);
                    ReactionParticles.spawnVaporize(entity);
                }
                ReactionText.send(entity, r, trigger, aura);
            }
        }
    }
}