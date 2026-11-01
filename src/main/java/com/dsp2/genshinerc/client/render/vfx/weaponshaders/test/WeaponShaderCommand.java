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

    private static final String[] COMMANDS = {
            "s00", "s01", "s02", "s03", "s04", "s05", "s06", "s07", "s08", "s09",
            "s10", "s11", "s12", "s13", "s14", "s15", "s16", "s17", "s18", "s19", "s20"
    };

    private static final WeaponShaderMode[] MODES = {
            WeaponShaderMode.KALEIDOSCOPE, WeaponShaderMode.PIXELSORT, WeaponShaderMode.EDGEGLOW, WeaponShaderMode.WAVE, WeaponShaderMode.EMBOSS,
            WeaponShaderMode.CRT, WeaponShaderMode.GLITCH_SLICE, WeaponShaderMode.FISHEYE, WeaponShaderMode.RADIAL_BLUR, WeaponShaderMode.HSV_FILTER,
            WeaponShaderMode.DISTORT_WAVE, WeaponShaderMode.DISTORT_RADIAL, WeaponShaderMode.RGB_SPLIT, WeaponShaderMode.PIXELATE, WeaponShaderMode.SCANLINE,
            WeaponShaderMode.KALEIDO, WeaponShaderMode.GLITCH, WeaponShaderMode.FISHEYE_SCREEN, WeaponShaderMode.INVERT, WeaponShaderMode.DISTORT_TWIST, WeaponShaderMode.DISTORT_MIRROR
    };

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        for (int i = 0; i < COMMANDS.length; i++) {
            final WeaponShaderMode mode = MODES[i];
            final String cmdName = COMMANDS[i];

            dispatcher.register(Commands.literal(cmdName)
                    .executes(context -> {
                        WeaponShaderMode.currentMode = mode;
                        context.getSource().sendSuccess(
                                () -> Component.literal("§a[特效] 切换至: §e" + mode.name() + " §7(指令: /" + cmdName + ")"),
                                false
                        );
                        return 1;
                    })
            );
        }

        dispatcher.register(Commands.literal("stest")
                .executes(context -> {
                    WeaponShaderMode.currentMode = WeaponShaderMode.TEST_RED;
                    context.getSource().sendSuccess(() -> Component.literal("§a[特效] 切换至: §e原色刀光"), false);
                    return 1;
                })
        );

        dispatcher.register(Commands.literal("soff")
                .executes(context -> {
                    WeaponShaderMode.currentMode = WeaponShaderMode.NONE;
                    context.getSource().sendSuccess(() -> Component.literal("§a[特效] 已恢复原版 EFN 空气扰动"), false);
                    return 1;
                })
        );
    }
}