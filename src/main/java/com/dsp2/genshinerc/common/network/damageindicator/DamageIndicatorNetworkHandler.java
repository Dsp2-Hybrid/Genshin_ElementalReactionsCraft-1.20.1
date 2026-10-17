package com.dsp2.genshinerc.common.network.damageindicator;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class DamageIndicatorNetworkHandler {
    private static final String P = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            ResourceLocation.fromNamespaceAndPath("genshinerc", "damage_indicator"),
            () -> P, P::equals, P::equals);

    public static volatile int currentRange = 35;

    public static void register() {
        int id = 0;
        CHANNEL.registerMessage(id++, DamagePacket.class, DamagePacket::encode, DamagePacket::decode, DamagePacket::handle);
        CHANNEL.registerMessage(id++, DamageIndicatorConfigSyncPacket.class, DamageIndicatorConfigSyncPacket::encode, DamageIndicatorConfigSyncPacket::decode, DamageIndicatorConfigSyncPacket::handle);
        CHANNEL.registerMessage(id++, ServerConfigUpdatePacket.class, ServerConfigUpdatePacket::encode, ServerConfigUpdatePacket::decode, ServerConfigUpdatePacket::handle);
    }

    public static void sendDamage(LivingEntity e, String k, float a, boolean c, boolean kl, boolean fh,
                                  float ab, String oc, Float os, boolean absorbDelta, boolean secondary) {
        if (e.level().isClientSide()) return;
        if (e.getServer() == null) return;
        DamagePacket p = new DamagePacket(e.getId(), k, a, c, kl, fh, ab, oc, os, absorbDelta, secondary);
        double r = currentRange;
        double rSq = r * r;
        for (ServerPlayer sp : e.getServer().getPlayerList().getPlayers()) {
            if (sp.distanceToSqr(e) <= rSq) {
                CHANNEL.send(PacketDistributor.PLAYER.with(() -> sp), p);
            }
        }
    }

    public static void syncConfigTo(ServerPlayer player, DamageIndicatorConfigSyncPacket packet) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
        currentRange = packet.maxDistance;
    }
}