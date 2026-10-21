package com.dsp2.genshinerc.common.level;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = "genshinerc")
public class EntityLevelManager {
    private static final String NBT_LEVEL = "GenshinLevel";
    private static final UUID HEALTH_MODIFIER_UUID = UUID.fromString("cb3f55d3-645c-4f38-a497-9c13a33db5cf");
    private static final UUID ATTACK_MODIFIER_UUID = UUID.fromString("cb3f55d3-645c-4f38-a497-9c13a33db5c0");

    public static int getLevel(LivingEntity entity) {
        AttributeInstance maxHealth = entity.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth != null) {
            for (AttributeModifier mod : maxHealth.getModifiers()) {
                if (mod.getName().equals("genshin_level_health") && mod.getId().equals(HEALTH_MODIFIER_UUID)) {
                    double amount = mod.getAmount();
                    double level = Math.log(amount + 1.0) / Math.log(500.0) * 100.0;
                    return (int) Math.round(level);
                }
            }
        }
        return 0;
    }

    public static int getPlayerLevel(Player player) {
        CompoundTag tag = player.getPersistentData();
        return tag.contains("GenshinPlayerLevel") ? tag.getInt("GenshinPlayerLevel") : 0;
    }

    public static void setPlayerLevel(Player player, int level) {
        player.getPersistentData().putInt("GenshinPlayerLevel", level);
    }

    public static void setLevel(LivingEntity entity, int level) {
        if (entity instanceof Player) return;
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
                double healthMultiplier = Math.pow(500.0, level / 100.0) - 1.0;
                maxHealth.addPermanentModifier(new AttributeModifier(HEALTH_MODIFIER_UUID, "genshin_level_health", healthMultiplier, AttributeModifier.Operation.MULTIPLY_BASE));
            }
            entity.setHealth((float) maxHealth.getValue());
        }

        if (attackDamage != null) {
            attackDamage.removeModifier(ATTACK_MODIFIER_UUID);
            if (level > 0) {
                double attackMultiplier = Math.pow(200.0, level / 100.0) - 1.0;
                attackDamage.addPermanentModifier(new AttributeModifier(ATTACK_MODIFIER_UUID, "genshin_level_attack", attackMultiplier, AttributeModifier.Operation.MULTIPLY_BASE));
            }
        }
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        if (event.getAmount() <= 0) return;

        net.minecraft.world.damagesource.DamageSource source = event.getSource();
        int level = 0;

        if (source.getDirectEntity() instanceof net.minecraft.world.entity.projectile.AbstractArrow arrow) {
            if (arrow.getOwner() instanceof LivingEntity owner) level = getLevel(owner);
        } else if (source.getDirectEntity() instanceof net.minecraft.world.entity.projectile.ThrownPotion potion) {
            if (potion.getOwner() instanceof LivingEntity owner) level = getLevel(owner);
        } else if (source.getEntity() instanceof net.minecraft.world.entity.monster.Creeper creeper) {
            level = getLevel(creeper);
        }

        if (level > 0) {
            double multiplier = Math.pow(200.0, level / 100.0);
            event.setAmount((float) (event.getAmount() * multiplier));
        }
    }
}