package com.dsp2.genshinerc.common.level;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
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
                            Entity target = findTarget(context.getSource());
                            if (target instanceof LivingEntity living && !(living instanceof Player)) {
                                EntityLevelManager.setLevel(living, level);
                                context.getSource().sendSuccess(() -> Component.literal("Set level of " + living.getName().getString() + " to: " + level), false);
                                return 1;
                            }
                            context.getSource().sendFailure(Component.literal("No valid target found (cannot be player)!"));
                            return 0;
                        })
                )
        );

        dispatcher.register(Commands.literal("pl")
                .then(Commands.argument("level", IntegerArgumentType.integer(0, 120))
                        .executes(context -> {
                            if (context.getSource().getPlayer() != null) {
                                EntityLevelManager.setPlayerLevel(context.getSource().getPlayerOrException(), IntegerArgumentType.getInteger(context, "level"));
                                context.getSource().sendSuccess(() -> Component.literal("Player flowing level updated"), false);
                                return 1;
                            }
                            return 0;
                        })
                )
        );

    }

    public static Entity findTarget(CommandSourceStack source) {
        Player player = source.getPlayer();
        if (player != null) {
            var hit = ProjectileUtil.getEntityHitResult(player, player.getEyePosition(), player.getEyePosition().add(player.
                            getViewVector(1.0F).scale(30.0D)), player.getBoundingBox()
                            .expandTowards(player.getViewVector(1.0F).scale(30.0D)).inflate(1.0D),
                    e -> !e.isSpectator() && e.isPickable() && e != player, 900.0D);
            if (hit != null) return hit.getEntity();
            List<LivingEntity> entities = player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(30.0D),
                    e -> e != player && e.isAlive());
            return entities.stream().min(Comparator.comparingDouble(e -> e.distanceToSqr(player))).orElse(null);
        }
        return source.getEntity();
    }
}