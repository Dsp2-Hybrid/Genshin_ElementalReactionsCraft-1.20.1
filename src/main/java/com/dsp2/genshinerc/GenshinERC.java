package com.dsp2.genshinerc;

import com.dsp2.genshinerc.client.render.bar.StatusBar;
import com.dsp2.genshinerc.common.config.ServerConfig;
import com.dsp2.genshinerc.common.config.ConfigInitCore;
import com.dsp2.genshinerc.common.level.EntityLevelManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(GenshinERC.MODID)
public class GenshinERC {
    public static final String MODID = "genshinerc";

    public GenshinERC(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();

        context.registerConfig(ModConfig.Type.SERVER, ServerConfig.SPEC);
        ConfigInitCore.init();
        MinecraftForge.EVENT_BUS.register(StatusBar.class);
        MinecraftForge.EVENT_BUS.register(EntityLevelManager.class);

        modEventBus.addListener(this::onCommonSetup);
    }

    //Debug_HPATK++
    private void onCommonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            try {
                java.lang.reflect.Field maxValueField = net.minecraft.world.entity.ai.attributes.RangedAttribute.class.getDeclaredField("maxValue");
                maxValueField.setAccessible(true);
                maxValueField.setDouble(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH, 13107200.0D);
                maxValueField.setDouble(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE, 13107200.0D);
            } catch (Exception e) {
                System.err.println("失败");
                e.printStackTrace();
            }
        });
    }
}