package com.dsp2.genshinerc.common.level.command.debug;

import com.dsp2.genshinerc.common.core.damage.ElementalStats;
import com.dsp2.genshinerc.common.core.reaction.DendroCoreEntity;
import com.dsp2.genshinerc.common.core.reaction.Reaction;
import com.dsp2.genshinerc.common.registry.ModEntities;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "genshinerc")
public final class DendroCoreDebugCommand {
    private DendroCoreDebugCommand() {}

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    private static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("gerc_debug")
                .then(Commands.literal("dendrocore")
                        .executes(context -> spawnCore(context.getSource())))
                .then(Commands.literal("hyperbloom")
                        .executes(context -> spawnHyperbloom(context.getSource()))));
    }

    private static int spawnCore(CommandSourceStack source) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        ServerLevel level = player.serverLevel();

        Vec3 direction = player.getLookAngle().normalize();
        Vec3 position = player.position()
                .add(direction.scale(1.35))
                .add(0.0, 0.65, 0.0);

        DendroCoreEntity core = new DendroCoreEntity(
                ModEntities.DENDRO_CORE.get(),
                level);

        core.setPos(position.x, position.y, position.z);
        core.setOwner(player.getUUID(), ElementalStats.of(player));

        if (!level.addFreshEntity(core)) {
            source.sendFailure(Component.literal("草原核生成失败。"));
            return 0;
        }

        source.sendSuccess(
                () -> Component.literal("已生成归属于你的草原核。"),
                false);

        return 1;
    }

    private static int spawnHyperbloom(CommandSourceStack source) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        ServerLevel level = player.serverLevel();

        Vec3 direction = player.getLookAngle().normalize();
        Vec3 position = player.position()
                .add(direction.scale(1.35))
                .add(0.0, 0.65, 0.0);

        DendroCoreEntity core = new DendroCoreEntity(
                ModEntities.DENDRO_CORE.get(),
                level);

        core.setPos(position.x, position.y, position.z);
        core.setOwner(player.getUUID(), ElementalStats.of(player));

        if (!level.addFreshEntity(core)) {
            source.sendFailure(Component.literal("超绽放导弹生成失败。"));
            return 0;
        }

        core.detonate(Reaction.HYPERBLOOM, player);

        if (!core.isHoming()) {
            source.sendFailure(Component.literal("附近没有可追踪的敌对目标，未生成导弹。"));
            return 0;
        }

        source.sendSuccess(
                () -> Component.literal("已生成归属于你的超绽放导弹。"),
                false);

        return 1;
    }
}