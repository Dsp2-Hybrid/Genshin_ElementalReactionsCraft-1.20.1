package com.dsp2.genshinerc.common.level.command.debug;

import com.dsp2.genshinerc.common.level.WorldLevel;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "genshinerc")
public final class WorldLevelDebugCommand {
    private WorldLevelDebugCommand() {}

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("dwl").requires(s -> s.hasPermission(2))
                .executes(ctx -> {
                    reply(ctx.getSource(), "World level: " + WorldLevel.get());
                    return 1;
                })
                .then(Commands.argument("level", IntegerArgumentType.integer(0, 8))
                        .executes(ctx -> {
                            WorldLevel.set(IntegerArgumentType.getInteger(ctx, "level"));
                            reply(ctx.getSource(), "World level set to " + WorldLevel.get()
                                    + " (affects mobs spawned from now on)");
                            return 1;
                        })));
    }

    private static void reply(CommandSourceStack src, String msg) {
        src.sendSuccess(() -> Component.literal(msg), true);
    }
}