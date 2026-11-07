package com.dsp2.genshinerc.common.core.damage;

import com.dsp2.genshinerc.GenshinERC;
import com.dsp2.genshinerc.common.config.ServerConfig;
import com.dsp2.genshinerc.common.core.reaction.ReactionDamage;
import com.dsp2.genshinerc.common.registry.ModAttributes;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.MagmaCube;
import net.minecraft.world.entity.monster.Slime;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = GenshinERC.MODID)
public final class ElementalSettlement {
    private ElementalSettlement() {}

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onHurt(LivingHurtEvent event) {
        LivingEntity victim = event.getEntity();
        if (victim.level().isClientSide()) return;
        DamageSource source = event.getSource();
        DamageKind kind = DamageMapper.classify(source);

        if ((kind == DamageKind.PYRO && victim instanceof MagmaCube)
                || (kind == DamageKind.DENDRO && victim instanceof Slime && !(victim instanceof MagmaCube))) {
            event.setCanceled(true);
            return;
        }
        if (ReactionDamage.isDealing() || kind.isTrue()) return;

        double damage = event.getAmount();
        double original = damage;

        if (!source.is(DamageTypeTags.BYPASSES_ARMOR)) {
            double defense = value(victim, Attributes.ARMOR) + value(victim, ModAttributes.DEFENSE.get());
            int attackerLevel = ElementalStats.of(source.getEntity()).level();
            damage *= ElementalMath.defenseMultiplier(attackerLevel, defense);
        }

        DamageKind resKind = (kind == DamageKind.PHYSICAL && source.is(DamageTypeTags.IS_EXPLOSION)) ? DamageKind.PYRO : kind;
        Attribute resistance = resKind.resistanceAttribute();
        if (resistance != null) damage *= ElementalMath.resistanceMultiplier(value(victim, resistance));

        double tough = value(victim, Attributes.ARMOR_TOUGHNESS) * 10.0 + value(victim, ModAttributes.ELEMENTAL_TOUGHNESS.get());
        damage = ElementalMath.toughnessDamage(damage, victim.getHealth(), tough);

        if (source.is(DamageTypes.DROWN)) {
            damage = Math.max(damage, original * ServerConfig.DROWN_FLOOR.get());
        }

        event.setAmount((float) damage);
    }

    private static double value(LivingEntity entity, Attribute attribute) {
        AttributeInstance instance = entity.getAttribute(attribute);
        return instance == null ? 0.0 : instance.getValue();
    }
}
