package com.dsp2.genshinerc.common.network;

import com.dsp2.genshinerc.client.render.damageindicator.event.ClientPacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class DamagePacket {
    public final int entityId;
    public final String typeKey;
    public final float amount;
    public final boolean crit, kill;
    public final float absorbed;
    public final String overrideColor;
    public final Float overrideSize;

    public DamagePacket(int entityId, String typeKey, float amount, boolean crit, boolean kill,
                        float absorbed, String overrideColor, Float overrideSize) {
        this.entityId = entityId; this.typeKey = typeKey; this.amount = amount;
        this.crit = crit; this.kill = kill; this.absorbed = absorbed;
        this.overrideColor = overrideColor; this.overrideSize = overrideSize;
    }

    public static void encode(DamagePacket p, FriendlyByteBuf b) {
        b.writeVarInt(p.entityId);
        b.writeUtf(p.typeKey);
        b.writeFloat(p.amount);
        b.writeBoolean(p.crit);
        b.writeBoolean(p.kill);
        b.writeFloat(p.absorbed);
        b.writeBoolean(p.overrideColor != null);
        if (p.overrideColor != null) b.writeUtf(p.overrideColor);
        b.writeBoolean(p.overrideSize != null);
        if (p.overrideSize != null) b.writeFloat(p.overrideSize);
    }

    public static DamagePacket decode(FriendlyByteBuf b) {
        int id = b.readVarInt();
        String k = b.readUtf();
        float a = b.readFloat();
        boolean c = b.readBoolean(), kl = b.readBoolean();
        float ab = b.readFloat();
        String oc = b.readBoolean() ? b.readUtf() : null;
        Float os = b.readBoolean() ? b.readFloat() : null;
        return new DamagePacket(id, k, a, c, kl, ab, oc, os);
    }

    public static void handle(DamagePacket p, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                        ClientPacketHandler.handleDamage(p.entityId, p.typeKey, p.amount,
                                p.crit, p.kill, p.absorbed, p.overrideColor, p.overrideSize)));
        ctx.get().setPacketHandled(true);
    }
}