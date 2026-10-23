package com.dsp2.genshinerc.client.render.bar.api;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ClientElementCache {
    private static final Map<Integer, List<ElementType>> CACHE = new ConcurrentHashMap<>();

    private ClientElementCache() {}

    public static void set(int entityId, List<ElementType> elements) {
        if (elements.isEmpty()) CACHE.remove(entityId);
        else CACHE.put(entityId, List.copyOf(elements));
    }

    public static List<ElementType> get(int entityId) {
        return CACHE.getOrDefault(entityId, List.of());
    }

    public static void remove(int entityId) {
        CACHE.remove(entityId);
    }
}