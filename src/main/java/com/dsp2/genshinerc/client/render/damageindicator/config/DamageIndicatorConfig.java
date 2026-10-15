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
import java.util.Set;

public class DamageIndicatorConfig {

    @SerialEntry public boolean enabled = true;
    @SerialEntry public boolean drawShadow = true;

    @SerialEntry public double maxDistance = 35.00;
    @SerialEntry public float lifeTime = 16.00f;
    @SerialEntry public float velocityMultiplier = 0.50f;
    @SerialEntry public float gravityStrength = 0.25f;
    @SerialEntry public boolean randomVelocityXZ = true;
    @SerialEntry public float randomVelocityXZAmount = 0.50f;
    @SerialEntry public float velocityY = 0.25f;
    @SerialEntry public float offsetY = 0.25f;
    @SerialEntry public float normalSize = 1.50f;
    @SerialEntry public float critSize = 2.50f;
    @SerialEntry public float killSize = 3.00f;
    @SerialEntry public String absorbColor = "#FFEE00";
    @SerialEntry public boolean lifeShrinkEnabled = true;
    @SerialEntry public int lifeShrinkThreshold = 15;
    @SerialEntry public int lifeShrinkDuration = 5;
    @SerialEntry public boolean lifeFadeEnabled = true;
    @SerialEntry public int lifeFadeDuration = 3;
    @SerialEntry public boolean killFlashEnabled = true;
    @SerialEntry public int killFlashTicks = 10;
    @SerialEntry public int killScaleStartTick = 0;
    @SerialEntry public int killScaleEndTick = 15;
    @SerialEntry public boolean debug = false;

    public static final Set<String> SERVER_FIELDS = Set.of(
            "maxDistance","lifeTime","velocityMultiplier","gravityStrength",
            "randomVelocityXZ","randomVelocityXZAmount","velocityY","offsetY",
            "normalSize","critSize","killSize","absorbColor",
            "lifeShrinkEnabled","lifeShrinkThreshold","lifeShrinkDuration",
            "lifeFadeEnabled","lifeFadeDuration",
            "killFlashEnabled","killFlashTicks","killScaleStartTick","killScaleEndTick"
    );

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

    public static Screen createScreen() {
        DamageIndicatorConfig c = get();
        boolean lockServer = isMultiplayerClient();

        OptionGroup general = OptionGroup.createBuilder()
                .name(t("genshinerc.damageindicator.group.damage"))
                .description(OptionDescription.of(td("genshinerc.damageindicator.group.damage")))
                .option(Option.<Boolean>createBuilder()
                        .name(t("genshinerc.damageindicator.config.enabled"))
                        .description(OptionDescription.of(td("genshinerc.damageindicator.config.enabled")))
                        .binding(true, () -> c.enabled, v -> c.enabled = v)
                        .controller(TickBoxControllerBuilder::create).build())
                .option(Option.<Boolean>createBuilder()
                        .name(t("genshinerc.damageindicator.config.draw_shadow"))
                        .description(OptionDescription.of(td("genshinerc.damageindicator.config.draw_shadow")))
                        .binding(true, () -> c.drawShadow, v -> c.drawShadow = v)
                        .controller(TickBoxControllerBuilder::create).build())
                .option(serverDouble(lockServer, "max_distance", 35.00, () -> c.maxDistance, v -> c.maxDistance = v, 17.50, 52.50))
                .option(serverFloat(lockServer, "life_time", 16.00f, () -> c.lifeTime, v -> c.lifeTime = v, 8.00f, 24.00f))
                .option(serverFloat(lockServer, "velocity_multiplier", 0.50f, () -> c.velocityMultiplier, v -> c.velocityMultiplier = v, 0.25f, 0.75f))
                .option(serverFloat(lockServer, "gravity_strength", 0.25f, () -> c.gravityStrength, v -> c.gravityStrength = v, 0.00f, 0.38f))
                .option(serverBool(lockServer, "random_velocity_xz", true, () -> c.randomVelocityXZ, v -> c.randomVelocityXZ = v))
                .option(serverFloat(lockServer, "random_velocity_xz_amount", 0.50f, () -> c.randomVelocityXZAmount, v -> c.randomVelocityXZAmount = v, 0.00f, 0.75f))
                .option(serverFloat(lockServer, "velocity_y", 0.25f, () -> c.velocityY, v -> c.velocityY = v, 0.13f, 0.38f))
                .option(serverFloat(lockServer, "offset_y", 0.25f, () -> c.offsetY, v -> c.offsetY = v, 0.00f, 0.38f))
                .option(serverFloat(lockServer, "normal_size", 1.50f, () -> c.normalSize, v -> c.normalSize = v, 0.75f, 2.25f))
                .option(serverFloat(lockServer, "crit_size", 2.50f, () -> c.critSize, v -> c.critSize = v, 1.25f, 3.75f))
                .option(serverFloat(lockServer, "kill_size", 3.00f, () -> c.killSize, v -> c.killSize = v, 1.50f, 4.50f))
                .option(serverColor(lockServer, "absorb_color", new Color(0xFFEE00), () -> HexUtil.toAwt(c.absorbColor), v -> c.absorbColor = HexUtil.fromAwt(v)))
                .option(serverBool(lockServer, "debug", false, () -> c.debug, v -> c.debug = v))
                .build();

        OptionGroup life = OptionGroup.createBuilder()
                .name(t("genshinerc.damageindicator.group.life_anim"))
                .description(OptionDescription.of(td("genshinerc.damageindicator.group.life_anim")))
                .option(serverBool(lockServer, "life_shrink_enabled", true, () -> c.lifeShrinkEnabled, v -> c.lifeShrinkEnabled = v))
                .option(serverInt(lockServer, "life_shrink_threshold", 15, () -> c.lifeShrinkThreshold, v -> c.lifeShrinkThreshold = v, 8, 23))
                .option(serverInt(lockServer, "life_shrink_duration", 5, () -> c.lifeShrinkDuration, v -> c.lifeShrinkDuration = v, 3, 8))
                .option(serverBool(lockServer, "life_fade_enabled", true, () -> c.lifeFadeEnabled, v -> c.lifeFadeEnabled = v))
                .option(serverInt(lockServer, "life_fade_duration", 3, () -> c.lifeFadeDuration, v -> c.lifeFadeDuration = v, 2, 5))
                .build();

        OptionGroup kill = OptionGroup.createBuilder()
                .name(t("genshinerc.damageindicator.group.kill_anim"))
                .description(OptionDescription.of(td("genshinerc.damageindicator.group.kill_anim")))
                .option(serverBool(lockServer, "kill_flash_enabled", true, () -> c.killFlashEnabled, v -> c.killFlashEnabled = v))
                .option(serverInt(lockServer, "kill_flash_ticks", 10, () -> c.killFlashTicks, v -> c.killFlashTicks = v, 5, 15))
                .option(serverInt(lockServer, "kill_scale_start_tick", 0, () -> c.killScaleStartTick, v -> c.killScaleStartTick = v, 0, 10))
                .option(serverInt(lockServer, "kill_scale_end_tick", 15, () -> c.killScaleEndTick, v -> c.killScaleEndTick = v, 8, 23))
                .build();

        return YetAnotherConfigLib.createBuilder()
                .title(t("genshinerc.damageindicator.title"))
                .category(ConfigCategory.createBuilder()
                        .name(t("genshinerc.damageindicator.category.display"))
                        .group(general).group(life).group(kill).build())
                .save(DamageIndicatorConfig::save)
                .build()
                .generateScreen(null);
    }

