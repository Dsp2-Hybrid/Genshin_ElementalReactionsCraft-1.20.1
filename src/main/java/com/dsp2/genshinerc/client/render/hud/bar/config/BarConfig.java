package com.dsp2.genshinerc.client.render.hud.bar.config;

import com.dsp2.genshinerc.client.render.hud.bar.config.yaclfixi.YaclScrollMemory;
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
import java.util.LinkedHashMap;
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
    public enum ZhenMieBlendMode { NORMAL, MULTIPLY, SCREEN, ADD, OVERLAY }

    @SerialEntry public boolean enabled = true;
    @SerialEntry public PlayerMode playerMode = PlayerMode.ON_CHANGE;
    @SerialEntry public MobMode mobMode = MobMode.ALWAYS_WHEN_NOT_FULLHP;
    @SerialEntry public int maxDistance = 32;
    @SerialEntry public float yOffset = 0.25f;
    @SerialEntry public float yOffsetHeightFactor = 0.05f;

    @SerialEntry public float baseLength = 1.10f;
    @SerialEntry public float baseWidth = 0.03f;
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
    @SerialEntry public float hudColorMix = 0.90f;

    @SerialEntry public float playerOverlayShrinkTime = 1.35f;
    @SerialEntry public float mobOverlayShrinkTime = 1.25f;
    @SerialEntry public EasingType whiteEasing = EasingType.EASE_OUT_CUBIC;
    @SerialEntry public float overlayFadeTime = 0.30f;

    @SerialEntry public boolean smallChangeEnabled = true;
    @SerialEntry public float smallChangeThreshold = 0.03f;
    @SerialEntry public float smallChangeDuration = 0.75f;
    @SerialEntry public float smallChangeFadeOut = 0.35f;
    @SerialEntry public boolean smallChangeUseShaderMode = true;
    @SerialEntry public float smallChangeShaderDuration = 0.20f;

    @SerialEntry public float shakeDuration = 0.35f;
    @SerialEntry public float shakeAmplitudeFactor = 1.75f;
    @SerialEntry public float shakeFrequencyFactor = 5.5f;
    @SerialEntry public float shakeDecayFactor = 0.50f;

    @SerialEntry public float fatalShakeMultiplier = 2.75f;
    @SerialEntry public float fatalShakeMinPercent = 0.60f;
    @SerialEntry public float fatalShakeMaxPercent = 1.00f;

    @SerialEntry public float hurtDisplayTime = 3.0f;
    @SerialEntry public float fadeInTime = 0.25f;
    @SerialEntry public float fadeOutTime = 0.4f;
    @SerialEntry public float deathFadeOutTime = 0.45f;

    @SerialEntry public BuffDirection buffDirection = BuffDirection.RIGHT_TO_LEFT;
    @SerialEntry public int hudBuffOffsetX = -4;
    @SerialEntry public int hudBuffOffsetY = 2;
    @SerialEntry public float buffSpacing = 37.0f;
    @SerialEntry public int maxBuffs = 8;
    @SerialEntry public boolean buffRenderBackground = true;
    @SerialEntry public float buffBackgroundAlpha = 0.50f;
    @SerialEntry public float buffOpacity = 0.70f;
    @SerialEntry public int buffBlinkRate = 10;
    @SerialEntry public float buffSingleScale = 0.50f;
    @SerialEntry public float buffListScale = 0.75f;

    @SerialEntry public int levelOffsetX = 0;
    @SerialEntry public int levelOffsetY = 44;
    @SerialEntry public float levelOffsetZ = 0f;
    @SerialEntry public float levelScale = 0.020f;

    @SerialEntry public boolean levelShadowEnabled = false;
    @SerialEntry public float levelShadowBrightness = 0.5f;
    @SerialEntry public float levelShadowOffsetX = 0.01f;
    @SerialEntry public float levelShadowOffsetY = 0.01f;
    @SerialEntry public float levelShadowOffsetZ = 0.0f;
    @SerialEntry public float levelShadowAlpha = 0.5f;

    @SerialEntry public String levelColorWhite = "#FFFFFF";
    @SerialEntry public String levelColorGreen = "#55FF55";
    @SerialEntry public String levelColorYellow = "#FFFF55";
    @SerialEntry public String levelColorOrange = "#FFAA00";
    @SerialEntry public String levelColorRed = "#FF5555";
    @SerialEntry public String levelColorDarkRed = "#AA0000";
    @SerialEntry public float buffOffsetZ = 0.0f;
    @SerialEntry public float elementOffsetZ = 0.0f;

    @SerialEntry public float elementBoxSize = 30.0f;
    @SerialEntry public float elementSpacing = 0.0f;
    @SerialEntry public int elementOffsetY = 30;
    @SerialEntry public int elementOffsetX = 4;
    @SerialEntry public float elementScale = 1.50f;

    @SerialEntry public float elementMinOpacity = 0.60f;
    @SerialEntry public float elementGaugeWidth = 80.0f;
    @SerialEntry public float elementGaugeHeight = 8.0f;
    @SerialEntry public float elementGaugeGap = -10.0f;
    @SerialEntry public float elementGaugeBgBrightness = 0.35f;
    @SerialEntry public float elementGaugeBgAlpha = 0.80f;
    @SerialEntry public float elementGaugeAnimTime = 0.30f;
    @SerialEntry public float elementLowGaugeThreshold = 0.50f;
    @SerialEntry public float elementFlickerMinAlpha = 0.25f;
    @SerialEntry public float elementFlickerMinHz = 1.5f;
    @SerialEntry public float elementFlickerMaxHz = 5.5f;
    @SerialEntry public float elementFlashInTime = 0.08f;
    @SerialEntry public float elementFlashHoldTime = 0.06f;
    @SerialEntry public float elementFlashOutTime = 0.35f;
    @SerialEntry public float elementGhostFadeTime = 0.40f;
    @SerialEntry public float elementSlotSlideSpeed = 12.0f;

    @SerialEntry public float subBarWidth = 95f;
    @SerialEntry public float shieldBarHeight = 45f;
    @SerialEntry public float zhenMieBarHeight = 65f;
    @SerialEntry public float subBarGap = -27f;
    @SerialEntry public float subBarFillSpeed = 12f;
    @SerialEntry public float subBarWhiteHoldTime = 0.35f;
    @SerialEntry public float subBarWhiteDrainSpeed = 6f;
    @SerialEntry public float subBarFlashTime = 0.25f;
    @SerialEntry public float shieldBgBrightness = 0.30f;
    @SerialEntry public float shieldBgAlpha = 0.85f;
    @SerialEntry public float zhenMieBuffCycleHz = 0.3f;
    @SerialEntry public Color shieldSpecialColor = new Color(0xE6E6E6);
    @SerialEntry public Color zhenColor = new Color(0x79C9FF);
    @SerialEntry public Color mieColor = new Color(0xFFAFAF);
    @SerialEntry public Color zhenBuffColor = new Color(0x0045FF);
    @SerialEntry public Color mieBuffColor = new Color(0xFF00CE);
    @SerialEntry public float shieldLayer2WhiteMix = 0.55f;
    @SerialEntry public float shieldLayer3WhiteMix = 0.80f;
    @SerialEntry public float zhenMieClashHueHz = 6.25f;
    @SerialEntry public ZhenMieBlendMode zhenMieBlendMode = ZhenMieBlendMode.NORMAL;
    @SerialEntry public float zhenMieBlendStrength = 1.0f;
    @SerialEntry public float zhenMieBgOpacity = 0.5f;
    @SerialEntry public float zhenMieBreathHz = 0.4f;
    @SerialEntry public Color zhenMieBreathColor = new Color(0xBA0012);

    @SerialEntry public boolean rpgHudEnabled = true;
    @SerialEntry public boolean rpgHideEfHud = true;
    @SerialEntry public float rpgHudScale = 1.0f;
    @SerialEntry public float rpgHudOffsetX = 6f;
    @SerialEntry public float rpgHudOffsetY = 6f;
    @SerialEntry public float rpgModelYaw = -10f;
    @SerialEntry public float rpgModelZoom = 1.00f;
    @SerialEntry public float rpgDiamondBgAlpha = 0.7f;
    @SerialEntry public boolean rpgModelStencil = true;
    @SerialEntry public float rpgModelPivot = 1.00f;
    @SerialEntry public float rpgCamX = 0f;
    @SerialEntry public float rpgCamY = 7.5f;
    @SerialEntry public float rpgCamZ = 0f;
    @SerialEntry public float rpgCamYaw = 0f;
    @SerialEntry public float rpgCamPitch = -8f;
    @SerialEntry public float rpgCamRoll = 0f;
    @SerialEntry public float rpgTextPopScale = 0.12f;
    @SerialEntry public float rpgTextPopTime = 0.22f;
    @SerialEntry public float rpgOxygenRetractTime = 0.30f;
    @SerialEntry public float rpgOxygenSettleTime = 0.35f;
    @SerialEntry public float rpgOxygenGainIdleTime = 0.50f;
    @SerialEntry public float rpgShakePixels = 12.0f;
    @SerialEntry public float rpgCountSpeed = 12.0f;
    @SerialEntry public float rpgFoodPreviewHz = 1.2f;
    @SerialEntry public Color rpgExpColor = new Color(0x5BE35B);
    @SerialEntry public float rpgPortraitX = -8.0f, rpgPortraitY = -87.0f, rpgPortraitW = 81.0f, rpgPortraitH = 77.0f;
    @SerialEntry public float rpgLevelTextX = 1.0f, rpgLevelTextY = -11.0f, rpgLevelTextW = 58.0f, rpgLevelTextH = 15.0f;
    @SerialEntry public float rpgHpX = 59.0f, rpgHpY = -36.0f, rpgHpW = 138.0f, rpgHpH = 10.0f;
    @SerialEntry public float rpgStaminaX = 57.0f, rpgStaminaY = -27.0f, rpgStaminaW = 141.0f, rpgStaminaH = 4.0f;
    @SerialEntry public float rpgShieldX = 68.0f, rpgShieldY = -38.0f, rpgShieldW = 122.0f, rpgShieldH = 2.0f;
    @SerialEntry public boolean rpgPortraitFlipX = false, rpgPortraitFlipY = false;
    @SerialEntry public boolean rpgLevelTextFlipX = false, rpgLevelTextFlipY = false;
    @SerialEntry public boolean rpgHpFlipX = false, rpgHpFlipY = false;
    @SerialEntry public boolean rpgStaminaFlipX = false, rpgStaminaFlipY = false;
    @SerialEntry public boolean rpgShieldFlipX = false, rpgShieldFlipY = false;
    @SerialEntry public int rpgFrameSlice = 0;
    @SerialEntry public float rpgFrameCornerPx = 0f;
    @SerialEntry public float rpgOrbHz = 1.2f;
    @SerialEntry public float rpgOrbBrightness = 0.6f;
    @SerialEntry public float rpgOrbMinAlpha = 0.3f;
    @SerialEntry public float rpgHpTextScale = 0.90f;
    @SerialEntry public float rpgFoodTextScale = 1.50f;
    @SerialEntry public float rpgHpTextOffsetY = 0.50f;
    @SerialEntry public Map<String, float[]> rpgLayout = new LinkedHashMap<>();

    public Map<String, float[]> layout() {
        if (rpgLayout == null) rpgLayout = new LinkedHashMap<>();
        return rpgLayout;
    }

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
                .option(floatOpt("base_width", 0.03f, () -> c.baseWidth, v -> c.baseWidth = v, 0.01f, 0.08f))
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
                .option(percentOpt("hud_color_mix", 0.90f, () -> c.hudColorMix, v -> c.hudColorMix = v, 0.0f, 1.0f))
                .option(floatOpt("player_overlay_shrink_time", 1.35f, () -> c.playerOverlayShrinkTime, v -> c.playerOverlayShrinkTime = v, 0f, 6f))
                .option(floatOpt("mob_overlay_shrink_time", 1.25f, () -> c.mobOverlayShrinkTime, v -> c.mobOverlayShrinkTime = v, 0f, 3f))
                .option(enumOpt("white_easing", EasingType.class, EasingType.EASE_OUT_CUBIC, () -> c.whiteEasing, v -> c.whiteEasing = v))
                .option(floatOpt("overlay_fade_time", 0.30f, () -> c.overlayFadeTime, v -> c.overlayFadeTime = v, 0.0f, 1.0f))
                .option(boolOpt("small_change_enabled", true, () -> c.smallChangeEnabled, v -> c.smallChangeEnabled = v))
                .option(percentOpt("small_change_threshold", 0.03f, () -> c.smallChangeThreshold, v -> c.smallChangeThreshold = v, 0.0f, 0.15f))
                .option(floatOpt("small_change_duration", 0.75f, () -> c.smallChangeDuration, v -> c.smallChangeDuration = v, 0.05f, 1f))
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
                .option(intOpt("hud_offset_x", -4, () -> c.hudBuffOffsetX, v -> c.hudBuffOffsetX = v, -100, 100))
                .option(intOpt("hud_offset_y", 2, () -> c.hudBuffOffsetY, v -> c.hudBuffOffsetY = v, -100, 100))
                .option(floatOpt("buff_single_scale", 0.50f, () -> c.buffSingleScale, v -> c.buffSingleScale = v, 0.25f, 3.00f))
                .option(floatOpt("buff_list_scale", 0.75f, () -> c.buffListScale, v -> c.buffListScale = v, 0.25f, 3.00f))
                .option(floatOptCustomStep("buff_spacing", 37.0f, () -> c.buffSpacing, v -> c.buffSpacing = v, 10.0f, 80.0f, 0.1f, "%.1f"))
                .option(intOpt("max_buffs", 8, () -> c.maxBuffs, v -> c.maxBuffs = v, 1, 20))
                .option(boolOpt("buff_render_background", true, () -> c.buffRenderBackground, v -> c.buffRenderBackground = v))
                .option(percentOpt("buff_background_alpha", 0.50f, () -> c.buffBackgroundAlpha, v -> c.buffBackgroundAlpha = v, 0.0f, 1.0f))
                .option(percentOpt("buff_opacity", 0.70f, () -> c.buffOpacity, v -> c.buffOpacity = v, 0.0f, 1.0f))
                .option(intOpt("buff_blink_rate", 10, () -> c.buffBlinkRate, v -> c.buffBlinkRate = v, 3, 60))

                .option(intOpt("level_offset_x", 0, () -> c.levelOffsetX, v -> c.levelOffsetX = v, -50, 50))
                .option(intOpt("level_offset_y", 44, () -> c.levelOffsetY, v -> c.levelOffsetY = v, -100, 100))
                .option(floatOpt("level_offset_z", 0f, () -> c.levelOffsetZ, v -> c.levelOffsetZ = v, -1f, 1f))
                .option(floatOptCustomStep("level_scale", 0.020f, () -> c.levelScale, v -> c.levelScale = v, 0.005f, 0.075f, 0.001f, "%.3f"))

                .option(boolOpt("level_shadow_enabled", false, () -> c.levelShadowEnabled, v -> c.levelShadowEnabled = v))
                .option(percentOpt("level_shadow_brightness", 0.5f, () -> c.levelShadowBrightness, v -> c.levelShadowBrightness = v, 0.0f, 1.0f))
                .option(percentOpt("level_shadow_alpha", 0.5f, () -> c.levelShadowAlpha, v -> c.levelShadowAlpha = v, 0f, 1f))
                .option(floatOpt("level_shadow_offset_x", 0.01f, () -> c.levelShadowOffsetX, v -> c.levelShadowOffsetX = v, -0.05f, 0.05f))
                .option(floatOpt("level_shadow_offset_y", 0.01f, () -> c.levelShadowOffsetY, v -> c.levelShadowOffsetY = v, -0.05f, 0.05f))
                .option(floatOpt("level_shadow_offset_z", 0.0f, () -> c.levelShadowOffsetZ, v -> c.levelShadowOffsetZ = v, -0.05f, 0.05f))

                .option(colorOpt("level_color_white", new Color(0xFFFFFF), () -> toAwt(c.levelColorWhite), v -> c.levelColorWhite = fromAwt(v)))
                .option(colorOpt("level_color_green", new Color(0x55FF55), () -> toAwt(c.levelColorGreen), v -> c.levelColorGreen = fromAwt(v)))
                .option(colorOpt("level_color_yellow", new Color(0xFFFF55), () -> toAwt(c.levelColorYellow), v -> c.levelColorYellow = fromAwt(v)))
                .option(colorOpt("level_color_orange", new Color(0xFFAA00), () -> toAwt(c.levelColorOrange), v -> c.levelColorOrange = fromAwt(v)))
                .option(colorOpt("level_color_red", new Color(0xFF5555), () -> toAwt(c.levelColorRed), v -> c.levelColorRed = fromAwt(v)))
                .option(colorOpt("level_color_dark_red", new Color(0xAA0000), () -> toAwt(c.levelColorDarkRed), v -> c.levelColorDarkRed = fromAwt(v)))

                .option(floatOptCustomStep("element_box_size", 30.0f, () -> c.elementBoxSize, v -> c.elementBoxSize = v, 10.0f, 70.0f, 0.1f, "%.1f"))
                .option(floatOptCustomStep("element_spacing", 0.0f, () -> c.elementSpacing, v -> c.elementSpacing = v, -5.0f, 15.0f, 0.1f, "%.1f"))
                .option(intOpt("element_offset_y", 30, () -> c.elementOffsetY, v -> c.elementOffsetY = v, -100, 120, 1))
                .option(intOpt("element_offset_x", 4, () -> c.elementOffsetX, v -> c.elementOffsetX = v, -100, 100, 1))
                .option(floatOpt("element_scale", 1.50f, () -> c.elementScale, v -> c.elementScale = v, 0.1f, 2.0f))
                .option(floatOpt("buff_offset_z", 0.0f, () -> c.buffOffsetZ, v -> c.buffOffsetZ = v, -1f, 1f))
                .option(floatOpt("element_offset_z", 0.0f, () -> c.elementOffsetZ, v -> c.elementOffsetZ = v, -1f, 1f))
                .option(percentOpt("element_min_opacity", 0.60f, () -> c.elementMinOpacity, v -> c.elementMinOpacity = v, 0f, 1f))
                .option(floatOptCustomStep("element_gauge_width", 80f, () -> c.elementGaugeWidth, v -> c.elementGaugeWidth = v, 20f, 150f, 1f, "%.0f"))
                .option(floatOptCustomStep("element_gauge_height", 8f, () -> c.elementGaugeHeight, v -> c.elementGaugeHeight = v, 2f, 30f, 0.5f, "%.1f"))
                .option(floatOptCustomStep("element_gauge_gap", -10.0f, () -> c.elementGaugeGap, v -> c.elementGaugeGap = v, -50f, 30f, 0.5f, "%.1f"))
                .option(percentOpt("element_gauge_bg_brightness", 0.35f, () -> c.elementGaugeBgBrightness, v -> c.elementGaugeBgBrightness = v, 0f, 1f))
                .option(percentOpt("element_gauge_bg_alpha", 0.80f, () -> c.elementGaugeBgAlpha, v -> c.elementGaugeBgAlpha = v, 0f, 1f))
                .option(floatOpt("element_gauge_anim_time", 0.30f, () -> c.elementGaugeAnimTime, v -> c.elementGaugeAnimTime = v, 0f, 2f))
                .option(floatOpt("element_low_gauge_threshold", 0.50f, () -> c.elementLowGaugeThreshold, v -> c.elementLowGaugeThreshold = v, 0f, 2f))
                .option(percentOpt("element_flicker_min_alpha", 0.25f, () -> c.elementFlickerMinAlpha, v -> c.elementFlickerMinAlpha = v, 0f, 1f))
                .option(floatOptCustomStep("element_flicker_min_hz", 1.5f, () -> c.elementFlickerMinHz, v -> c.elementFlickerMinHz = v, 0.1f, 20f, 0.1f, "%.1f"))
                .option(floatOptCustomStep("element_flicker_max_hz", 5.5f, () -> c.elementFlickerMaxHz, v -> c.elementFlickerMaxHz = v, 2.0f, 12.5f, 0.1f, "%.1f"))
                .option(floatOpt("element_flash_in_time", 0.08f, () -> c.elementFlashInTime, v -> c.elementFlashInTime = v, 0f, 1f))
                .option(floatOpt("element_flash_hold_time", 0.06f, () -> c.elementFlashHoldTime, v -> c.elementFlashHoldTime = v, 0f, 1f))
                .option(floatOpt("element_flash_out_time", 0.35f, () -> c.elementFlashOutTime, v -> c.elementFlashOutTime = v, 0f, 2f))
                .option(floatOpt("element_ghost_fade_time", 0.40f, () -> c.elementGhostFadeTime, v -> c.elementGhostFadeTime = v, 0f, 2f))
                .option(floatOptCustomStep("element_slot_slide_speed", 12f, () -> c.elementSlotSlideSpeed, v -> c.elementSlotSlideSpeed = v, 1f, 40f, 0.5f, "%.1f"))

                .option(floatOptCustomStep("sub_bar_width", 95f, () -> c.subBarWidth, v -> c.subBarWidth = v, 10f, 150f, 1f, "%.0f"))
                .option(floatOptCustomStep("shield_bar_height", 45f, () -> c.shieldBarHeight, v -> c.shieldBarHeight = v, 5f, 150f, 1f, "%.0f"))
                .option(floatOptCustomStep("zhen_mie_bar_height", 65f, () -> c.zhenMieBarHeight, v -> c.zhenMieBarHeight = v, 5f, 150f, 1f, "%.0f"))
                .option(floatOptCustomStep("sub_bar_gap", -27f, () -> c.subBarGap, v -> c.subBarGap = v, -50f, 25f, 0.5f, "%.1f"))
                .option(floatOptCustomStep("sub_bar_fill_speed", 12f, () -> c.subBarFillSpeed, v -> c.subBarFillSpeed = v, 1f, 50f, 0.5f, "%.1f"))
                .option(floatOpt("sub_bar_white_hold_time", 0.35f, () -> c.subBarWhiteHoldTime, v -> c.subBarWhiteHoldTime = v, 0f, 2f))
                .option(floatOptCustomStep("sub_bar_white_drain_speed", 6f, () -> c.subBarWhiteDrainSpeed, v -> c.subBarWhiteDrainSpeed = v, 1f, 20f, 0.5f, "%.1f"))
                .option(floatOpt("sub_bar_flash_time", 0.25f, () -> c.subBarFlashTime, v -> c.subBarFlashTime = v, 0f, 2f))
                .option(percentOpt("shield_bg_brightness", 0.30f, () -> c.shieldBgBrightness, v -> c.shieldBgBrightness = v, 0f, 1f))
                .option(percentOpt("shield_bg_alpha", 0.85f, () -> c.shieldBgAlpha, v -> c.shieldBgAlpha = v, 0f, 1f))
                .option(floatOptCustomStep("zhen_mie_buff_cycle_hz", 0.3f, () -> c.zhenMieBuffCycleHz, v -> c.zhenMieBuffCycleHz = v, 0.05f, 2.00f, 0.01f, "%.2f"))
                .option(colorOpt("shield_special_color", new Color(0xE6E6E6), () -> c.shieldSpecialColor, v -> c.shieldSpecialColor = v))
                .option(colorOpt("zhen_color", new Color(0x79C9FF), () -> c.zhenColor, v -> c.zhenColor = v))
                .option(colorOpt("mie_color", new Color(0xFFAFAF), () -> c.mieColor, v -> c.mieColor = v))
                .option(colorOpt("zhen_buff_color", new Color(0x0045FF), () -> c.zhenBuffColor, v -> c.zhenBuffColor = v))
                .option(colorOpt("mie_buff_color", new Color(0xFF00CE), () -> c.mieBuffColor, v -> c.mieBuffColor = v))
                .option(floatOpt("shield_layer2_white_mix", 0.55f, () -> c.shieldLayer2WhiteMix, v -> c.shieldLayer2WhiteMix = v, 0.0f, 1.0f))
                .option(floatOpt("shield_layer3_white_mix", 0.80f, () -> c.shieldLayer3WhiteMix, v -> c.shieldLayer3WhiteMix = v, 0.0f, 1.0f))
                .option(floatOpt("zhen_mie_clash_hue_hz", 6.25f, () -> c.zhenMieClashHueHz, v -> c.zhenMieClashHueHz = v, 0.1f, 30.0f))
                .option(enumOpt("zhen_mie_blend_mode", ZhenMieBlendMode.class, ZhenMieBlendMode.NORMAL, () -> c.zhenMieBlendMode, v -> c.zhenMieBlendMode = v))
                .option(floatOpt("zhen_mie_blend_strength", 1.0f, () -> c.zhenMieBlendStrength, v -> c.zhenMieBlendStrength = v, 0.0f, 1.0f))
                .option(floatOpt("zhen_mie_bg_opacity", 0.5f, () -> c.zhenMieBgOpacity, v -> c.zhenMieBgOpacity = v, 0.0f, 1.0f))
                .option(floatOpt("zhen_mie_breath_hz", 0.4f, () -> c.zhenMieBreathHz, v -> c.zhenMieBreathHz = v, 0.1f, 5.0f))
                .option(colorOpt("zhen_mie_breath_color", new Color(0xBA0012), () -> c.zhenMieBreathColor, v -> c.zhenMieBreathColor = v))

                .option(boolOpt("rpg_hud_enabled", true, () -> c.rpgHudEnabled, v -> c.rpgHudEnabled = v))
                .option(boolOpt("rpg_hide_ef_hud", true, () -> c.rpgHideEfHud, v -> c.rpgHideEfHud = v))
                .option(floatOptCustomStep("rpg_hud_scale", 1f, () -> c.rpgHudScale, v -> c.rpgHudScale = v, 0.5f, 3f, 0.05f, "%.2f"))
                .option(floatOptCustomStep("rpg_hud_offset_x", 6f, () -> c.rpgHudOffsetX, v -> c.rpgHudOffsetX = v, 0f, 400f, 1f, "%.0f"))
                .option(floatOptCustomStep("rpg_hud_offset_y", 6f, () -> c.rpgHudOffsetY, v -> c.rpgHudOffsetY = v, 0f, 400f, 1f, "%.0f"))
                .option(floatOptCustomStep("rpg_model_yaw", -10f, () -> c.rpgModelYaw, v -> c.rpgModelYaw = v, -90f, 90f, 1f, "%.0f"))
                .option(floatOptCustomStep("rpg_model_zoom", 1.00f, () -> c.rpgModelZoom, v -> c.rpgModelZoom = v, 0.50f, 2.00f, 0.01f, "%.2f"))
                .option(floatOptCustomStep("rpg_diamond_bg_alpha", 0.7f, () -> c.rpgDiamondBgAlpha, v -> c.rpgDiamondBgAlpha = v, 0f, 1f, 0.05f, "%.2f"))
                .option(boolOpt("rpg_model_stencil", true, () -> c.rpgModelStencil, v -> c.rpgModelStencil = v))
                .option(floatOptCustomStep("rpg_model_pivot", 1.00f, () -> c.rpgModelPivot, v -> c.rpgModelPivot = v, 0f, 2f, 0.01f, "%.2f"))
                .option(floatOptCustomStep("rpg_cam_x", 0f, () -> c.rpgCamX, v -> c.rpgCamX = v, -40f, 40f, 0.5f, "%.1f"))
                .option(floatOptCustomStep("rpg_cam_y", 7.5f, () -> c.rpgCamY, v -> c.rpgCamY = v, -40f, 50f, 0.5f, "%.1f"))
                .option(floatOptCustomStep("rpg_cam_z", 0f, () -> c.rpgCamZ, v -> c.rpgCamZ = v, -100f, 80f, 1f, "%.0f"))
                .option(floatOptCustomStep("rpg_cam_yaw", 0f, () -> c.rpgCamYaw, v -> c.rpgCamYaw = v, -180f, 180f, 1f, "%.0f"))
                .option(floatOptCustomStep("rpg_cam_pitch", -8f, () -> c.rpgCamPitch, v -> c.rpgCamPitch = v, -90f, 90f, 1f, "%.0f"))
                .option(floatOptCustomStep("rpg_cam_roll", 0f, () -> c.rpgCamRoll, v -> c.rpgCamRoll = v, -180f, 180f, 1f, "%.0f"))
                .option(floatOptCustomStep("rpg_text_pop_scale", 0.12f, () -> c.rpgTextPopScale, v -> c.rpgTextPopScale = v, 0f, 0.5f, 0.01f, "%.2f"))
                .option(floatOptCustomStep("rpg_text_pop_time", 0.22f, () -> c.rpgTextPopTime, v -> c.rpgTextPopTime = v, 0f, 1f, 0.01f, "%.2f"))
                .option(floatOptCustomStep("rpg_oxygen_retract_time", 0.30f, () -> c.rpgOxygenRetractTime, v -> c.rpgOxygenRetractTime = v, 0.05f, 2f, 0.05f, "%.2f"))
                .option(floatOptCustomStep("rpg_oxygen_settle_time", 0.35f, () -> c.rpgOxygenSettleTime, v -> c.rpgOxygenSettleTime = v, 0.05f, 2f, 0.05f, "%.2f"))
                .option(floatOptCustomStep("rpg_oxygen_gain_idle_time", 0.50f, () -> c.rpgOxygenGainIdleTime, v -> c.rpgOxygenGainIdleTime = v, 0.1f, 3f, 0.05f, "%.2f"))
                .option(floatOptCustomStep("rpg_shake_pixels", 12.0f, () -> c.rpgShakePixels, v -> c.rpgShakePixels = v, 0f, 20f, 0.5f, "%.1f"))
                .option(floatOptCustomStep("rpg_count_speed", 12f, () -> c.rpgCountSpeed, v -> c.rpgCountSpeed = v, 1f, 40f, 0.5f, "%.1f"))
                .option(floatOptCustomStep("rpg_food_preview_hz", 1.2f, () -> c.rpgFoodPreviewHz, v -> c.rpgFoodPreviewHz = v, 0.2f, 4f, 0.1f, "%.1f"))
                .option(colorOpt("rpg_exp_color", new Color(0x5BE35B), () -> c.rpgExpColor, v -> c.rpgExpColor = v))
                .option(intOpt("rpg_frame_slice", 0, () -> c.rpgFrameSlice, v -> c.rpgFrameSlice = v, 0, 32))
                .option(floatOptCustomStep("rpg_frame_corner", 0f, () -> c.rpgFrameCornerPx, v -> c.rpgFrameCornerPx = v, 0f, 16f, 0.5f, "%.1f"))
                .option(floatOptCustomStep("rpg_orb_hz", 1.2f, () -> c.rpgOrbHz, v -> c.rpgOrbHz = v, 0.1f, 5f, 0.1f, "%.1f"))
                .option(floatOptCustomStep("rpg_orb_brightness", 0.6f, () -> c.rpgOrbBrightness, v -> c.rpgOrbBrightness = v, 0.1f, 2.5f, 0.1f, "%.1f"))
                .option(floatOptCustomStep("rpg_orb_min_alpha", 0.3f, () -> c.rpgOrbMinAlpha, v -> c.rpgOrbMinAlpha = v, 0f, 1f, 0.01f, "%.2f"))
                .option(floatOptCustomStep("rpg_hp_text_scale", 0.90f, () -> c.rpgHpTextScale, v -> c.rpgHpTextScale = v, 0.3f, 2.0f, 0.01f, "%.2f"))
                .option(floatOptCustomStep("rpg_food_text_scale", 1.50f, () -> c.rpgFoodTextScale, v -> c.rpgFoodTextScale = v, 0.5f, 3.0f, 0.01f, "%.2f"))
                .option(floatOptCustomStep("rpg_hp_text_offset_y", 0.50f, () -> c.rpgHpTextOffsetY, v -> c.rpgHpTextOffsetY = v, -3f, 3f, 0.01f, "%.2f"))
                .option(ButtonOption.createBuilder()
                        .name(t(P("rpg_hud_editor")))
                        .description(OptionDescription.of(td(P("rpg_hud_editor"))))
                        .action((screen, option) -> {
                            YaclScrollMemory.save((YACLScreen) screen);
                            Minecraft.getInstance().setScreen(new com.dsp2.genshinerc.client.render.hud.gui.rpg.editor.RpgHudEditorScreen(screen));
                        })
                        .build())

                .option(ButtonOption.createBuilder()
                        .name(t(P("entity_y_offsets")))
                        .description(OptionDescription.of(td(P("entity_y_offsets"))))
                        .action((screen, option) -> {
                            YaclScrollMemory.save((YACLScreen) screen);
                            Minecraft.getInstance().setScreen(createOffsetScreen(screen));
                        })
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
        return Option.<Boolean>createBuilder().name(t(P(key))).description(OptionDescription.of(td(P(key))))
                .binding(def, g, s).controller(TickBoxControllerBuilder::create).build();
    }

    private static Option<Float> floatOpt(String key, float def, java.util.function.Supplier<Float> g, java.util.function.Consumer<Float> s, float min, float max) {
        return floatOptCustomStep(key, def, g, s, min, max, 0.01f, "%.2f");
    }

    private static Option<Float> floatOptCustomStep(String key, float def, java.util.function.Supplier<Float> g, java.util.function.Consumer<Float> s, float min, float max, float step, String format) {
        return Option.<Float>createBuilder().name(t(P(key))).description(OptionDescription.of(td(P(key)))).binding(def, g, s)
                .controller(opt -> FloatSliderControllerBuilder.create(opt).range(min, max).step(step)
                        .formatValue(v -> Component.literal(String.format(format, v)))).build();
    }

    private static Option<Float> percentOpt(String key, float def, java.util.function.Supplier<Float> g, java.util.function.Consumer<Float> s, float min, float max) {
        return Option.<Float>createBuilder().name(t(P(key))).description(OptionDescription.of(td(P(key)))).binding(def, g, s).
                controller(opt -> FloatSliderControllerBuilder.create(opt).range(min, max).step(0.01f).
                        formatValue(v -> Component.literal(String.format("%.0f%%", v * 100f)))).build();
    }

    private static Option<Integer> intOpt(String key, int def, java.util.function.Supplier<Integer> g, java.util.function.Consumer<Integer> s, int min, int max, int step) {
        return Option.<Integer>createBuilder().name(t(P(key))).description(OptionDescription.of(td(P(key)))).
                binding(def, g, s).controller(opt -> IntegerSliderControllerBuilder.create(opt).range(min, max).step(step)).build();
    }

    private static Option<Integer> intOpt(String key, int def, java.util.function.Supplier<Integer> g, java.util.function.Consumer<Integer> s, int min, int max) {
        return intOpt(key, def, g, s, min, max, 1);
    }

    private static Option<Color> colorOpt(String key, Color def, java.util.function.Supplier<Color> g, java.util.function.Consumer<Color> s) {
        return Option.<Color>createBuilder().name(t(P(key))).description(OptionDescription.of(td(P(key)))).
                binding(def, g, s).controller(ColorControllerBuilder::create).build();
    }

    private static <T extends Enum<T>> Option<T> enumOpt(String key, Class<T> enumClass, T def, java.util.function.Supplier<T> g, java.util.function.Consumer<T> s) {
        return Option.<T>createBuilder().name(t(P(key))).description(OptionDescription.of(td(P(key)))).binding(def, g, s).
                controller(opt -> EnumControllerBuilder.create(opt).enumClass(enumClass)).build();
    }

    public static Color toAwt(String hex) {
        try { return new Color(Integer.parseInt(hex.replace("#","").trim(), 16)); } catch (Exception e) { return Color.WHITE; }
    }
    public static String fromAwt(Color c) { return String.format("#%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue()); }
}