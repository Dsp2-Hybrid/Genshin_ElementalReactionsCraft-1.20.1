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
import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = "genshinerc", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class WeaponPostPasses {
    public static PostPassBase blit;
    public static WeaponCompositePass composite;
    public static Map<String, WeaponCompositePass> screenEffects = new HashMap<>();

    @SubscribeEvent
    public static void onRegisterShaders(RegisterShadersEvent event) {
        close();
        ResourceManager rm = Minecraft.getInstance().getResourceManager();
        try {
            blit = new PostPassBase("genshinerc:weapon_blit", rm);
            composite = new WeaponCompositePass("genshinerc:weapon_composite", rm);

            screenEffects.put("distort_wave", new WeaponCompositePass("genshinerc:weapon_distort_wave", rm));
            screenEffects.put("distort_radial", new WeaponCompositePass("genshinerc:weapon_distort_radial", rm));
            screenEffects.put("rgbsplit", new WeaponCompositePass("genshinerc:weapon_rgbsplit", rm));
            screenEffects.put("pixelate", new WeaponCompositePass("genshinerc:weapon_pixelate", rm));
            screenEffects.put("scanline", new WeaponCompositePass("genshinerc:weapon_scanline", rm));
            screenEffects.put("kaleido", new WeaponCompositePass("genshinerc:weapon_kaleido", rm));
            screenEffects.put("glitch", new WeaponCompositePass("genshinerc:weapon_glitch", rm));
            screenEffects.put("fisheye_screen", new WeaponCompositePass("genshinerc:weapon_fisheye_screen", rm));
            screenEffects.put("invert", new WeaponCompositePass("genshinerc:weapon_invert", rm));
            screenEffects.put("twist", new WeaponCompositePass("genshinerc:weapon_twist", rm));
            screenEffects.put("mirror", new WeaponCompositePass("genshinerc:weapon_mirror", rm));
        } catch (IOException e) {
            LogUtils.getLogger().error("genshinerc: failed to load post passes", e);
            close();
        }
    }

    private static void close() {
        if (blit != null) blit.close();
        if (composite != null) composite.close();
        screenEffects.values().forEach(PostPassBase::close);
        screenEffects.clear();
    }
}