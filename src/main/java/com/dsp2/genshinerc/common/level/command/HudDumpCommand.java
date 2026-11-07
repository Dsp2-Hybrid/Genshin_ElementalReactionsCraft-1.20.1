package com.dsp2.genshinerc.common.level.command;

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

                            mc.player.sendSystemMessage(Component.literal("搂e=== RPG HUD 褰撳墠鍧愭爣鏁版嵁 ==="));
                            mc.player.sendSystemMessage(Component.literal("搂7璇峰鍒朵互涓嬫枃鏈彂缁欎綔鑰呬綔涓烘柊鐨勯粯璁ゅ€?"));

                            for (RpgHudElement e : RpgHudElement.values()) {
                                RpgHudElement.Box b = e.get(cfg);
                                String text = String.format(Locale.ROOT, "%s(%.1ff, %.1ff, %.1ff, %.1ff, %b, %b)",
                                        e.name(), b.x(), b.y(), b.aw(), b.ah(), b.flipX(), b.flipY());
                                mc.player.sendSystemMessage(Component.literal("搂a" + text));
                            }

                            mc.player.sendSystemMessage(Component.literal("搂e==========================="));
                            return 1;
                        })
        );
    }
}