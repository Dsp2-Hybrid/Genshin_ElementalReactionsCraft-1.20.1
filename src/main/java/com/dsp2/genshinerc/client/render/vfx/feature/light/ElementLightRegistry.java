package com.dsp2.genshinerc.client.render.vfx.feature.light;

import mod.chloeprime.aaaparticles.api.client.effekseer.ParticleEmitter;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Map;

public final class ElementLightRegistry {
    public record Light(double x, double y, double z, float radius, float r, float g, float b, float intensity) {}

    private static final Map<ParticleEmitter, Light> LIGHTS = new IdentityHashMap<>();

    private ElementLightRegistry() {}

    public static void put(ParticleEmitter e, Light l) { LIGHTS.put(e, l); }
    public static void remove(ParticleEmitter e) { LIGHTS.remove(e); }
    public static void clear() { LIGHTS.clear(); }
    public static Collection<Light> all() { return LIGHTS.values(); }
}