package com.dsp2.genshinerc.common.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class NetworkHandler {
    private static final String P = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            ResourceLocation.fromNamespaceAndPath("genshinerc", "damage_indicator"),
            () -> P, P::equals, P::equals);

    public static volatile double currentRange = 35.0;

    public static void register() {
        int id = 0;
        CHANNEL.registerMessage(id++, DamagePacket.class, DamagePacket::encode, DamagePacket::decode, DamagePacket::handle);
        CHANNEL.registerMessage(id++, ConfigSyncPacket.class, ConfigSyncPacket::encode, ConfigSyncPacket::decode, ConfigSyncPacket::handle);
    }

    public static void sendDamage(LivingEntity e, String k, float a, boolean c, boolean kl, float ab, String oc, Float os) {
        if (e.level().isClientSide()) return;
        if (e.getServer() == null) return;
        DamagePacket p = new DamagePacket(e.getId(), k, a, c, kl, ab, oc, os);
        double r = currentRange;
        double rSq = r * r;
        for (ServerPlayer sp : e.getServer().getPlayerList().getPlayers()) {
            if (sp.distanceToSqr(e) <= rSq) {
                CHANNEL.send(PacketDistributor.PLAYER.with(() -> sp), p);
            }
        }
    }

    public static void syncConfigTo(ServerPlayer player, ConfigSyncPacket packet) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
        currentRange = packet.maxDistance;
    }
}