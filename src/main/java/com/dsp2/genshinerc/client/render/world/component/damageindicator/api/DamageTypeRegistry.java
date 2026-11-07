package com.dsp2.genshinerc.client.render.world.component.damageindicator.api;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class DamageTypeRegistry {
    private static final Map<String, DamageType> REGISTRY = new HashMap<>();
    private static final String NP = "genshinerc.core.damage_type.";
    private static final String CP = "color_";

    public static final DamageType PHYSICAL        = register("physical",        NP+"physical",        CP+"physical");
    public static final DamageType PYRO            = register("pyro",            NP+"pyro",            CP+"pyro");
    public static final DamageType HYDRO           = register("hydro",           NP+"hydro",           CP+"hydro");
    public static final DamageType ANEMO           = register("anemo",           NP+"anemo",           CP+"anemo");
    public static final DamageType ELECTRO         = register("electro",         NP+"electro",         CP+"electro");
    public static final DamageType DENDRO          = register("dendro",          NP+"dendro",          CP+"dendro");
    public static final DamageType CRYO            = register("cryo",            NP+"cryo",            CP+"cryo");
    public static final DamageType GEO             = register("geo",             NP+"geo",             CP+"geo");
    public static final DamageType OVERLOAD        = register("overload",        NP+"overload",        CP+"overload");
    public static final DamageType TRUE            = register("true",            NP+"true",            CP+"true");
    public static final DamageType SUPERCONDUCT    = register("superconduct",    NP+"superconduct",    CP+"superconduct");
    public static final DamageType ELECTRO_CHARGED = register("electro_charged", NP+"electro_charged", CP+"electro_charged");
    public static final DamageType SHATTER         = register("shatter",         NP+"shatter",         CP+"shatter");
    public static final DamageType BLOOM           = register("bloom",           NP+"bloom",           CP+"bloom");
    public static final DamageType HYPERBLOOM      = register("hyperbloom",      NP+"hyperbloom",      CP+"hyperbloom");
    public static final DamageType BURGEON         = register("burgeon",         NP+"burgeon",         CP+"burgeon");
    public static final DamageType BURNING         = register("burning",         NP+"burning",         CP+"burning");
    public static final DamageType QUICKEN         = register("quicken",         NP+"quicken",         CP+"quicken");
    public static final DamageType AGGRAVATE       = register("aggravate",       NP+"aggravate",       CP+"aggravate");
    public static final DamageType SPREAD          = register("spread",          NP+"spread",          CP+"spread");

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
