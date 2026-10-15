package com.dsp2.genshinerc.common.network;

import com.dsp2.genshinerc.client.render.damageindicator.config.DamageIndicatorConfig;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ConfigSyncPacket {
    public final int maxDistance, lifeTime;
    public final double velocityMultiplier, gravityStrength, randomVelocityXZAmount, offsetY;
    public final double sizeYFactorK, heightYFactor;
    public final boolean randomVelocityXZ;
    public final double normalSize, critSize, killSize, birthSizeRandomPercent;
    public final String absorbColor, shadowColor;
    public final String colorPhysical, colorPyro, colorHydro, colorAnemo, colorElectro, colorDendro, colorCryo, colorGeo, colorOverload;
    public final boolean lifeShrinkEnabled, lifeFadeEnabled;
    public final int lifeShrinkStartTick, lifeFadeStartTick;
    public final String lifeShrinkEasing, lifeFadeEasing;
    public final boolean birthScaleEnabled, birthFadeEnabled;
    public final int birthScaleDuration, birthFadeDuration;
    public final String birthScaleEasing, birthFadeEasing;
    public final boolean killFlashEnabled;
    public final int killFlashInterval, killFlashTicks, killExtraTicks;

    public ConfigSyncPacket(DamageIndicatorConfig c) {
        this.maxDistance = c.maxDistance;
        this.lifeTime = c.lifeTime;
        this.velocityMultiplier = c.velocityMultiplier;
        this.gravityStrength = c.gravityStrength;
        this.randomVelocityXZ = c.randomVelocityXZ;
        this.randomVelocityXZAmount = c.randomVelocityXZAmount;
        this.offsetY = c.offsetY;
        this.sizeYFactorK = c.sizeYFactorK;
        this.heightYFactor = c.heightYFactor;
        this.normalSize = c.normalSize;
        this.critSize = c.critSize;
        this.killSize = c.killSize;
        this.birthSizeRandomPercent = c.birthSizeRandomPercent;
        this.absorbColor = c.absorbColor;
        this.shadowColor = c.shadowColor;
        this.colorPhysical = c.colorPhysical;
        this.colorPyro = c.colorPyro; this.colorHydro = c.colorHydro; this.colorAnemo = c.colorAnemo;
        this.colorElectro = c.colorElectro; this.colorDendro = c.colorDendro; this.colorCryo = c.colorCryo;
        this.colorGeo = c.colorGeo; this.colorOverload = c.colorOverload;
        this.lifeShrinkEnabled = c.lifeShrinkEnabled;
        this.lifeShrinkStartTick = c.lifeShrinkStartTick;
        this.lifeShrinkEasing = c.lifeShrinkEasing.name();
        this.lifeFadeEnabled = c.lifeFadeEnabled;
        this.lifeFadeStartTick = c.lifeFadeStartTick;
        this.lifeFadeEasing = c.lifeFadeEasing.name();
        this.birthScaleEnabled = c.birthScaleEnabled;
        this.birthScaleDuration = c.birthScaleDuration;
        this.birthScaleEasing = c.birthScaleEasing.name();
        this.birthFadeEnabled = c.birthFadeEnabled;
        this.birthFadeDuration = c.birthFadeDuration;
        this.birthFadeEasing = c.birthFadeEasing.name();
        this.killFlashEnabled = c.killFlashEnabled;
        this.killFlashInterval = c.killFlashInterval;
        this.killFlashTicks = c.killFlashTicks;
        this.killExtraTicks = c.killExtraTicks;
    }

    public ConfigSyncPacket(FriendlyByteBuf b) {
        this.maxDistance = b.readVarInt();
        this.lifeTime = b.readVarInt();
        this.velocityMultiplier = b.readDouble();
        this.gravityStrength = b.readDouble();
        this.randomVelocityXZ = b.readBoolean();
        this.randomVelocityXZAmount = b.readDouble();
        this.offsetY = b.readDouble();
        this.sizeYFactorK = b.readDouble();
        this.heightYFactor = b.readDouble();
        this.normalSize = b.readDouble();
        this.critSize = b.readDouble();
        this.killSize = b.readDouble();
        this.birthSizeRandomPercent = b.readDouble();
        this.absorbColor = b.readUtf(); this.shadowColor = b.readUtf();
        this.colorPhysical = b.readUtf(); this.colorPyro = b.readUtf(); this.colorHydro = b.readUtf();
        this.colorAnemo = b.readUtf(); this.colorElectro = b.readUtf(); this.colorDendro = b.readUtf();
        this.colorCryo = b.readUtf(); this.colorGeo = b.readUtf(); this.colorOverload = b.readUtf();
        this.lifeShrinkEnabled = b.readBoolean();
        this.lifeShrinkStartTick = b.readVarInt();
        this.lifeShrinkEasing = b.readUtf();
        this.lifeFadeEnabled = b.readBoolean();
        this.lifeFadeStartTick = b.readVarInt();
        this.lifeFadeEasing = b.readUtf();
        this.birthScaleEnabled = b.readBoolean();
        this.birthScaleDuration = b.readVarInt();
        this.birthScaleEasing = b.readUtf();
        this.birthFadeEnabled = b.readBoolean();
        this.birthFadeDuration = b.readVarInt();
        this.birthFadeEasing = b.readUtf();
        this.killFlashEnabled = b.readBoolean();
        this.killFlashInterval = b.readVarInt();
        this.killFlashTicks = b.readVarInt();
        this.killExtraTicks = b.readVarInt();
    }

    public static void encode(ConfigSyncPacket p, FriendlyByteBuf b) {
        b.writeVarInt(p.maxDistance); b.writeVarInt(p.lifeTime);
        b.writeDouble(p.velocityMultiplier); b.writeDouble(p.gravityStrength);
        b.writeBoolean(p.randomVelocityXZ); b.writeDouble(p.randomVelocityXZAmount);
        b.writeDouble(p.offsetY); b.writeDouble(p.sizeYFactorK); b.writeDouble(p.heightYFactor);
        b.writeDouble(p.normalSize); b.writeDouble(p.critSize); b.writeDouble(p.killSize);
        b.writeDouble(p.birthSizeRandomPercent);
        b.writeUtf(p.absorbColor); b.writeUtf(p.shadowColor);
        b.writeUtf(p.colorPhysical); b.writeUtf(p.colorPyro); b.writeUtf(p.colorHydro);
        b.writeUtf(p.colorAnemo); b.writeUtf(p.colorElectro); b.writeUtf(p.colorDendro);
        b.writeUtf(p.colorCryo); b.writeUtf(p.colorGeo); b.writeUtf(p.colorOverload);
        b.writeBoolean(p.lifeShrinkEnabled); b.writeVarInt(p.lifeShrinkStartTick); b.writeUtf(p.lifeShrinkEasing);
        b.writeBoolean(p.lifeFadeEnabled); b.writeVarInt(p.lifeFadeStartTick); b.writeUtf(p.lifeFadeEasing);
        b.writeBoolean(p.birthScaleEnabled); b.writeVarInt(p.birthScaleDuration); b.writeUtf(p.birthScaleEasing);
        b.writeBoolean(p.birthFadeEnabled); b.writeVarInt(p.birthFadeDuration); b.writeUtf(p.birthFadeEasing);
        b.writeBoolean(p.killFlashEnabled); b.writeVarInt(p.killFlashInterval); b.writeVarInt(p.killFlashTicks); b.writeVarInt(p.killExtraTicks);
    }

    public static ConfigSyncPacket decode(FriendlyByteBuf b) { return new ConfigSyncPacket(b); }

    public static void handle(ConfigSyncPacket p, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            DamageIndicatorConfig c = DamageIndicatorConfig.get();
            c.maxDistance = p.maxDistance; c.lifeTime = p.lifeTime;
            c.velocityMultiplier = p.velocityMultiplier; c.gravityStrength = p.gravityStrength;
            c.randomVelocityXZ = p.randomVelocityXZ; c.randomVelocityXZAmount = p.randomVelocityXZAmount;
            c.offsetY = p.offsetY; c.sizeYFactorK = p.sizeYFactorK; c.heightYFactor = p.heightYFactor;
            c.normalSize = p.normalSize; c.critSize = p.critSize; c.killSize = p.killSize;
            c.birthSizeRandomPercent = p.birthSizeRandomPercent;
            c.absorbColor = p.absorbColor; c.shadowColor = p.shadowColor;
            c.colorPhysical = p.colorPhysical; c.colorPyro = p.colorPyro; c.colorHydro = p.colorHydro;
            c.colorAnemo = p.colorAnemo; c.colorElectro = p.colorElectro; c.colorDendro = p.colorDendro;
            c.colorCryo = p.colorCryo; c.colorGeo = p.colorGeo; c.colorOverload = p.colorOverload;
            c.lifeShrinkEnabled = p.lifeShrinkEnabled; c.lifeShrinkStartTick = p.lifeShrinkStartTick;
            c.lifeShrinkEasing = DamageIndicatorConfig.EasingWrapper.fromString(p.lifeShrinkEasing);
            c.lifeFadeEnabled = p.lifeFadeEnabled; c.lifeFadeStartTick = p.lifeFadeStartTick;
            c.lifeFadeEasing = DamageIndicatorConfig.EasingWrapper.fromString(p.lifeFadeEasing);
            c.birthScaleEnabled = p.birthScaleEnabled; c.birthScaleDuration = p.birthScaleDuration;
            c.birthScaleEasing = DamageIndicatorConfig.EasingWrapper.fromString(p.birthScaleEasing);
            c.birthFadeEnabled = p.birthFadeEnabled; c.birthFadeDuration = p.birthFadeDuration;
            c.birthFadeEasing = DamageIndicatorConfig.EasingWrapper.fromString(p.birthFadeEasing);
            c.killFlashEnabled = p.killFlashEnabled; c.killFlashInterval = p.killFlashInterval;
            c.killFlashTicks = p.killFlashTicks; c.killExtraTicks = p.killExtraTicks;
        }));
        ctx.get().setPacketHandled(true);
    }
}