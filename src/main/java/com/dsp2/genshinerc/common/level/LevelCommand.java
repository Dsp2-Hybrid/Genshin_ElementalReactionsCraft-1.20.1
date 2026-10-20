package com.dsp2.genshinerc.common.level;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "genshinerc")
public class LevelCommand {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(Commands.literal("l")
                .then(Commands.argument("level", IntegerArgumentType.integer(0, 120))
                        .executes(context -> {
                            int level = IntegerArgumentType.getInteger(context, "level");
                            Entity target = context.getSource().getEntity();

                            if (target instanceof LivingEntity living) {
                                EntityLevelManager.setLevel(living, level);
                                context.getSource().sendSuccess(() -> Component.literal("已将实体等级设置为: " + level), false);
                                return 1;
                            } else {
                                context.getSource().sendFailure(Component.literal("没有有效的实体目标！"));
                                return 0;
                            }
                        })
                )
        );
    }
}