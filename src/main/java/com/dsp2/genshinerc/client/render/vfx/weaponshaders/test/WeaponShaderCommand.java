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
            WeaponShaderMode.KALEIDOSCOPE,
            WeaponShaderMode.PIXELSORT,
            WeaponShaderMode.EDGEGLOW,
            WeaponShaderMode.WAVE,
            WeaponShaderMode.EMBOSS,
            WeaponShaderMode.CRT,
            WeaponShaderMode.GLITCH_SLICE,
            WeaponShaderMode.FISHEYE,
            WeaponShaderMode.RADIAL_BLUR,
            WeaponShaderMode.HSV_FILTER
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
                                () -> Component.literal("Switched to: " + mode.name()),
                                false
                        );
                        return 1;
                    })
            );
        }

        dispatcher.register(Commands.literal("stest")
                .executes(context -> {
                    WeaponShaderMode.currentMode = WeaponShaderMode.TEST_RED;
                    context.getSource().sendSuccess(
                            () -> Component.literal("Switched to: TEST_RED"),
                            false
                    );
                    return 1;
                })
        );

        dispatcher.register(Commands.literal("soff")
                .executes(context -> {
                    WeaponShaderMode.currentMode = WeaponShaderMode.NONE;
                    context.getSource().sendSuccess(
                            () -> Component.literal("Switched to: NONE"),
                            false
                    );
                    return 1;
                })
        );
    }
}