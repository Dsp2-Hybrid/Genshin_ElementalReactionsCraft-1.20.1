package com.dsp2.genshinerc.common.core.damage;

import com.dsp2.genshinerc.common.core.reaction.DrownReaction;
import com.dsp2.genshinerc.common.core.reaction.ReactionDamage;
import com.dsp2.genshinerc.common.level.WorldLevelScaling;
import com.dsp2.genshinerc.server.render.damageindicator.DamageServerEventHandler;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "genshinerc")
public final class EnvironmentDamageHandler {
    private static final float VANILLA_BASE_HEALTH = 20f;

    private EnvironmentDamageHandler() {}

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onHurt(LivingHurtEvent event) {
        LivingEntity entity = event.getEntity();
        float amount = event.getAmount();
        if (entity.level().isClientSide() || amount <= 0f || entity instanceof ArmorStand || ReactionDamage.isDealing()) return;
        DamageSource src = event.getSource();

        if (src.is(DamageTypes.DROWN)) {
            double dmg = amount / VANILLA_BASE_HEALTH * entity.getMaxHealth() * resistance(entity, DamageKind.HYDRO);
            dmg *= DrownReaction.trigger(entity);
            DamageServerEventHandler.tag(entity, DamageKind.HYDRO.indicatorKey());
            event.setAmount((float) dmg);
        } else if (src.is(DamageTypes.FALL) || src.is(DamageTypes.IN_WALL)) {
            double dmg = amount / VANILLA_BASE_HEALTH * entity.getMaxHealth();
            event.setAmount((float) (dmg * resistance(entity, DamageKind.PHYSICAL)));
        } else if (src.is(DamageTypeTags.IS_FIRE) || src.is(DamageTypeTags.IS_FREEZING)
                || src.is(DamageTypes.CACTUS) || src.is(DamageTypes.SWEET_BERRY_BUSH)
                || src.is(DamageTypes.LIGHTNING_BOLT) || src.is(DamageTypes.STALAGMITE)) {
            event.setAmount((float) (amount * WorldLevelScaling.damageMultiplier()));
        }
    }

    private static double resistance(LivingEntity e, DamageKind kind) {
        Attribute attr = kind.resistanceAttribute();
        AttributeInstance inst = attr == null ? null : e.getAttribute(attr);
        return inst == null ? 1.0 : ElementalMath.resistanceMultiplier(inst.getValue());
    }
}