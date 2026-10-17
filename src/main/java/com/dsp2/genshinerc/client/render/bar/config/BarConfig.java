package com.dsp2.genshinerc.client.render.bar.config;

import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.ColorControllerBuilder;
import dev.isxander.yacl3.api.controller.EnumControllerBuilder;
import dev.isxander.yacl3.api.controller.FloatSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import net.minecraft.client.gui.screens.Screen;
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

    public enum PlayerMode { NONE, ON_HURT, ALWAYS }

    @SerialEntry public boolean enabled = true;
    @SerialEntry public PlayerMode playerMode = PlayerMode.ON_HURT;
    @SerialEntry public boolean showFullHealthMobs = false;
    @SerialEntry public float yOffset = 0.25f;
    @SerialEntry public float baseSize = 1.0f;
    @SerialEntry public float maxDistance = 64.0f;
    @SerialEntry public float heightScaleFactor = 0.1f;
    @SerialEntry public float bboxSizeFactor = 0.2f;
    @SerialEntry public String enemyColor = "#FF4444";
    @SerialEntry public String allyColor = "#44FF44";

    @SerialEntry public int uvSize = 64;
    @SerialEntry public int bgV0 = 3;
    @SerialEntry public int bgV1 = 19;
    @SerialEntry public int fillV0 = 27;
    @SerialEntry public int fillV1 = 43;

    private static Component t(String k) { return Component.translatable(k); }
    private static Component td(String k) { return Component.translatable(k + ".desc"); }
    private static String P(String k) { return "genshinerc.bar.config." + k; }

    public static OptionGroup createGroup() {
        BarConfig c = get();
        return OptionGroup.createBuilder()
                .name(t("genshinerc.bar.group.title"))
                .description(OptionDescription.of(td("genshinerc.bar.group")))
                .option(boolOpt("enabled", true, () -> c.enabled, v -> c.enabled = v))
                .option(enumOpt("player_mode", PlayerMode.ON_HURT, () -> c.playerMode, v -> c.playerMode = v))
                .option(boolOpt("show_full_health_mobs", false, () -> c.showFullHealthMobs, v -> c.showFullHealthMobs = v))
                .option(floatOpt("max_distance", 64f, () -> c.maxDistance, v -> c.maxDistance = v, 4f, 128f))
                .option(floatOpt("base_size", 1.0f, () -> c.baseSize, v -> c.baseSize = v, 0.1f, 3f))
                .option(floatOpt("y_offset", 0.25f, () -> c.yOffset, v -> c.yOffset = v, -1f, 3f))
                .option(floatOpt("height_scale_factor", 0.1f, () -> c.heightScaleFactor, v -> c.heightScaleFactor = v, 0f, 2f))
                .option(floatOpt("bbox_size_factor", 0.2f, () -> c.bboxSizeFactor, v -> c.bboxSizeFactor = v, 0f, 2f))
                .option(colorOpt("enemy_color", new Color(0xFF4444), () -> toAwt(c.enemyColor), v -> c.enemyColor = fromAwt(v)))
                .option(colorOpt("ally_color", new Color(0x44FF44), () -> toAwt(c.allyColor), v -> c.allyColor = fromAwt(v)))
                .option(intOpt("uv_size", 64, () -> c.uvSize, v -> c.uvSize = v, 1, 1024))
                .option(intOpt("bg_v0", 3, () -> c.bgV0, v -> c.bgV0 = v, 0, 1024))
                .option(intOpt("bg_v1", 19, () -> c.bgV1, v -> c.bgV1 = v, 0, 1024))
                .option(intOpt("fill_v0", 27, () -> c.fillV0, v -> c.fillV0 = v, 0, 1024))
                .option(intOpt("fill_v1", 43, () -> c.fillV1, v -> c.fillV1 = v, 0, 1024))
                .build();
    }

    private static Option<Boolean> boolOpt(String key, boolean def, java.util.function.Supplier<Boolean> g, java.util.function.Consumer<Boolean> s) {
        return Option.<Boolean>createBuilder().name(t(P(key))).description(OptionDescription.of(td(P(key))))
                .binding(def, g, s).controller(TickBoxControllerBuilder::create).build();
    }
    private static Option<Float> floatOpt(String key, float def, java.util.function.Supplier<Float> g, java.util.function.Consumer<Float> s, float min, float max) {
        return Option.<Float>createBuilder().name(t(P(key))).description(OptionDescription.of(td(P(key))))
                .binding(def, g, s).controller(opt -> FloatSliderControllerBuilder.create(opt).range(min, max).step(0.01f)).build();
    }
    private static Option<Integer> intOpt(String key, int def, java.util.function.Supplier<Integer> g, java.util.function.Consumer<Integer> s, int min, int max) {
        return Option.<Integer>createBuilder().name(t(P(key))).description(OptionDescription.of(td(P(key))))
                .binding(def, g, s).controller(opt -> IntegerSliderControllerBuilder.create(opt).range(min, max).step(1)).build();
    }
    private static Option<Color> colorOpt(String key, Color def, java.util.function.Supplier<Color> g, java.util.function.Consumer<Color> s) {
        return Option.<Color>createBuilder().name(t(P(key))).description(OptionDescription.of(td(P(key))))
                .binding(def, g, s).controller(ColorControllerBuilder::create).build();
    }
    private static Option<PlayerMode> enumOpt(String key, PlayerMode def, java.util.function.Supplier<PlayerMode> g, java.util.function.Consumer<PlayerMode> s) {
        return Option.<PlayerMode>createBuilder().name(t(P(key))).description(OptionDescription.of(td(P(key))))
                .binding(def, g, s).controller(opt -> EnumControllerBuilder.create(opt).enumClass(PlayerMode.class)).build();
    }
    static Color toAwt(String hex) {
        try { return new Color(Integer.parseInt(hex.replace("#","").trim(), 16)); }
        catch (Exception e) { return Color.WHITE; }
    }
    static String fromAwt(Color c) { return String.format("#%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue()); }
}