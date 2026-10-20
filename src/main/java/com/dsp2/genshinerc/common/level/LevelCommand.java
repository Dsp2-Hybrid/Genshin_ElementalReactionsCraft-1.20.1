package com.dsp2.genshinerc.common.level;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Comparator;
import java.util.List;

@Mod.EventBusSubscriber(modid = "genshinerc")
public class LevelCommand {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(Commands.literal("l")
                .then(Commands.argument("level", IntegerArgumentType.integer(0, 120))
                        .executes(context -> {
                            int level = IntegerArgumentType.getInteger(context, "level");
                            Entity target = null;
                            Player player = context.getSource().getPlayer();

                            if (player != null) {
                                HitResult hit = player.pick(30.0D, 0.0F, false);
                                if (hit.getType() == HitResult.Type.ENTITY) {
                                    target = ((EntityHitResult) hit).getEntity();
                                } else {
                                    List<LivingEntity> entities = player.level().getEntitiesOfClass(LivingEntity.class,
                                            player.getBoundingBox().inflate(30.0D),
                                            e -> !(e instanceof Player) && e != player && e.isAlive());
                                    target = entities.stream()
                                            .min(Comparator.comparingDouble(e -> e.distanceToSqr(player)))
                                            .orElse(null);
                                }
                            } else {
                                target = context.getSource().getEntity();
                            }

                            if (target instanceof LivingEntity living && !(living instanceof Player)) {
                                EntityLevelManager.setLevel(living, level);

                                living.setHealth(living.getMaxHealth());

                                context.getSource().sendSuccess(() -> Component.literal("已将 " + living.getName().getString() + " 等级设置为: " + level + " 并回满血"), false);
                                return 1;
                            } else {
                                context.getSource().sendFailure(Component.literal("未找到目标：请确保准星对准了非玩家生物，或在30格内有存活的生物！"));
                                return 0;
                            }
                        })
                )
        );
    }
}