package com.dsp2.genshinerc.common.core.combat.epicfight;

import com.dsp2.genshinerc.common.core.combat.zhenmie.AttackIntensity;
import com.dsp2.genshinerc.common.core.combat.zhenmie.ClashResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.damagesource.EpicFightDamageSource;
import yesman.epicfight.world.damagesource.StunType;

import java.util.Optional;

public final class EfCombatBridge {
    private static final float HEAVY_IMPACT = 2.0f;
    private EfCombatBridge() {}

    public static void applyStun(LivingEntity entity, ClashResult result) {
        if (entity == null || result == null) return;
        try {
            Optional<?> opt = EpicFightCapabilities.getUnparameterizedEntityPatch(entity, LivingEntityPatch.class);
            if (opt != null && opt.isPresent()) {
                ((LivingEntityPatch<?>) opt.get()).applyStun(result.stunType(), result.stunSeconds());
            }
        } catch (NoSuchMethodError ignored) {}
    }

    public static AttackIntensity classify(DamageSource source) {
        if (source instanceof EpicFightDamageSource ef) {
            if (ef.getStunType() == StunType.KNOCKDOWN) return AttackIntensity.ASSAULT;
            if (ef.calculateImpact() >= HEAVY_IMPACT) return AttackIntensity.HEAVY;
        }
        return AttackIntensity.LIGHT;
    }
}