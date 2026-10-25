package com.dsp2.genshinerc.client.gui.rpg;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@SuppressWarnings({"deprecation", "removal"})
public final class HudIconProvider {
    private static final Map<String, Optional<ResourceLocation>> CACHE = new HashMap<>();

    private HudIconProvider() {}

    public static Optional<ResourceLocation> get(String name) {
        return CACHE.computeIfAbsent(name, n -> {
            ResourceLocation rl = new ResourceLocation("genshinerc", "textures/gui/rpg/" + n + ".png");
            return Minecraft.getInstance().getResourceManager().getResource(rl).map(r -> rl);
        });
    }

    public static void clear() {
        CACHE.clear();
    }
}