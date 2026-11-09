package com.dsp2.genshinerc.common.api;

import com.dsp2.genshinerc.common.core.damage.DamageKind;
import com.dsp2.genshinerc.common.core.damage.ElementalDamage;
import com.dsp2.genshinerc.common.core.damage.ElementalMath;
import com.dsp2.genshinerc.common.core.element.*;
import com.dsp2.genshinerc.common.core.reaction.ElementReactions;
import com.dsp2.genshinerc.common.core.reaction.ReactionResult;
import com.dsp2.genshinerc.common.registry.ModAttributes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import org.jetbrains.annotations.Nullable;

public final class ElementalCombatAPI {
    private ElementalCombatAPI() {}

    public static ReactionResult attach(LivingEntity target, @Nullable Entity attacker,
                                        ElementType type, GaugeTier tier, String attackKey) {
        return ElementAttachment.attach(target, attacker, type, tier, attackKey, AttachCooldown.STANDARD);
    }

    public static ReactionResult react(LivingEntity target, @Nullable Entity attacker,
                                       ElementType type, float units) {
        return ElementReactions.trigger(target, attacker, type, units);
    }

    public static float deal(LivingEntity target, @Nullable Entity attacker, ElementType type,
                             float baseDamage, boolean attach, GaugeTier tier, String attackKey) {
        if (target.level().isClientSide() || !target.isAlive()) return 0f;
        DamageKind kind = DamageKind.fromElement(type);
        double dmg = baseDamage;

        if (attacker instanceof LivingEntity le) {
            AttributeInstance bonus = le.getAttribute(
                    switch (kind) {
                        case PHYSICAL -> ModAttributes.PHYSICAL_DAMAGE_BONUS.get();
                        case PYRO     -> ModAttributes.PYRO_DAMAGE_BONUS.get();
                        case HYDRO    -> ModAttributes.HYDRO_DAMAGE_BONUS.get();
                        case ELECTRO  -> ModAttributes.ELECTRO_DAMAGE_BONUS.get();
                        case CRYO     -> ModAttributes.CRYO_DAMAGE_BONUS.get();
                        case ANEMO    -> ModAttributes.ANEMO_DAMAGE_BONUS.get();
                        case GEO      -> ModAttributes.GEO_DAMAGE_BONUS.get();
                        case DENDRO   -> ModAttributes.DENDRO_DAMAGE_BONUS.get();
                        default       -> null;
                    });
            if (bonus != null) dmg *= 1.0 + bonus.getValue();
        }

        if (attach) {
            ReactionResult rr = attach(target, attacker, type, tier, attackKey);
            
            if (baseDamage > 0 && rr != null && !rr.reactions().isEmpty()) {
                ElementReactions.forceSendText(target, rr);
            }
            
            if (rr.amplify() != 1f) dmg *= rr.amplify();
            dmg += rr.catalyzeFlat();
        }

        if (!(target.level() instanceof ServerLevel level)) return 0f;
        float before = target.getHealth();
        target.hurt(ElementalDamage.source(level, kind, attacker, attacker), (float) dmg);
        return Math.max(0f, before - target.getHealth());
    }

    public static void addDamageBonus(LivingEntity e, DamageKind kind, java.util.UUID id, double amount, int ticks) {
        var obj = kind.damageBonusObject();
        if (obj == null) return;
        AttributeInstance inst = e.getAttribute(obj.get());
        if (inst == null) return;
        inst.removeModifier(id);
        inst.addTransientModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(
                id, "genshinerc_dmg_bonus", amount,
                net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADDITION));
    }
}