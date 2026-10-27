package com.dsp2.genshinerc.common.network.sync;

import com.dsp2.genshinerc.common.level.PlayerHudSync;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record SyncPlayerHudPacket(int level, float exhaustion) {
    public static void encode(SyncPlayerHudPacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.level);
        buf.writeFloat(msg.exhaustion);
    }

    public static SyncPlayerHudPacket decode(FriendlyByteBuf buf) {
        return new SyncPlayerHudPacket(buf.readVarInt(), buf.readFloat());
    }

    public static void handle(SyncPlayerHudPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> PlayerHudSync.applyOnClient(msg.level, msg.exhaustion));
        ctx.get().setPacketHandled(true);
    }
}