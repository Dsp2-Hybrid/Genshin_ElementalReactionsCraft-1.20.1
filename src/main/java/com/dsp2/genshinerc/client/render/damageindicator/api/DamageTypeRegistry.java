package com.dsp2.genshinerc.client.render.damageindicator.api;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class DamageTypeRegistry {
    private static final Map<String, DamageType> REGISTRY = new HashMap<>();
    private static final String P = "genshinerc.damageindicator.damage_type.";

    public static final DamageType PHYSICAL     = register("physical",      CustomColor.hex(0xFFFFFF), P + "physical");
    public static final DamageType PYRO         = register("pyro",          CustomColor.hex(0xFF6600), P + "pyro");
    public static final DamageType HYDRO        = register("hydro",         CustomColor.hex(0x00CCFF), P + "hydro");
    public static final DamageType ANEMO        = register("anemo",         CustomColor.hex(0x66FFCC), P + "anemo");
    public static final DamageType ELECTRO      = register("electro",       CustomColor.hex(0xCC66FF), P + "electro");
    public static final DamageType DENDRO       = register("dendro",        CustomColor.hex(0x66CC33), P + "dendro");
    public static final DamageType CRYO         = register("cryo",          CustomColor.hex(0x99FFFF), P + "cryo");
    public static final DamageType GEO          = register("geo",           CustomColor.hex(0xFFCC33), P + "geo");
    public static final DamageType OVERLOAD     = register("overload",      CustomColor.hex(0xFF3333), P + "overload");
    public static final DamageType VAPORIZE     = register("vaporize",      CustomColor.hex(0xFF5533), P + "vaporize");
    public static final DamageType MELT         = register("melt",          CustomColor.hex(0xFF8844), P + "melt");
    public static final DamageType SUPERCONDUCT = register("superconduct",  CustomColor.hex(0x7766FF), P + "superconduct");
    public static final DamageType ELECTRO_CHARGED = register("electro_charged", CustomColor.hex(0xAA66FF), P + "electro_charged");
    public static final DamageType FROZEN       = register("frozen",        CustomColor.hex(0x99DDFF), P + "frozen");
    public static final DamageType SHATTER      = register("shatter",       CustomColor.hex(0xCCEEFF), P + "shatter");
    public static final DamageType SWIRL        = register("swirl",         CustomColor.hex(0x66DDAA), P + "swirl");
    public static final DamageType CRYSTALLIZE  = register("crystallize",   CustomColor.hex(0xEECC55), P + "crystallize");
    public static final DamageType BURNING      = register("burning",       CustomColor.hex(0xFF6633), P + "burning");
    public static final DamageType QUICKEN      = register("quicken",       CustomColor.hex(0x99DD44), P + "quicken");
    public static final DamageType AGGRAVATE    = register("aggravate",     CustomColor.hex(0xBB99FF), P + "aggravate");
    public static final DamageType SPREAD       = register("spread",        CustomColor.hex(0x66AA33), P + "spread");
    public static final DamageType BLOOM        = register("bloom",         CustomColor.hex(0x88DD55), P + "bloom");
    public static final DamageType HYPERBLOOM   = register("hyperbloom",    CustomColor.hex(0xAA66FF), P + "hyperbloom");
    public static final DamageType BURGEON      = register("burgeon",       CustomColor.hex(0xFF8844), P + "burgeon");

    public static DamageType register(String key, CustomColor color, String nameKey) {
        DamageType t = new DamageType(key, color, nameKey);
        REGISTRY.put(key, t);
        return t;
    }
    public static DamageType get(String key) {
        if (key == null) return PHYSICAL;
        return REGISTRY.getOrDefault(key, PHYSICAL);
    }
    public static boolean exists(String key) { return key != null && REGISTRY.containsKey(key); }
    public static Map<String, DamageType> all() { return Collections.unmodifiableMap(REGISTRY); }
}