package com.dsp2.genshinerc.common.network;

import com.dsp2.genshinerc.client.render.damageindicator.config.DamageIndicatorConfig;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class DamageIndicatorConfigSyncPacket {
    public final boolean enabled;
    public final int maxDistance;

    public DamageIndicatorConfigSyncPacket(DamageIndicatorConfig c) {
        this.enabled = c.enabled;
        this.maxDistance = c.maxDistance;
    }

    public DamageIndicatorConfigSyncPacket(FriendlyByteBuf b) {
        this.enabled = b.readBoolean();
        this.maxDistance = b.readVarInt();
    }

    public static void encode(DamageIndicatorConfigSyncPacket p, FriendlyByteBuf b) {
        b.writeBoolean(p.enabled);
        b.writeVarInt(p.maxDistance);
    }

    public static DamageIndicatorConfigSyncPacket decode(FriendlyByteBuf b) {
        return new DamageIndicatorConfigSyncPacket(b);
    }

    public static void handle(DamageIndicatorConfigSyncPacket p, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        if (context.getSender() != null) {
            context.setPacketHandled(true);
            return;
        }
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            DamageIndicatorConfig c = DamageIndicatorConfig.get();
            c.enabled = p.enabled;
            c.maxDistance = p.maxDistance;
        }));
        context.setPacketHandled(true);
    }
}