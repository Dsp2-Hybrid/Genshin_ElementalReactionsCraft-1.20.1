package com.dsp2.genshinerc.common.level;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = "genshinerc")
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
                maxHealth.addPermanentModifier(new AttributeModifier(HEALTH_MODIFIER_UUID, "genshin_level_health", healthMultiplier, AttributeModifier.Operation.MULTIPLY_BASE));
                if (entity.getHealth() > maxHealth.getValue()) entity.setHealth((float) maxHealth.getValue());
            }
        }

        if (attackDamage != null) {
            attackDamage.removeModifier(ATTACK_MODIFIER_UUID);
            if (level > 0) {
                double attackMultiplier = Math.pow(level / 100.0, 1.2) * 199.0;
                attackDamage.addPermanentModifier(new AttributeModifier(ATTACK_MODIFIER_UUID, "genshin_level_attack", attackMultiplier, AttributeModifier.Operation.MULTIPLY_BASE));
            }
        }
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(Commands.literal("l")
                .then(Commands.argument("level", IntegerArgumentType.integer(0, 120))
                        .executes(context -> {
                            int level = IntegerArgumentType.getInteger(context, "level");
                            Player player = context.getSource().getPlayer();
                            Entity target = null;

                            if (player != null) {
                                HitResult hit = player.pick(30.0D, 0.0F, false);
                                if (hit.getType() == HitResult.Type.ENTITY) target = ((EntityHitResult) hit).getEntity();
                                else {
                                    List<LivingEntity> entities = player.level().getEntitiesOfClass(LivingEntity.class, new AABB(player.blockPosition()).inflate(30.0D), e -> e != player && e.isAlive());
                                    target = entities.stream().min(Comparator.comparingDouble(e -> e.distanceToSqr(player))).orElse(null);
                                }
                            } else if (context.getSource().getEntity() != null) target = context.getSource().getEntity();

                            if (target instanceof LivingEntity living) {
                                EntityLevelManager.setLevel(living, level);
                                context.getSource().sendSuccess(() -> Component.literal("已将实体 " + living.getName().getString() + " 等级设置为: " + level), false);
                                return 1;
                            } else {
                                context.getSource().sendFailure(Component.literal("没有找到目标实体！"));
                                return 0;
                            }
                        })
                )
        );
    }
}