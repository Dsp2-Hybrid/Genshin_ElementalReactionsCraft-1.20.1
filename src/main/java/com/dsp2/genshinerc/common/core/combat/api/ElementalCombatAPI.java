package com.dsp2.genshinerc.common.core.combat.api;

import com.dsp2.genshinerc.common.config.ServerConfig;
import com.dsp2.genshinerc.common.registry.ModAttributes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;

public final class ElementalCombatAPI {
    private ElementalCombatAPI() {}
    public static double getTotalAttack(LivingEntity e) {
        if (ServerConfig.USE_BASE_BONUS_ATK.get()) {
            return v(e, ModAttributes.BASE_ATTACK.get()) + v(e, ModAttributes.BONUS_ATTACK.get()) + v(e, ModAttributes.FLAT_ATTACK_BONUS.get());
        }
        return v(e, Attributes.ATTACK_DAMAGE);
    }
    public static double getTotalDefense(LivingEntity e) {
        double base = v(e, Attributes.ARMOR) + v(e, ModAttributes.DEFENSE.get());
        return base * ServerConfig.DEFENSE_COEFFICIENT.get() + v(e, ModAttributes.BONUS_DEFENSE.get())
             + v(e, ModAttributes.SCALED_DEFENSE.get()) + v(e, ModAttributes.INNATE_DEFENSE.get());
    }
    public static double getTotalHP(LivingEntity e) { return e.getMaxHealth() + v(e, ModAttributes.FLAT_HP_BONUS.get()); }
    public static double getCritRate(LivingEntity e) {
        if (!ServerConfig.USE_CRIT_SYSTEM.get()) return 0.0;
        return Math.max(0.0, Math.min(1.0, v(e, ModAttributes.CRIT_RATE.get())));
    }
    public static double getCritDamage(LivingEntity e) {
        if (!ServerConfig.USE_CRIT_SYSTEM.get()) return 0.0;
        return Math.max(0.0, v(e, ModAttributes.CRIT_DAMAGE.get()));
    }
    public static boolean rollCrit(LivingEntity e) { return e.level().getRandom().nextDouble() < getCritRate(e); }
    private static double v(LivingEntity e, Attribute a) { AttributeInstance i = e.getAttribute(a); return i == null ? 0.0 : i.getValue(); }
}