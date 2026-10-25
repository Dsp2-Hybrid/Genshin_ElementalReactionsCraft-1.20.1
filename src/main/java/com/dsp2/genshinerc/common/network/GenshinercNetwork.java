package com.dsp2.genshinerc.common.network;

import com.dsp2.genshinerc.common.network.combat.SyncShieldPacket;
import com.dsp2.genshinerc.common.network.combat.SyncZhenMiePacket;
import com.dsp2.genshinerc.common.network.element.SyncAuraPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

@SuppressWarnings({"deprecation", "removal"})
public final class GenshinercNetwork {
    private static final String VERSION = "2";
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
    }
}