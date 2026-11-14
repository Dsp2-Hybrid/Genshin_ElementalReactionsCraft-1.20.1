package com.dsp2.genshinerc.common.level.command.op;

import com.dsp2.genshinerc.common.level.WorldLevel;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "genshinerc")
public class WorldLevelCommand {
    @SubscribeEvent
    public static void onRegister(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> d = event.getDispatcher();
        d.register(Commands.literal("worldlevel").requires(s -> s.hasPermission(2))
            .then(Commands.argument("level", IntegerArgumentType.integer(0, 8))
                .executes(ctx -> {
                    int lv = IntegerArgumentType.getInteger(ctx, "level");
                    WorldLevel.set(lv); WorldLevel.saveToConfig();
                    ctx.getSource().sendSuccess(() -> Component.literal("World Level: " + lv), true);
                    return 1;
                }))
            .executes(ctx -> {
                ctx.getSource().sendSuccess(() -> Component.literal("Current World Level: " + WorldLevel.get()), false);
                return 1;
            }));
    }
}