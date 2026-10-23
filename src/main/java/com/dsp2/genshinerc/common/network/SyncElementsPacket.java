package com.dsp2.genshinerc.common.network;

import com.dsp2.genshinerc.client.render.bar.api.ClientElementCache;
import com.dsp2.genshinerc.client.render.bar.api.ElementType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public record SyncElementsPacket(int entityId, List<ElementType> elements) {

    public static void encode(SyncElementsPacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.entityId);
        buf.writeCollection(msg.elements, FriendlyByteBuf::writeEnum);
    }

    public static SyncElementsPacket decode(FriendlyByteBuf buf) {
        int id = buf.readVarInt();
        List<ElementType> list = buf.readCollection(ArrayList::new, b -> b.readEnum(ElementType.class));
        return new SyncElementsPacket(id, list);
    }

    public static void handle(SyncElementsPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ClientElementCache.set(msg.entityId, msg.elements);
    }
}