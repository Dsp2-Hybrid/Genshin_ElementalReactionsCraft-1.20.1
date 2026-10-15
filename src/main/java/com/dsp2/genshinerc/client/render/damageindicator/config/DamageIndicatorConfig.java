package com.dsp2.genshinerc.client.render.damageindicator.config;

import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.*;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.loading.FMLPaths;

import java.awt.Color;

public class DamageIndicatorConfig {

    @SerialEntry public boolean enabled = true;
    @SerialEntry public boolean drawShadow = true;
    @SerialEntry public String shadowColor = "#000000";

    @SerialEntry public int maxDistance = 35;
    @SerialEntry public int lifeTime = 16;
    @SerialEntry public double velocityMultiplier = 0.30;
    @SerialEntry public double gravityStrength = -0.20;
    @SerialEntry public boolean randomVelocityXZ = true;
    @SerialEntry public double randomVelocityXZAmount = 0.80;
    @SerialEntry public double offsetY = 0.30;

    @SerialEntry public double normalSize = 1.50;
    @SerialEntry public double critSize = 2.50;
    @SerialEntry public double killSize = 3.00;
    @SerialEntry public String absorbColor = "#FFEE00";

    @SerialEntry public String colorPhysical = "#FFFFFF";
    @SerialEntry public String colorPyro     = "#FF6600";
    @SerialEntry public String colorHydro    = "#00CCFF";
    @SerialEntry public String colorAnemo    = "#66FFCC";
    @SerialEntry public String colorElectro  = "#CC66FF";
    @SerialEntry public String colorDendro   = "#66CC33";
    @SerialEntry public String colorCryo     = "#99FFFF";
    @SerialEntry public String colorGeo      = "#FFCC33";
    @SerialEntry public String colorOverload = "#FF3333";

    @SerialEntry public boolean lifeShrinkEnabled = true;
    @SerialEntry public int lifeShrinkStartTick = 5;
    @SerialEntry public EasingWrapper lifeShrinkEasing = EasingWrapper.EASE_IN_OUT;

    @SerialEntry public boolean lifeFadeEnabled = true;
    @SerialEntry public int lifeFadeStartTick = 3;
    @SerialEntry public EasingWrapper lifeFadeEasing = EasingWrapper.EASE_IN_OUT;

    @SerialEntry public boolean killFlashEnabled = true;
    @SerialEntry public int killFlashInterval = 2;
    @SerialEntry public int killFlashTicks = 12;

    @SerialEntry public boolean debug = false;

    public enum EasingWrapper {
        LINEAR, EASE_IN, EASE_OUT, EASE_IN_OUT;
        public static EasingWrapper fromString(String s) {
            if (s == null) return EASE_IN_OUT;
            try { return valueOf(s); } catch (Exception e) { return EASE_IN_OUT; }
        }
    }

    public String colorFor(String typeKey) {
        return switch (typeKey) {
            case "pyro" -> colorPyro;
            case "hydro" -> colorHydro;
            case "anemo" -> colorAnemo;
            case "electro" -> colorElectro;
            case "dendro" -> colorDendro;
            case "cryo" -> colorCryo;
            case "geo" -> colorGeo;
            case "overload" -> colorOverload;
            default -> colorPhysical;
        };
    }

    public static final ConfigClassHandler<DamageIndicatorConfig> HANDLER =
            ConfigClassHandler.createBuilder(DamageIndicatorConfig.class)
                    .id(ResourceLocation.fromNamespaceAndPath("genshinerc", "damageindicator"))
                    .serializer(cfg -> GsonConfigSerializerBuilder.create(cfg)
                            .setPath(FMLPaths.CONFIGDIR.get().resolve("genshinerc-damageindicator.json5"))
                            .build())
                    .build();

    public static DamageIndicatorConfig get() { return HANDLER.instance(); }
    public static void load() { HANDLER.load(); }
    public static void save() { HANDLER.save(); }

    public static boolean isMultiplayerClient() {
        Minecraft mc = Minecraft.getInstance();
        return mc != null && mc.level != null && !mc.hasSingleplayerServer();
    }

    private static Component t(String k) { return Component.translatable(k); }
    private static Component td(String k) { return Component.translatable(k + ".desc"); }
    private static Component tsrv(String k) { return Component.translatable(k).append(" ").append(Component.translatable("genshinerc.damageindicator.label.server")); }
    private static String P(String k) { return "genshinerc.damageindicator.config." + k; }

