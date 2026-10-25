package com.dsp2.genshinerc.common.network.base.combat;

import com.dsp2.genshinerc.client.render.hud.bar.ClientCombatBars;
import com.dsp2.genshinerc.common.core.combat.zhenmie.ClashResult;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public record SyncZhenMiePacket(int entityId, float zhen, float mie, boolean zhenBuff, boolean mieBuff,
                                boolean decaying, @Nullable ClashResult clash, float clashPoint) {

    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(entityId);
        buf.writeFloat(zhen);
        buf.writeFloat(mie);
        buf.writeByte((zhenBuff ? 1 : 0) | (mieBuff ? 2 : 0) | (decaying ? 4 : 0));
        buf.writeByte(clash == null ? -1 : clash.ordinal());
        buf.writeFloat(clashPoint);
    }

    public static SyncZhenMiePacket decode(FriendlyByteBuf buf) {
        int id = buf.readVarInt();
        float zhen = buf.readFloat();
        float mie = buf.readFloat();
        int flags = buf.readByte();
        int c = buf.readByte();
        ClashResult[] results = ClashResult.values();
        ClashResult clash = c >= 0 && c < results.length ? results[c] : null;
        return new SyncZhenMiePacket(id, zhen, mie, (flags & 1) != 0, (flags & 2) != 0, (flags & 4) != 0, clash, buf.readFloat());
    }

    public static void handle(SyncZhenMiePacket msg, Supplier<NetworkEvent.Context> ctx) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientCombatBars.applyZhenMie(msg));
    }
}