package com.dsp2.genshinerc.common.core.damage;

import com.dsp2.genshinerc.GenshinERC;
import com.dsp2.genshinerc.common.config.ServerConfig;
import com.dsp2.genshinerc.common.core.combat.shield.ShieldManager;
import com.dsp2.genshinerc.common.core.element.ElementType;
import com.dsp2.genshinerc.common.core.reaction.ElementReactions;
import com.dsp2.genshinerc.common.core.reaction.ReactionDamage;
import com.dsp2.genshinerc.common.core.reaction.ReactionResult;
import com.dsp2.genshinerc.common.network.base.damageindicator.DamageIndicatorNetworkHandler;
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
import net.minecraft.world.phys.Vec3;
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
        boolean explosion = source.is(DamageTypeTags.IS_EXPLOSION);
        DamageKind kind = explosion ? DamageKind.PYRO : DamageMapper.classify(source);

        if ((kind == DamageKind.PYRO && victim instanceof MagmaCube && !explosion)
                || (kind == DamageKind.DENDRO && victim instanceof Slime && !(victim instanceof MagmaCube))) {
            event.setCanceled(true);
            return;
        }
        if (ReactionDamage.isDealing() || kind.isTrue()) return;

        double damage = event.getAmount();
        double original = damage;

        if (explosion) {
            Vec3 center = source.getSourcePosition();
            if (center == null && source.getEntity() != null) center = source.getEntity().position();
            if (center != null && ServerConfig.EXPLOSION_REACTION_ENABLED.get()) {
                double radius = ServerConfig.TNT_BLAST_RADIUS.get();
                float gauge = ElementReactions.tntGauge(victim.position().distanceTo(center), radius);
                ReactionResult rr = ElementReactions.trigger(victim, source.getEntity(), ElementType.PYRO, gauge);
                if (rr.amplify() != 1f) damage *= rr.amplify();
            }
            
            if (!ServerConfig.EXPLOSION_USES_PYRO_RESISTANCE.get()) {
                event.setAmount((float) damage);
                return;
            }
        }

        if (!source.is(DamageTypeTags.BYPASSES_ARMOR)) {
            double defense = value(victim, Attributes.ARMOR) + value(victim, ModAttributes.DEFENSE.get());
            damage *= ElementalMath.defenseMultiplier(ElementalStats.of(source.getEntity()).level(), defense);
        }

        Attribute resistance = kind.resistanceAttribute();
        if (resistance != null) damage *= ElementalMath.resistanceMultiplier(value(victim, resistance));

        double tough = value(victim, Attributes.ARMOR_TOUGHNESS) * 10.0 + value(victim, ModAttributes.ELEMENTAL_TOUGHNESS.get());
        damage = ElementalMath.toughnessDamage(damage, victim.getHealth(), tough);

        if (source.is(DamageTypes.DROWN)) damage = Math.max(damage, original * ServerConfig.DROWN_FLOOR.get());

        if (ServerConfig.SHIELD_ENABLED.get()) {
            ShieldManager.AbsorbResult ar = ShieldManager.absorbElemental(victim, (float) damage, kind);
            if (ar.blocked() > 0f) {
                if (ar.shieldSpent() > 0f) {
                    DamageIndicatorNetworkHandler.sendDamage(victim, kind.indicatorKey(), -ar.shieldSpent(),
                            false, false, false, 0f, null, null, false, true);
                }
                damage -= ar.blocked();
                if (damage <= 0.001) { event.setAmount(0f); event.setCanceled(true); return; }
            }
        }

        event.setAmount((float) damage);
    }

    private static double value(LivingEntity entity, Attribute attribute) {
        AttributeInstance instance = entity.getAttribute(attribute);
        return instance == null ? 0.0 : instance.getValue();
    }
}