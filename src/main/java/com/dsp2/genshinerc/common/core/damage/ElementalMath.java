package com.dsp2.genshinerc.common.core.damage;

import com.dsp2.genshinerc.common.config.ServerConfig;
import net.minecraft.world.entity.LivingEntity;

public final class ElementalMath {
    private ElementalMath() {}

    public static double levelMultiplier(int level) {
        int l = Math.max(1, level);
        double m1 = ServerConfig.LEVEL_MULT_1.get();
        double m80 = ServerConfig.LEVEL_MULT_80.get();
        double m90 = ServerConfig.LEVEL_MULT_90.get();
        if (l <= 80) return m1 * Math.pow(m80 / m1, (l - 1) / 79.0);
        return m80 * Math.pow(m90 / m80, (l - 80) / 10.0);
    }

    public static float transformative(float coefficient, ElementalStats s) {
        double em = s.mastery();
        return (float) (coefficient * levelMultiplier(s.level()) * (1.0 + 16.0 * em / (em + 2000.0)) * ServerConfig.SCALE.get());
    }

    public static float amplify(float base, double em) {
        return (float) (base * (1.0 + 2.78 * em / (em + 1400.0)));
    }

    public static float catalyze(float k, ElementalStats s) {
        double em = s.mastery();
        return (float) (levelMultiplier(s.level()) * k * (1.0 + 5.0 * em / (em + 1200.0)) * ServerConfig.SCALE.get());
    }

    public static double defenseMultiplier(int attackerLevel, double defense) {
        double l = Math.max(1, attackerLevel) + 100.0;
        return l / (l + ServerConfig.DEFENSE_COEFFICIENT.get() * Math.max(0.0, defense));
    }

    public static double resistanceMultiplier(double r) {
        if (r < 0.0) return 1.0 - r / 2.0;
        if (r <= 0.75) return 1.0 - r;
        return 1.0 / (1.0 + 4.0 * r);
    }

    public static double toughnessDamage(double damage, double hpCurr, double tough) {
        if (damage <= 0.0 || hpCurr <= 0.0 || tough <= 0.0) return damage;
        double rho = tough / (tough + ServerConfig.TOUGHNESS_C.get());
        double b = 1.0 - 0.75 * rho;
        double p = 1.0 + ServerConfig.TOUGHNESS_K.get() * rho;
        double x = damage / hpCurr;
        if (x <= b || x >= 1.0) return damage;
        return (b + (1.0 - b) * Math.pow((x - b) / (1.0 - b), p)) * hpCurr;
    }

    public static double capReaction(double damage, LivingEntity target) {
        double cap = ServerConfig.REACTION_MAX_HP_FRACTION.get() * target.getMaxHealth();
        return Math.min(damage, cap);
    }

    public static double crystallizeShield(int level, double em) {
        double emBonus = 1.0 + (40.0 / 9.0) * em / (em + 1400.0);
        return ServerConfig.CRYSTALLIZE_SHIELD_COEF.get() * levelMultiplier(level) * ServerConfig.SCALE.get() * emBonus;
    }
}
