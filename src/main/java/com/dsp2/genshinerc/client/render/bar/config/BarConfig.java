package com.dsp2.genshinerc.client.render.bar.config;

import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.controller.ColorControllerBuilder;
import dev.isxander.yacl3.api.controller.EnumControllerBuilder;
import dev.isxander.yacl3.api.controller.FloatSliderControllerBuilder;
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

    @SerialEntry public boolean enabled = true;
    @SerialEntry public PlayerMode playerMode = PlayerMode.ON_CHANGE;
    @SerialEntry public boolean showFullHealthMobs = true;
    @SerialEntry public float yOffset = 0.25f;
    @SerialEntry public float maxDistance = 64.0f;

    // 用户严格要求的 6 个配置项
    @SerialEntry public float baseLength = 1.0f;
    @SerialEntry public float baseWidth = 0.09765625f;
    @SerialEntry public float heightLengthFactor = 0.2f;
    @SerialEntry public float heightWidthFactor = 0.0f;
    @SerialEntry public float bboxLengthFactor = 0.4f;
    @SerialEntry public float bboxWidthFactor = 0.0f;

    @SerialEntry public String enemyColor = "#FF4444";
    @SerialEntry public String allyColor = "#44FF44";

    @SerialEntry public float overlayShrinkTime = 0.5f;
    @SerialEntry public float overlayFadeTime = 0.2f;

    private static Component t(String k) { return Component.translatable(k); }
    private static Component td(String k) { return Component.translatable(k + ".desc"); }
    private static String P(String k) { return "genshinerc.bar.config." + k; }

    public static OptionGroup createGroup() {
        BarConfig c = get();
        return OptionGroup.createBuilder()
                .name(t("genshinerc.bar.group.title"))
                .description(OptionDescription.of(td("genshinerc.bar.group")))
                .option(boolOpt("enabled", true, () -> c.enabled, v -> c.enabled = v))
                .option(enumOpt("player_mode", PlayerMode.ON_CHANGE, () -> c.playerMode, v -> c.playerMode = v))
                .option(boolOpt("show_full_health_mobs", true, () -> c.showFullHealthMobs, v -> c.showFullHealthMobs = v))
                .option(floatOpt("max_distance", 64f, () -> c.maxDistance, v -> c.maxDistance = v, 4f, 128f))
                .option(floatOpt("y_offset", 0.25f, () -> c.yOffset, v -> c.yOffset = v, -1f, 3f))
                .option(floatOpt("base_length", 1.0f, () -> c.baseLength, v -> c.baseLength = v, 0.1f, 5f))
                .option(floatOpt("base_width", 0.09765625f, () -> c.baseWidth, v -> c.baseWidth = v, 0.01f, 1.0f))
                .option(floatOpt("height_length_factor", 0.2f, () -> c.heightLengthFactor, v -> c.heightLengthFactor = v, 0f, 2f))
                .option(floatOpt("height_width_factor", 0.0f, () -> c.heightWidthFactor, v -> c.heightWidthFactor = v, 0f, 2f))
                .option(floatOpt("bbox_length_factor", 0.4f, () -> c.bboxLengthFactor, v -> c.bboxLengthFactor = v, 0f, 2f))
                .option(floatOpt("bbox_width_factor", 0.0f, () -> c.bboxWidthFactor, v -> c.bboxWidthFactor = v, 0f, 2f))
                .option(floatOpt("overlay_shrink_time", 0.5f, () -> c.overlayShrinkTime, v -> c.overlayShrinkTime = v, 0.01f, 2f))
                .option(floatOpt("overlay_fade_time", 0.2f, () -> c.overlayFadeTime, v -> c.overlayFadeTime = v, 0.01f, 2f))
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

    private static Option<Color> colorOpt(String key, Color def, java.util.function.Supplier<Color> g, java.util.function.Consumer<Color> s) {
        return Option.<Color>createBuilder().name(t(P(key))).description(OptionDescription.of(td(P(key))))
                .binding(def, g, s).controller(ColorControllerBuilder::create).build();
    }

    private static Option<PlayerMode> enumOpt(String key, PlayerMode def, java.util.function.Supplier<PlayerMode> g, java.util.function.Consumer<PlayerMode> s) {
        return Option.<PlayerMode>createBuilder().name(t(P(key))).description(OptionDescription.of(td(P(key))))
                .binding(def, g, s).controller(opt -> EnumControllerBuilder.create(opt).enumClass(PlayerMode.class)).build();
    }

    public static Color toAwt(String hex) {
        try { return new Color(Integer.parseInt(hex.replace("#","").trim(), 16)); }
        catch (Exception e) { return Color.WHITE; }
    }
    public static String fromAwt(Color c) { return String.format("#%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue()); }
}