    public static Screen createScreen() {
        DamageIndicatorConfig c = get();
        boolean lock = isMultiplayerClient();

        OptionGroup group = OptionGroup.createBuilder()
                .name(t("genshinerc.damageindicator.group.indicator"))
                .description(OptionDescription.of(td("genshinerc.damageindicator.group.indicator")))

                .option(boolOpt(lock, "enabled", true, () -> c.enabled, v -> c.enabled = v))
                .option(boolOpt(lock, "draw_shadow", true, () -> c.drawShadow, v -> c.drawShadow = v))
                .option(colorOpt(lock, "shadow_color", new Color(0), () -> toAwt(c.shadowColor), v -> c.shadowColor = fromAwt(v)))
                .option(intOpt(lock, "max_distance", 35, () -> c.maxDistance, v -> c.maxDistance = v, 1, 128))
                .option(intOpt(lock, "life_time", 16, () -> c.lifeTime, v -> c.lifeTime = v, 5, 60))
                .option(dblOpt(lock, "velocity_multiplier", 0.30, () -> c.velocityMultiplier, v -> c.velocityMultiplier = v, 0.00, 1.00))
                .option(dblOpt(lock, "gravity_strength", -0.20, () -> c.gravityStrength, v -> c.gravityStrength = v, -1.00, 1.00))
                .option(boolOpt(lock, "random_velocity_xz", true, () -> c.randomVelocityXZ, v -> c.randomVelocityXZ = v))
                .option(dblOpt(lock, "random_velocity_xz_amount", 0.80, () -> c.randomVelocityXZAmount, v -> c.randomVelocityXZAmount = v, 0.00, 2.00))
                .option(dblOpt(lock, "offset_y", 0.30, () -> c.offsetY, v -> c.offsetY = v, 0.00, 1.00))
                .option(dblOpt(lock, "normal_size", 1.50, () -> c.normalSize, v -> c.normalSize = v, 0.50, 3.00))
                .option(dblOpt(lock, "crit_size", 2.50, () -> c.critSize, v -> c.critSize = v, 0.50, 5.00))
                .option(dblOpt(lock, "kill_size", 3.00, () -> c.killSize, v -> c.killSize = v, 0.50, 5.00))
                .option(colorOpt(lock, "absorb_color", new Color(0xFFEE00), () -> toAwt(c.absorbColor), v -> c.absorbColor = fromAwt(v)))

                .option(colorOpt(lock, "color_physical", new Color(0xFFFFFF), () -> toAwt(c.colorPhysical), v -> c.colorPhysical = fromAwt(v)))
                .option(colorOpt(lock, "color_pyro", new Color(0xFF6600), () -> toAwt(c.colorPyro), v -> c.colorPyro = fromAwt(v)))
                .option(colorOpt(lock, "color_hydro", new Color(0x00CCFF), () -> toAwt(c.colorHydro), v -> c.colorHydro = fromAwt(v)))
                .option(colorOpt(lock, "color_anemo", new Color(0x66FFCC), () -> toAwt(c.colorAnemo), v -> c.colorAnemo = fromAwt(v)))
                .option(colorOpt(lock, "color_electro", new Color(0xCC66FF), () -> toAwt(c.colorElectro), v -> c.colorElectro = fromAwt(v)))
                .option(colorOpt(lock, "color_dendro", new Color(0x66CC33), () -> toAwt(c.colorDendro), v -> c.colorDendro = fromAwt(v)))
                .option(colorOpt(lock, "color_cryo", new Color(0x99FFFF), () -> toAwt(c.colorCryo), v -> c.colorCryo = fromAwt(v)))
                .option(colorOpt(lock, "color_geo", new Color(0xFFCC33), () -> toAwt(c.colorGeo), v -> c.colorGeo = fromAwt(v)))
                .option(colorOpt(lock, "color_overload", new Color(0xFF3333), () -> toAwt(c.colorOverload), v -> c.colorOverload = fromAwt(v)))

                .option(boolOpt(lock, "life_shrink_enabled", true, () -> c.lifeShrinkEnabled, v -> c.lifeShrinkEnabled = v))
                .option(intOpt(lock, "life_shrink_start_tick", 5, () -> c.lifeShrinkStartTick, v -> c.lifeShrinkStartTick = v, 1, 30))
                .option(easingOpt(lock, "life_shrink_easing", EasingWrapper.EASE_IN_OUT, () -> c.lifeShrinkEasing, v -> c.lifeShrinkEasing = v))

                .option(boolOpt(lock, "life_fade_enabled", true, () -> c.lifeFadeEnabled, v -> c.lifeFadeEnabled = v))
                .option(intOpt(lock, "life_fade_start_tick", 3, () -> c.lifeFadeStartTick, v -> c.lifeFadeStartTick = v, 1, 30))
                .option(easingOpt(lock, "life_fade_easing", EasingWrapper.EASE_IN_OUT, () -> c.lifeFadeEasing, v -> c.lifeFadeEasing = v))

                .option(boolOpt(lock, "kill_flash_enabled", true, () -> c.killFlashEnabled, v -> c.killFlashEnabled = v))
                .option(intOpt(lock, "kill_flash_interval", 2, () -> c.killFlashInterval, v -> c.killFlashInterval = v, 1, 10))
                .option(intOpt(lock, "kill_flash_ticks", 12, () -> c.killFlashTicks, v -> c.killFlashTicks = v, 5, 30))

                .option(boolOpt(lock, "debug", false, () -> c.debug, v -> c.debug = v))
                .build();

        return YetAnotherConfigLib.createBuilder()
                .title(t("genshinerc.damageindicator.title"))
                .category(ConfigCategory.createBuilder()
                        .name(t("genshinerc.damageindicator.category.display"))
                        .group(group)
                        .build())
                .save(DamageIndicatorConfig::save)
                .build()
                .generateScreen(null);
    }

