package com.dsp2.genshinerc;

import com.dsp2.genshinerc.client.render.bar.CustomHealthBar;
import com.dsp2.genshinerc.client.render.bar.StatusBar;
import com.dsp2.genshinerc.common.config.ServerConfig;
import com.dsp2.genshinerc.common.config.ConfigInitCore;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(GenshinERC.MODID)
public class GenshinERC {
    public static final String MODID = "genshinerc";

    public GenshinERC(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();
        context.registerConfig(ModConfig.Type.SERVER, ServerConfig.SPEC);
        ConfigInitCore.init();
        MinecraftForge.EVENT_BUS.register(StatusBar.class);
    }
}