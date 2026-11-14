package com.dsp2.genshinerc.common.network.packet.vfx.aaap;

import com.dsp2.genshinerc.client.nwhander.sys.fight.HitEffectClientHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

@SuppressWarnings({"deprecation", "removal"})
public class S2CHitEffectPacket {
    private final double x, y, z;
    private final ResourceLocation effectId;
    private final float scale, lightSec, fadeSec;

    public S2CHitEffectPacket(double x, double y, double z, ResourceLocation effectId) {
        this(x, y, z, effectId, 1.0f, 0f, 0f);
    }

    public S2CHitEffectPacket(double x, double y, double z, ResourceLocation effectId, float scale, float lightSec, float fadeSec) {
        this.x = x; this.y = y; this.z = z; this.effectId = effectId;
        this.scale = scale; this.lightSec = lightSec; this.fadeSec = fadeSec;
    }

    public static void encode(S2CHitEffectPacket msg, FriendlyByteBuf buf) {
        buf.writeDouble(msg.x); buf.writeDouble(msg.y); buf.writeDouble(msg.z);
        buf.writeResourceLocation(msg.effectId);
        buf.writeFloat(msg.scale); buf.writeFloat(msg.lightSec); buf.writeFloat(msg.fadeSec);
    }

    public static S2CHitEffectPacket decode(FriendlyByteBuf buf) {
        return new S2CHitEffectPacket(buf.readDouble(), buf.readDouble(), buf.readDouble(),
                buf.readResourceLocation(), buf.readFloat(), buf.readFloat(), buf.readFloat());
    }

    public static void handle(S2CHitEffectPacket msg, Supplier<NetworkEvent.Context> ctx) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> HitEffectClientHandler.spawn(
                msg.x, msg.y, msg.z, msg.effectId, msg.scale, msg.lightSec, msg.fadeSec));
    }
}