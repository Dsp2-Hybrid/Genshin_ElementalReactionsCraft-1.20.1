package com.dsp2.genshinerc.common.world.block.entity;

import com.dsp2.genshinerc.common.registry.block.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class SpecialIceBlockEntity extends BlockEntity {
    private long startTime;
    private long expiresAt;
    private long originalDuration;
    private long textureSeed;
    private int densityTier;
    private int waterLevel;

    public SpecialIceBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SPECIAL_ICE.get(), pos, state);
        textureSeed = pos.asLong();
    }

    public void setLifetime(long startTime, long expiresAt) {
        this.startTime = startTime;
        this.expiresAt = expiresAt;
        if (originalDuration <= 0L) originalDuration = Math.max(1L, expiresAt - startTime);
        setChanged();
        sync();
    }

    public void setFreezeData(long startTime, long expiresAt, long textureSeed, int densityTier, int waterLevel) {
        this.startTime = startTime;
        this.expiresAt = expiresAt;
        this.originalDuration = Math.max(1L, expiresAt - startTime);
        this.textureSeed = textureSeed;
        this.densityTier = Math.max(0, densityTier);
        this.waterLevel = Math.max(0, Math.min(7, waterLevel));
        setChanged();
        sync();
    }

    public void setExpiresAt(long expiresAt) {
        this.expiresAt = expiresAt;
        setChanged();
        sync();
    }

    private void sync() {
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public long getStartTime() {
        return startTime;
    }

    public long getExpiresAt() {
        return expiresAt;
    }

    public long getDuration() {
        return originalDuration > 0L ? originalDuration : Math.max(1L, expiresAt - startTime);
    }

    public long getTextureSeed() {
        return textureSeed;
    }

    public int getDensityTier() {
        return densityTier;
    }

    public int getWaterLevel() {
        return waterLevel;
    }

    public float opacity(float partialTick) {
        if (level == null || expiresAt <= startTime) return 1f;
        double now = level.getGameTime() + partialTick;
        double progress = Math.max(0.0, Math.min(1.0,
                (now - startTime) / (double) Math.max(1L, originalDuration)));
        return (float) (1.0 - 0.75 * progress);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putLong("StartTime", startTime);
        tag.putLong("ExpiresAt", expiresAt);
        tag.putLong("OriginalDuration", originalDuration);
        tag.putLong("TextureSeed", textureSeed);
        tag.putInt("DensityTier", densityTier);
        tag.putInt("WaterLevel", waterLevel);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        startTime = tag.getLong("StartTime");
        expiresAt = tag.getLong("ExpiresAt");
        originalDuration = tag.contains("OriginalDuration")
                ? tag.getLong("OriginalDuration") : Math.max(1L, expiresAt - startTime);
        textureSeed = tag.contains("TextureSeed") ? tag.getLong("TextureSeed") : worldPosition.asLong();
        densityTier = tag.getInt("DensityTier");
        waterLevel = tag.getInt("WaterLevel");
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        load(tag);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
