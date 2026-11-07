package com.dsp2.genshinerc.common.core.element;

import com.dsp2.genshinerc.common.core.reaction.ElementReactions;
import com.dsp2.genshinerc.common.core.reaction.ReactionResult;
import com.dsp2.genshinerc.common.level.command.LevelDebugCommand;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

@Mod.EventBusSubscriber(modid = "genshinerc")
public final class ElementDebugCommand {
    private static final float DEFAULT_UNITS = 2.0f;
    private static final float DEFAULT_CONSUME = 1.0f;

    private ElementDebugCommand() {}

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> d = event.getDispatcher();

        d.register(Commands.literal("d").requires(s -> s.hasPermission(2))
                .then(Commands.argument("id", IntegerArgumentType.integer(1, 7))
                        .executes(ctx -> apply(ctx, DEFAULT_UNITS))
                        .then(Commands.argument("units", FloatArgumentType.floatArg(0.1f, 8f))
                                .executes(ctx -> apply(ctx, FloatArgumentType.getFloat(ctx, "units"))))));

        d.register(Commands.literal("dr").requires(s -> s.hasPermission(2))
                .then(Commands.argument("id", IntegerArgumentType.integer(1, 7))
                        .executes(ctx -> consume(ctx, DEFAULT_CONSUME))
                        .then(Commands.argument("amount", FloatArgumentType.floatArg(0.01f, ElementGauge.MAX_GAUGE))
                                .executes(ctx -> consume(ctx, FloatArgumentType.getFloat(ctx, "amount"))))));

        d.register(Commands.literal("df").requires(s -> s.hasPermission(2))
                .then(Commands.argument("id", IntegerArgumentType.integer(1, 7))
                        .executes(ElementDebugCommand::flash)));

        d.register(Commands.literal("dc").requires(s -> s.hasPermission(2))
                .executes(ElementDebugCommand::clear));

        d.register(Commands.literal("da").requires(s -> s.hasPermission(2))
                .then(Commands.argument("id", IntegerArgumentType.integer(1, 7))
                        .executes(ctx -> react(ctx, 1.0f))
                        .then(Commands.argument("units", FloatArgumentType.floatArg(0.1f, 8f))
                                .executes(ctx -> react(ctx, FloatArgumentType.getFloat(ctx, "units"))))));
    }

    private static int apply(CommandContext<CommandSourceStack> ctx, float units) {
        ElementType type = ElementType.fromDebugId(IntegerArgumentType.getInteger(ctx, "id"));
        LivingEntity target = target(ctx);
        if (type == null || target == null) return fail(ctx);

        ElementAuraManager.applyUnits(target, type, units);
        float gauge = ElementGauge.gaugeFromUnits(units);
        float seconds = ElementGauge.durationTicksFromUnits(units) / 20f;
        ctx.getSource().sendSuccess(() -> Component.literal(String.format(Locale.ROOT,
                "Applied %s %.1fU (%.2f gauge, %.1fs) to %s",
                type.name(), units, gauge, seconds, target.getName().getString())), false);
        return 1;
    }

    private static int consume(CommandContext<CommandSourceStack> ctx, float amount) {
        ElementType type = ElementType.fromDebugId(IntegerArgumentType.getInteger(ctx, "id"));
        LivingEntity target = target(ctx);
        if (type == null || target == null) return fail(ctx);

        float used = ElementAuraManager.consume(target, type, amount, true);
        if (used <= 0f) {
            ctx.getSource().sendFailure(Component.literal("Target has no " + type.name() + " aura"));
            return 0;
        }
        ctx.getSource().sendSuccess(() -> Component.literal(String.format(Locale.ROOT,
                "Consumed %.2f %s gauge", used, type.name())), false);
        return 1;
    }

    private static int flash(CommandContext<CommandSourceStack> ctx) {
        ElementType type = ElementType.fromDebugId(IntegerArgumentType.getInteger(ctx, "id"));
        LivingEntity target = target(ctx);
        if (type == null || target == null) return fail(ctx);
        ElementAuraManager.flash(target, type);
        ctx.getSource().sendSuccess(() -> Component.literal("Flashed " + type.name()), false);
        return 1;
    }

    private static int clear(CommandContext<CommandSourceStack> ctx) {
        LivingEntity target = target(ctx);
        if (target == null) return fail(ctx);
        ElementAuraManager.clear(target);
        ctx.getSource().sendSuccess(() -> Component.literal("Cleared all auras on target"), false);
        return 1;
    }

    private static int react(CommandContext<CommandSourceStack> ctx, float units) {
        ElementType type = ElementType.fromDebugId(IntegerArgumentType.getInteger(ctx, "id"));
        LivingEntity target = target(ctx);
        if (type == null || target == null) return fail(ctx);
        ReactionResult result = ElementReactions.trigger(target, ctx.getSource().getEntity(), type, units);
        ctx.getSource().sendSuccess(() -> Component.literal("Reactions: " + result.reactions()
                + " amplify " + result.amplify() + " flat " + result.catalyzeFlat()), false);
        return 1;
    }

    @Nullable
    private static LivingEntity target(CommandContext<CommandSourceStack> ctx) {
        Entity e = LevelDebugCommand.findTarget(ctx.getSource());
        return e instanceof LivingEntity living ? living : null;
    }

    private static int fail(CommandContext<CommandSourceStack> ctx) {
        ctx.getSource().sendFailure(Component.literal("No valid target found!"));
        return 0;
    }
}
