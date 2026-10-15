package com.dsp2.genshinerc.client.render.damageindicator.config;

import com.dsp2.genshinerc.client.render.damageindicator.api.CustomColor;
import com.dsp2.genshinerc.client.render.damageindicator.api.DamageColorPreset;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.*;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.loading.FMLPaths;

import java.awt.Color;

public class DamageIndicatorConfig {

    @SerialEntry public boolean enabled = true;
    @SerialEntry public boolean showPercentageText = false;
    @SerialEntry public boolean showSelfDamage = true;
    @SerialEntry public double maxDistance = 35.0;

    @SerialEntry public int lifeTime = 25;
    @SerialEntry public float velocityMultiplier = 0.75f;
    @SerialEntry public float gravityStrength = -0.25f;
    @SerialEntry public boolean randomVelocityXZ = true;
    @SerialEntry public double velocityX = 0.5, velocityY = 0.25, velocityZ = 0.5;
    @SerialEntry public double offsetX = 0.0, offsetY = 0.25, offsetZ = 0.0;

    @SerialEntry public float normalSize = 1.8f;
    @SerialEntry public float critSize   = 2.85f;

    @SerialEntry public String defaultColor = "#FFFFFF";
    @SerialEntry public String healColor    = "#32C832";
    @SerialEntry public String presetColor  = "WHITE";
    @SerialEntry public boolean drawShadow  = true;

    public static final ConfigClassHandler<DamageIndicatorConfig> HANDLER =
            ConfigClassHandler.createBuilder(DamageIndicatorConfig.class)
                    .id(ResourceLocation.fromNamespaceAndPath("genshinerc", "damageindicator"))
                    .serializer(cfg -> GsonConfigSerializerBuilder.create(cfg)
                            .setPath(FMLPaths.CONFIGDIR.get().resolve("damageindicator.json5"))
                            .build())
                    .build();

    public static DamageIndicatorConfig get() { return HANDLER.instance(); }
    public static void load() { HANDLER.load(); }
    public static void save() { HANDLER.save(); }

    public CustomColor currentPresetColor() {
        return DamageColorPreset.fromName(presetColor).toColor();
    }

    /** 返回 Screen，可直接传给 mc.setScreen(...) */
    public static Screen createScreen() {
        DamageIndicatorConfig c = get();
        return YetAnotherConfigLib.createBuilder()
                .title(Component.literal("Damageful"))
                .category(ConfigCategory.createBuilder()
                        .name(Component.literal("General"))
                        .option(Option.<Boolean>createBuilder().name(Component.literal("Enabled"))
                                .binding(true, () -> c.enabled, v -> c.enabled = v)
                                .controller(TickBoxControllerBuilder::create).build())
                        .option(Option.<Boolean>createBuilder().name(Component.literal("Show damage as percentage"))
                                .binding(false, () -> c.showPercentageText, v -> c.showPercentageText = v)
                                .controller(TickBoxControllerBuilder::create).build())
                        .option(Option.<Boolean>createBuilder().name(Component.literal("Show self damage"))
                                .binding(true, () -> c.showSelfDamage, v -> c.showSelfDamage = v)
                                .controller(TickBoxControllerBuilder::create).build())
                        .option(Option.<Double>createBuilder().name(Component.literal("Max distance"))
                                .binding(35.0, () -> c.maxDistance, v -> c.maxDistance = v)
                                .controller(opt -> DoubleSliderControllerBuilder.create(opt).range(0.0, 128.0).step(1.0)).build())
                        .option(Option.<Integer>createBuilder().name(Component.literal("Life time"))
                                .binding(25, () -> c.lifeTime, v -> c.lifeTime = v)
                                .controller(opt -> IntegerSliderControllerBuilder.create(opt).range(5, 200).step(1)).build())
                        .option(Option.<Float>createBuilder().name(Component.literal("Velocity multiplier"))
                                .binding(0.75f, () -> c.velocityMultiplier, v -> c.velocityMultiplier = v)
                                .controller(opt -> FloatSliderControllerBuilder.create(opt).range(0f, 2f).step(0.05f)).build())
                        .option(Option.<Float>createBuilder().name(Component.literal("Gravity strength"))
                                .binding(-0.25f, () -> c.gravityStrength, v -> c.gravityStrength = v)
                                .controller(opt -> FloatSliderControllerBuilder.create(opt).range(-2f, 2f).step(0.05f)).build())
                        .option(Option.<Boolean>createBuilder().name(Component.literal("Random X and Z velocity"))
                                .binding(true, () -> c.randomVelocityXZ, v -> c.randomVelocityXZ = v)
                                .controller(TickBoxControllerBuilder::create).build())
                        .option(Option.<Boolean>createBuilder().name(Component.literal("Draw shadow"))
                                .binding(true, () -> c.drawShadow, v -> c.drawShadow = v)
                                .controller(TickBoxControllerBuilder::create).build())
                        .build())
                .category(ConfigCategory.createBuilder()
                        .name(Component.literal("Size"))
                        .option(Option.<Float>createBuilder().name(Component.literal("Normal size"))
                                .binding(1.8f, () -> c.normalSize, v -> c.normalSize = v)
                                .controller(opt -> FloatSliderControllerBuilder.create(opt).range(0.1f, 8f).step(0.05f)).build())
                        .option(Option.<Float>createBuilder().name(Component.literal("Crit size"))
                                .binding(2.85f, () -> c.critSize, v -> c.critSize = v)
                                .controller(opt -> FloatSliderControllerBuilder.create(opt).range(0.1f, 8f).step(0.05f)).build())
                        .build())
                .category(ConfigCategory.createBuilder()
                        .name(Component.literal("Colors"))
                        .option(Option.<Color>createBuilder().name(Component.literal("Default color"))
                                .binding(Color.WHITE, () -> HexUtil.toAwt(c.defaultColor), v -> c.defaultColor = HexUtil.fromAwt(v))
                                .controller(ColorControllerBuilder::create).build())
                        .option(Option.<Color>createBuilder().name(Component.literal("Heal color"))
                                .binding(new Color(0xFFD200), () -> HexUtil.toAwt(c.healColor), v -> c.healColor = HexUtil.fromAwt(v))
                                .controller(ColorControllerBuilder::create).build())
                        .option(Option.<String>createBuilder().name(Component.literal("Preset color name"))
                                .description(OptionDescription.of(Component.literal(
                                        "WHITE / ORANGE / EARTH_YELLOW / PURPLE / GREEN / LIGHT_BLUE / DARK_BLUE / CYAN_GREEN / RED")))
                                .binding("WHITE", () -> c.presetColor, v -> c.presetColor = v)
                                .controller(StringControllerBuilder::create).build())
                        .build())
                .save(DamageIndicatorConfig::save)
                .build()
                .generateScreen(null);
    }

    static class HexUtil {
        static Color toAwt(String hex) {
            try { return new Color(Integer.parseInt(hex.replace("#", "").trim(), 16)); }
            catch (Exception e) { return Color.WHITE; }
        }
        static String fromAwt(Color c) {
            return String.format("#%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue());
        }
    }
}