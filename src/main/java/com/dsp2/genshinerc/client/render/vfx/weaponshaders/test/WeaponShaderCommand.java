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
            "s00", "s01", "s02", "s03", "s04",
            "s05", "s06", "s07", "s08", "s09"
    };

    private static final WeaponShaderMode[] MODES = {
            WeaponShaderMode.KALEIDOSCOPE, // /s00
            WeaponShaderMode.PIXELSORT,    // /s01
            WeaponShaderMode.EDGEGLOW,     // /s02
            WeaponShaderMode.WAVE,         // /s03
            WeaponShaderMode.EMBOSS,       // /s04
            WeaponShaderMode.CRT,          // /s05
            WeaponShaderMode.GLITCH_SLICE, // /s06
            WeaponShaderMode.FISHEYE,      // /s07
            WeaponShaderMode.RADIAL_BLUR,  // /s08
            WeaponShaderMode.HSV_FILTER    // /s09
    };

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        // 循环注册 /s00 到 /s09
        for (int i = 0; i < COMMANDS.length; i++) {
            final WeaponShaderMode mode = MODES[i];
            final String cmdName = COMMANDS[i];

            dispatcher.register(Commands.literal(cmdName)
                    .executes(context -> {
                        WeaponShaderMode.currentMode = mode;
                        context.getSource().sendSuccess(
                                () -> Component.literal("§a[双刀刀光] 切换至: §e" + mode.name() + " §7(指令: /" + cmdName + ")"),
                                false
                        );
                        return 1;
                    })
            );
        }

        // 单独注册一个 /s-off 用来恢复原版
        dispatcher.register(Commands.literal("s-off")
                .executes(context -> {
                    WeaponShaderMode.currentMode = WeaponShaderMode.NONE;
                    context.getSource().sendSuccess(
                            () -> Component.literal("§a[双刀刀光] 已恢复原版 EFN 空气扰动"),
                            false
                    );
                    return 1;
                })
        );
    }
}