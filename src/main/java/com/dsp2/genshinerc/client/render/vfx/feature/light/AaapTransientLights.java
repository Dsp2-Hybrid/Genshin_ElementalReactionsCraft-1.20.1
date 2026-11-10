package com.dsp2.genshinerc.client.render.vfx.feature.light;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class AaapTransientLights {
    public record Spec(float r, float g, float b, float intensity, float radiusPerSize) {}

    private static final class Entry {
        final Object key = new Object();
        final Spec spec;
        double x, y, z;
        float size;
        double fadeStart = Double.POSITIVE_INFINITY;
        float fadeSec = 0.01f;
        boolean dead;
        Entry(Spec spec, double x, double y, double z, float size) {
            this.spec = spec; this.x = x; this.y = y; this.z = z; this.size = size;
        }
    }

    public static final class Handle {
        private final Entry e;
        private Handle(Entry e) { this.e = e; }
        public void moveTo(double x, double y, double z) { e.x = x; e.y = y; e.z = z; }
        public void setSize(float size) { e.size = size; }
        public boolean alive() { return !e.dead; }
        public void release(float fadeOutSec) {
            if (e.dead || e.fadeStart != Double.POSITIVE_INFINITY) return;
            e.fadeStart = clock;
            e.fadeSec = Math.max(fadeOutSec, 0.01f);
        }
    }

    private static final Map<ResourceLocation, Spec> SPECS = new ConcurrentHashMap<>();
    private static final List<Entry> ACTIVE = new ArrayList<>();
    private static final int MAX_ACTIVE = 64;
    private static double clock;
    private static long lastNanos;

    private AaapTransientLights() {}

    public static void register(ResourceLocation effectId, Spec spec) { SPECS.put(effectId, spec); }
    public static Spec spec(ResourceLocation effectId) { return SPECS.get(effectId); }

    public static void spawnCustom(Spec spec, double x, double y, double z, float size, float durationSec, float fadeOutSec) {
        if (spec == null || !(size > 0f) || !(durationSec > 0f)) return;
        Entry e = add(spec, x, y, z, size);
        float fade = Mth.clamp(fadeOutSec, 0.01f, durationSec);
        e.fadeStart = clock + durationSec - fade;
        e.fadeSec = fade;
    }

    public static void spawnCustom(ResourceLocation effectId, double x, double y, double z, float size, float durationSec, float fadeOutSec) {
        spawnCustom(SPECS.get(effectId), x, y, z, size, durationSec, fadeOutSec);
    }

    public static Handle attach(Spec spec, double x, double y, double z, float size) {
        if (spec == null || !(size > 0f)) return null;
        return new Handle(add(spec, x, y, z, size));
    }

    private static Entry add(Spec spec, double x, double y, double z, float size) {
        com.mojang.logging.LogUtils.getLogger().info("[lightdiag] transient light spawned size={} intensity={} radiusPerSize={}",
                size, spec.intensity(), spec.radiusPerSize());
        if (ACTIVE.size() >= MAX_ACTIVE) kill(ACTIVE.remove(0));
        Entry e = new Entry(spec, x, y, z, size);
        ACTIVE.add(e);
        return e;
    }

    static void tick() {
        long now = System.nanoTime();
        if (lastNanos != 0 && !Minecraft.getInstance().isPaused()) {
            clock += Math.min((now - lastNanos) / 1.0e9, 0.25);
        }
        lastNanos = now;

        for (Iterator<Entry> it = ACTIVE.iterator(); it.hasNext(); ) {
            Entry e = it.next();
            float k = 1f;
            if (clock >= e.fadeStart) {
                float t = (float) ((clock - e.fadeStart) / e.fadeSec);
                if (t >= 1f) { kill(e); it.remove(); continue; }
                k = 1f - t * t * (3f - 2f * t);
            }
            Spec s = e.spec;
            AaapLightRegistry.put(e.key, new AaapLightRegistry.Light(e.x, e.y, e.z,
                    s.radiusPerSize() * e.size, 0f, s.r(), s.g(), s.b(),
                    s.intensity() * (float) Math.sqrt(e.size) * k));
        }
    }

    private static void kill(Entry e) {
        AaapLightRegistry.remove(e.key);
        e.dead = true;
    }

    public static void clear() {
        for (Entry e : ACTIVE) kill(e);
        ACTIVE.clear();
    }
}