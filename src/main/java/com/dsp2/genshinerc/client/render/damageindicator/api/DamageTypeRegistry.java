package com.dsp2.genshinerc.client.render.damageindicator.api;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class DamageTypeRegistry {
    private static final Map<String, DamageType> REGISTRY = new HashMap<>();
    private static final String NP = "genshinerc.damageindicator.damage_type.";
    private static final String CP = "color_";

    public static final DamageType PHYSICAL    = register("physical",   NP+"physical",   CP+"physical");
    public static final DamageType PYRO        = register("pyro",       NP+"pyro",       CP+"pyro");
    public static final DamageType HYDRO       = register("hydro",      NP+"hydro",      CP+"hydro");
    public static final DamageType ANEMO       = register("anemo",      NP+"anemo",      CP+"anemo");
    public static final DamageType ELECTRO     = register("electro",    NP+"electro",    CP+"electro");
    public static final DamageType DENDRO      = register("dendro",     NP+"dendro",     CP+"dendro");
    public static final DamageType CRYO        = register("cryo",       NP+"cryo",       CP+"cryo");
    public static final DamageType GEO         = register("geo",        NP+"geo",        CP+"geo");
    public static final DamageType OVERLOAD    = register("overload",   NP+"overload",   CP+"overload");

    public static DamageType register(String key, String nameKey, String configColorKey) {
        DamageType t = new DamageType(key, nameKey, configColorKey);
        REGISTRY.put(key, t);
        return t;
    }
    public static DamageType get(String key) {
        if (key == null) return PHYSICAL;
        return REGISTRY.getOrDefault(key, PHYSICAL);
    }
    public static Map<String, DamageType> all() { return Collections.unmodifiableMap(REGISTRY); }
}