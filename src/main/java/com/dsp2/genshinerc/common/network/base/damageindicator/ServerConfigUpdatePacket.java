package com.dsp2.genshinerc.common.network.base.damageindicator;

import com.dsp2.genshinerc.common.config.ServerConfig;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ServerConfigUpdatePacket {
    public final boolean enabled;
    public final int maxDistance;

    public ServerConfigUpdatePacket(boolean enabled, int maxDistance) {
        this.enabled = enabled;
        this.maxDistance = maxDistance;
    }

    public ServerConfigUpdatePacket(FriendlyByteBuf b) {
        this.enabled = b.readBoolean();
        this.maxDistance = b.readVarInt();
    }

    public static void encode(ServerConfigUpdatePacket p, FriendlyByteBuf b) {
        b.writeBoolean(p.enabled);
        b.writeVarInt(p.maxDistance);
    }

    public static ServerConfigUpdatePacket decode(FriendlyByteBuf b) {
        return new ServerConfigUpdatePacket(b);
    }

    public static void handle(ServerConfigUpdatePacket p, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        ServerPlayer player = context.getSender();

        if (player == null) {
            context.setPacketHandled(true);
            return;
        }

        context.enqueueWork(() -> {
            MinecraftServer server = player.getServer();
            if (server == null) return;

            boolean isOwner = server.isSingleplayerOwner(player.getGameProfile());
            boolean isOp = player.hasPermissions(2);

            if (isOwner || isOp) {
                ServerConfig.ENABLED.set(p.enabled);
                ServerConfig.MAX_DISTANCE.set(p.maxDistance);
                ServerConfig.SPEC.save();

                for (ServerPlayer sp : server.getPlayerList().getPlayers()) {
                    DamageIndicatorNetworkHandler.syncConfigTo(sp,
                            new DamageIndicatorConfigSyncPacket(p.enabled, p.maxDistance));
                }
            }
        });
        context.setPacketHandled(true);
    }
}