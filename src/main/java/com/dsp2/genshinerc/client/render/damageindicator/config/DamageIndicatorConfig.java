package com.dsp2.genshinerc.client.render.damageindicator.config;

import com.dsp2.genshinerc.client.render.bar.config.BarConfig;
import com.dsp2.genshinerc.common.network.damageindicator.DamageIndicatorNetworkHandler;
import com.dsp2.genshinerc.common.network.damageindicator.ServerConfigUpdatePacket;
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

    public transient boolean serverEnabled = true;
    public transient int serverMaxDistance = 35;

    @SerialEntry public boolean drawShadow = false;
    @SerialEntry public String shadowColor = "#929292";
    @SerialEntry public double shadowAlpha = 0.55;
    @SerialEntry public double shadowOffsetX = 0.60;
    @SerialEntry public double shadowOffsetY = 0.80;
    @SerialEntry public double zOffset = -0.01;
    @SerialEntry public double particleMinSpacing = 0.30;
    @SerialEntry public int numberFormat = 1;
    @SerialEntry public int lifeTime = 16;
    @SerialEntry public double velocityMultiplier = 0.20;
    @SerialEntry public double gravityStrength = -0.15;
    @SerialEntry public boolean randomVelocityXZ = true;
    @SerialEntry public double randomVelocityXZAmount = 1.20;
    @SerialEntry public double offsetY = 0.00;
    @SerialEntry public double sizeYFactorK = 0.35;
    @SerialEntry public double heightYFactor = 0.15;
    @SerialEntry public double normalSize = 1.50;
    @SerialEntry public double critSize = 2.50;
    @SerialEntry public double killSize = 3.00;
    @SerialEntry public double birthSizeRandomPercent = 0.15;
    @SerialEntry public String absorbColor = "#FFEE00";
    @SerialEntry public String colorPhysical = "#FFFFFF";
    @SerialEntry public String colorPyro     = "#FF6600";
    @SerialEntry public String colorHydro    = "#00CCFF";
    @SerialEntry public String colorAnemo    = "#66FFCC";
    @SerialEntry public String colorElectro  = "#CC66FF";
    @SerialEntry public String colorDendro   = "#55A601";
    @SerialEntry public String colorCryo     = "#B4FFFE";
    @SerialEntry public String colorGeo      = "#EFC01B";
    @SerialEntry public String colorOverload = "#FD375A";
    @SerialEntry public boolean lifeShrinkEnabled = true;
    @SerialEntry public int lifeShrinkStartTick = 4;
    @SerialEntry public EasingWrapper lifeShrinkEasing = EasingWrapper.EASE_OUT;
    @SerialEntry public boolean lifeFadeEnabled = true;
    @SerialEntry public int lifeFadeStartTick = 6;
    @SerialEntry public EasingWrapper lifeFadeEasing = EasingWrapper.EASE_OUT;
    @SerialEntry public boolean birthScaleEnabled = true;
    @SerialEntry public int birthScaleDuration = 4;
    @SerialEntry public EasingWrapper birthScaleEasing = EasingWrapper.EASE_IN;
    @SerialEntry public boolean birthFadeEnabled = true;
    @SerialEntry public int birthFadeDuration = 4;
    @SerialEntry public EasingWrapper birthFadeEasing = EasingWrapper.EASE_IN;
    @SerialEntry public boolean killFlashEnabled = true;
    @SerialEntry public int killFlashInterval = 2;
    @SerialEntry public int killFlashTicks = 10;
    @SerialEntry public int killExtraTicks = 4;
    @SerialEntry public boolean debug = false;
    @SerialEntry public double zRandomMin = 0.00;
    @SerialEntry public double zRandomMax = 0.50;
    @SerialEntry public double zOffsetBurning = 0.50;

    public enum EasingWrapper {
        LINEAR, EASE_IN, EASE_OUT, EASE_IN_OUT;
        public static EasingWrapper fromString(String s) {
            if (s == null) return EASE_IN_OUT;
            try { return valueOf(s); } catch (Exception e) { return EASE_IN_OUT; }
        }
    }

    public enum NumberFormat {
        INTEGER, ONE_DECIMAL, TWO_DECIMALS;
        public static NumberFormat fromInt(int i) {
            return switch (i) { case 0 -> INTEGER; case 2 -> TWO_DECIMALS; default -> ONE_DECIMAL; };
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

    private static Component t(String k) { return Component.translatable(k); }
    private static Component td(String k) { return Component.translatable(k + ".desc"); }
    private static Component tsrv(String k) { return Component.translatable(k).append(" ").append(Component.translatable("genshinerc.damageindicator.label.server")); }
    private static String P(String k) { return "genshinerc.damageindicator.config." + k; }

    public static Screen createScreen(DamageIndicatorConfig c, boolean serverView, Screen escParent) {
        return serverView ? createServerScreen(c, escParent) : createClientScreen(c, escParent);
    }

    private static Screen createServerScreen(DamageIndicatorConfig c, Screen escParent) {
        Minecraft mc = Minecraft.getInstance();
        boolean isOp = mc.player != null && mc.player.hasPermissions(2);
        boolean lock = !isOp;

        OptionGroup group = OptionGroup.createBuilder()
                .name(t("genshinerc.damageindicator.group.server"))
                .description(OptionDescription.of(td("genshinerc.damageindicator.group.server")))
                .option(boolOpt(lock, "enabled", true, () -> c.serverEnabled, v -> c.serverEnabled = v))
                .option(intOpt(lock, "max_distance", 35, () -> c.serverMaxDistance, v -> c.serverMaxDistance = v, 1, 128))
                .build();

        return YetAnotherConfigLib.createBuilder()
                .title(t("genshinerc.damageindicator.title.server"))
                .category(ConfigCategory.createBuilder()
                        .name(t("genshinerc.damageindicator.category.display"))
                        .group(group).build())
                .save(() -> {
                    if (isOp) {
                        DamageIndicatorNetworkHandler.CHANNEL.sendToServer(
                                new ServerConfigUpdatePacket(c.serverEnabled, c.serverMaxDistance)
                        );
                    }
                })
                .build()
                .generateScreen(escParent);
    }

    private static Screen createClientScreen(DamageIndicatorConfig c, Screen escParent) {
        OptionGroup group = buildGroup();
        OptionGroup barGroup = BarConfig.createGroup();

        return YetAnotherConfigLib.createBuilder()
                .title(t("genshinerc.damageindicator.title"))
                .category(ConfigCategory.createBuilder()
                        .name(t("genshinerc.damageindicator.category.display"))
                        .group(group)
                        .group(barGroup)
                        .build())
                .save(() -> {
                    DamageIndicatorConfig.save();
                    BarConfig.save();
                })
                .build()
                .generateScreen(escParent);
    }

    private static OptionGroup buildGroup() {
        DamageIndicatorConfig c = get();
        return OptionGroup.createBuilder()
                .name(t("genshinerc.damageindicator.group.indicator"))
                .description(OptionDescription.of(td("genshinerc.damageindicator.group.indicator")))
                .option(boolOpt(false, "draw_shadow", false, () -> c.drawShadow, v -> c.drawShadow = v))
                .option(colorOpt(false, "shadow_color", new Color(0x929292), () -> toAwt(c.shadowColor), v -> c.shadowColor = fromAwt(v)))
                .option(dblOpt(false, "shadow_alpha", 0.55, () -> c.shadowAlpha, v -> c.shadowAlpha = v, 0.00, 1.00))
                .option(dblOpt(false, "shadow_offset_x", 0.60, () -> c.shadowOffsetX, v -> c.shadowOffsetX = v, -3.00, 3.00))
                .option(dblOpt(false, "shadow_offset_y", 0.80, () -> c.shadowOffsetY, v -> c.shadowOffsetY = v, -3.00, 3.00))
                .option(dblOpt(false, "z_offset", -0.01, () -> c.zOffset, v -> c.zOffset = v, -0.15, 0.15))
                .option(dblOpt(false, "particle_min_spacing", 0.30, () -> c.particleMinSpacing, v -> c.particleMinSpacing = v, 0.00, 2.00))
                .option(enumOpt(false, "number_format", NumberFormat.ONE_DECIMAL, () -> NumberFormat.fromInt(c.numberFormat), v -> c.numberFormat = v.ordinal()))
                .option(intOpt(false, "life_time", 16, () -> c.lifeTime, v -> c.lifeTime = v, 5, 60))
                .option(dblOpt(false, "velocity_multiplier", 0.20, () -> c.velocityMultiplier, v -> c.velocityMultiplier = v, 0.00, 1.00))
                .option(dblOpt(false, "gravity_strength", -0.15, () -> c.gravityStrength, v -> c.gravityStrength = v, -1.00, 1.00))
                .option(boolOpt(false, "random_velocity_xz", true, () -> c.randomVelocityXZ, v -> c.randomVelocityXZ = v))
                .option(dblOpt(false, "random_velocity_xz_amount", 1.20, () -> c.randomVelocityXZAmount, v -> c.randomVelocityXZAmount = v, 0.00, 2.00))
                .option(dblOpt(false, "offset_y", 0.00, () -> c.offsetY, v -> c.offsetY = v, -2.00, 2.00))
                .option(dblOpt(false, "size_y_factor_k", 0.35, () -> c.sizeYFactorK, v -> c.sizeYFactorK = v, 0.00, 1.00))
                .option(dblOpt(false, "height_y_factor", 0.15, () -> c.heightYFactor, v -> c.heightYFactor = v, 0.00, 2.00))
                .option(dblOpt(false, "normal_size", 1.50, () -> c.normalSize, v -> c.normalSize = v, 0.50, 3.00))
                .option(dblOpt(false, "crit_size", 2.50, () -> c.critSize, v -> c.critSize = v, 0.50, 5.00))
                .option(dblOpt(false, "kill_size", 3.00, () -> c.killSize, v -> c.killSize = v, 0.50, 5.00))
                .option(dblOpt(false, "birth_size_random_percent", 0.15, () -> c.birthSizeRandomPercent, v -> c.birthSizeRandomPercent = v, 0.00, 0.45))
                .option(colorOpt(false, "absorb_color", new Color(0xFFEE00), () -> toAwt(c.absorbColor), v -> c.absorbColor = fromAwt(v)))
                .option(colorOpt(false, "color_physical", new Color(0xFFFFFF), () -> toAwt(c.colorPhysical), v -> c.colorPhysical = fromAwt(v)))
                .option(colorOpt(false, "color_pyro", new Color(0xFF6600), () -> toAwt(c.colorPyro), v -> c.colorPyro = fromAwt(v)))
                .option(colorOpt(false, "color_hydro", new Color(0x00CCFF), () -> toAwt(c.colorHydro), v -> c.colorHydro = fromAwt(v)))
                .option(colorOpt(false, "color_anemo", new Color(0x66FFCC), () -> toAwt(c.colorAnemo), v -> c.colorAnemo = fromAwt(v)))
                .option(colorOpt(false, "color_electro", new Color(0xCC66FF), () -> toAwt(c.colorElectro), v -> c.colorElectro = fromAwt(v)))
                .option(colorOpt(false, "color_dendro", new Color(0x55A601), () -> toAwt(c.colorDendro), v -> c.colorDendro = fromAwt(v)))
                .option(colorOpt(false, "color_cryo", new Color(0xB4FFFE), () -> toAwt(c.colorCryo), v -> c.colorCryo = fromAwt(v)))
                .option(colorOpt(false, "color_geo", new Color(0xEFC01B), () -> toAwt(c.colorGeo), v -> c.colorGeo = fromAwt(v)))
                .option(colorOpt(false, "color_overload", new Color(0xFD375A), () -> toAwt(c.colorOverload), v -> c.colorOverload = fromAwt(v)))
                .option(boolOpt(false, "life_shrink_enabled", true, () -> c.lifeShrinkEnabled, v -> c.lifeShrinkEnabled = v))
                .option(intOpt(false, "life_shrink_start_tick", 4, () -> c.lifeShrinkStartTick, v -> c.lifeShrinkStartTick = v, 1, 30))
                .option(easingOpt(false, "life_shrink_easing", EasingWrapper.EASE_OUT, () -> c.lifeShrinkEasing, v -> c.lifeShrinkEasing = v))
                .option(boolOpt(false, "life_fade_enabled", true, () -> c.lifeFadeEnabled, v -> c.lifeFadeEnabled = v))
                .option(intOpt(false, "life_fade_start_tick", 6, () -> c.lifeFadeStartTick, v -> c.lifeFadeStartTick = v, 1, 30))
                .option(easingOpt(false, "life_fade_easing", EasingWrapper.EASE_OUT, () -> c.lifeFadeEasing, v -> c.lifeFadeEasing = v))
                .option(boolOpt(false, "birth_scale_enabled", true, () -> c.birthScaleEnabled, v -> c.birthScaleEnabled = v))
                .option(intOpt(false, "birth_scale_duration", 4, () -> c.birthScaleDuration, v -> c.birthScaleDuration = v, 1, 20))
                .option(easingOpt(false, "birth_scale_easing", EasingWrapper.EASE_IN, () -> c.birthScaleEasing, v -> c.birthScaleEasing = v))
                .option(boolOpt(false, "birth_fade_enabled", true, () -> c.birthFadeEnabled, v -> c.birthFadeEnabled = v))
                .option(intOpt(false, "birth_fade_duration", 4, () -> c.birthFadeDuration, v -> c.birthFadeDuration = v, 1, 20))
                .option(easingOpt(false, "birth_fade_easing", EasingWrapper.EASE_IN, () -> c.birthFadeEasing, v -> c.birthFadeEasing = v))
                .option(boolOpt(false, "kill_flash_enabled", true, () -> c.killFlashEnabled, v -> c.killFlashEnabled = v))
                .option(intOpt(false, "kill_flash_interval", 2, () -> c.killFlashInterval, v -> c.killFlashInterval = v, 1, 10))
                .option(intOpt(false, "kill_flash_ticks", 10, () -> c.killFlashTicks, v -> c.killFlashTicks = v, 5, 30))
                .option(intOpt(false, "kill_extra_ticks", 4, () -> c.killExtraTicks, v -> c.killExtraTicks = v, 0, 20))
                .option(boolOpt(false, "debug", false, () -> c.debug, v -> c.debug = v))
                .option(dblOpt(false, "z_random_min", 0.00, () -> c.zRandomMin, v -> c.zRandomMin = v, -1.00, 2.00))
                .option(dblOpt(false, "z_random_max", 0.50, () -> c.zRandomMax, v -> c.zRandomMax = v, -1.00, 2.00))
                .option(dblOpt(false, "z_offset_burning", 0.50, () -> c.zOffsetBurning, v -> c.zOffsetBurning = v, 0.00, 1.00))
                .build();
    }

    private static Option<Boolean> boolOpt(boolean lock, String key, boolean def, java.util.function.Supplier<Boolean> g, java.util.function.Consumer<Boolean> s) {
        Option.Builder<Boolean> b = Option.<Boolean>createBuilder().name(lock ? tsrv(P(key)) : t(P(key)))
                .description(OptionDescription.of(td(P(key)))).binding(def, g, s).controller(TickBoxControllerBuilder::create);
        if (lock) b.available(false);
        return b.build();
    }
    private static Option<Integer> intOpt(boolean lock, String key, int def, java.util.function.Supplier<Integer> g, java.util.function.Consumer<Integer> s, int min, int max) {
        Option.Builder<Integer> b = Option.<Integer>createBuilder().name(lock ? tsrv(P(key)) : t(P(key)))
                .description(OptionDescription.of(td(P(key)))).binding(def, g, s)
                .controller(opt -> IntegerSliderControllerBuilder.create(opt).range(min, max).step(1));
        if (lock) b.available(false);
        return b.build();
    }
    private static Option<Double> dblOpt(boolean lock, String key, double def, java.util.function.Supplier<Double> g, java.util.function.Consumer<Double> s, double min, double max) {
        Option.Builder<Double> b = Option.<Double>createBuilder().name(lock ? tsrv(P(key)) : t(P(key)))
                .description(OptionDescription.of(td(P(key)))).binding(def, g, s)
                .controller(opt -> DoubleSliderControllerBuilder.create(opt).range(min, max).step(0.01));
        if (lock) b.available(false);
        return b.build();
    }
    private static Option<Color> colorOpt(boolean lock, String key, Color def, java.util.function.Supplier<Color> g, java.util.function.Consumer<Color> s) {
        Option.Builder<Color> b = Option.<Color>createBuilder().name(lock ? tsrv(P(key)) : t(P(key)))
                .description(OptionDescription.of(td(P(key)))).binding(def, g, s).controller(ColorControllerBuilder::create);
        if (lock) b.available(false);
        return b.build();
    }
    private static Option<EasingWrapper> easingOpt(boolean lock, String key, EasingWrapper def, java.util.function.Supplier<EasingWrapper> g, java.util.function.Consumer<EasingWrapper> s) {
        Option.Builder<EasingWrapper> b = Option.<EasingWrapper>createBuilder().name(lock ? tsrv(P(key)) : t(P(key)))
                .description(OptionDescription.of(td(P(key)))).binding(def, g, s)
                .controller(opt -> EnumControllerBuilder.create(opt).enumClass(EasingWrapper.class)
                        .formatValue(e -> Component.translatable("genshinerc.damageindicator.easing." + e.name().toLowerCase())));
        if (lock) b.available(false);
        return b.build();
    }
    private static Option<NumberFormat> enumOpt(boolean lock, String key, NumberFormat def, java.util.function.Supplier<NumberFormat> g, java.util.function.Consumer<NumberFormat> s) {
        Option.Builder<NumberFormat> b = Option.<NumberFormat>createBuilder().name(lock ? tsrv(P(key)) : t(P(key)))
                .description(OptionDescription.of(td(P(key)))).binding(def, g, s)
                .controller(opt -> EnumControllerBuilder.create(opt).enumClass(NumberFormat.class)
                        .formatValue(e -> Component.translatable("genshinerc.damageindicator.number_format." + e.name().toLowerCase())));
        if (lock) b.available(false);
        return b.build();
    }

    static Color toAwt(String hex) {
        try { return new Color(Integer.parseInt(hex.replace("#","").trim(), 16)); }
        catch (Exception e) { return Color.WHITE; }
    }
    static String fromAwt(Color c) { return String.format("#%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue()); }
}