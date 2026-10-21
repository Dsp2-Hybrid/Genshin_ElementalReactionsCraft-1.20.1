package com.dsp2.genshinerc.client.render.bar.config;

import dev.isxander.yacl3.api.ButtonOption;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.ListOption;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.ColorControllerBuilder;
import dev.isxander.yacl3.api.controller.EnumControllerBuilder;
import dev.isxander.yacl3.api.controller.FloatSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import dev.isxander.yacl3.gui.YACLScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.ForgeRegistries;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BarConfig {
    public static final ConfigClassHandler<BarConfig> HANDLER =
            ConfigClassHandler.createBuilder(BarConfig.class)
                    .id(ResourceLocation.fromNamespaceAndPath("genshinerc", "healthbar"))
                    .serializer(cfg -> GsonConfigSerializerBuilder.create(cfg)
                            .setPath(FMLPaths.CONFIGDIR.get().resolve("genshinerc-healthbar.json5"))
                            .build())
                    .build();

    public static YACLScreen PENDING_LIST_SCREEN = null;
    public static Screen PENDING_LIST_PARENT = null;
    public static ListOption<String> PENDING_LIST_OPTION = null;

    public static BarConfig get() { return HANDLER.instance(); }
    public static void load() { HANDLER.load(); }
    public static void save() { HANDLER.save(); }

    public enum PlayerMode { NONE, ON_CHANGE, ALWAYS }
    public enum MobMode { NONE, ALWAYS, ALWAYS_WHEN_NOT_FULLHP, ON_HURT }
    public enum EasingType { EASE_IN_CUBIC, EASE_OUT_CUBIC, EASE_IN_OUT_CUBIC, LINEAR, SINE }
    public enum BuffDirection { RIGHT_TO_LEFT, LEFT_TO_RIGHT }

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

    @SerialEntry public float playerBarLengthBonus = 0f;
    @SerialEntry public float playerBarWidthBonus = 0f;
    @SerialEntry public float playerBarYOffsetBonus = 0f;
    @SerialEntry public float playerBarOpacityMultiplier = 1f;

    @SerialEntry public float entityBarLengthBonus = 0f;
    @SerialEntry public float entityBarWidthBonus = 0f;
    @SerialEntry public float entityBarYOffsetBonus = 0f;
    @SerialEntry public float entityBarOpacityMultiplier = 1f;

    @SerialEntry public String enemyColor = "#FF4444";
    @SerialEntry public String allyColor = "#44FF44";
    @SerialEntry public float hudColorMix = 1.0f;

    @SerialEntry public float playerOverlayShrinkTime = 1.35f;
    @SerialEntry public float mobOverlayShrinkTime = 1.25f;
    @SerialEntry public EasingType whiteEasing = EasingType.EASE_OUT_CUBIC;
    @SerialEntry public float overlayFadeTime = 0.30f;

    @SerialEntry public boolean smallChangeEnabled = true;
    @SerialEntry public float smallChangeThreshold = 0.03f;
    @SerialEntry public float smallChangeDuration = 0.75f;
    @SerialEntry public float smallChangeFadeIn = 0.15f;
    @SerialEntry public float smallChangeFadeOut = 0.35f;
    @SerialEntry public boolean smallChangeUseShaderMode = true;
    @SerialEntry public float smallChangeShaderDuration = 0.20f;

    @SerialEntry public float shakeDuration = 0.35f;
    @SerialEntry public float shakeAmplitudeFactor = 1.75f;
    @SerialEntry public float shakeFrequencyFactor = 8.0f;
    @SerialEntry public float shakeDecayFactor = 0.50f;

    @SerialEntry public float fatalShakeMultiplier = 2.75f;
    @SerialEntry public float fatalShakeMinPercent = 0.60f;
    @SerialEntry public float fatalShakeMaxPercent = 1.00f;

    @SerialEntry public float hurtDisplayTime = 3.0f;
    @SerialEntry public float deathFadeDelay = 3.0f;
    @SerialEntry public float fadeInTime = 0.25f;
    @SerialEntry public float fadeOutTime = 0.4f;
    @SerialEntry public float deathFadeOutTime = 0.45f;

    @SerialEntry public BuffDirection buffDirection = BuffDirection.RIGHT_TO_LEFT;
    @SerialEntry public int hudOffsetX = 10;
    @SerialEntry public int hudOffsetY = 10;
    @SerialEntry public int buffSpacing = 22;
    @SerialEntry public int maxBuffs = 5;
    @SerialEntry public boolean buffRenderBackground = true;
    @SerialEntry public float buffBackgroundAlpha = 0.5f;
    @SerialEntry public float buffOpacity = 1.0f;
    @SerialEntry public int buffBlinkRate = 20;

    @SerialEntry public int levelOffsetX = -20;
    @SerialEntry public int levelOffsetY = -30;
    @SerialEntry public float levelOffsetZ = 0f;
    @SerialEntry public float levelScale = 0.025f;
    @SerialEntry public float levelShadowOffsetX = 0.01f;
    @SerialEntry public float levelShadowOffsetY = 0.01f;
    @SerialEntry public float levelShadowOffsetZ = 0.01f;
    @SerialEntry public String levelShadowColor = "#000000";
    @SerialEntry public float levelShadowAlpha = 0.5f;
    @SerialEntry public String levelColorWhite = "#FFFFFF";
    @SerialEntry public String levelColorGreen = "#55FF55";
    @SerialEntry public String levelColorYellow = "#FFFF55";
    @SerialEntry public String levelColorOrange = "#FFAA00";
    @SerialEntry public String levelColorRed = "#FF5555";
    @SerialEntry public String levelColorDarkRed = "#AA0000";
    @SerialEntry public float buffOffsetZ = 0.0f;
    @SerialEntry public float elementOffsetZ = 0.0f;

    @SerialEntry public int elementBoxSize = 20;
    @SerialEntry public int elementSpacing = 2;
    @SerialEntry public int elementOffsetY = -50;
    @SerialEntry public int elementOffsetX = 0;
    @SerialEntry public float elementScale = 1.0f;

    @SerialEntry public List<String> entityYOffsets = new ArrayList<>(List.of("minecraft:witch 0.60"));

    public float getYOffsetForEntity(String entityKey) {
        if (entityYOffsets != null && !entityYOffsets.isEmpty()) {
            for (String pair : entityYOffsets) {
                String[] parts = pair.trim().split("\\s+");
                if (parts.length >= 2 && parts[0].equals(entityKey)) {
                    try {
                        float val = Float.parseFloat(parts[1]);
                        return Mth.clamp(val, -10.0f, 10.0f);
                    } catch (NumberFormatException ignored) {}
                }
            }
        }
        return 0f;
    }

    public static boolean hasInvalidEntityYOffset(List<String> list) {
        if (list == null || list.isEmpty()) return false;
        for (String pair : list) {
            if (pair == null || pair.trim().isEmpty()) continue;
            String[] parts = pair.trim().split("\\s+");
            if (parts.length != 2) return true;
            String keyStr = parts[0];
            ResourceLocation rl = ResourceLocation.tryParse(keyStr);
            if (rl == null) return true;
            if (!ForgeRegistries.ENTITY_TYPES.containsKey(rl)) return true;
            String numStr = parts[1];
            if (numStr.contains("e") || numStr.contains("E")) return true;
            try {
                float val = Float.parseFloat(numStr);
                if (val < -10.0f || val > 10.0f) return true;
            } catch (NumberFormatException e) {
                return true;
            }
        }
        return false;
    }

    public static void registerDefaultYOffsets(Map<String, Float> defaults) {
        BarConfig c = get();
        if (c.entityYOffsets == null) c.entityYOffsets = new ArrayList<>();
        boolean changed = false;
        for (Map.Entry<String, Float> entry : defaults.entrySet()) {
            boolean exists = false;
            for (String s : c.entityYOffsets) {
                if (s.trim().startsWith(entry.getKey() + " ")) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                c.entityYOffsets.add(entry.getKey() + " " + entry.getValue());
                changed = true;
            }
        }
        if (changed) save();
    }

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
            case SINE: return (float)(1.0f - Math.cos((x * Math.PI) / 2.0f));
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
                .option(floatOpt("player_bar_length_bonus", 0f, () -> c.playerBarLengthBonus, v -> c.playerBarLengthBonus = v, -1f, 1f))
                .option(floatOpt("player_bar_width_bonus", 0f, () -> c.playerBarWidthBonus, v -> c.playerBarWidthBonus = v, -0.25f, 0.25f))
                .option(floatOpt("player_bar_y_offset_bonus", 0f, () -> c.playerBarYOffsetBonus, v -> c.playerBarYOffsetBonus = v, -1.5f, 1.5f))
                .option(floatOpt("player_bar_opacity_multiplier", 1f, () -> c.playerBarOpacityMultiplier, v -> c.playerBarOpacityMultiplier = v, 0f, 1f))
                .option(floatOpt("entity_bar_length_bonus", 0f, () -> c.entityBarLengthBonus, v -> c.entityBarLengthBonus = v, -1f, 1f))
                .option(floatOpt("entity_bar_width_bonus", 0f, () -> c.entityBarWidthBonus, v -> c.entityBarWidthBonus = v, -0.25f, 0.25f))
                .option(floatOpt("entity_bar_y_offset_bonus", 0f, () -> c.entityBarYOffsetBonus, v -> c.entityBarYOffsetBonus = v, -1.5f, 1.5f))
                .option(floatOpt("entity_bar_opacity_multiplier", 1f, () -> c.entityBarOpacityMultiplier, v -> c.entityBarOpacityMultiplier = v, 0f, 1f))
                .option(colorOpt("enemy_color", new Color(0xFF4444), () -> toAwt(c.enemyColor), v -> c.enemyColor = fromAwt(v)))
                .option(colorOpt("ally_color", new Color(0x44FF44), () -> toAwt(c.allyColor), v -> c.allyColor = fromAwt(v)))
                .option(percentOpt("hud_color_mix", 1.0f, () -> c.hudColorMix, v -> c.hudColorMix = v, 0.0f, 1.0f))
                .option(floatOpt("player_overlay_shrink_time", 1.35f, () -> c.playerOverlayShrinkTime, v -> c.playerOverlayShrinkTime = v, 0f, 6f))
                .option(floatOpt("mob_overlay_shrink_time", 1.25f, () -> c.mobOverlayShrinkTime, v -> c.mobOverlayShrinkTime = v, 0f, 3f))
                .option(enumOpt("white_easing", EasingType.class, EasingType.EASE_OUT_CUBIC, () -> c.whiteEasing, v -> c.whiteEasing = v))
                .option(floatOpt("overlay_fade_time", 0.30f, () -> c.overlayFadeTime, v -> c.overlayFadeTime = v, 0.0f, 1.0f))
                .option(boolOpt("small_change_enabled", true, () -> c.smallChangeEnabled, v -> c.smallChangeEnabled = v))
                .option(percentOpt("small_change_threshold", 0.03f, () -> c.smallChangeThreshold, v -> c.smallChangeThreshold = v, 0.0f, 0.15f))
                .option(floatOpt("small_change_duration", 0.75f, () -> c.smallChangeDuration, v -> c.smallChangeDuration = v, 0.05f, 1f))
                .option(floatOpt("small_change_fade_in", 0.15f, () -> c.smallChangeFadeIn, v -> c.smallChangeFadeIn = v, 0.0f, 1.0f))
                .option(floatOpt("small_change_fade_out", 0.35f, () -> c.smallChangeFadeOut, v -> c.smallChangeFadeOut = v, 0.0f, 1.0f))
                .option(boolOpt("small_change_use_shader_mode", true, () -> c.smallChangeUseShaderMode, v -> c.smallChangeUseShaderMode = v))
                .option(floatOpt("small_change_shader_duration", 0.20f, () -> c.smallChangeShaderDuration, v -> c.smallChangeShaderDuration = v, 0.0f, 1.0f))
                .option(floatOpt("shake_duration", 0.35f, () -> c.shakeDuration, v -> c.shakeDuration = v, 0.0f, 1.0f))
                .option(floatOpt("shake_amplitude_factor", 1.75f, () -> c.shakeAmplitudeFactor, v -> c.shakeAmplitudeFactor = v, 0f, 3f))
                .option(floatOpt("shake_frequency_factor", 8.0f, () -> c.shakeFrequencyFactor, v -> c.shakeFrequencyFactor = v, 1f, 16f))
                .option(floatOpt("shake_decay_factor", 0.50f, () -> c.shakeDecayFactor, v -> c.shakeDecayFactor = v, 0.05f, 1.5f))
                .option(floatOpt("fatal_shake_multiplier", 2.75f, () -> c.fatalShakeMultiplier, v -> c.fatalShakeMultiplier = v, 1f, 5f))
                .option(percentOpt("fatal_shake_min_percent", 0.60f, () -> c.fatalShakeMinPercent, v -> c.fatalShakeMinPercent = v, 0.35f, 0.70f))
                .option(percentOpt("fatal_shake_max_percent", 1.00f, () -> c.fatalShakeMaxPercent, v -> c.fatalShakeMaxPercent = v, 0.75f, 1.65f))
                .option(floatOpt("hurt_display_time", 3.0f, () -> c.hurtDisplayTime, v -> c.hurtDisplayTime = v, 0.1f, 10f))
                .option(floatOpt("fade_in_time", 0.25f, () -> c.fadeInTime, v -> c.fadeInTime = v, 0f, 1f))
                .option(floatOpt("fade_out_time", 0.4f, () -> c.fadeOutTime, v -> c.fadeOutTime = v, 0f, 2f))
                .option(floatOpt("death_fade_out_time", 0.45f, () -> c.deathFadeOutTime, v -> c.deathFadeOutTime = v, 0.10f, 2.00f))

                .option(enumOpt("buff_direction", BuffDirection.class, BuffDirection.RIGHT_TO_LEFT, () -> c.buffDirection, v -> c.buffDirection = v))
                .option(intOpt("hud_offset_x", 10, () -> c.hudOffsetX, v -> c.hudOffsetX = v, 0, 100))
                .option(intOpt("hud_offset_y", 10, () -> c.hudOffsetY, v -> c.hudOffsetY = v, 0, 100))
                .option(intOpt("buff_spacing", 22, () -> c.buffSpacing, v -> c.buffSpacing = v, 10, 50))
                .option(intOpt("max_buffs", 5, () -> c.maxBuffs, v -> c.maxBuffs = v, 1, 20))
                .option(boolOpt("buff_render_background", true, () -> c.buffRenderBackground, v -> c.buffRenderBackground = v))
                .option(percentOpt("buff_background_alpha", 0.5f, () -> c.buffBackgroundAlpha, v -> c.buffBackgroundAlpha = v, 0.0f, 1.0f))
                .option(percentOpt("buff_opacity", 1.0f, () -> c.buffOpacity, v -> c.buffOpacity = v, 0.0f, 1.0f))
                .option(intOpt("buff_blink_rate", 20, () -> c.buffBlinkRate, v -> c.buffBlinkRate = v, 10, 60))

                .option(intOpt("level_offset_x", -20, () -> c.levelOffsetX, v -> c.levelOffsetX = v, -100, 100))
                .option(intOpt("level_offset_y", -30, () -> c.levelOffsetY, v -> c.levelOffsetY = v, -100, 100))
                .option(floatOpt("level_offset_z", 0f, () -> c.levelOffsetZ, v -> c.levelOffsetZ = v, -1f, 1f))
                .option(floatOpt("level_scale", 0.025f, () -> c.levelScale, v -> c.levelScale = v, 0.01f, 0.1f))
                .option(floatOpt("level_shadow_offset_x", 0.01f, () -> c.levelShadowOffsetX, v -> c.levelShadowOffsetX = v, -0.05f, 0.05f))
                .option(floatOpt("level_shadow_offset_y", 0.01f, () -> c.levelShadowOffsetY, v -> c.levelShadowOffsetY = v, -0.05f, 0.05f))
                .option(floatOpt("level_shadow_offset_z", 0.01f, () -> c.levelShadowOffsetZ, v -> c.levelShadowOffsetZ = v, -0.05f, 0.05f))
                .option(colorOpt("level_shadow_color", new Color(0x000000), () -> toAwt(c.levelShadowColor), v -> c.levelShadowColor = fromAwt(v)))
                .option(percentOpt("level_shadow_alpha", 0.5f, () -> c.levelShadowAlpha, v -> c.levelShadowAlpha = v, 0f, 1f))

                .option(colorOpt("level_color_white", new Color(0xFFFFFF), () -> toAwt(c.levelColorWhite), v -> c.levelColorWhite = fromAwt(v)))
                .option(colorOpt("level_color_green", new Color(0x55FF55), () -> toAwt(c.levelColorGreen), v -> c.levelColorGreen = fromAwt(v)))
                .option(colorOpt("level_color_yellow", new Color(0xFFFF55), () -> toAwt(c.levelColorYellow), v -> c.levelColorYellow = fromAwt(v)))
                .option(colorOpt("level_color_orange", new Color(0xFFAA00), () -> toAwt(c.levelColorOrange), v -> c.levelColorOrange = fromAwt(v)))
                .option(colorOpt("level_color_red", new Color(0xFF5555), () -> toAwt(c.levelColorRed), v -> c.levelColorRed = fromAwt(v)))
                .option(colorOpt("level_color_dark_red", new Color(0xAA0000), () -> toAwt(c.levelColorDarkRed), v -> c.levelColorDarkRed = fromAwt(v)))

                .option(intOpt("element_box_size", 20, () -> c.elementBoxSize, v -> c.elementBoxSize = v, 10, 50))
                .option(intOpt("element_spacing", 2, () -> c.elementSpacing, v -> c.elementSpacing = v, 0, 20))
                .option(intOpt("element_offset_y", -50, () -> c.elementOffsetY, v -> c.elementOffsetY = v, -100, 100))
                .option(intOpt("element_offset_x", 0, () -> c.elementOffsetX, v -> c.elementOffsetX = v, -100, 100))
                .option(floatOpt("element_scale", 1.0f, () -> c.elementScale, v -> c.elementScale = v, 0.1f, 3.0f))
                .option(floatOpt("buff_offset_z", 0.0f, () -> c.buffOffsetZ, v -> c.buffOffsetZ = v, -1f, 1f))
                .option(floatOpt("element_offset_z", 0.0f, () -> c.elementOffsetZ, v -> c.elementOffsetZ = v, -1f, 1f))

                .option(ButtonOption.createBuilder()
                        .name(t(P("entity_y_offsets")))
                        .description(OptionDescription.of(td(P("entity_y_offsets"))))
                        .action((screen, option) -> Minecraft.getInstance().setScreen(createOffsetScreen(screen)))
                        .build())
                .build();
    }

    public static Screen createOffsetScreen(Screen parent) {
        BarConfig c = get();
        ListOption<String> listOption = ListOption.<String>createBuilder()
                .name(t(P("entity_y_offsets")))
                .description(OptionDescription.of(td(P("entity_y_offsets"))))
                .binding(List.of("minecraft:witch 0.60"), () -> c.entityYOffsets, v -> c.entityYOffsets = new ArrayList<>(v))
                .controller(StringControllerBuilder::create)
                .initial("minecraft:creeper 0.5")
                .build();

        YACLScreen screen = (YACLScreen) YetAnotherConfigLib.createBuilder()
                .title(t(P("entity_y_offsets")))
                .category(ConfigCategory.createBuilder()
                        .name(t(P("entity_y_offsets")))
                        .option(listOption)
                        .build())
                .save(() -> {
                    if (!hasInvalidEntityYOffset(listOption.pendingValue())) BarConfig.save();
                })
                .build()
                .generateScreen(parent);

        PENDING_LIST_SCREEN = screen;
        PENDING_LIST_PARENT = parent;
        PENDING_LIST_OPTION = listOption;
        return screen;
    }

    private static Option<Boolean> boolOpt(String key, boolean def, java.util.function.Supplier<Boolean> g, java.util.function.Consumer<Boolean> s) {
        return Option.<Boolean>createBuilder().name(t(P(key))).description(OptionDescription.of(td(P(key)))).binding(def, g, s).controller(TickBoxControllerBuilder::create).build();
    }
    private static Option<Float> floatOpt(String key, float def, java.util.function.Supplier<Float> g, java.util.function.Consumer<Float> s, float min, float max) {
        return Option.<Float>createBuilder().name(t(P(key))).description(OptionDescription.of(td(P(key)))).binding(def, g, s).controller(opt -> FloatSliderControllerBuilder.create(opt).range(min, max).step(0.01f).formatValue(v -> Component.literal(String.format("%.2f", v)))).build();
    }
    private static Option<Float> percentOpt(String key, float def, java.util.function.Supplier<Float> g, java.util.function.Consumer<Float> s, float min, float max) {
        return Option.<Float>createBuilder().name(t(P(key))).description(OptionDescription.of(td(P(key)))).binding(def, g, s).controller(opt -> FloatSliderControllerBuilder.create(opt).range(min, max).step(0.01f).formatValue(v -> Component.literal(String.format("%.0f%%", v * 100f)))).build();
    }
    private static Option<Integer> intOpt(String key, int def, java.util.function.Supplier<Integer> g, java.util.function.Consumer<Integer> s, int min, int max) {
        return Option.<Integer>createBuilder().name(t(P(key))).description(OptionDescription.of(td(P(key)))).binding(def, g, s).controller(opt -> IntegerSliderControllerBuilder.create(opt).range(min, max).step(1)).build();
    }
    private static Option<Color> colorOpt(String key, Color def, java.util.function.Supplier<Color> g, java.util.function.Consumer<Color> s) {
        return Option.<Color>createBuilder().name(t(P(key))).description(OptionDescription.of(td(P(key)))).binding(def, g, s).controller(ColorControllerBuilder::create).build();
    }
    private static <T extends Enum<T>> Option<T> enumOpt(String key, Class<T> enumClass, T def, java.util.function.Supplier<T> g, java.util.function.Consumer<T> s) {
        return Option.<T>createBuilder().name(t(P(key))).description(OptionDescription.of(td(P(key)))).binding(def, g, s).controller(opt -> EnumControllerBuilder.create(opt).enumClass(enumClass)).build();
    }

    public static Color toAwt(String hex) {
        try { return new Color(Integer.parseInt(hex.replace("#","").trim(), 16)); } catch (Exception e) { return Color.WHITE; }
    }
    public static String fromAwt(Color c) { return String.format("#%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue()); }
}