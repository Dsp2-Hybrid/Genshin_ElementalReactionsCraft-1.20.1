package com.dsp2.genshinerc.common.network.sync.render;

import com.dsp2.genshinerc.client.render.hud.bar.api.ClientElementCache;
import com.dsp2.genshinerc.client.render.hud.bar.api.ElementManager;
import com.dsp2.genshinerc.client.render.hud.bar.api.ElementType;
import com.dsp2.genshinerc.common.network.GenshinercNetwork;
import com.dsp2.genshinerc.common.network.sync.packet.SyncElementsPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import java.util.List;

@Mod.EventBusSubscriber(modid = "genshinerc")
public class RenderElementSyncEvents {

    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (event.getTarget() instanceof LivingEntity living && event.getEntity() instanceof ServerPlayer player) {
            List<ElementType> elements = ElementManager.getElements(living);
            if (!elements.isEmpty()) {
                GenshinercNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                        new SyncElementsPacket(living.getId(), elements));
            }
        }
    }

    @SubscribeEvent
    public static void onLeave(EntityLeaveLevelEvent event) {
        if (event.getLevel().isClientSide()) {
            ClientElementCache.remove(event.getEntity().getId());
        }
    }
}