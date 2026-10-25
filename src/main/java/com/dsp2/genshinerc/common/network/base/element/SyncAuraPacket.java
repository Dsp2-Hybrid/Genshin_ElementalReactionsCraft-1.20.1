package com.dsp2.genshinerc.common.network.base.element;

import com.dsp2.genshinerc.client.render.hud.bar.element.ClientAuraCache;
import com.dsp2.genshinerc.common.core.element.ElementType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Supplier;

public record SyncAuraPacket(int entityId, List<Entry> auras, EnumSet<ElementType> flashed) {

    public record Entry(ElementType type, float amount, float decayPerTick) {}

    public static void encode(SyncAuraPacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.entityId);
        buf.writeCollection(msg.auras, (b, e) -> {
            b.writeEnum(e.type());
            b.writeFloat(e.amount());
            b.writeFloat(e.decayPerTick());
        });
        buf.writeEnumSet(msg.flashed, ElementType.class);
    }

    public static SyncAuraPacket decode(FriendlyByteBuf buf) {
        int id = buf.readVarInt();
        List<Entry> auras = buf.readCollection(ArrayList::new,
                b -> new Entry(b.readEnum(ElementType.class), b.readFloat(), b.readFloat()));
        EnumSet<ElementType> flashed = buf.readEnumSet(ElementType.class);
        return new SyncAuraPacket(id, auras, flashed);
    }

    public static void handle(SyncAuraPacket msg, Supplier<NetworkEvent.Context> ctx) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientAuraCache.apply(msg));
    }
}