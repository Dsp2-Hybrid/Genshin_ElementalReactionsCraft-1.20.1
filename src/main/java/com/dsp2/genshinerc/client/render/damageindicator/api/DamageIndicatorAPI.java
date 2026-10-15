package com.dsp2.genshinerc.client.render.damageindicator.api;

import com.dsp2.genshinerc.common.network.NetworkHandler;
import net.minecraft.world.entity.LivingEntity;

public class DamageIndicatorAPI {
    public static void reportDamage(LivingEntity e, String typeKey, float amount) {
        NetworkHandler.sendDamage(e, typeKey, amount, false, false, 0f, null, null);
    }
    public static void reportDamage(LivingEntity e, String typeKey, float amount, CustomColor color) {
        NetworkHandler.sendDamage(e, typeKey, amount, false, false, 0f, color.toHexString(), null);
    }
    public static void reportDamage(LivingEntity e, String typeKey, float amount, CustomColor color, float size) {
        NetworkHandler.sendDamage(e, typeKey, amount, false, false, 0f, color.toHexString(), size);
    }
    public static void reportCrit(LivingEntity e, String typeKey, float amount) {
        NetworkHandler.sendDamage(e, typeKey, amount, true, false, 0f, null, null);
    }
    public static void reportHeal(LivingEntity e, float amount) {
        NetworkHandler.sendDamage(e, "physical", -Math.abs(amount), false, false, 0f, null, null);
    }
    public static void reportKill(LivingEntity e, String typeKey, float amount) {
        NetworkHandler.sendDamage(e, typeKey, amount, false, true, 0f, null, null);
    }
    public static void reportAbsorbDamage(LivingEntity e, float absorbed, float realDamage, String typeKey) {
        NetworkHandler.sendDamage(e, typeKey, realDamage, false, false, absorbed, null, null);
    }
    public static void registerDamageType(String key, CustomColor color, String nameKey) {
        DamageTypeRegistry.register(key, color, nameKey);
    }
    public static DamageType getDamageType(String key) { return DamageTypeRegistry.get(key); }
}