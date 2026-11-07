package com.dsp2.genshinerc.common.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class ServerConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.BooleanValue ENABLED;
    public static final ForgeConfigSpec.IntValue MAX_DISTANCE;

    public static final ForgeConfigSpec.DoubleValue SCALE, LEVEL_MULT_1, LEVEL_MULT_80, LEVEL_MULT_90,
            SWIRL_RADIUS, SWIRL_SPREAD_UNITS, CRYSTALLIZE_SHIELD, CRYSTALLIZE_SECONDS,
            CORE_TRIGGER_RADIUS, CORE_BLAST_RADIUS, HYPERBLOOM_RANGE, CORE_LIMIT_RADIUS, EC_RADIUS;
    public static final ForgeConfigSpec.IntValue FREEZE_MEMORY_TICKS, FREEZE_SLOW_AMPLIFIER, CORE_MAX, CORE_FUSE_TICKS;

    public static final ForgeConfigSpec.BooleanValue NATURAL_ENABLED;
    public static final ForgeConfigSpec.DoubleValue DEFENSE_COEFFICIENT, DROWN_FLOOR, SWIRL_SHRED, SWIRL_SHRED_SECONDS,
            NATURAL_WEAK_GAUGE, NATURAL_STRONG_GAUGE, RAIN_SECONDS, WATER_SECONDS, SNOW_SECONDS, POWDER_SNOW_SECONDS,
            SWAMP_WATER_SECONDS, SWAMP_NEAR_MIN_SECONDS, SWAMP_NEAR_MAX_SECONDS, SWAMP_RADIUS, FIRE_SECONDS, LAVA_SECONDS;
    public static final ForgeConfigSpec.IntValue PERIODIC_INTERVAL_TICKS;

    public static final ForgeConfigSpec.DoubleValue TOUGHNESS_C, TOUGHNESS_K;
    public static final ForgeConfigSpec.DoubleValue SUPERCONDUCT_RES_SHRED, SUPERCONDUCT_RES_SECONDS;
    public static final ForgeConfigSpec.IntValue REACTION_TEXT_COOLDOWN;
    public static final ForgeConfigSpec.DoubleValue REACTION_MAX_HP_FRACTION, CRYSTALLIZE_SHIELD_COEF, FROZEN_CRIT_RATE_BONUS;

    static {
        BUILDER.push("Damage Indicator Settings");

        ENABLED = BUILDER.comment("Globally enable or disable damage indicators.")
                .define("enabled", true);

        MAX_DISTANCE = BUILDER.comment("Max broadcast distance. Players outside this range won't receive damage numbers.")
                .defineInRange("maxDistance", 35, 1, 128);

        BUILDER.pop();

        BUILDER.push("Elements");

        SCALE = BUILDER.comment("Genshin to MC scale. Applied to transformative/catalyze/shield values. EM is not scaled.")
                .defineInRange("scale", 1.0, 0.01, 1000.0);
        LEVEL_MULT_1 = BUILDER.comment("Level multiplier anchor, level 1 (= 1.25 * 500^(1/100)).")
                .defineInRange("levelMult1", 1.330, 0.01, 100000.0);
        LEVEL_MULT_80 = BUILDER.comment("Level multiplier anchor, level 80 (= 1.25 * 500^(80/100)).")
                .defineInRange("levelMult80", 180.3, 0.01, 100000.0);
        LEVEL_MULT_90 = BUILDER.comment("Level multiplier anchor, level 90 (= 1.25 * 500^(90/100)). Above 90 extrapolates.")
                .defineInRange("levelMult90", 335.7, 0.01, 100000.0);
        FREEZE_MEMORY_TICKS = BUILDER.comment("A re-freeze within this many ticks inherits the previous Frozen decay progress (UNVERIFIED).")
                .defineInRange("freezeMemoryTicks", 120, 0, 72000);
        FREEZE_SLOW_AMPLIFIER = BUILDER.comment("Slowness amplifier applied to frozen mobs.")
                .defineInRange("freezeSlowAmplifier", 6, 0, 30);
        SWIRL_RADIUS = BUILDER.comment("Swirl spread radius (UNVERIFIED).")
                .defineInRange("swirlRadius", 5.0, 0.5, 32.0);
        SWIRL_SPREAD_UNITS = BUILDER.comment("Trigger gauge units of the swirled element applied to nearby enemies (UNVERIFIED).")
                .defineInRange("swirlSpreadUnits", 0.5, 0.1, 8.0);
        CRYSTALLIZE_SHIELD = BUILDER.comment("DEPRECATED (Crystallize now uses crystallizeShieldCoef). Kept for compatibility.")
                .defineInRange("crystallizeShield", 2000.0, 1.0, 1000000.0);
        CRYSTALLIZE_SECONDS = BUILDER.comment("Crystallize shield duration in seconds (UNVERIFIED).")
                .defineInRange("crystallizeSeconds", 15.0, 1.0, 600.0);
        CORE_TRIGGER_RADIUS = BUILDER.comment("Electro/Pyro hit detonates Dendro Cores within this radius of the target (UNVERIFIED).")
                .defineInRange("coreTriggerRadius", 3.0, 0.5, 32.0);
        CORE_BLAST_RADIUS = BUILDER.comment("Dendro Core explosion radius (UNVERIFIED).")
                .defineInRange("coreBlastRadius", 3.0, 0.5, 32.0);
        HYPERBLOOM_RANGE = BUILDER.comment("Hyperbloom hits the nearest enemy within this range (UNVERIFIED).")
                .defineInRange("hyperbloomRange", 10.0, 1.0, 64.0);
        CORE_LIMIT_RADIUS = BUILDER.comment("Dendro Core limit radius (design doc: 8).")
                .defineInRange("coreLimitRadius", 8.0, 1.0, 64.0);
        CORE_MAX = BUILDER.comment("Max Dendro Cores in the limit radius (design doc: 5).")
                .defineInRange("coreMax", 5, 1, 64);
        CORE_FUSE_TICKS = BUILDER.comment("Dendro Core fuse in ticks (design doc: 6 s).")
                .defineInRange("coreFuseTicks", 120, 1, 1200);
        EC_RADIUS = BUILDER.comment("Electro-Charged damage source selection radius (design doc: 8).")
                .defineInRange("ecRadius", 8.0, 1.0, 64.0);
        DEFENSE_COEFFICIENT = BUILDER.comment("M_def = (L+100)/((L+100)+k*Def). UNVERIFIED, tuned so Def 20 ~ 39% reduction at level 90.")
                .defineInRange("defenseCoefficient", 6.0, 0.1, 100.0);
        DROWN_FLOOR = BUILDER.comment("Drowning damage cannot be reduced below this fraction of the original (UNVERIFIED reading of the doc).")
                .defineInRange("drownFloor", 0.5, 0.0, 1.0);
        SWIRL_SHRED = BUILDER.comment("RES change applied to the swirled element on swirled targets. Outline says reaction, lecture says Viridescent Venerer set (UNVERIFIED).")
                .defineInRange("swirlShred", -0.4, -1.0, 0.0);
        SWIRL_SHRED_SECONDS = BUILDER.comment("Swirl RES shred duration (outline: 10 s).")
                .defineInRange("swirlShredSeconds", 10.0, 0.5, 600.0);
        NATURAL_ENABLED = BUILDER.comment("Rain/snow/water/swamp/fire/lava exposure and Slime/Magma Cube periodic auras.")
                .define("naturalEnabled", true);
        NATURAL_WEAK_GAUGE = BUILDER.comment("Natural weak gauge cap, no wear. Doc example: rain = 0.5 (conflicts with weak = 1 x 0.8; UNVERIFIED).")
                .defineInRange("naturalWeakGauge", 0.5, 0.05, 4.0);
        NATURAL_STRONG_GAUGE = BUILDER.comment("Natural strong gauge cap (2 units, no wear; UNVERIFIED).")
                .defineInRange("naturalStrongGauge", 2.0, 0.05, 4.0);
        RAIN_SECONDS = BUILDER.comment("Seconds of rain to reach the weak cap (doc: 2-3 s).").defineInRange("rainSeconds", 2.5, 0.2, 120.0);
        WATER_SECONDS = BUILDER.comment("Seconds submerged to reach strong Hydro (doc: 0.5 s).").defineInRange("waterSeconds", 0.5, 0.2, 120.0);
        SNOW_SECONDS = BUILDER.comment("Seconds of snowfall to reach weak Cryo (UNVERIFIED, same as rain).").defineInRange("snowSeconds", 2.5, 0.2, 120.0);
        POWDER_SNOW_SECONDS = BUILDER.comment("Seconds in powder snow to reach strong Cryo (doc: 0.5 s).").defineInRange("powderSnowSeconds", 0.5, 0.2, 120.0);
        SWAMP_WATER_SECONDS = BUILDER.comment("Seconds in swamp water to reach weak Dendro (doc: 2-3 s).").defineInRange("swampWaterSeconds", 2.5, 0.2, 120.0);
        SWAMP_NEAR_MIN_SECONDS = BUILDER.comment("Near a swamp, weak Dendro time at distance 0 (doc: 5 s).").defineInRange("swampNearMinSeconds", 5.0, 0.2, 600.0);
        SWAMP_NEAR_MAX_SECONDS = BUILDER.comment("Near a swamp, weak Dendro time at the far edge (doc: 30 s).").defineInRange("swampNearMaxSeconds", 30.0, 0.2, 600.0);
        SWAMP_RADIUS = BUILDER.comment("Swamp proximity radius (doc: 10).").defineInRange("swampRadius", 10.0, 1.0, 64.0);
        FIRE_SECONDS = BUILDER.comment("Seconds on fire to reach weak Pyro (doc: 0.2 s).").defineInRange("fireSeconds", 0.2, 0.2, 120.0);
        LAVA_SECONDS = BUILDER.comment("Seconds in lava to reach strong Pyro (doc: 0.5 s).").defineInRange("lavaSeconds", 0.5, 0.2, 120.0);
        PERIODIC_INTERVAL_TICKS = BUILDER.comment("Slime (Dendro) / Magma Cube (Pyro) re-apply their weak aura this often if it is missing (UNVERIFIED).")
                .defineInRange("periodicIntervalTicks", 300, 20, 72000);
        TOUGHNESS_C = BUILDER.comment("Toughness curvature constant C, panel units (UNVERIFIED, chosen so a full netherite set = 0.5 conversion).")
                .defineInRange("toughnessC", 120.0, 1.0, 100000.0);
        TOUGHNESS_K = BUILDER.comment("Toughness global steepness K (UNVERIFIED).")
                .defineInRange("toughnessK", 2.0, 0.0, 100.0);
        SUPERCONDUCT_RES_SHRED = BUILDER.comment("Physical RES change from Superconduct (outline: -40%).")
                .defineInRange("superconductResShred", -0.4, -1.0, 0.0);
        SUPERCONDUCT_RES_SECONDS = BUILDER.comment("Superconduct RES shred duration (docs: 12 s).")
                .defineInRange("superconductResSeconds", 12.0, 0.5, 600.0);
        REACTION_TEXT_COOLDOWN = BUILDER.comment("Min ticks between identical reaction texts on one target (UNVERIFIED).")
                .defineInRange("reactionTextCooldownTicks", 10, 0, 200);
        REACTION_MAX_HP_FRACTION = BUILDER.comment("One reaction hit cannot deal more than this fraction of the target's max HP (UNVERIFIED). Stops EM from one-shotting.")
                .defineInRange("reactionMaxHpFraction", 0.5, 0.05, 1.0);
        CRYSTALLIZE_SHIELD_COEF = BUILDER.comment("Crystallize shield = coef x levelMult x EM bonus. 1.14 = 1647/1447 at level 90 (UNVERIFIED, from memory).")
                .defineInRange("crystallizeShieldCoef", 1.14, 0.0, 100.0);
        FROZEN_CRIT_RATE_BONUS = BUILDER.comment("Crit rate bonus against Frozen targets (doc: 40%).")
                .defineInRange("frozenCritRateBonus", 0.4, 0.0, 1.0);

        BUILDER.pop();

        SPEC = BUILDER.build();
    }
}
