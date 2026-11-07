package com.dsp2.genshinerc.common.world.entity.net;
import com.dsp2.genshinerc.common.world.entity.UnitBootstrap;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

@SuppressWarnings({"deprecation", "removal"})
public class NetChannel {
    private static final String VER = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(UnitBootstrap.NS, "entity_main"), () -> VER, VER::equals, VER::equals);
    public static void init() {
        int id = 0;
        CHANNEL.registerMessage(id++, SyncTacticPacket.class, SyncTacticPacket::encode,
                SyncTacticPacket::decode, SyncTacticPacket::handle);
    }
}