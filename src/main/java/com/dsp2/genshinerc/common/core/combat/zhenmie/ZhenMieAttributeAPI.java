package com.dsp2.genshinerc.common.core.combat.zhenmie;

import com.dsp2.genshinerc.common.registry.ModAttributes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraftforge.registries.RegistryObject;

public final class ZhenMieAttributeAPI {
    private ZhenMieAttributeAPI() {}

    public static void setBreakPower(LivingEntity entity, double value) {
        set(entity, ModAttributes.ZHEN_MIE_BREAK_POWER, value);
    }

    public static void setCrushPower(LivingEntity entity, double value) {
        set(entity, ModAttributes.ZHEN_MIE_CRUSH_POWER, value);
    }

    public static void setResolve(LivingEntity entity, double value) {
        set(entity, ModAttributes.ZHEN_MIE_RESOLVE, value);
    }

    public static void setBreakResistance(LivingEntity entity, double value) {
        set(entity, ModAttributes.ZHEN_MIE_BREAK_RESISTANCE, value);
    }

    public static void setCrushResistance(LivingEntity entity, double value) {
        set(entity, ModAttributes.ZHEN_MIE_CRUSH_RESISTANCE, value);
    }

    public static void setCritForce(LivingEntity entity, double value) {
        set(entity, ModAttributes.ZHEN_MIE_CRIT_FORCE, value);
    }

    public static void addBreakPower(LivingEntity entity, double value) {
        add(entity, ModAttributes.ZHEN_MIE_BREAK_POWER, value);
    }

    public static void addCrushPower(LivingEntity entity, double value) {
        add(entity, ModAttributes.ZHEN_MIE_CRUSH_POWER, value);
    }

    public static void addResolve(LivingEntity entity, double value) {
        add(entity, ModAttributes.ZHEN_MIE_RESOLVE, value);
    }

    public static void addBreakResistance(LivingEntity entity, double value) {
        add(entity, ModAttributes.ZHEN_MIE_BREAK_RESISTANCE, value);
    }

    public static void addCrushResistance(LivingEntity entity, double value) {
        add(entity, ModAttributes.ZHEN_MIE_CRUSH_RESISTANCE, value);
    }

    public static void addCritForce(LivingEntity entity, double value) {
        add(entity, ModAttributes.ZHEN_MIE_CRIT_FORCE, value);
    }

    public static double breakPower(LivingEntity entity) {
        return value(entity, ModAttributes.ZHEN_MIE_BREAK_POWER);
    }

    public static double crushPower(LivingEntity entity) {
        return value(entity, ModAttributes.ZHEN_MIE_CRUSH_POWER);
    }

    public static double resolve(LivingEntity entity) {
        return value(entity, ModAttributes.ZHEN_MIE_RESOLVE);
    }

    public static double breakResistance(LivingEntity entity) {
        return value(entity, ModAttributes.ZHEN_MIE_BREAK_RESISTANCE);
    }

    public static double crushResistance(LivingEntity entity) {
        return value(entity, ModAttributes.ZHEN_MIE_CRUSH_RESISTANCE);
    }

    public static double critForce(LivingEntity entity) {
        return value(entity, ModAttributes.ZHEN_MIE_CRIT_FORCE);
    }

    public static void clear(LivingEntity entity) {
        setBreakPower(entity, 0.0);
        setCrushPower(entity, 0.0);
        setResolve(entity, 0.0);
        setBreakResistance(entity, 0.0);
        setCrushResistance(entity, 0.0);
        setCritForce(entity, 0.0);
    }

    private static void set(
            LivingEntity entity,
            RegistryObject<Attribute> attribute,
            double value) {
        if (entity == null || entity.level().isClientSide()) return;

        AttributeInstance instance = entity.getAttribute(attribute.get());
        if (instance == null) return;

        instance.setBaseValue(Math.max(0.0, Math.min(10000.0, value)));
    }

    private static void add(
            LivingEntity entity,
            RegistryObject<Attribute> attribute,
            double value) {
        if (entity == null || entity.level().isClientSide()) return;

        AttributeInstance instance = entity.getAttribute(attribute.get());
        if (instance == null) return;

        double next = instance.getBaseValue() + value;
        instance.setBaseValue(Math.max(0.0, Math.min(10000.0, next)));
    }

    private static double value(
            LivingEntity entity,
            RegistryObject<Attribute> attribute) {
        if (entity == null) return 0.0;

        AttributeInstance instance = entity.getAttribute(attribute.get());
        return instance == null ? 0.0 : Math.max(0.0, instance.getValue());
    }
}