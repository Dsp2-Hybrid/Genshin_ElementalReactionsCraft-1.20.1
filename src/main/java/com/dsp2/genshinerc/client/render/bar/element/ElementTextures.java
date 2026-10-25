package com.dsp2.genshinerc.client.render.bar.element;

import com.dsp2.genshinerc.common.core.element.ElementType;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

import java.util.*;

public final class ElementTextures {
    private static final String NAMESPACE = "genshinerc";
    private static final double FRAME_MS = 1000.0 / 24.0;
    private static final Map<ElementType, List<ResourceLocation>> FRAMES = new EnumMap<>(ElementType.class);

    private ElementTextures() {}

    public static ResourceLocation staticIcon(ElementType type) {
        return ResourceLocation.fromNamespaceAndPath(NAMESPACE, "textures/hud/" + type.getStaticPath());
    }

    public static ResourceLocation currentFrame(ElementType type, long nowMs) {
        if (!type.isAnimated()) return staticIcon(type);
        List<ResourceLocation> frames = FRAMES.computeIfAbsent(type, ElementTextures::loadFrames);
        if (frames.isEmpty()) return staticIcon(type);
        return frames.get((int) ((nowMs / FRAME_MS) % frames.size()));
    }

    public static void clear() {
        FRAMES.clear();
    }

    private static List<ResourceLocation> loadFrames(ElementType type) {
        List<ResourceLocation> list = new ArrayList<>();
        Minecraft.getInstance().getResourceManager()
                .listResources("textures/hud/" + type.getAnimFolder(), rl -> rl.getPath().endsWith(".png"))
                .keySet().stream()
                .filter(rl -> rl.getNamespace().equals(NAMESPACE))
                .forEach(list::add);
        list.sort(Comparator.comparingInt(ElementTextures::frameIndex));
        return list;
    }

    private static int frameIndex(ResourceLocation rl) {
        String path = rl.getPath();
        String name = path.substring(path.lastIndexOf('/') + 1, path.lastIndexOf('.'));
        int us = name.lastIndexOf('_');
        if (us != -1 && us + 1 < name.length()) {
            try {
                return Integer.parseInt(name.substring(us + 1));
            } catch (NumberFormatException ignored) {}
        }
        return 0;
    }
}