package com.dsp2.genshinerc.client.render.vfx.weaponshaders.test;

import com.guhao.vix.client.shaderpasses.PostPassBase;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.io.IOException;

@Mod.EventBusSubscriber(modid = "genshinerc", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class WeaponPostPasses {
    public static PostPassBase blit;
    public static WeaponCompositePass composite;

    @SubscribeEvent
    public static void onRegisterShaders(RegisterShadersEvent event) {
        close();
        ResourceManager rm = Minecraft.getInstance().getResourceManager();
        try {
            blit = new PostPassBase("genshinerc:weapon_blit", rm);
            composite = new WeaponCompositePass("genshinerc:weapon_composite", rm);
        } catch (IOException e) {
            LogUtils.getLogger().error("genshinerc: failed to load weapon post passes", e);
            close();
        }
    }

    private static void close() {
        if (blit != null) blit.close();
        if (composite != null) composite.close();
        blit = null;
        composite = null;
    }
}