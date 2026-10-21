package com.dsp2.genshinerc.common.level;

import com.dsp2.genshinerc.client.render.bar.api.ElementManager;
import com.dsp2.genshinerc.client.render.bar.api.ElementType;
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
                                EntityLevelManager.setPlayerLevel(context.getSource().getPlayerOrException(), IntegerArgumentType.getInteger(context, "level"));
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
                                context.getSource().sendSuccess(() -> Component.literal("已添加元素状态"), false);
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
                        context.getSource().sendSuccess(() -> Component.literal("已清除目标所有元素"), false);
                        return 1;
                    }
                    return 0;
                })
        );
    }

    private static Entity findTarget(CommandSourceStack source) {
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