package com.dsp2.genshinerc.client.render.vfx.weaponshaders.test;

import java.util.HashMap;
import java.util.Map;

public final class WeaponShaderParams {
    private static final Map<WeaponShaderMode, Map<String, Float>> VALUES = new HashMap<>();

    static {
        for (WeaponShaderMode mode : WeaponShaderMode.values()) {
            Map<String, Float> m = new HashMap<>();
            for (ParamSpec spec : mode.params) m.put(spec.name(), spec.def());
            VALUES.put(mode, m);
        }
    }

    private WeaponShaderParams() {}

    public static float get(WeaponShaderMode mode, String name) {
        Map<String, Float> m = VALUES.get(mode);
        if (m == null) return 0f;
        Float v = m.get(name);
        return v == null ? 0f : v;
    }

    public static boolean set(WeaponShaderMode mode, String name, float value) {
        for (ParamSpec spec : mode.params) {
            if (spec.name().equals(name)) {
                float v = Math.max(spec.min(), Math.min(spec.max(), value));
                VALUES.computeIfAbsent(mode, k -> new HashMap<>()).put(name, v);
                return true;
            }
        }
        return false;
    }

    public static void reset(WeaponShaderMode mode) {
        Map<String, Float> m = VALUES.get(mode);
        if (m == null) return;
        for (ParamSpec spec : mode.params) m.put(spec.name(), spec.def());
    }

    public static void resetAll() {
        for (WeaponShaderMode m : WeaponShaderMode.values()) reset(m);
    }

    public static boolean has(WeaponShaderMode mode, String name) {
        for (ParamSpec spec : mode.params) if (spec.name().equals(name)) return true;
        return false;
    }
}