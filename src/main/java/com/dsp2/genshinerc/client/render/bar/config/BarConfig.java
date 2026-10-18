package com.dsp2.genshinerc.client.render.bar.config;

import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.controller.ColorControllerBuilder;
import dev.isxander.yacl3.api.controller.EnumControllerBuilder;
import dev.isxander.yacl3.api.controller.FloatSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.loading.FMLPaths;

import java.awt.Color;

public class BarConfig {
    public static final ConfigClassHandler<BarConfig> HANDLER =
            ConfigClassHandler.createBuilder(BarConfig.class)
                    .id(ResourceLocation.fromNamespaceAndPath("genshinerc", "healthbar"))
                    .serializer(cfg -> GsonConfigSerializerBuilder.create(cfg)
                            .setPath(FMLPaths.CONFIGDIR.get().resolve("genshinerc-healthbar.json5"))
                            .build())
                    .build();

    public static BarConfig get() { return HANDLER.instance(); }
    public static void load() { HANDLER.load(); }
    public static void save() { HANDLER.save(); }

    public enum PlayerMode { NONE, ON_CHANGE, ALWAYS }
    public enum MobMode { NONE, ALWAYS, ALWAYS_WHEN_NOT_FULLHP, ON_HURT }
    public enum EasingType { EASE_IN_CUBIC, EASE_OUT_CUBIC, EASE_IN_OUT_CUBIC, LINEAR, SINE }

    @SerialEntry public boolean enabled = true;
    @SerialEntry public PlayerMode playerMode = PlayerMode.ON_CHANGE;
    @SerialEntry public MobMode mobMode = MobMode.ALWAYS_WHEN_NOT_FULLHP;
    @SerialEntry public int maxDistance = 32;
    @SerialEntry public float yOffset = 0.25f;
    @SerialEntry public float yOffsetHeightFactor = 0.05f;

    @SerialEntry public float baseLength = 1.10f;
    @SerialEntry public float baseWidth = 0.25f;
    @SerialEntry public float heightLengthFactor = 0.35f;
    @SerialEntry public float heightWidthFactor = 0.0f;
    @SerialEntry public float bboxLengthFactor = 0.40f;
    @SerialEntry public float bboxWidthFactor = 0.0f;

    @SerialEntry public String enemyColor = "#FF4444";
    @SerialEntry public String allyColor = "#44FF44";

    @SerialEntry public float playerOverlayShrinkTime = 1.5f;
    @SerialEntry public float mobOverlayShrinkTime = 1.5f;
    @SerialEntry public EasingType whiteEasing = EasingType.EASE_OUT_CUBIC;
    @SerialEntry public float overlayFadeTime = 0.55f;

    @SerialEntry public float hurtDisplayTime = 2.0f;
    @SerialEntry public float deathFadeDelay = 0.35f;
    @SerialEntry public float fadeInTime = 0.25f;
    @SerialEntry public float fadeOutTime = 0.25f;

    public static float interpolate(float x, EasingType type) {
        if (x <= 0f) return 0f;
        if (x >= 1f) return 1f;
        switch (type) {
            case LINEAR: return x;
            case EASE_IN_CUBIC: return x * x * x;
            case EASE_OUT_CUBIC:
                float f = 1.0f - x;
                return 1.0f - (f * f * f);
            case EASE_IN_OUT_CUBIC:
                return x < 0.5f ? 4.0f * x * x * x : 1.0f - (float)Math.pow(-2.0f * x + 2.0f, 3.0f) / 2.0f;
            case SINE:
                return (float)(1.0f - Math.cos((x * Math.PI) / 2.0f));
            default: return x;
        }
    }

    private static Component t(String k) { return Component.translatable(k); }
    private static Component td(String k) { return Component.translatable(k + ".desc"); }
    private static String P(String k) { return "genshinerc.bar.config." + k; }

