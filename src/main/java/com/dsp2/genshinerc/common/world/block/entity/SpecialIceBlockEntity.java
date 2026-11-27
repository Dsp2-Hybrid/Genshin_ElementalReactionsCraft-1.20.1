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
    private long duration;
    private long anchorTime;
    private double anchorLife;
    private int rate = 1;
    private long textureSeed;
    private int densityTier;
    private int waterLevel;

    public SpecialIceBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SPECIAL_ICE.get(), pos, state);
        textureSeed = pos.asLong();
    }

    public void setFreezeData(long startTime, long expiresAt, long textureSeed, int densityTier, int waterLevel) {
        this.startTime = startTime;
        this.duration = Math.max(1L, expiresAt - startTime);
        this.anchorTime = startTime;
        this.anchorLife = this.duration;
        this.rate = 1;
        this.textureSeed = textureSeed;
        this.densityTier = Math.max(0, densityTier);
        this.waterLevel = Math.max(0, Math.min(7, waterLevel));
        sync();
    }

    public double remaining(double now) {
        return Math.max(0.0, anchorLife - Math.max(0.0, now - anchorTime) * rate);
    }

    public void setRate(long now, int nextRate) {
        nextRate = nextRate == 2 ? 2 : 1;
        if (rate == nextRate) return;
        anchorLife = remaining(now);
        anchorTime = now;
        rate = nextRate;
        sync();
    }

    public long getStartTime() {
        return startTime;
    }

    public long getExpiresAt() {
        return anchorTime + (long) Math.ceil(anchorLife / rate);
    }

    public long getDuration() {
        return Math.max(1L, duration);
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
        if (level == null || duration <= 0L) return 0.85f;
        float remainingRatio = (float) (remaining(level.getGameTime() + partialTick) / duration);
        return net.minecraft.util.Mth.clamp(0.20f + 0.65f * remainingRatio, 0.20f, 0.85f);
    }

    private void sync() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putLong("StartTime", startTime);
        tag.putLong("OriginalDuration", duration);
        tag.putLong("IceAnchorTime", anchorTime);
        tag.putDouble("IceAnchorLife", anchorLife);
        tag.putInt("IceRate", rate);
        tag.putLong("TextureSeed", textureSeed);
        tag.putInt("DensityTier", densityTier);
        tag.putInt("WaterLevel", waterLevel);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        startTime = tag.getLong("StartTime");
        duration = tag.contains("OriginalDuration") ? tag.getLong("OriginalDuration") : 1L;
        anchorTime = tag.contains("IceAnchorTime") ? tag.getLong("IceAnchorTime") : startTime;
        anchorLife = tag.contains("IceAnchorLife") ? tag.getDouble("IceAnchorLife")
                : Math.max(0L, duration);
        rate = tag.getInt("IceRate") == 2 ? 2 : 1;
        textureSeed = tag.contains("TextureSeed") ? tag.getLong("TextureSeed") : worldPosition.asLong();
        densityTier = Math.max(0, tag.getInt("DensityTier"));
        waterLevel = Math.max(0, Math.min(7, tag.getInt("WaterLevel")));
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
