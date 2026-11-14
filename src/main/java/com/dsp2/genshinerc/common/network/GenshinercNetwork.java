package com.dsp2.genshinerc.common.network;
import com.dsp2.genshinerc.common.network.sync.packet.SyncElementsPacket;

import com.dsp2.genshinerc.common.network.packet.vfx.aaap.S2CHitEffectPacket;
import com.dsp2.genshinerc.common.network.base.combat.SyncShieldPacket;
import com.dsp2.genshinerc.common.network.base.combat.SyncZhenMiePacket;
import com.dsp2.genshinerc.common.network.base.element.ReactionTextPacket;
import com.dsp2.genshinerc.common.network.base.element.SyncAuraPacket;
import com.dsp2.genshinerc.common.network.sync.packet.SyncPlayerHudPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

@SuppressWarnings({"deprecation", "removal"})
public final class GenshinercNetwork {
    private static final String VERSION = "4";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation("genshinerc", "main"), () -> VERSION, VERSION::equals, VERSION::equals);

    private GenshinercNetwork() {}

    public static void register() {
        int id = 0;

        CHANNEL.messageBuilder(SyncAuraPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(SyncAuraPacket::encode)
                .decoder(SyncAuraPacket::decode)
                .consumerMainThread(SyncAuraPacket::handle)
                .add();

        CHANNEL.messageBuilder(SyncShieldPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(SyncShieldPacket::encode)
                .decoder(SyncShieldPacket::decode)
                .consumerMainThread(SyncShieldPacket::handle)
                .add();

        CHANNEL.messageBuilder(SyncZhenMiePacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(SyncZhenMiePacket::encode)
                .decoder(SyncZhenMiePacket::decode)
                .consumerMainThread(SyncZhenMiePacket::handle)
                .add();

        CHANNEL.messageBuilder(SyncPlayerHudPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(SyncPlayerHudPacket::encode)
                .decoder(SyncPlayerHudPacket::decode)
                .consumerMainThread(SyncPlayerHudPacket::handle)
                .add();

        CHANNEL.messageBuilder(S2CHitEffectPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(S2CHitEffectPacket::encode)
                .decoder(S2CHitEffectPacket::decode)
                .consumerMainThread(S2CHitEffectPacket::handle)
                .add();

        CHANNEL.messageBuilder(ReactionTextPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ReactionTextPacket::encode)
                .decoder(ReactionTextPacket::decode)
                .consumerMainThread(ReactionTextPacket::handle)
                .add();
    }
}
