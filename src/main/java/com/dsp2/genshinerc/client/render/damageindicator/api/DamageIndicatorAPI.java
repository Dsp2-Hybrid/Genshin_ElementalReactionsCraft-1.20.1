package com.dsp2.genshinerc.client.render.damageindicator.api;

import com.dsp2.genshinerc.common.network.damageindicator.DamageIndicatorNetworkHandler;
import net.minecraft.world.entity.LivingEntity;

public class DamageIndicatorAPI {
    public static void reportDamage(LivingEntity e, String typeKey, float amount) {
        DamageIndicatorNetworkHandler.sendDamage(e, typeKey, amount, false, false, false, 0f, null, null, false, false);
    }
    public static void reportDamage(LivingEntity e, String typeKey, float amount, CustomColor color) {
        DamageIndicatorNetworkHandler.sendDamage(e, typeKey, amount, false, false, false, 0f, color.toHexString(), null, false, false);
    }
    public static void reportDamage(LivingEntity e, String typeKey, float amount, CustomColor color, float size) {
        DamageIndicatorNetworkHandler.sendDamage(e, typeKey, amount, false, false, false, 0f, color.toHexString(), size, false, false);
    }
    public static void reportCrit(LivingEntity e, String typeKey, float amount) {
        DamageIndicatorNetworkHandler.sendDamage(e, typeKey, amount, true, false, false, 0f, null, null, false, false);
    }
    public static void reportHeal(LivingEntity e, float amount) {
        float after = Math.min(e.getHealth() + Math.abs(amount), e.getMaxHealth());
        boolean fullHeal = after >= e.getMaxHealth() - 0.001f;
        DamageIndicatorNetworkHandler.sendDamage(e, "physical", -Math.abs(amount), false, false, fullHeal, 0f, null, null, false, false);
    }
    public static void reportKill(LivingEntity e, String typeKey, float amount) {
        DamageIndicatorNetworkHandler.sendDamage(e, typeKey, amount, false, true, false, 0f, null, null, false, false);
    }
    public static void reportAbsorbRecovery(LivingEntity e, float amount) {
        DamageIndicatorNetworkHandler.sendDamage(e, "physical", Math.abs(amount), false, false, false, 0f, null, null, true, false);
    }
    public static void registerDamageType(String key, String nameKey, String configColorKey) {
        DamageTypeRegistry.register(key, nameKey, configColorKey);
    }
}