package com.dsp2.genshinerc.common.network.combat;

import com.dsp2.genshinerc.client.render.bar.ClientCombatBars;
import com.dsp2.genshinerc.common.combat.shield.ShieldKind;
import com.dsp2.genshinerc.common.element.ElementType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public record SyncShieldPacket(int entityId, boolean hit, List<Entry> shields) {
    private static final int MAX_ENTRIES = 32;

    public record Entry(ShieldKind kind, @Nullable ElementType element, float amount, float max) {}

    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(entityId);
        buf.writeBoolean(hit);
        buf.writeVarInt(shields.size());
        for (Entry e : shields) {
            buf.writeEnum(e.kind());
            buf.writeVarInt(e.element() == null ? -1 : e.element().ordinal());
            buf.writeFloat(e.amount());
            buf.writeFloat(e.max());
        }
    }

    public static SyncShieldPacket decode(FriendlyByteBuf buf) {
        int id = buf.readVarInt();
        boolean hit = buf.readBoolean();
        int n = buf.readVarInt();
        if (n < 0 || n > MAX_ENTRIES) throw new IllegalArgumentException("Bad shield count " + n);
        ElementType[] types = ElementType.values();
        List<Entry> list = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            ShieldKind kind = buf.readEnum(ShieldKind.class);
            int ord = buf.readVarInt();
            ElementType element = ord >= 0 && ord < types.length ? types[ord] : null;
            list.add(new Entry(kind, element, buf.readFloat(), buf.readFloat()));
        }
        return new SyncShieldPacket(id, hit, list);
    }

    public static void handle(SyncShieldPacket msg, Supplier<NetworkEvent.Context> ctx) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientCombatBars.applyShield(msg));
    }
}