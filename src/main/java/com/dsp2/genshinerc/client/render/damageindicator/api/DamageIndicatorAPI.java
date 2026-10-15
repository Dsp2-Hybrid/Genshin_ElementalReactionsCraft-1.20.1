package com.dsp2.genshinerc.client.render.damageindicator.api;

import com.dsp2.genshinerc.common.network.NetworkHandler;
import net.minecraft.world.entity.LivingEntity;

public class DamageIndicatorAPI {
    public static void reportDamage(LivingEntity e, String typeKey, float amount) {
        NetworkHandler.sendDamage(e, typeKey, amount, false, false, 0f, null, null, false, false);
    }
    public static void reportDamage(LivingEntity e, String typeKey, float amount, CustomColor color) {
        NetworkHandler.sendDamage(e, typeKey, amount, false, false, 0f, color.toHexString(), null, false, false);
    }
    public static void reportDamage(LivingEntity e, String typeKey, float amount, CustomColor color, float size) {
        NetworkHandler.sendDamage(e, typeKey, amount, false, false, 0f, color.toHexString(), size, false, false);
    }
    public static void reportCrit(LivingEntity e, String typeKey, float amount) {
        NetworkHandler.sendDamage(e, typeKey, amount, true, false, 0f, null, null, false, false);
    }
    public static void reportHeal(LivingEntity e, float amount) {
        NetworkHandler.sendDamage(e, "physical", -Math.abs(amount), false, false, 0f, null, null, false, false);
    }
    public static void reportKill(LivingEntity e, String typeKey, float amount) {
        NetworkHandler.sendDamage(e, typeKey, amount, false, true, 0f, null, null, false, false);
    }
    public static void reportAbsorbRecovery(LivingEntity e, float amount) {
        NetworkHandler.sendDamage(e, "physical", Math.abs(amount), false, false, 0f, null, null, true, false);
    }
    public static void registerDamageType(String key, String nameKey, String configColorKey) {
        DamageTypeRegistry.register(key, nameKey, configColorKey);
    }
}