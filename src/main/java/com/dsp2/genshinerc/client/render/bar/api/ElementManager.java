package com.dsp2.genshinerc.client.render.bar.api;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ElementManager {
    private static final Map<String, List<ResourceLocation>> ANIMATION_CACHE = new ConcurrentHashMap<>();

    public static void addElement(LivingEntity entity, ElementType type) {
        CompoundTag tag = entity.getPersistentData();
        List<String> current = getElementStrings(tag);
        if (current.size() < 3 && !current.contains(type.name())) {
            current.add(type.name());
            tag.putString("GenshinElements", String.join(",", current));
        }
    }

    public static void clearElements(LivingEntity entity) {
        entity.getPersistentData().remove("GenshinElements");
    }

    public static List<ElementType> getElements(LivingEntity entity) {
        List<ElementType> list = new ArrayList<>();
        for (String s : getElementStrings(entity.getPersistentData())) {
            try {
                list.add(ElementType.valueOf(s));
            } catch (IllegalArgumentException ignored) {}
        }
        return list;
    }

    private static List<String> getElementStrings(CompoundTag tag) {
        String str = tag.getString("GenshinElements");
        if (str.isEmpty()) return new ArrayList<>();
        return new ArrayList<>(Arrays.asList(str.split(",")));
    }

    public static ResourceLocation getCurrentFrame(ElementType type) {
        if (!type.isAnimated()) {
            return ResourceLocation.fromNamespaceAndPath("genshinerc", "textures/hud/" + type.getStaticPath());
        }

        List<ResourceLocation> frames = ANIMATION_CACHE.computeIfAbsent(type.getAnimFolder(), folder -> {
            List<ResourceLocation> list = new ArrayList<>();
            try {
                Minecraft.getInstance().getResourceManager().listResources("textures/hud/" + folder, rl -> rl.getPath().endsWith(".png"))
                        .forEach((rl, res) -> list.add(rl));

                list.sort(Comparator.comparing(rl -> {
                    String path = rl.getPath();
                    String fileName = path.substring(path.lastIndexOf('/') + 1, path.lastIndexOf('.'));
                    int lastUnderscore = fileName.lastIndexOf('_');
                    if (lastUnderscore != -1 && fileName.length() >= lastUnderscore + 6) {
                        try {
                            return Integer.parseInt(fileName.substring(lastUnderscore + 1));
                        } catch (NumberFormatException ignored) {}
                    }
                    return 0;
                }));
            } catch (Exception ignored) {}
            return list;
        });

        if (frames.isEmpty()) {
            return ResourceLocation.fromNamespaceAndPath("genshinerc", "textures/hud/" + type.getStaticPath());
        }

        int index = (int) ((System.currentTimeMillis() / 41.66) % frames.size());
        return frames.get(index);
    }
}