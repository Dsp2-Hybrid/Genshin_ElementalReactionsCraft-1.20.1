package com.dsp2.genshinerc.common.core.damage;

import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * FALLBACK ONLY. Do not use together with MixinLivingEntityArmor: the Mixin already removes vanilla armor,
 * so inflating the amount here would multiply the damage. Use only if the Mixin is removed.
 */
public final class VanillaArmorCompensation {
    private VanillaArmorCompensation() {}

    public static float inflate(LivingEntity entity, DamageSource source, float desired) {
        if (desired <= 0f || source.is(DamageTypeTags.BYPASSES_ARMOR)) return desired;
        float armor = (float) entity.getArmorValue();
        float toughness = (float) entity.getAttributeValue(Attributes.ARMOR_TOUGHNESS);
        float lo = desired;
        float hi = desired * 5f;
        for (int i = 0; i < 40; i++) {
            float mid = (lo + hi) * 0.5f;
            if (CombatRules.getDamageAfterAbsorb(mid, armor, toughness) < desired) lo = mid; else hi = mid;
        }
        return hi;
    }
}
