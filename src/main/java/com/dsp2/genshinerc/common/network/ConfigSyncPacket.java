package com.dsp2.genshinerc.common.network;

import com.dsp2.genshinerc.client.render.damageindicator.config.DamageIndicatorConfig;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ConfigSyncPacket {
    public final double maxDistance;
    public final int lifeTime;
    public final float velocityMultiplier, gravityStrength, randomVelocityXZAmount, offsetY;
    public final boolean randomVelocityXZ;
    public final float normalSize, critSize, killSize;
    public final String absorbColor, shadowColor;
    public final String colorPhysical, colorPyro, colorHydro, colorAnemo, colorElectro, colorDendro, colorCryo, colorGeo, colorOverload;
    public final boolean lifeShrinkEnabled, lifeFadeEnabled, killFlashEnabled;
    public final int lifeShrinkStartTick, lifeFadeStartTick, killFlashInterval, killFlashTicks;
    public final String lifeShrinkEasing, lifeFadeEasing;

    public ConfigSyncPacket(DamageIndicatorConfig c) {
        this.maxDistance = c.maxDistance;
        this.lifeTime = c.lifeTime;
        this.velocityMultiplier = c.velocityMultiplier;
        this.gravityStrength = c.gravityStrength;
        this.randomVelocityXZ = c.randomVelocityXZ;
        this.randomVelocityXZAmount = c.randomVelocityXZAmount;
        this.offsetY = c.offsetY;
        this.normalSize = c.normalSize;
        this.critSize = c.critSize;
        this.killSize = c.killSize;
        this.absorbColor = c.absorbColor;
        this.shadowColor = c.shadowColor;
        this.colorPhysical = c.colorPhysical;
        this.colorPyro = c.colorPyro;
        this.colorHydro = c.colorHydro;
        this.colorAnemo = c.colorAnemo;
        this.colorElectro = c.colorElectro;
        this.colorDendro = c.colorDendro;
        this.colorCryo = c.colorCryo;
        this.colorGeo = c.colorGeo;
        this.colorOverload = c.colorOverload;
        this.lifeShrinkEnabled = c.lifeShrinkEnabled;
        this.lifeShrinkStartTick = c.lifeShrinkStartTick;
        this.lifeShrinkEasing = c.lifeShrinkEasing.name();
        this.lifeFadeEnabled = c.lifeFadeEnabled;
        this.lifeFadeStartTick = c.lifeFadeStartTick;
        this.lifeFadeEasing = c.lifeFadeEasing.name();
        this.killFlashEnabled = c.killFlashEnabled;
        this.killFlashInterval = c.killFlashInterval;
        this.killFlashTicks = c.killFlashTicks;
    }

    public ConfigSyncPacket(FriendlyByteBuf b) {
        this.maxDistance = b.readDouble();
        this.lifeTime = b.readVarInt();
        this.velocityMultiplier = b.readFloat();
        this.gravityStrength = b.readFloat();
        this.randomVelocityXZ = b.readBoolean();
        this.randomVelocityXZAmount = b.readFloat();
        this.offsetY = b.readFloat();
        this.normalSize = b.readFloat();
        this.critSize = b.readFloat();
        this.killSize = b.readFloat();
        this.absorbColor = b.readUtf();
        this.shadowColor = b.readUtf();
        this.colorPhysical = b.readUtf(); this.colorPyro = b.readUtf(); this.colorHydro = b.readUtf();
        this.colorAnemo = b.readUtf(); this.colorElectro = b.readUtf(); this.colorDendro = b.readUtf();
        this.colorCryo = b.readUtf(); this.colorGeo = b.readUtf(); this.colorOverload = b.readUtf();
        this.lifeShrinkEnabled = b.readBoolean();
        this.lifeShrinkStartTick = b.readVarInt();
        this.lifeShrinkEasing = b.readUtf();
        this.lifeFadeEnabled = b.readBoolean();
        this.lifeFadeStartTick = b.readVarInt();
        this.lifeFadeEasing = b.readUtf();
        this.killFlashEnabled = b.readBoolean();
        this.killFlashInterval = b.readVarInt();
        this.killFlashTicks = b.readVarInt();
    }

    public static void encode(ConfigSyncPacket p, FriendlyByteBuf b) {
        b.writeDouble(p.maxDistance);
        b.writeVarInt(p.lifeTime);
        b.writeFloat(p.velocityMultiplier);
        b.writeFloat(p.gravityStrength);
        b.writeBoolean(p.randomVelocityXZ);
        b.writeFloat(p.randomVelocityXZAmount);
        b.writeFloat(p.offsetY);
        b.writeFloat(p.normalSize);
        b.writeFloat(p.critSize);
        b.writeFloat(p.killSize);
        b.writeUtf(p.absorbColor);
        b.writeUtf(p.shadowColor);
        b.writeUtf(p.colorPhysical); b.writeUtf(p.colorPyro); b.writeUtf(p.colorHydro);
        b.writeUtf(p.colorAnemo); b.writeUtf(p.colorElectro); b.writeUtf(p.colorDendro);
        b.writeUtf(p.colorCryo); b.writeUtf(p.colorGeo); b.writeUtf(p.colorOverload);
        b.writeBoolean(p.lifeShrinkEnabled);
        b.writeVarInt(p.lifeShrinkStartTick);
        b.writeUtf(p.lifeShrinkEasing);
        b.writeBoolean(p.lifeFadeEnabled);
        b.writeVarInt(p.lifeFadeStartTick);
        b.writeUtf(p.lifeFadeEasing);
        b.writeBoolean(p.killFlashEnabled);
        b.writeVarInt(p.killFlashInterval);
        b.writeVarInt(p.killFlashTicks);
    }

    public static ConfigSyncPacket decode(FriendlyByteBuf b) { return new ConfigSyncPacket(b); }

    public static void handle(ConfigSyncPacket p, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            DamageIndicatorConfig c = DamageIndicatorConfig.get();
            c.maxDistance = p.maxDistance;
            c.lifeTime = p.lifeTime;
            c.velocityMultiplier = p.velocityMultiplier;
            c.gravityStrength = p.gravityStrength;
            c.randomVelocityXZ = p.randomVelocityXZ;
            c.randomVelocityXZAmount = p.randomVelocityXZAmount;
            c.offsetY = p.offsetY;
            c.normalSize = p.normalSize;
            c.critSize = p.critSize;
            c.killSize = p.killSize;
            c.absorbColor = p.absorbColor;
            c.shadowColor = p.shadowColor;
            c.colorPhysical = p.colorPhysical;
            c.colorPyro = p.colorPyro;
            c.colorHydro = p.colorHydro;
            c.colorAnemo = p.colorAnemo;
            c.colorElectro = p.colorElectro;
            c.colorDendro = p.colorDendro;
            c.colorCryo = p.colorCryo;
            c.colorGeo = p.colorGeo;
            c.colorOverload = p.colorOverload;
            c.lifeShrinkEnabled = p.lifeShrinkEnabled;
            c.lifeShrinkStartTick = p.lifeShrinkStartTick;
            c.lifeShrinkEasing = DamageIndicatorConfig.EasingWrapper.fromString(p.lifeShrinkEasing);
            c.lifeFadeEnabled = p.lifeFadeEnabled;
            c.lifeFadeStartTick = p.lifeFadeStartTick;
            c.lifeFadeEasing = DamageIndicatorConfig.EasingWrapper.fromString(p.lifeFadeEasing);
            c.killFlashEnabled = p.killFlashEnabled;
            c.killFlashInterval = p.killFlashInterval;
            c.killFlashTicks = p.killFlashTicks;
        }));
        ctx.get().setPacketHandled(true);
    }
}