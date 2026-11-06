package com.dsp2.genshinerc.common.world.entity.net;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;
public class SyncTacticPacket {
    public int entityId;
    public String cursor;
    public SyncTacticPacket() { }
    public SyncTacticPacket(int entityId, String cursor) { this.entityId = entityId; this.cursor = cursor; }
    public void encode(FriendlyByteBuf buf) { buf.writeVarInt(entityId); buf.writeUtf(cursor); }
    public static SyncTacticPacket decode(FriendlyByteBuf buf) { return new SyncTacticPacket(buf.readVarInt(), buf.readUtf()); }
    public void handle(Supplier<NetworkEvent.Context> ctx) { ctx.get().setPacketHandled(true); }
}