    private static Option<Double> serverDouble(boolean lock, String key, double def, java.util.function.Supplier<Double> get, java.util.function.Consumer<Double> set, double min, double max) {
        Option.Builder<Double> b = Option.<Double>createBuilder()
                .name(lock ? tsrv("genshinerc.damageindicator.config." + key) : t("genshinerc.damageindicator.config." + key))
                .description(OptionDescription.of(td("genshinerc.damageindicator.config." + key)))
                .binding(def, get, set)
                .controller(opt -> DoubleSliderControllerBuilder.create(opt).range(min, max).step(0.01));
        if (lock) b.available(false);
        return b.build();
    }
    private static Option<Float> serverFloat(boolean lock, String key, float def, java.util.function.Supplier<Float> get, java.util.function.Consumer<Float> set, float min, float max) {
        Option.Builder<Float> b = Option.<Float>createBuilder()
                .name(lock ? tsrv("genshinerc.damageindicator.config." + key) : t("genshinerc.damageindicator.config." + key))
                .description(OptionDescription.of(td("genshinerc.damageindicator.config." + key)))
                .binding(def, get, set)
                .controller(opt -> FloatSliderControllerBuilder.create(opt).range(min, max).step(0.01f));
        if (lock) b.available(false);
        return b.build();
    }
    private static Option<Integer> serverInt(boolean lock, String key, int def, java.util.function.Supplier<Integer> get, java.util.function.Consumer<Integer> set, int min, int max) {
        Option.Builder<Integer> b = Option.<Integer>createBuilder()
                .name(lock ? tsrv("genshinerc.damageindicator.config." + key) : t("genshinerc.damageindicator.config." + key))
                .description(OptionDescription.of(td("genshinerc.damageindicator.config." + key)))
                .binding(def, get, set)
                .controller(opt -> IntegerSliderControllerBuilder.create(opt).range(min, max).step(1));
        if (lock) b.available(false);
        return b.build();
    }
    private static Option<Boolean> serverBool(boolean lock, String key, boolean def, java.util.function.Supplier<Boolean> get, java.util.function.Consumer<Boolean> set) {
        Option.Builder<Boolean> b = Option.<Boolean>createBuilder()
                .name(lock ? tsrv("genshinerc.damageindicator.config." + key) : t("genshinerc.damageindicator.config." + key))
                .description(OptionDescription.of(td("genshinerc.damageindicator.config." + key)))
                .binding(def, get, set)
                .controller(TickBoxControllerBuilder::create);
        if (lock) b.available(false);
        return b.build();
    }
    private static Option<Color> serverColor(boolean lock, String key, Color def, java.util.function.Supplier<Color> get, java.util.function.Consumer<Color> set) {
        Option.Builder<Color> b = Option.<Color>createBuilder()
                .name(lock ? tsrv("genshinerc.damageindicator.config." + key) : t("genshinerc.damageindicator.config." + key))
                .description(OptionDescription.of(td("genshinerc.damageindicator.config." + key)))
                .binding(def, get, set)
                .controller(ColorControllerBuilder::create);
        if (lock) b.available(false);
        return b.build();
    }

    static class HexUtil {
        static Color toAwt(String hex) {
            try { return new Color(Integer.parseInt(hex.replace("#", "").trim(), 16)); }
            catch (Exception e) { return Color.WHITE; }
        }
        static String fromAwt(Color c) { return String.format("#%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue()); }
    }
}