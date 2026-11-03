package com.dsp2.genshinerc.common.network.packet.vfx.aaap;

import mod.chloeprime.aaaparticles.api.common.AAALevel;
import mod.chloeprime.aaaparticles.api.common.ParticleEmitterInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
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

    public S2CHitEffectPacket(double x, double y, double z, ResourceLocation effectId) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.effectId = effectId;
    }

    public static void encode(S2CHitEffectPacket msg, FriendlyByteBuf buf) {
        buf.writeDouble(msg.x);
        buf.writeDouble(msg.y);
        buf.writeDouble(msg.z);
        buf.writeResourceLocation(msg.effectId);
    }

    public static S2CHitEffectPacket decode(FriendlyByteBuf buf) {
        return new S2CHitEffectPacket(buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readResourceLocation());
    }

    public static void handle(S2CHitEffectPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                ClientLevel level = Minecraft.getInstance().level;
                if (level != null) {
                    ParticleEmitterInfo info = ParticleEmitterInfo.create(level, msg.effectId)
                            .position(msg.x, msg.y, msg.z)
                            .scale(1.0f);
                    AAALevel.addParticle(level, true, info);
                }
            });
        });
        ctx.get().setPacketHandled(true);
    }
}