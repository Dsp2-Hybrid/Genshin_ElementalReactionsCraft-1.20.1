package com.dsp2.genshinerc.common.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class ServerConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.BooleanValue ENABLED;
    public static final ForgeConfigSpec.IntValue MAX_DISTANCE;

    static {
        BUILDER.push("Damage Indicator Settings");

        ENABLED = BUILDER.comment("Globally enable or disable damage indicators.")
                .define("enabled", true);

        MAX_DISTANCE = BUILDER.comment("Max broadcast distance. Players outside this range won't receive damage numbers.")
                .defineInRange("maxDistance", 35, 1, 128);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}