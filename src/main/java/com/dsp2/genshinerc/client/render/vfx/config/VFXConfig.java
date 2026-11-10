package com.dsp2.genshinerc.client.render.vfx.config;

import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.controller.DoubleSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.FloatSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.loading.FMLPaths;

public class VFXConfig {
    public static final ConfigClassHandler<VFXConfig> HANDLER =
            ConfigClassHandler.createBuilder(VFXConfig.class)
                    .id(ResourceLocation.fromNamespaceAndPath("genshinerc", "vfx"))
                    .serializer(cfg -> GsonConfigSerializerBuilder.create(cfg)
                            .setPath(FMLPaths.CONFIGDIR.get().resolve("genshinerc-vfx.json5"))
                            .build())
                    .build();

    public static VFXConfig get() { return HANDLER.instance(); }
    public static void load() { HANDLER.load(); }
    public static void save() { HANDLER.save(); }

    @SerialEntry public boolean statusTextEnabled = true;
    @SerialEntry public double statusTextScale = 0.9;
    @SerialEntry public boolean elementShellEnabled = true;
    @SerialEntry public boolean aaaLightEnabled = true;
    @SerialEntry public float aaaLightIntensity = 5.0f;
    @SerialEntry public int aaaLightMaxCount = 30;
    @SerialEntry public float aaaLightRadiusScale = 6.0f;
    @SerialEntry public float shellScale = 1.0f;
    @SerialEntry public float shellReactionDim = 0.35f;
    @SerialEntry public float shellAlphaSmoothSpeed = 6.0f;
    @SerialEntry public float shellReactionHoldMs = 600f;
    @SerialEntry public float shellReactionFadeMs = 700f;

    @SerialEntry public boolean cryoTintEnabled = true;
    @SerialEntry public float cryoTintAlpha = 0.45f;
    @SerialEntry public float cryoPulseSeconds = 2.2f;
    @SerialEntry public float cryoPulseMin = 0.45f;

    @SerialEntry public boolean frostOverlayEnabled = true;
    @SerialEntry public float frostMaxAlpha = 0.55f;
    @SerialEntry public float frostEaseInSpeed = 1.6f;
    @SerialEntry public float frostEaseOutSpeed = 2.4f;


    private static Component t(String k) { return Component.translatable(k); }
    private static Component td(String k) { return Component.translatable(k + ".desc"); }
    private static String P(String k) { return "genshinerc.bar.config." + k; }

    public static OptionGroup createGroup() {
        VFXConfig c = get();
        return OptionGroup.createBuilder()
                .name(t("genshinerc.bar.group.visuals"))
                .description(OptionDescription.of(td("genshinerc.bar.group.visuals")))
                .option(boolOpt("aaa_light_enabled", true, () -> c.aaaLightEnabled, v -> c.aaaLightEnabled = v))
                .option(floatOpt("aaa_light_intensity", 5.0f, () -> c.aaaLightIntensity, v -> c.aaaLightIntensity = v, 0.0f, 50.0f))
                .option(intOpt("aaa_light_max_count", 30, () -> c.aaaLightMaxCount, v -> c.aaaLightMaxCount = v, 8, 128))
                .option(floatOpt("aaa_light_radius_scale", 6.0f, () -> c.aaaLightRadiusScale, v -> c.aaaLightRadiusScale = v, 0.1f, 64.0f))
                .option(floatOpt("shell_scale", 1.0f, () -> c.shellScale, v -> c.shellScale = v, 0.1f, 3f))
                .option(percentOpt("shell_reaction_dim", 0.35f, () -> c.shellReactionDim, v -> c.shellReactionDim = v, 0f, 1f))
                .option(floatOpt("shell_alpha_smooth_speed", 6.0f, () -> c.shellAlphaSmoothSpeed, v -> c.shellAlphaSmoothSpeed = v, 0.1f, 20f))
                .option(floatOpt("shell_reaction_hold_ms", 600f, () -> c.shellReactionHoldMs, v -> c.shellReactionHoldMs = v, 0f, 5000f))
                .option(floatOpt("shell_reaction_fade_ms", 700f, () -> c.shellReactionFadeMs, v -> c.shellReactionFadeMs = v, 0f, 5000f))
                .option(boolOpt("cryo_tint_enabled", true, () -> c.cryoTintEnabled, v -> c.cryoTintEnabled = v))
                .option(percentOpt("cryo_tint_alpha", 0.45f, () -> c.cryoTintAlpha, v -> c.cryoTintAlpha = v, 0f, 1f))
                .option(floatOpt("cryo_pulse_seconds", 2.2f, () -> c.cryoPulseSeconds, v -> c.cryoPulseSeconds = v, 0.1f, 10f))
                .option(percentOpt("cryo_pulse_min", 0.45f, () -> c.cryoPulseMin, v -> c.cryoPulseMin = v, 0f, 1f))
                .option(boolOpt("frost_overlay_enabled", true, () -> c.frostOverlayEnabled, v -> c.frostOverlayEnabled = v))
                .option(percentOpt("frost_max_alpha", 0.55f, () -> c.frostMaxAlpha, v -> c.frostMaxAlpha = v, 0f, 1f))
                .option(floatOpt("frost_ease_in_speed", 1.6f, () -> c.frostEaseInSpeed, v -> c.frostEaseInSpeed = v, 0.1f, 10f))
                .option(floatOpt("frost_ease_out_speed", 2.4f, () -> c.frostEaseOutSpeed, v -> c.frostEaseOutSpeed = v, 0.1f, 10f))
                .build();
    }

    private static Option<Boolean> boolOpt(String key, boolean def, java.util.function.Supplier<Boolean> g, java.util.function.Consumer<Boolean> s) {
        return Option.<Boolean>createBuilder().name(t(P(key))).description(OptionDescription.of(td(P(key))))
                .binding(def, g, s).controller(TickBoxControllerBuilder::create).build();
    }
    private static Option<Integer> intOpt(String key, int def, java.util.function.Supplier<Integer> g, java.util.function.Consumer<Integer> s, int min, int max) {
        return Option.<Integer>createBuilder().name(t(P(key))).description(OptionDescription.of(td(P(key))))
                .binding(def, g, s).controller(opt -> IntegerSliderControllerBuilder.create(opt).range(min, max).step(1)).build();
    }
    private static Option<Float> floatOpt(String key, float def, java.util.function.Supplier<Float> g, java.util.function.Consumer<Float> s, float min, float max) {
        return Option.<Float>createBuilder().name(t(P(key))).description(OptionDescription.of(td(P(key))))
                .binding(def, g, s).controller(opt -> FloatSliderControllerBuilder.create(opt).range(min, max).step(0.01f)).build();
    }
    private static Option<Float> percentOpt(String key, float def, java.util.function.Supplier<Float> g, java.util.function.Consumer<Float> s, float min, float max) {
        return Option.<Float>createBuilder().name(t(P(key))).description(OptionDescription.of(td(P(key))))
                .binding(def, g, s).controller(opt -> FloatSliderControllerBuilder.create(opt).range(min, max).step(0.01f)
                        .formatValue(v -> Component.literal(String.format("%.0f%%", v * 100f)))).build();
    }
    private static Option<Double> dblOpt(String key, double def, java.util.function.Supplier<Double> g, java.util.function.Consumer<Double> s, double min, double max) {
        return Option.<Double>createBuilder().name(t(P(key))).description(OptionDescription.of(td(P(key))))
                .binding(def, g, s).controller(opt -> DoubleSliderControllerBuilder.create(opt).range(min, max).step(0.01)).build();
    }
}