    public static OptionGroup createGroup() {
        BarConfig c = get();
        return OptionGroup.createBuilder()
                .name(t("genshinerc.bar.group.title"))
                .description(OptionDescription.of(td("genshinerc.bar.group")))
                .option(boolOpt("enabled", true, () -> c.enabled, v -> c.enabled = v))
                .option(enumOpt("player_mode", PlayerMode.class, PlayerMode.ON_CHANGE, () -> c.playerMode, v -> c.playerMode = v))
                .option(enumOpt("mob_mode", MobMode.class, MobMode.ALWAYS_WHEN_NOT_FULLHP, () -> c.mobMode, v -> c.mobMode = v))
                .option(intOpt("max_distance", 32, () -> c.maxDistance, v -> c.maxDistance = v, 4, 128))
                .option(floatOpt("y_offset", 0.25f, () -> c.yOffset, v -> c.yOffset = v, -1f, 3f))
                .option(floatOpt("y_offset_height_factor", 0.05f, () -> c.yOffsetHeightFactor, v -> c.yOffsetHeightFactor = v, 0f, 1f))
                .option(floatOpt("base_length", 1.10f, () -> c.baseLength, v -> c.baseLength = v, 0.5f, 2.0f))
                .option(floatOpt("base_width", 0.25f, () -> c.baseWidth, v -> c.baseWidth = v, 0.05f, 0.50f))
                .option(floatOpt("height_length_factor", 0.35f, () -> c.heightLengthFactor, v -> c.heightLengthFactor = v, 0.0f, 1.0f))
                .option(floatOpt("bbox_length_factor", 0.40f, () -> c.bboxLengthFactor, v -> c.bboxLengthFactor = v, 0.0f, 1.0f))
                .option(floatOpt("height_width_factor", 0.0f, () -> c.heightWidthFactor, v -> c.heightWidthFactor = v, 0.0f, 0.25f))
                .option(floatOpt("bbox_width_factor", 0.0f, () -> c.bboxWidthFactor, v -> c.bboxWidthFactor = v, 0.0f, 0.25f))
                .option(floatOpt("player_overlay_shrink_time", 1.5f, () -> c.playerOverlayShrinkTime, v -> c.playerOverlayShrinkTime = v, 0.1f, 5f))
                .option(floatOpt("mob_overlay_shrink_time", 1.5f, () -> c.mobOverlayShrinkTime, v -> c.mobOverlayShrinkTime = v, 0.1f, 5f))
                .option(enumOpt("white_easing", EasingType.class, EasingType.EASE_OUT_CUBIC, () -> c.whiteEasing, v -> c.whiteEasing = v))
                .option(floatOpt("overlay_fade_time", 0.55f, () -> c.overlayFadeTime, v -> c.overlayFadeTime = v, 0.1f, 2f))
                .option(floatOpt("hurt_display_time", 2.0f, () -> c.hurtDisplayTime, v -> c.hurtDisplayTime = v, 0.1f, 10f))
                .option(floatOpt("death_fade_delay", 0.35f, () -> c.deathFadeDelay, v -> c.deathFadeDelay = v, 0f, 1f))
                .option(floatOpt("fade_in_time", 0.25f, () -> c.fadeInTime, v -> c.fadeInTime = v, 0f, 1f))
                .option(floatOpt("fade_out_time", 0.25f, () -> c.fadeOutTime, v -> c.fadeOutTime = v, 0f, 1f))
                .option(colorOpt("enemy_color", new Color(0xFF4444), () -> toAwt(c.enemyColor), v -> c.enemyColor = fromAwt(v)))
                .option(colorOpt("ally_color", new Color(0x44FF44), () -> toAwt(c.allyColor), v -> c.allyColor = fromAwt(v)))
                .build();
    }

    private static Option<Boolean> boolOpt(String key, boolean def, java.util.function.Supplier<Boolean> g, java.util.function.Consumer<Boolean> s) {
        return Option.<Boolean>createBuilder().name(t(P(key))).description(OptionDescription.of(td(P(key))))
                .binding(def, g, s).controller(TickBoxControllerBuilder::create).build();
    }

    private static Option<Float> floatOpt(String key, float def, java.util.function.Supplier<Float> g, java.util.function.Consumer<Float> s, float min, float max) {
        return Option.<Float>createBuilder().name(t(P(key))).description(OptionDescription.of(td(P(key))))
                .binding(def, g, s)
                .controller(opt -> FloatSliderControllerBuilder.create(opt).range(min, max).step(0.01f).formatValue(v -> Component.literal(String.format("%.2f", v))))
                .build();
    }

    private static Option<Integer> intOpt(String key, int def, java.util.function.Supplier<Integer> g, java.util.function.Consumer<Integer> s, int min, int max) {
        return Option.<Integer>createBuilder().name(t(P(key))).description(OptionDescription.of(td(P(key))))
                .binding(def, g, s).controller(opt -> IntegerSliderControllerBuilder.create(opt).range(min, max).step(1)).build();
    }

    private static Option<Color> colorOpt(String key, Color def, java.util.function.Supplier<Color> g, java.util.function.Consumer<Color> s) {
        return Option.<Color>createBuilder().name(t(P(key))).description(OptionDescription.of(td(P(key))))
                .binding(def, g, s).controller(ColorControllerBuilder::create).build();
    }

    private static <T extends Enum<T>> Option<T> enumOpt(String key, Class<T> enumClass, T def, java.util.function.Supplier<T> g, java.util.function.Consumer<T> s) {
        return Option.<T>createBuilder().name(t(P(key))).description(OptionDescription.of(td(P(key))))
                .binding(def, g, s).controller(opt -> EnumControllerBuilder.create(opt).enumClass(enumClass)).build();
    }

    public static Color toAwt(String hex) {
        try { return new Color(Integer.parseInt(hex.replace("#","").trim(), 16)); }
        catch (Exception e) { return Color.WHITE; }
    }
    public static String fromAwt(Color c) { return String.format("#%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue()); }
}