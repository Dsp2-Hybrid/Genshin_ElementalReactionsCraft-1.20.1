package com.dsp2.genshinerc.client.render.vfx.weaponshaders.test;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = "genshinerc")
public class WeaponShaderCommand {

    private static final WeaponShaderMode[] MODES = {
        WeaponShaderMode.KALEIDOSCOPE, WeaponShaderMode.PIXELSORT, WeaponShaderMode.EDGEGLOW, WeaponShaderMode.WAVE, WeaponShaderMode.EMBOSS,
        WeaponShaderMode.CRT, WeaponShaderMode.GLITCH_SLICE, WeaponShaderMode.FISHEYE, WeaponShaderMode.RADIAL_BLUR, WeaponShaderMode.HSV_FILTER,
        WeaponShaderMode.TWIST, WeaponShaderMode.PULSE, WeaponShaderMode.RGBSPLIT, WeaponShaderMode.PIXELATE, WeaponShaderMode.SQUASH,
        WeaponShaderMode.MIRROR, WeaponShaderMode.VORTEX, WeaponShaderMode.SOBEL_EDGE, WeaponShaderMode.HALFTONE, WeaponShaderMode.OIL_PAINT,
        WeaponShaderMode.NEON_GLOW, WeaponShaderMode.THERMAL, WeaponShaderMode.POSTERIZE, WeaponShaderMode.RIPPLE, WeaponShaderMode.NOISE_GRAIN,
        WeaponShaderMode.ZOOM_BLUR, WeaponShaderMode.DIAMOND_GRID, WeaponShaderMode.HEXAGON, WeaponShaderMode.CHROMATIC, WeaponShaderMode.BLOOM
    };

    private static int indexOf(WeaponShaderMode m) {
        for (int i = 0; i < MODES.length; i++) if (MODES[i] == m) return i;
        return -1;
    }

    private static final SuggestionProvider<CommandSourceStack> PARAM_SUGGESTIONS = (ctx, builder) -> {
        List<String> names = new ArrayList<>();
        for (ParamSpec spec : WeaponShaderMode.currentMode.params) names.add(spec.name());
        return SharedSuggestionProvider.suggest(names, builder);
    };

    private static void printAll(WeaponShaderMode mode, CommandSourceStack src) {
        StringBuilder sb = new StringBuilder("\u00a7a[VFX] " + mode.name());
        for (ParamSpec spec : mode.params) {
            sb.append(" \u00a77").append(spec.name()).append("=\u00a7e")
              .append(String.format("%.2f", WeaponShaderParams.get(mode, spec.name())))
              .append("\u00a78[").append(String.format("%.2f", spec.min())).append("-")
              .append(String.format("%.2f", spec.max())).append("]");
        }
        src.sendSuccess(() -> Component.literal(sb.toString()), false);
    }

    private static void printOne(WeaponShaderMode mode, ParamSpec spec, CommandSourceStack src) {
        src.sendSuccess(() -> Component.literal("\u00a7a[VFX] " + mode.name() + " \u00a77" + spec.name()
                + "=\u00a7e" + String.format("%.2f", WeaponShaderParams.get(mode, spec.name()))
                + " \u00a78[" + String.format("%.2f", spec.min()) + " - " + String.format("%.2f", spec.max()) + "]"), false);
    }

    @SubscribeEvent
    public static void onRegister(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> d = event.getDispatcher();

        for (WeaponShaderMode mode : MODES) {
            int i = indexOf(mode);
            if (i < 0) continue;
            LiteralArgumentBuilder<CommandSourceStack> root =
                Commands.literal(String.format("s%02d", i))
                    .executes(ctx -> {
                        WeaponShaderMode.currentMode = mode;
                        printAll(mode, ctx.getSource());
                        return 1;
                    });
            d.register(root);
        }

        d.register(Commands.literal("sp")
                .executes(ctx -> {
                    printAll(WeaponShaderMode.currentMode, ctx.getSource());
                    return 1;
                })
                .then(Commands.argument("name", StringArgumentType.word())
                        .suggests(PARAM_SUGGESTIONS)
                        .executes(ctx -> {
                            String name = StringArgumentType.getString(ctx, "name");
                            WeaponShaderMode m = WeaponShaderMode.currentMode;
                            for (ParamSpec spec : m.params) {
                                if (spec.name().equals(name)) {
                                    printOne(m, spec, ctx.getSource());
                                    return 1;
                                }
                            }
                            ctx.getSource().sendFailure(Component.literal(
                                    "\u00a7c[VFX] " + m.name() + " \u6ca1\u6709\u53c2\u6570 " + name));
                            return 0;
                        })
                        .then(Commands.argument("value", FloatArgumentType.floatArg(-100f, 100f))
                                .executes(ctx -> {
                                    String name = StringArgumentType.getString(ctx, "name");
                                    float value = FloatArgumentType.getFloat(ctx, "value");
                                    WeaponShaderMode m = WeaponShaderMode.currentMode;
                                    if (!WeaponShaderParams.set(m, name, value)) {
                                        ctx.getSource().sendFailure(Component.literal(
                                                "\u00a7c[VFX] " + m.name() + " \u6ca1\u6709\u53c2\u6570 " + name));
                                        return 0;
                                    }
                                    for (ParamSpec spec : m.params) {
                                        if (spec.name().equals(name)) {
                                            printOne(m, spec, ctx.getSource());
                                            break;
                                        }
                                    }
                                    return 1;
                                })
                        )
                )
        );

        d.register(Commands.literal("sreset")
                .executes(ctx -> {
                    WeaponShaderParams.reset(WeaponShaderMode.currentMode);
                    printAll(WeaponShaderMode.currentMode, ctx.getSource());
                    return 1;
                })
                .then(Commands.literal("all").executes(ctx -> {
                    WeaponShaderParams.resetAll();
                    ctx.getSource().sendSuccess(() -> Component.literal("\u00a7a[VFX] \u5168\u90e8\u6a21\u5f0f\u53c2\u6570\u5df2\u91cd\u7f6e"), false);
                    return 1;
                }))
        );

        d.register(Commands.literal("s0").executes(ctx -> {
            WeaponShaderMode.currentMode = WeaponShaderMode.TEST_RED;
            printAll(WeaponShaderMode.TEST_RED, ctx.getSource());
            return 1;
        }));

        d.register(Commands.literal("soff").executes(ctx -> {
            WeaponShaderMode.currentMode = WeaponShaderMode.NONE;
            ctx.getSource().sendSuccess(() -> Component.literal("\u00a7a[VFX] \u5df2\u6062\u590d\u539f\u7248"), false);
            return 1;
        }));
    }
}