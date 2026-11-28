package com.dsp2.genshinerc.common.network.base.element;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ReactionSoundPacket {
    public final ResourceLocation soundId;
    public final double x;
    public final double y;
    public final double z;
    public final float volume;
    public final float pitch;
    public final int clipTicks;
    public final int fadeTicks;

    public ReactionSoundPacket(ResourceLocation soundId, double x, double y, double z, float volume, float pitch, int clipTicks, int fadeTicks) {
        this.soundId = soundId;
        this.x = x;
        this.y = y;
        this.z = z;
        this.volume = volume;
        this.pitch = pitch;
        this.clipTicks = clipTicks;
        this.fadeTicks = fadeTicks;
    }

    public ReactionSoundPacket(SoundEvent event, double x, double y, double z, float volume, float pitch, int clipTicks, int fadeTicks) {
        this(BuiltInRegistries.SOUND_EVENT.getKey(event), x, y, z, volume, pitch, clipTicks, fadeTicks);
    }

    public static void encode(ReactionSoundPacket msg, FriendlyByteBuf buf) {
        buf.writeResourceLocation(msg.soundId);
        buf.writeDouble(msg.x);
        buf.writeDouble(msg.y);
        buf.writeDouble(msg.z);
        buf.writeFloat(msg.volume);
        buf.writeFloat(msg.pitch);
        buf.writeVarInt(msg.clipTicks);
        buf.writeVarInt(msg.fadeTicks);
    }

    public static ReactionSoundPacket decode(FriendlyByteBuf buf) {
        return new ReactionSoundPacket(
                buf.readResourceLocation(),
                buf.readDouble(), buf.readDouble(), buf.readDouble(),
                buf.readFloat(), buf.readFloat(),
                buf.readVarInt(), buf.readVarInt()
        );
    }

    public static void handle(ReactionSoundPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> net.minecraftforge.fml.DistExecutor.unsafeRunWhenOn(
                net.minecraftforge.api.distmarker.Dist.CLIENT,
                () -> () -> com.dsp2.genshinerc.client.audio.ClientReactionSound.play(msg)
        ));
        ctx.setPacketHandled(true);
    }

    public BlockPos pos() {
        return new BlockPos((int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z));
    }
}