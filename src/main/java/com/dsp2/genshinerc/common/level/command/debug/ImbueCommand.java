package com.dsp2.genshinerc.common.level.command.debug;

import com.dsp2.genshinerc.GenshinERC;
import com.dsp2.genshinerc.common.core.element.ElementType;
import com.dsp2.genshinerc.common.core.element.apply.probe.ElementImbue;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = GenshinERC.MODID)
public final class ImbueCommand {
    private ImbueCommand() {}

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("ercimbue")
                .then(Commands.literal("clear").executes(ctx -> {
                    ElementImbue.clear(ctx.getSource().getPlayerOrException());
                    ctx.getSource().sendSuccess(() -> Component.literal("imbue cleared"), false);
                    return 1;
                }))
                .then(Commands.argument("element", StringArgumentType.word()).executes(ctx -> apply(ctx, ElementImbue.DEFAULT_UNITS))
                        .then(Commands.argument("units", FloatArgumentType.floatArg(0.1f, 8f))
                                .executes(ctx -> apply(ctx, FloatArgumentType.getFloat(ctx, "units"))))));
    }

    private static int apply(com.mojang.brigadier.context.CommandContext<net.minecraft.commands.CommandSourceStack> ctx, float units) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ElementType element = ElementType.valueOf(StringArgumentType.getString(ctx, "element").toUpperCase());
        ElementImbue.set(ctx.getSource().getPlayerOrException(), element, units);
        ctx.getSource().sendSuccess(() -> Component.literal(element.name() + " " + units), false);
        return 1;
    }
}
