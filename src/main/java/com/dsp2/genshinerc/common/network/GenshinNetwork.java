package com.dsp2.genshinerc.common.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public final class GenshinNetwork {
    private static final String VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation("genshinerc", "main"), () -> VERSION, VERSION::equals, VERSION::equals);

    private GenshinNetwork() {}

    public static void register() {
        int id = 0;
        CHANNEL.messageBuilder(SyncElementsPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(SyncElementsPacket::encode)
                .decoder(SyncElementsPacket::decode)
                .consumerMainThread(SyncElementsPacket::handle)
                .add();
    }
}