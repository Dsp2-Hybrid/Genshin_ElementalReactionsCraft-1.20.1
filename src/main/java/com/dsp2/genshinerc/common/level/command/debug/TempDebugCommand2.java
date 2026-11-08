package com.dsp2.genshinerc.common.level.command.debug;

import com.dsp2.genshinerc.common.api.ElementalCombatAPI;
import com.dsp2.genshinerc.common.core.combat.shield.ShieldManager;
import com.dsp2.genshinerc.common.core.element.ElementAuraManager;
import com.dsp2.genshinerc.common.core.element.ElementGauge;
import com.dsp2.genshinerc.common.core.element.ElementType;
import com.dsp2.genshinerc.common.core.element.GaugeTier;
import com.dsp2.genshinerc.common.core.reaction.ElementReactions;
import com.dsp2.genshinerc.common.core.reaction.ReactionResult;
import com.dsp2.genshinerc.common.level.EntityLevelManager;
import com.dsp2.genshinerc.common.level.WorldLevel;
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
public final class TempDebugCommand2 {
    private static final float DEFAULT_UNITS = 2.0f;
    private static final float DEFAULT_CONSUME = 1.0f;
    private TempDebugCommand2() {}
    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> d = event.getDispatcher();
        d.register(Commands.literal("d").requires(s -> s.hasPermission(2)).then(Commands.argument("id", IntegerArgumentType.integer(1, 7)).executes(ctx -> apply(ctx, DEFAULT_UNITS)).then(Commands.argument("units", FloatArgumentType.floatArg(0.1f, 8f)).executes(ctx -> apply(ctx, FloatArgumentType.getFloat(ctx, "units"))))));
        d.register(Commands.literal("dr").requires(s -> s.hasPermission(2)).then(Commands.argument("id", IntegerArgumentType.integer(1, 7)).executes(ctx -> consume(ctx, DEFAULT_CONSUME)).then(Commands.argument("amount", FloatArgumentType.floatArg(0.01f, ElementGauge.MAX_GAUGE)).executes(ctx -> consume(ctx, FloatArgumentType.getFloat(ctx, "amount"))))));
        d.register(Commands.literal("df").requires(s -> s.hasPermission(2)).then(Commands.argument("id", IntegerArgumentType.integer(1, 7)).executes(TempDebugCommand2::flash)));
        d.register(Commands.literal("dc").requires(s -> s.hasPermission(2)).executes(TempDebugCommand2::clear));
        d.register(Commands.literal("da").requires(s -> s.hasPermission(2)).then(Commands.argument("id", IntegerArgumentType.integer(1, 7)).executes(ctx -> react(ctx, 1.0f)).then(Commands.argument("units", FloatArgumentType.floatArg(0.1f, 8f)).executes(ctx -> react(ctx, FloatArgumentType.getFloat(ctx, "units"))))));
        d.register(Commands.literal("dh").requires(s -> s.hasPermission(2)).then(Commands.argument("id", IntegerArgumentType.integer(1, 7)).then(Commands.argument("damage", FloatArgumentType.floatArg(0.1f, 100000f)).executes(ctx -> {
            ElementType type = ElementType.fromDebugId(IntegerArgumentType.getInteger(ctx, "id"));
            LivingEntity t = target(ctx);
            if (type == null || t == null) return fail(ctx);
            float dmg = FloatArgumentType.getFloat(ctx, "damage");
            float dealt = ElementalCombatAPI.deal(t, ctx.getSource().getEntity(), type, dmg, true, GaugeTier.STRONG, "debug:hit");
            ctx.getSource().sendSuccess(() -> Component.literal(String.format(Locale.ROOT, "Dealt %.2f %s (%.2f applied)", dmg, type.name(), dealt)), false);
            return 1;
        }))));
        d.register(Commands.literal("dinfo").requires(s -> s.hasPermission(2)).executes(ctx -> {
            LivingEntity t = target(ctx);
            if (t == null) return fail(ctx);
            var auras = ElementAuraManager.getAuras(t);
            if (auras.isEmpty()) ctx.getSource().sendSuccess(() -> Component.literal("No auras"), false);
            auras.forEach((type, a) -> ctx.getSource().sendSuccess(() -> Component.literal(String.format(Locale.ROOT, "%s = %.3f gauge (decay %.5f/tick)", type.name(), a.amount(), a.decayPerTick())), false));
            ctx.getSource().sendSuccess(() -> Component.literal("Level " + EntityLevelManager.getLevel(t) + " | WL " + WorldLevel.get() + " | Shields " + ShieldManager.get(t).size()), false);
            return 1;
        }));
        d.register(Commands.literal("dw").requires(s -> s.hasPermission(2)).then(Commands.argument("level", IntegerArgumentType.integer(0, 8)).executes(ctx -> {
            int wl = IntegerArgumentType.getInteger(ctx, "level");
            WorldLevel.set(wl);
            ctx.getSource().sendSuccess(() -> Component.literal("World Level = " + wl), false);
            return 1;
        })));
    }
    private static int apply(CommandContext<CommandSourceStack> ctx, float units) {
        ElementType type = ElementType.fromDebugId(IntegerArgumentType.getInteger(ctx, "id"));
        LivingEntity target = target(ctx);
        if (type == null || target == null) return fail(ctx);
        ElementAuraManager.applyUnits(target, type, units);
        float gauge = ElementGauge.gaugeFromUnits(units);
        float seconds = ElementGauge.durationTicksFromUnits(units) / 20f;
        ctx.getSource().sendSuccess(() -> Component.literal(String.format(Locale.ROOT, "Applied %s %.1fU (%.2f gauge, %.1fs) to %s", type.name(), units, gauge, seconds, target.getName().getString())), false);
        return 1;
    }
    private static int consume(CommandContext<CommandSourceStack> ctx, float amount) {
        ElementType type = ElementType.fromDebugId(IntegerArgumentType.getInteger(ctx, "id"));
        LivingEntity target = target(ctx);
        if (type == null || target == null) return fail(ctx);
        float used = ElementAuraManager.consume(target, type, amount, true);
        if (used <= 0f) { ctx.getSource().sendFailure(Component.literal("Target has no " + type.name() + " aura")); return 0; }
        ctx.getSource().sendSuccess(() -> Component.literal(String.format(Locale.ROOT, "Consumed %.2f %s gauge", used, type.name())), false);
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
        ctx.getSource().sendSuccess(() -> Component.literal("Reactions: " + result.reactions() + " amplify " + result.amplify() + " flat " + result.catalyzeFlat()), false);
        return 1;
    }
    @Nullable
    private static LivingEntity target(CommandContext<CommandSourceStack> ctx) { Entity e = LevelDebugCommand.findTarget(ctx.getSource()); return e instanceof LivingEntity living ? living : null; }
    private static int fail(CommandContext<CommandSourceStack> ctx) { ctx.getSource().sendFailure(Component.literal("No valid target found!")); return 0; }
}