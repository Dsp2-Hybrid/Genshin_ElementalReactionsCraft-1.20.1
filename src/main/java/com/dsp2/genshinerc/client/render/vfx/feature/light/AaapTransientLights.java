package com.dsp2.genshinerc.client.render.vfx.feature.light;

import com.dsp2.genshinerc.client.render.vfx.config.VFXConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class AaapTransientLights {
    public record Spec(
            float r,
            float g,
            float b,
            float intensity,
            float radiusPerSize,
            boolean isTrail) {}

    private static final class Entry {
        final Object key = new Object();
        final Spec spec;
        double x;
        double y;
        double z;
        float size;
        double fadeStart = Double.POSITIVE_INFINITY;
        float fadeSec = 0.01f;
        boolean dead;

        Entry(Spec spec, double x, double y, double z, float size) {
            this.spec = spec;
            this.x = x;
            this.y = y;
            this.z = z;
            this.size = size;
        }
    }

    public static final class Handle {
        private final Entry entry;

        private Handle(Entry entry) {
            this.entry = entry;
        }

        public void moveTo(double x, double y, double z) {
            entry.x = x;
            entry.y = y;
            entry.z = z;
        }

        public void setSize(float size) {
            entry.size = Math.max(0f, size);
        }

        public boolean alive() {
            return !entry.dead;
        }

        public void release(float fadeOutSec) {
            if (entry.dead || entry.fadeStart != Double.POSITIVE_INFINITY) return;
            entry.fadeStart = clock;
            entry.fadeSec = Math.max(0.01f, fadeOutSec);
        }
    }

    private static final Map<ResourceLocation, Spec> SPECS = new ConcurrentHashMap<>();
    private static final List<Entry> ACTIVE = new ArrayList<>();
    private static final int MAX_ACTIVE = 96;
    private static double clock;
    private static long lastNanos;

    private AaapTransientLights() {}

    public static void register(ResourceLocation effectId, Spec spec) {
        SPECS.put(effectId, spec);
    }

    public static Spec spec(ResourceLocation effectId) {
        return SPECS.get(effectId);
    }

    public static void spawnCustom(
            Spec spec,
            double x,
            double y,
            double z,
            float size,
            float durationSec,
            float fadeOutSec) {
        if (spec == null || !(size > 0f) || !(durationSec > 0f)) return;

        Entry entry = add(spec, x, y, z, size);
        float fade = Mth.clamp(fadeOutSec, 0.01f, durationSec);
        entry.fadeStart = clock + durationSec - fade;
        entry.fadeSec = fade;
    }

    public static void spawnCustom(
            ResourceLocation effectId,
            double x,
            double y,
            double z,
            float size,
            float durationSec,
            float fadeOutSec) {
        spawnCustom(SPECS.get(effectId), x, y, z, size, durationSec, fadeOutSec);
    }

    public static Handle attach(Spec spec, double x, double y, double z, float size) {
        if (spec == null || !(size > 0f)) return null;
        return new Handle(add(spec, x, y, z, size));
    }

    private static Entry add(Spec spec, double x, double y, double z, float size) {
        if (ACTIVE.size() >= MAX_ACTIVE) kill(ACTIVE.remove(0));
        Entry entry = new Entry(spec, x, y, z, size);
        ACTIVE.add(entry);
        return entry;
    }

    static void tick() {
        long now = System.nanoTime();

        if (lastNanos != 0L && !Minecraft.getInstance().isPaused()) {
            clock += Math.min((now - lastNanos) / 1.0e9, 0.25);
        }

        lastNanos = now;

        for (Iterator<Entry> iterator = ACTIVE.iterator(); iterator.hasNext();) {
            Entry entry = iterator.next();
            float fade = 1f;

            if (clock >= entry.fadeStart) {
                float progress = (float) ((clock - entry.fadeStart) / entry.fadeSec);
                if (progress >= 1f) {
                    kill(entry);
                    iterator.remove();
                    continue;
                }
                float smooth = progress * progress * (3f - 2f * progress);
                fade = 1f - smooth;
            }

            Spec spec = entry.spec;
            VFXConfig config = VFXConfig.get();
            float radius = spec.radiusPerSize()
                    * entry.size
                    * config.aaaLightRadiusScale
                    * AaapLightExtraBoost.getRadiusMultiplier(spec.isTrail());
            float intensity = spec.intensity()
                    * (float) Math.sqrt(Math.max(0f, entry.size))
                    * fade
                    * config.aaaLightIntensity
                    * AaapLightExtraBoost.getIntensityMultiplier(spec.isTrail());

            AaapLightRegistry.put(
                    entry.key,
                    new AaapLightRegistry.Light(
                            entry.x,
                            entry.y,
                            entry.z,
                            radius,
                            0f,
                            spec.r(),
                            spec.g(),
                            spec.b(),
                            intensity));
        }
    }

    private static void kill(Entry entry) {
        AaapLightRegistry.remove(entry.key);
        entry.dead = true;
    }

    public static void clear() {
        for (Entry entry : ACTIVE) kill(entry);
        ACTIVE.clear();
    }
}