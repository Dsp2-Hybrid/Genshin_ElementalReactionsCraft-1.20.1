package com.dsp2.genshinerc.client.render.vfx.weaponshaders.test;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

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

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        for (int i = 0; i < MODES.length; i++) {
            final WeaponShaderMode mode = MODES[i];
            final String cmdName = String.format("s%02d", i);

            dispatcher.register(Commands.literal(cmdName)
                    .executes(context -> {
                        WeaponShaderMode.currentMode = mode;
                        context.getSource().sendSuccess(() -> Component.literal("§a[特效] 切换至: §e" + mode.name() + " §7(/" + cmdName + ")"), false);
                        return 1;
                    })
            );
        }

        dispatcher.register(Commands.literal("stest").executes(context -> {
            WeaponShaderMode.currentMode = WeaponShaderMode.TEST_RED;
            context.getSource().sendSuccess(() -> Component.literal("§a[特效] 切换至: 原版测试"), false);
            return 1;
        }));

        dispatcher.register(Commands.literal("soff").executes(context -> {
            WeaponShaderMode.currentMode = WeaponShaderMode.NONE;
            context.getSource().sendSuccess(() -> Component.literal("§a[特效] 已恢复原版"), false);
            return 1;
        }));
    }
}