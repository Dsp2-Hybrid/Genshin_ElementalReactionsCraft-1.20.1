package com.dsp2.genshinerc.common.level.command.debug;

import com.dsp2.genshinerc.common.core.element.ElementAuraManager;
import com.dsp2.genshinerc.common.core.element.ElementType;
import com.dsp2.genshinerc.common.core.reaction.ElementReactions;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Locale;
import java.util.function.Function;

@Mod.EventBusSubscriber(modid = "genshinerc")
public final class ElementDebugCommand {
    private ElementDebugCommand() {}

    @SubscribeEvent
    public static void onRegister(RegisterCommandsEvent event) {
        for (String root : new String[] {"dele", "deles"}) {
            boolean self = root.equals("deles");
            LiteralArgumentBuilder<CommandSourceStack> b = Commands.literal(root).requires(s -> s.hasPermission(2));
            LiteralArgumentBuilder<CommandSourceStack> react = Commands.literal("react");
            LiteralArgumentBuilder<CommandSourceStack> raw = Commands.literal("raw");
            for (ElementType type : ElementType.values()) {
                String n = type.name().toLowerCase(Locale.ROOT);
                react.then(Commands.literal(n).then(Commands.argument("units", FloatArgumentType.floatArg(0.1f, 4f))
                        .executes(ctx -> run(ctx, self, t -> "reactions: "
                                + ElementReactions.trigger(t, ctx.getSource().getEntity(), type, FloatArgumentType.getFloat(ctx, "units")).reactions()))));
                raw.then(Commands.literal(n).then(Commands.argument("units", FloatArgumentType.floatArg(0.1f, 4f))
                        .executes(ctx -> run(ctx, self, t -> {
                            ElementAuraManager.applyUnits(t, type, FloatArgumentType.getFloat(ctx, "units"));
                            return "applied " + n;
                        }))));
            }
            b.then(react).then(raw);
            b.then(Commands.literal("freeze").then(Commands.argument("gauge", FloatArgumentType.floatArg(0.1f, 4f))
                    .executes(ctx -> run(ctx, self, t -> {
                        ElementAuraManager.freeze(t, FloatArgumentType.getFloat(ctx, "gauge"));
                        return "frozen";
                    }))));
            b.then(Commands.literal("shatter").executes(ctx -> run(ctx, self,
                    t -> ElementReactions.blunt(t, ctx.getSource().getEntity()) ? "shattered" : "target is not frozen")));
            b.then(Commands.literal("clear").executes(ctx -> run(ctx, self, t -> { ElementAuraManager.clear(t); return "cleared"; })));
            b.then(Commands.literal("list").executes(ctx -> run(ctx, self, t -> {
                StringBuilder sb = new StringBuilder("auras:");
                ElementAuraManager.getAuras(t).forEach((k, a) -> sb.append(' ').append(k).append('=').append(String.format(Locale.ROOT, "%.2f", a.amount())));
                return sb.toString();
            })));
            event.getDispatcher().register(b);
        }
    }

    private static int run(CommandContext<CommandSourceStack> ctx, boolean self, Function<LivingEntity, String> action) {
        Entity e = self ? ctx.getSource().getEntity() : LevelDebugCommand.findTarget(ctx.getSource());
        if (!(e instanceof LivingEntity target)) {
            ctx.getSource().sendFailure(Component.literal("No valid target found!"));
            return 0;
        }
        String msg = action.apply(target);
        ctx.getSource().sendSuccess(() -> Component.literal(msg), false);
        return 1;
    }
}