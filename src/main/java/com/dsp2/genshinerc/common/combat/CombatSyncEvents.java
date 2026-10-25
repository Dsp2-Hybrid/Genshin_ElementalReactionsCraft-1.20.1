package com.dsp2.genshinerc.common.combat;

import com.dsp2.genshinerc.common.combat.shield.ShieldManager;
import com.dsp2.genshinerc.common.combat.zhenmie.ZhenMieManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "genshinerc")
public final class CombatSyncEvents {
    private CombatSyncEvents() {}

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        ShieldManager.tick();
        ZhenMieManager.tick();
    }

    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (event.getTarget() instanceof LivingEntity living && event.getEntity() instanceof ServerPlayer player) {
            ShieldManager.sendTo(player, living);
            ZhenMieManager.sendTo(player, living);
        }
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        ShieldManager.reset();
        ZhenMieManager.reset();
    }
}