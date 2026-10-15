package com.dsp2.genshinerc.client.render.damageindicator.keybind;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = "genshinerc", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class KeyBindings {
    public static final KeyMapping OPEN_CONFIG = new KeyMapping(
            "key.genshinerc.damageindicator.config",
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_H,
            "category.genshinerc.damageindicator");

    @SubscribeEvent
    public static void register(RegisterKeyMappingsEvent e) { e.register(OPEN_CONFIG); }
}