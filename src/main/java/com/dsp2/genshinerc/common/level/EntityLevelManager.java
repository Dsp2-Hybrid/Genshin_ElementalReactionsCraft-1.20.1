package com.dsp2.genshinerc.common.level;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.UUID;

public class EntityLevelManager {
    private static final String NBT_LEVEL = "GenshinLevel";
    private static final UUID HEALTH_MODIFIER_UUID = UUID.fromString("cb3f55d3-645c-4f38-a497-9c13a33db5cf");
    private static final UUID ATTACK_MODIFIER_UUID = UUID.fromString("cb3f55d3-645c-4f38-a497-9c13a33db5cg");

    public static int getLevel(LivingEntity entity) {
        CompoundTag tag = entity.getPersistentData();
        return tag.contains(NBT_LEVEL) ? tag.getInt(NBT_LEVEL) : 0;
    }

    public static void setLevel(LivingEntity entity, int level) {
        level = Math.max(0, Math.min(120, level));
        CompoundTag tag = entity.getPersistentData();
        tag.putInt(NBT_LEVEL, level);
        applyAttributes(entity, level);

    }

    private static void applyAttributes(LivingEntity entity, int level) {
        AttributeInstance maxHealth = entity.getAttribute(Attributes.MAX_HEALTH);
        AttributeInstance attackDamage = entity.getAttribute(Attributes.ATTACK_DAMAGE);

        if (maxHealth != null) {
            maxHealth.removeModifier(HEALTH_MODIFIER_UUID);
            if (level > 0) {
                double healthMultiplier = Math.pow(level / 100.0, 1.2) * 499.0;
                maxHealth.addPermanentModifier(new AttributeModifier(
                        HEALTH_MODIFIER_UUID, "genshin_level_health", healthMultiplier, AttributeModifier.Operation.MULTIPLY_BASE));
                if (entity.getHealth() > maxHealth.getValue()) {
                    entity.setHealth((float) maxHealth.getValue());
                }
            }
        }

        if (attackDamage != null) {
            attackDamage.removeModifier(ATTACK_MODIFIER_UUID);
            if (level > 0) {
                double attackMultiplier = Math.pow(level / 100.0, 1.2) * 199.0;
                attackDamage.addPermanentModifier(new AttributeModifier(
                        ATTACK_MODIFIER_UUID, "genshin_level_attack", attackMultiplier, AttributeModifier.Operation.MULTIPLY_BASE));
            }
        }
    }
}