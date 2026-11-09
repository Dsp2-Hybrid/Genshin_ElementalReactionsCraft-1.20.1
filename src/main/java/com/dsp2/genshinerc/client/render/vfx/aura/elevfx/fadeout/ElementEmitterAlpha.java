package com.dsp2.genshinerc.client.render.vfx.aura.elevfx.fadeout;

import mod.chloeprime.aaaparticles.api.client.effekseer.ParticleEmitter;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public final class ElementEmitterAlpha {
    private static final Map<ParticleEmitter, Float> ALPHA = new IdentityHashMap<>();

    private ElementEmitterAlpha() {}

    public static void put(ParticleEmitter e, float a) { ALPHA.put(e, a); }
    public static void remove(ParticleEmitter e) { ALPHA.remove(e); }
    public static void clear() { ALPHA.clear(); }

    public static float resolve(List<ParticleEmitter> group, float fallback) {
        if (group == null || group.isEmpty()) return fallback;
        float best = -1f;
        for (ParticleEmitter e : group) {
            Float a = ALPHA.get(e);
            if (a != null && a > best) best = a;
        }
        return best < 0f ? fallback : best;
    }
}