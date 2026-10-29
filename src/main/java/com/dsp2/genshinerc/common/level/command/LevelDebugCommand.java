package com.dsp2.genshinerc.common.level.command;

import com.dsp2.genshinerc.common.core.combat.shield.ShieldKind;
import com.dsp2.genshinerc.common.core.combat.shield.ShieldManager;
import com.dsp2.genshinerc.common.core.combat.zhenmie.AttackIntensity;
import com.dsp2.genshinerc.common.core.combat.zhenmie.DefenseOutcome;
import com.dsp2.genshinerc.common.core.combat.zhenmie.ZhenMieManager;
import com.dsp2.genshinerc.common.core.element.ElementType;
import com.dsp2.genshinerc.common.level.EntityLevelManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
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
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Mod.EventBusSubscriber(modid = "genshinerc")
public class LevelDebugCommand {

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

        dispatcher.register(Commands.literal("ds").requires(s -> s.hasPermission(2))
                .then(Commands.argument("type", IntegerArgumentType.integer(0, 7))
                        .then(Commands.argument("amount", FloatArgumentType.floatArg(0.1f, 100000f))
                                .executes(ctx -> grantShield(ctx, -1))
                                .then(Commands.argument("seconds", FloatArgumentType.floatArg(0.05f, 3600f))
                                        .executes(ctx -> grantShield(ctx, Math.round(FloatArgumentType.getFloat(ctx, "seconds") * 20f)))))));

        dispatcher.register(Commands.literal("dss").requires(s -> s.hasPermission(2))
                .then(Commands.argument("type", IntegerArgumentType.integer(0, 7))
                        .then(Commands.argument("amount", FloatArgumentType.floatArg(0.1f, 100000f))
                                .executes(ctx -> grantShieldToSelf(ctx, -1))
                                .then(Commands.argument("seconds", FloatArgumentType.floatArg(0.05f, 3600f))
                                        .executes(ctx -> grantShieldToSelf(ctx, Math.round(FloatArgumentType.getFloat(ctx, "seconds") * 20f)))))));

        dispatcher.register(Commands.literal("dsh").requires(s -> s.hasPermission(2))
                .then(Commands.argument("damage", FloatArgumentType.floatArg(0.01f, 100000f))
                        .executes(LevelDebugCommand::hitShield)));

        dispatcher.register(Commands.literal("dsc").requires(s -> s.hasPermission(2))
                .executes(ctx -> withTarget(ctx, t -> { ShieldManager.clear(t); return "Cleared shields"; })));

        dispatcher.register(Commands.literal("dscs").requires(s -> s.hasPermission(2))
                .executes(ctx -> {
                    Player player = ctx.getSource().getPlayer();
                    if (player == null) {
                        ctx.getSource().sendFailure(Component.literal("Must be run by a player!"));
                        return 0;
                    }
                    ShieldManager.clear(player);
                    ctx.getSource().sendSuccess(() -> Component.literal("Cleared own shields"), false);
                    return 1;
                }));

        dispatcher.register(Commands.literal("dz").requires(s -> s.hasPermission(2))
                .then(Commands.argument("zhen", FloatArgumentType.floatArg(-1f, 1f))
                        .then(Commands.argument("mie", FloatArgumentType.floatArg(-1f, 1f))
                                .executes(ctx -> withTarget(ctx, t -> {
                                    ZhenMieManager.add(t, FloatArgumentType.getFloat(ctx, "zhen"), FloatArgumentType.getFloat(ctx, "mie"));
                                    return zmStatus(t);
                                })))));

        LiteralArgumentBuilder<CommandSourceStack> dzd = Commands.literal("dzd").requires(s -> s.hasPermission(2));
        for (AttackIntensity intensity : AttackIntensity.values()) {
            LiteralArgumentBuilder<CommandSourceStack> lit = Commands.literal(intensity.name().toLowerCase(Locale.ROOT));
            for (DefenseOutcome outcome : DefenseOutcome.values()) {
                lit.then(Commands.literal(outcome.name().toLowerCase(Locale.ROOT))
                        .executes(ctx -> withTarget(ctx, t -> {
                            ZhenMieManager.applyDefense(t, intensity, outcome);
                            return intensity + " " + outcome + " -> " + zmStatus(t);
                        })));
            }
            dzd.then(lit);
        }
        dispatcher.register(dzd);

        dispatcher.register(Commands.literal("dzb").requires(s -> s.hasPermission(2))
                .then(Commands.argument("zhenBuff", BoolArgumentType.bool())
                        .then(Commands.argument("mieBuff", BoolArgumentType.bool())
                                .executes(ctx -> withTarget(ctx, t -> {
                                    ZhenMieManager.setBuffs(t, BoolArgumentType.getBool(ctx, "zhenBuff"), BoolArgumentType.getBool(ctx, "mieBuff"));
                                    return "Buffs updated";
                                })))));

        dispatcher.register(Commands.literal("dzc").requires(s -> s.hasPermission(2))
                .executes(ctx -> withTarget(ctx, t -> { ZhenMieManager.clear(t); return "Cleared Zhen-Mie"; })));
    }

    private static int grantShield(CommandContext<CommandSourceStack> ctx, int durationTicks) {
        return withTarget(ctx, t -> addShield(ctx, t, durationTicks));
    }

    private static int grantShieldToSelf(CommandContext<CommandSourceStack> ctx, int durationTicks) {
        Player player = ctx.getSource().getPlayer();
        if (player == null) {
            ctx.getSource().sendFailure(Component.literal("Must be run by a player!"));
            return 0;
        }
        String msg = addShield(ctx, player, durationTicks);
        ctx.getSource().sendSuccess(() -> Component.literal(msg), false);
        return 1;
    }

    private static String addShield(CommandContext<CommandSourceStack> ctx, LivingEntity target, int durationTicks) {
        int typeId = IntegerArgumentType.getInteger(ctx, "type");
        float amount = FloatArgumentType.getFloat(ctx, "amount");
        ElementType element = typeId == 0 ? null : ElementType.fromDebugId(typeId);
        ShieldKind kind = element == null ? ShieldKind.SPECIAL : ShieldKind.ELEMENTAL;
        ShieldManager.add(target, kind, element, amount, durationTicks);
        return String.format(Locale.ROOT, "Granted %s shield %.1f (%s)", element == null ? "special" : element.name(),
                amount, durationTicks < 0 ? "permanent" : durationTicks / 20f + "s");
    }

    private static int hitShield(CommandContext<CommandSourceStack> ctx) {
        float damage = FloatArgumentType.getFloat(ctx, "damage");
        return withTarget(ctx, t -> String.format(Locale.ROOT, "Absorbed %.1f, largest left %.1f",
                ShieldManager.absorb(t, damage), ShieldManager.largest(t)));
    }

    private static String zmStatus(LivingEntity t) {
        return String.format(Locale.ROOT, "zhen %.2f, mie %.2f", ZhenMieManager.zhen(t), ZhenMieManager.mie(t));
    }

    private interface TargetAction { String run(LivingEntity target); }

    private static int withTarget(CommandContext<CommandSourceStack> ctx, TargetAction action) {
        LivingEntity target = target(ctx);
        if (target == null) {
            ctx.getSource().sendFailure(Component.literal("No valid target found!"));
            return 0;
        }
        String msg = action.run(target);
        ctx.getSource().sendSuccess(() -> Component.literal(msg), false);
        return 1;
    }

    @Nullable
    private static LivingEntity target(CommandContext<CommandSourceStack> ctx) {
        Entity e = findTarget(ctx.getSource());
        return e instanceof LivingEntity living ? living : null;
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