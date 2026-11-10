package com.dsp2.genshinerc.client.render.vfx.feature.light;

import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Map;

public final class AaapLightRegistry {
    public record Light(double x, double y, double z, float radius, float innerRadius,
                        float r, float g, float b, float intensity) {}
    private static final Map<Object, Light> LIGHTS = new IdentityHashMap<>();

    private AaapLightRegistry() {}
    public static void put(Object key, Light l) { LIGHTS.put(key, l); }
    public static void remove(Object key) { LIGHTS.remove(key); }
    public static void clear() { LIGHTS.clear(); }
    public static Collection<Light> all() { return LIGHTS.values(); }
}