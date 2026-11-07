package com.dsp2.genshinerc.common.network.base.element;

import com.dsp2.genshinerc.client.render.world.component.damageindicator.event.ReactionTextTracker;
import com.dsp2.genshinerc.common.core.element.ElementType;
import com.dsp2.genshinerc.common.core.reaction.Reaction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public record ReactionTextPacket(int entityId, Reaction reaction, @Nullable ElementType first, @Nullable ElementType second) {

    public static void encode(ReactionTextPacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.entityId);
        buf.writeEnum(msg.reaction);
        buf.writeVarInt(msg.first == null ? 0 : msg.first.ordinal() + 1);
        buf.writeVarInt(msg.second == null ? 0 : msg.second.ordinal() + 1);
    }

    public static ReactionTextPacket decode(FriendlyByteBuf buf) {
        int id = buf.readVarInt();
        Reaction reaction = buf.readEnum(Reaction.class);
        return new ReactionTextPacket(id, reaction, element(buf.readVarInt()), element(buf.readVarInt()));
    }

    @Nullable
    private static ElementType element(int v) {
        ElementType[] all = ElementType.values();
        return v <= 0 || v > all.length ? null : all[v - 1];
    }

    public static void handle(ReactionTextPacket msg, Supplier<NetworkEvent.Context> ctx) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                ReactionTextTracker.add(msg.entityId, msg.reaction, msg.first, msg.second));
    }
}
