package com.dsp2.genshinerc.common.core.element;

import com.dsp2.genshinerc.common.config.ServerConfig;
import net.minecraft.world.entity.LivingEntity;

public final class FrozenCrit {
    private FrozenCrit() {}

    public static double critRateBonus(LivingEntity target) {
        return ElementAuraManager.hasAura(target, ElementType.FROZEN)
                ? ServerConfig.FROZEN_CRIT_RATE_BONUS.get() : 0.0;
    }

    public static double effectiveCritRate(LivingEntity target, double baseCritRate) {
        return Math.min(1.0, Math.max(0.0, baseCritRate) + critRateBonus(target));
    }

    public static double expectedBonus(LivingEntity target, double critDamage) {
        return critRateBonus(target) * Math.max(0.0, critDamage);
    }
}
