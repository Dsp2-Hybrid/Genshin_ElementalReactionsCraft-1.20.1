package com.dsp2.genshinerc.common.level.command.debug;

import com.dsp2.genshinerc.client.render.hud.bar.config.BarConfig;
import com.dsp2.genshinerc.client.render.hud.gui.rpg.RpgHudElement;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Locale;

//CLIENT SIDE
@Mod.EventBusSubscriber(modid = "genshinerc", value = Dist.CLIENT)
public class HudDumpCommand {

    @SubscribeEvent
    public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("dumphud")
                        .executes(context -> {
                            BarConfig cfg = BarConfig.get();
                            Minecraft mc = Minecraft.getInstance();
                            if (mc.player == null) return 0;

                            mc.player.sendSystemMessage(Component.literal("§e=== RPG HUD Element Dump ==="));
                            mc.player.sendSystemMessage(Component.literal("§7Current Box Configuration (x, y, w, h, flipX, flipY):"));

                            for (RpgHudElement e : RpgHudElement.values()) {
                                RpgHudElement.Box b = e.get(cfg);
                                String text = String.format(Locale.ROOT, "%s(%.1ff, %.1ff, %.1ff, %.1ff, %b, %b)",
                                        e.name(), b.x(), b.y(), b.aw(), b.ah(), b.flipX(), b.flipY());
                                mc.player.sendSystemMessage(Component.literal("§a" + text));
                            }

                            mc.player.sendSystemMessage(Component.literal("§e==========================="));
                            return 1;
                        })
        );
    }
}