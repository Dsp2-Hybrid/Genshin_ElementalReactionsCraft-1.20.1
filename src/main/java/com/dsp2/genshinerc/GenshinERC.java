package com.dsp2.genshinerc;

import com.dsp2.genshinerc.common.core.damageindicator.DamageIndicatorCore;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(GenshinERC.MODID)
public class GenshinERC {
    public static final String MODID = "genshinerc";

    public GenshinERC(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, ServerConfig.SPEC);
        DamageIndicatorCore.init();
    }
}