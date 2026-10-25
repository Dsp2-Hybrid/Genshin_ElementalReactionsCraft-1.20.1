package com.dsp2.genshinerc.client.render.hud.bar.api;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ElementStatusAPI {
    private static final Map<Integer, List<ResourceLocation>> ELEMENT_CACHE = new HashMap<>();

    public static void addElement(Entity entity, ResourceLocation icon) {
        List<ResourceLocation> list = ELEMENT_CACHE.computeIfAbsent(entity.getId(), k -> new ArrayList<>());
        if (list.size() < 3) {
            list.add(icon);
        }
    }

    public static void removeElement(Entity entity, ResourceLocation icon) {
        List<ResourceLocation> list = ELEMENT_CACHE.get(entity.getId());
        if (list != null) {
            list.remove(icon);
            if (list.isEmpty()) ELEMENT_CACHE.remove(entity.getId());
        }
    }

    public static void clearElements(Entity entity) {
        ELEMENT_CACHE.remove(entity.getId());
    }

    public static List<ResourceLocation> getElements(Entity entity) {
        return ELEMENT_CACHE.getOrDefault(entity.getId(), new ArrayList<>());
    }
}