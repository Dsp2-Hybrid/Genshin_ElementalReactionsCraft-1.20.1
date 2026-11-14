package com.dsp2.genshinerc.common.network.sync.packet;

import com.dsp2.genshinerc.common.network.sync.PlayerHudSync;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record SyncPlayerHudPacket(
        int level, float exhaustion,
        float baseAttack, float bonusAttack, float flatAttackBonus,
        float flatHPBonus, float flatDefenseBonus,
        float bonusDefense, float scaledDefense, float innateDefense,
        float critRate, float critDamage
) {
    public SyncPlayerHudPacket(int level, float exhaustion) {
        this(level, exhaustion, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f);
    }

    public static void encode(SyncPlayerHudPacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.level);
        buf.writeFloat(msg.exhaustion);
        buf.writeFloat(msg.baseAttack);
        buf.writeFloat(msg.bonusAttack);
        buf.writeFloat(msg.flatAttackBonus);
        buf.writeFloat(msg.flatHPBonus);
        buf.writeFloat(msg.flatDefenseBonus);
        buf.writeFloat(msg.bonusDefense);
        buf.writeFloat(msg.scaledDefense);
        buf.writeFloat(msg.innateDefense);
        buf.writeFloat(msg.critRate);
        buf.writeFloat(msg.critDamage);
    }

    public static SyncPlayerHudPacket decode(FriendlyByteBuf buf) {
        return new SyncPlayerHudPacket(
                buf.readVarInt(), buf.readFloat(),
                buf.readFloat(), buf.readFloat(), buf.readFloat(),
                buf.readFloat(), buf.readFloat(), buf.readFloat(),
                buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat());
    }

    public static void handle(SyncPlayerHudPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> PlayerHudSync.applyOnClient(msg.level, msg.exhaustion));
        ctx.get().setPacketHandled(true);
    }
}