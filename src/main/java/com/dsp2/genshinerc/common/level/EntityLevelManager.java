package com.dsp2.genshinerc.common.level;

import com.dsp2.genshinerc.client.render.bar.api.ElementManager;
import com.dsp2.genshinerc.client.render.bar.api.ElementType;
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
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Comparator;
import java.util.List;
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

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(Commands.literal("l")
                .then(Commands.argument("level", IntegerArgumentType.integer(0, 120))
                        .executes(context -> {
                            int level = IntegerArgumentType.getInteger(context, "level");
                            Entity target = findTarget(context.getSource());
                            if (target instanceof LivingEntity living && !(living instanceof Player)) {
                                setLevel(living, level);
                                context.getSource().sendSuccess(() -> Component.literal("已将 " + living.getName().getString() + " 等级设置为: " + level), false);
                                return 1;
                            }
                            context.getSource().sendFailure(Component.literal("没有找到有效目标（不能是玩家）！"));
                            return 0;
                        })
                )
        );

        dispatcher.register(Commands.literal("pl")
                .then(Commands.argument("level", IntegerArgumentType.integer(0, 120))
                        .executes(context -> {
                            if (context.getSource().getPlayer() != null) {
                                setPlayerLevel(context.getSource().getPlayerOrException(), IntegerArgumentType.getInteger(context, "level"));
                                context.getSource().sendSuccess(() -> Component.literal("玩家流动等级已更新"), false);
                                return 1;
                            }
                            return 0;
                        })
                )
        );

        dispatcher.register(Commands.literal("d")
                .then(Commands.argument("id", IntegerArgumentType.integer(1, 3))
                        .executes(context -> {
                            int id = IntegerArgumentType.getInteger(context, "id");
                            Entity target = findTarget(context.getSource());
                            if (target instanceof LivingEntity living) {
                                if (id == 1) ElementManager.addElement(living, ElementType.PYRO);
                                else if (id == 2) ElementManager.addElement(living, ElementType.BURNING);
                                else if (id == 3) ElementManager.addElement(living, ElementType.FROZEN);
                                return 1;
                            }
                            return 0;
                        })
                )
        );

        dispatcher.register(Commands.literal("dc")
                .executes(context -> {
                    Entity target = findTarget(context.getSource());
                    if (target instanceof LivingEntity living) {
                        ElementManager.clearElements(living);
                        return 1;
                    }
                    return 0;
                })
        );
    }

    private static Entity findTarget(CommandSourceStack source) {
        Player player = source.getPlayer();
        if (player != null) {
            var hit = ProjectileUtil.getEntityHitResult(player, player.getEyePosition(), player.getEyePosition().add(player.getViewVector(1.0F).scale(30.0D)), player.getBoundingBox().expandTowards(player.getViewVector(1.0F).scale(30.0D)).inflate(1.0D), e -> !e.isSpectator() && e.isPickable() && e != player, 900.0D);
            if (hit != null) return hit.getEntity();
            List<LivingEntity> entities = player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(30.0D), e -> e != player && e.isAlive());
            return entities.stream().min(Comparator.comparingDouble(e -> e.distanceToSqr(player))).orElse(null);
        }
        return source.getEntity();
    }
}