    private static Option<Boolean> boolOpt(boolean lock, String key, boolean def, java.util.function.Supplier<Boolean> g, java.util.function.Consumer<Boolean> s) {
        Option.Builder<Boolean> b = Option.<Boolean>createBuilder()
                .name(lock ? tsrv(P(key)) : t(P(key)))
                .description(OptionDescription.of(td(P(key))))
                .binding(def, g, s).controller(TickBoxControllerBuilder::create);
        if (lock) b.available(false);
        return b.build();
    }
    private static Option<Integer> intOpt(boolean lock, String key, int def, java.util.function.Supplier<Integer> g, java.util.function.Consumer<Integer> s, int min, int max) {
        Option.Builder<Integer> b = Option.<Integer>createBuilder()
                .name(lock ? tsrv(P(key)) : t(P(key)))
                .description(OptionDescription.of(td(P(key))))
                .binding(def, g, s)
                .controller(opt -> IntegerSliderControllerBuilder.create(opt).range(min, max).step(1));
        if (lock) b.available(false);
        return b.build();
    }
    private static Option<Double> dblOpt(boolean lock, String key, double def, java.util.function.Supplier<Double> g, java.util.function.Consumer<Double> s, double min, double max) {
        Option.Builder<Double> b = Option.<Double>createBuilder()
                .name(lock ? tsrv(P(key)) : t(P(key)))
                .description(OptionDescription.of(td(P(key))))
                .binding(def, g, s)
                .controller(opt -> DoubleSliderControllerBuilder.create(opt).range(min, max).step(0.01));
        if (lock) b.available(false);
        return b.build();
    }
    private static Option<Color> colorOpt(boolean lock, String key, Color def, java.util.function.Supplier<Color> g, java.util.function.Consumer<Color> s) {
        Option.Builder<Color> b = Option.<Color>createBuilder()
                .name(lock ? tsrv(P(key)) : t(P(key)))
                .description(OptionDescription.of(td(P(key))))
                .binding(def, g, s).controller(ColorControllerBuilder::create);
        if (lock) b.available(false);
        return b.build();
    }
    private static Option<EasingWrapper> easingOpt(boolean lock, String key, EasingWrapper def, java.util.function.Supplier<EasingWrapper> g, java.util.function.Consumer<EasingWrapper> s) {
        Option.Builder<EasingWrapper> b = Option.<EasingWrapper>createBuilder()
                .name(lock ? tsrv(P(key)) : t(P(key)))
                .description(OptionDescription.of(td(P(key))))
                .binding(def, g, s)
                .controller(opt -> EnumControllerBuilder.create(opt).enumClass(EasingWrapper.class)
                        .formatValue(e -> Component.translatable("genshinerc.damageindicator.easing." + e.name().toLowerCase())));
        if (lock) b.available(false);
        return b.build();
    }

    static Color toAwt(String hex) {
        try { return new Color(Integer.parseInt(hex.replace("#","").trim(), 16)); }
        catch (Exception e) { return Color.WHITE; }
    }
    static String fromAwt(Color c) { return String.format("#%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue()); }
}