package com.dsp2.genshinerc.common.network;

import com.dsp2.genshinerc.client.render.damageindicator.config.DamageIndicatorConfig;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ConfigSyncPacket {
    public final double maxDistance;
    public final float lifeTime, velocityMultiplier, gravityStrength, randomVelocityXZAmount, velocityY, offsetY;
    public final boolean randomVelocityXZ;
    public final float normalSize, critSize, killSize;
    public final String absorbColor;
    public final boolean lifeShrinkEnabled, lifeFadeEnabled, killFlashEnabled;
    public final int lifeShrinkThreshold, lifeShrinkDuration, lifeFadeDuration, killFlashTicks, killScaleStartTick, killScaleEndTick;

    public ConfigSyncPacket(DamageIndicatorConfig c) {
        this.maxDistance = c.maxDistance;
        this.lifeTime = c.lifeTime;
        this.velocityMultiplier = c.velocityMultiplier;
        this.gravityStrength = c.gravityStrength;
        this.randomVelocityXZ = c.randomVelocityXZ;
        this.randomVelocityXZAmount = c.randomVelocityXZAmount;
        this.velocityY = c.velocityY;
        this.offsetY = c.offsetY;
        this.normalSize = c.normalSize;
        this.critSize = c.critSize;
        this.killSize = c.killSize;
        this.absorbColor = c.absorbColor;
        this.lifeShrinkEnabled = c.lifeShrinkEnabled;
        this.lifeShrinkThreshold = c.lifeShrinkThreshold;
        this.lifeShrinkDuration = c.lifeShrinkDuration;
        this.lifeFadeEnabled = c.lifeFadeEnabled;
        this.lifeFadeDuration = c.lifeFadeDuration;
        this.killFlashEnabled = c.killFlashEnabled;
        this.killFlashTicks = c.killFlashTicks;
        this.killScaleStartTick = c.killScaleStartTick;
        this.killScaleEndTick = c.killScaleEndTick;
    }

    public ConfigSyncPacket(FriendlyByteBuf b) {
        this.maxDistance = b.readDouble();
        this.lifeTime = b.readFloat();
        this.velocityMultiplier = b.readFloat();
        this.gravityStrength = b.readFloat();
        this.randomVelocityXZ = b.readBoolean();
        this.randomVelocityXZAmount = b.readFloat();
        this.velocityY = b.readFloat();
        this.offsetY = b.readFloat();
        this.normalSize = b.readFloat();
        this.critSize = b.readFloat();
        this.killSize = b.readFloat();
        this.absorbColor = b.readUtf();
        this.lifeShrinkEnabled = b.readBoolean();
        this.lifeShrinkThreshold = b.readVarInt();
        this.lifeShrinkDuration = b.readVarInt();
        this.lifeFadeEnabled = b.readBoolean();
        this.lifeFadeDuration = b.readVarInt();
        this.killFlashEnabled = b.readBoolean();
        this.killFlashTicks = b.readVarInt();
        this.killScaleStartTick = b.readVarInt();
        this.killScaleEndTick = b.readVarInt();
    }

    public static void encode(ConfigSyncPacket p, FriendlyByteBuf b) {
        b.writeDouble(p.maxDistance);
        b.writeFloat(p.lifeTime);
        b.writeFloat(p.velocityMultiplier);
        b.writeFloat(p.gravityStrength);
        b.writeBoolean(p.randomVelocityXZ);
        b.writeFloat(p.randomVelocityXZAmount);
        b.writeFloat(p.velocityY);
        b.writeFloat(p.offsetY);
        b.writeFloat(p.normalSize);
        b.writeFloat(p.critSize);
        b.writeFloat(p.killSize);
        b.writeUtf(p.absorbColor);
        b.writeBoolean(p.lifeShrinkEnabled);
        b.writeVarInt(p.lifeShrinkThreshold);
        b.writeVarInt(p.lifeShrinkDuration);
        b.writeBoolean(p.lifeFadeEnabled);
        b.writeVarInt(p.lifeFadeDuration);
        b.writeBoolean(p.killFlashEnabled);
        b.writeVarInt(p.killFlashTicks);
        b.writeVarInt(p.killScaleStartTick);
        b.writeVarInt(p.killScaleEndTick);
    }

    public static ConfigSyncPacket decode(FriendlyByteBuf b) { return new ConfigSyncPacket(b); }

    public static void handle(ConfigSyncPacket p, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                    DamageIndicatorConfig c = DamageIndicatorConfig.get();
                    c.maxDistance = p.maxDistance;
                    c.lifeTime = p.lifeTime;
                    c.velocityMultiplier = p.velocityMultiplier;
                    c.gravityStrength = p.gravityStrength;
                    c.randomVelocityXZ = p.randomVelocityXZ;
                    c.randomVelocityXZAmount = p.randomVelocityXZAmount;
                    c.velocityY = p.velocityY;
                    c.offsetY = p.offsetY;
                    c.normalSize = p.normalSize;
                    c.critSize = p.critSize;
                    c.killSize = p.killSize;
                    c.absorbColor = p.absorbColor;
                    c.lifeShrinkEnabled = p.lifeShrinkEnabled;
                    c.lifeShrinkThreshold = p.lifeShrinkThreshold;
                    c.lifeShrinkDuration = p.lifeShrinkDuration;
                    c.lifeFadeEnabled = p.lifeFadeEnabled;
                    c.lifeFadeDuration = p.lifeFadeDuration;
                    c.killFlashEnabled = p.killFlashEnabled;
                    c.killFlashTicks = p.killFlashTicks;
                    c.killScaleStartTick = p.killScaleStartTick;
                    c.killScaleEndTick = p.killScaleEndTick;
                }));
        ctx.get().setPacketHandled(true);
    }
}