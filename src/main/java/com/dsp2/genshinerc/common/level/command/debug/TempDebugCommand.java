package com.dsp2.genshinerc.common.level.command.debug;

import com.dsp2.genshinerc.client.render.hud.bar.element.ClientAuraCache;
import com.dsp2.genshinerc.client.render.hud.bar.element.EntityAuras;
import com.dsp2.genshinerc.client.render.world.component.damageindicator.config.DamageIndicatorConfig;
import com.dsp2.genshinerc.client.render.world.component.damageindicator.event.DamageTracker;
import com.dsp2.genshinerc.common.core.element.ElementType;
import com.dsp2.genshinerc.common.core.reaction.Reaction;
import com.dsp2.genshinerc.common.core.reaction.ReactionColors;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Locale;

@Mod.EventBusSubscriber(modid = "genshinerc", value = Dist.CLIENT)
public final class TempDebugCommand {
    private TempDebugCommand() {}

    @SubscribeEvent
    public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> d = event.getDispatcher();
        Reaction[] reactions = Reaction.values();
        
        for (int i = 0; i < reactions.length; i++) {
            final int id = 100 + i;
            final Reaction r = reactions[i];
            d.register(Commands.literal(String.valueOf(id))
                    .executes(ctx -> testReaction(ctx, r)));
        }

        d.register(Commands.literal("116")
                .executes(TempDebugCommand::showAuras));
    }

    private static int testReaction(CommandContext<CommandSourceStack> ctx, Reaction r) {
        if (Minecraft.getInstance().player == null) return 0;

        String name = Component.translatable("genshinerc.reaction." + r.name().toLowerCase(Locale.ROOT)).getString();
        
        ReactionColors.Ends ends = ReactionColors.ends(r, null, null);
        
        float scale = (float) DamageIndicatorConfig.get().normalSize;
        DamageTracker.addCustomText(Minecraft.getInstance().player, name, ends.a(), ends.b(), scale);
        
        ctx.getSource().sendSuccess(() -> Component.literal(String.format(Locale.ROOT, "§aTriggered: %s", name)), false);
        return 1;
    }

    private static int showAuras(CommandContext<CommandSourceStack> ctx) {
        if (Minecraft.getInstance().player == null) return 0;
        
        EntityAuras auras = ClientAuraCache.get(Minecraft.getInstance().player.getId());
        if (auras == null) {
            ctx.getSource().sendSuccess(() -> Component.literal("§eNo auras present."), false);
            return 1;
        }

        ctx.getSource().sendSuccess(() -> Component.literal("§e==== Current Auras ===="), false);
        
        boolean found = false;
        for (ElementType type : ElementType.values()) {
            float gauge = auras.gauge(type);
            if (gauge > 0.01f) {
                found = true;
                ctx.getSource().sendSuccess(() -> Component.literal(String.format(Locale.ROOT, "§b%s: %.2f", type.name(), gauge)), false);
            }
        }
        
        if (!found) {
            ctx.getSource().sendSuccess(() -> Component.literal("§7Empty."), false);
        }
        
        return 1;
    }
}