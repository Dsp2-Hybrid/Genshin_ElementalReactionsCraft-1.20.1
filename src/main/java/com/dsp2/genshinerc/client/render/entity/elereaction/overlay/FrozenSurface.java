package com.dsp2.genshinerc.client.render.entity.elereaction.overlay;

import com.dsp2.genshinerc.client.render.hud.bar.element.ClientAuraCache;
import com.dsp2.genshinerc.client.render.vfx.config.VFXConfig;
import com.dsp2.genshinerc.common.core.element.ElementType;
import com.dsp2.genshinerc.common.core.element.FrozenControl;
import com.dsp2.genshinerc.patch.core.gui.render.GuiRenderContext;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

@SuppressWarnings({"deprecation", "removal"})
@Mod.EventBusSubscriber(modid = "genshinerc", value = Dist.CLIENT)
public final class FrozenSurface {
    private static final int SIZE = 512;
    private static final int WRAP = SIZE - 1;
    private static final int COLS = 2;
    private static final int ROWS = 4;

    private static final float PULSE_THRESHOLD = FrozenControl.MAX_GAUGE * 0.575f;
    private static final double SLOW_HZ = 0.023;
    private static final double FAST_HZ = 0.185;
    private static final float BASE_ALPHA_MIN = 0.35f;
    private static final float BASE_ALPHA_MAX = 0.8f;

    private static final float DEEP_FREEZE_START = 3.0f;
    private static final float DEEP_FREEZE_END = 4.0f;
    private static final int DEEP_FREEZE_STEPS = 6;

    private static final ResourceLocation MASKS = new ResourceLocation(
            "genshinerc", "textures/entity/frozen_long_crack_masks.png");
    private static final RenderType BASE = RenderType.entityTranslucent(MASKS, false);

    private static final Map<LivingEntity, Visual> VISUALS = new IdentityHashMap<>();

    private static MaskSheet sheet;
    private static long generation;

    private FrozenSurface() {}

    private static float amount(LivingEntity entity) {
        var auras = ClientAuraCache.get(entity.getId());
        if (!entity.isAlive() || auras == null || !auras.hasActive(ElementType.FROZEN)) return 0f;
        return Mth.clamp(auras.gauge(ElementType.FROZEN), 0f, FrozenControl.MAX_GAUGE);
    }

    public static boolean frozen(LivingEntity entity) {
        return amount(entity) > 0f;
    }

    public static float gaugeRatio(LivingEntity entity) {
        return amount(entity) / FrozenControl.MAX_GAUGE;
    }

    public static float tintWeight(LivingEntity entity, float partialTick) {
        return 1f - gaugeRatio(entity);
    }

    private static Visual visual(LivingEntity entity) {
        return VISUALS.computeIfAbsent(entity, ignored -> new Visual());
    }

    private static double smooth(double value) {
        return value * value * (3.0 - 2.0 * value);
    }

    private static float pulse(LivingEntity entity, Visual visual, float gauge, float partialTick) {
        double now = entity.tickCount + (double) Mth.clamp(partialTick, 0f, 1f);
        double progress = smooth(Mth.clamp((PULSE_THRESHOLD - gauge) / PULSE_THRESHOLD, 0.0, 1.0));
        if (progress <= 0.0) {
            visual.pulsing = false;
            visual.phase = 0.0;
            visual.time = now;
            return 0f;
        }

        double hz = SLOW_HZ + (FAST_HZ - SLOW_HZ) * progress;
        if (!visual.pulsing) {
            visual.pulsing = true;
            visual.phase = 0.0;
            visual.time = now;
            visual.hz = hz;
        } else if (now > visual.time) {
            double elapsed = now - visual.time;
            visual.phase = (visual.phase + elapsed * (visual.hz + hz) / 2.0) % 1.0;
            visual.time = now;
            visual.hz = hz;
        }

        return (float) (progress * (0.5 - 0.5 * Math.cos(visual.phase * Math.PI * 2.0)));
    }

    public static float whiteFlashStrength(LivingEntity entity, float partialTick) {
        float gauge = amount(entity);
        return gauge <= 0f ? 0f : pulse(entity, visual(entity), gauge, partialTick);
    }

    public static boolean whiteFlash(LivingEntity entity, float partialTick) {
        return whiteFlashStrength(entity, partialTick) >= 0.98f;
    }

    public static float alpha(LivingEntity entity) {
        return alpha(entity, Minecraft.getInstance().getFrameTime());
    }

    public static float alpha(LivingEntity entity, float partialTick) {
        var mc = Minecraft.getInstance();
        float gauge = amount(entity);
        if (gauge <= 0f || !VFXConfig.get().cryoTintEnabled || GuiRenderContext.isInGui()
                || entity.isInvisible() || entity.isSpectator()
                || (entity == mc.getCameraEntity() && mc.options.getCameraType().isFirstPerson())) {
            return 0f;
        }

        float ratio = Mth.clamp(gauge / FrozenControl.MAX_GAUGE, 0f, 1f);
        float eased = (float) Math.sqrt(ratio);
        float baseAlpha = Mth.lerp(eased, BASE_ALPHA_MIN, BASE_ALPHA_MAX);
        float pulse = pulse(entity, visual(entity), gauge, partialTick);
        float pulseMultiplier = 1f - pulse;
        return Mth.clamp(baseAlpha * pulseMultiplier, 0f, BASE_ALPHA_MAX);
    }

    public static RenderType layer(LivingEntity entity, boolean triangles) {
        Visual visual = visual(entity);
        if (visual.skin == null) visual.skin = new Skin();
        Skin skin = visual.skin;

        float gauge = amount(entity);
        long tick = entity.level().getGameTime();
        int deepStep = deepFreezeStep(gauge);

        boolean first = !skin.initialized;
        boolean changed = !first && tick - skin.lastPatternTick >= 20L
                && Math.abs(gauge - skin.patternGauge) >= 0.5f;
        boolean deepChanged = !first && deepStep != skin.deepStep;

        if (first || changed) {
            skin.patternGauge = gauge;
            skin.lastPatternTick = tick;
            skin.band = Mth.clamp(Mth.ceil(gauge * 2f), 1, 16);
            skin.seed = visual.random.nextLong();
            skin.initialized = true;
        }

        if (first || changed || deepChanged || skin.generation != generation) {
            skin.deepStep = deepStep;
            paint(skin.texture.getPixels(), skin.band, skin.seed, deepStep, masks());
            skin.texture.upload();
            skin.generation = generation;
        }

        return triangles ? skin.triangles : skin.quads;
    }

    private static int deepFreezeStep(float gauge) {
        if (gauge <= DEEP_FREEZE_START) return 0;
        float t = Mth.clamp((gauge - DEEP_FREEZE_START) / (DEEP_FREEZE_END - DEEP_FREEZE_START), 0f, 1f);
        return Math.round(t * DEEP_FREEZE_STEPS);
    }

    private static final class Visual {
        final Random random = new Random(ThreadLocalRandom.current().nextLong());
        boolean pulsing;
        double time;
        double hz;
        double phase;
        Skin skin;
    }

    private static final class Skin {
        final DynamicTexture texture = new DynamicTexture(SIZE, SIZE, true);
        final ResourceLocation id = Minecraft.getInstance().getTextureManager()
                .register("genshinerc_frozen_coating", texture);
        final RenderType quads = new SkinLayer(id, VertexFormat.Mode.QUADS);
        final RenderType triangles = new SkinLayer(id, VertexFormat.Mode.TRIANGLES);

        boolean initialized;
        float patternGauge;
        int band;
        int deepStep = -1;
        long lastPatternTick;
        long seed;
        long generation = -1;
    }

    private static final class SkinLayer extends RenderType {
        SkinLayer(ResourceLocation texture, VertexFormat.Mode mode) {
            super("genshinerc_frozen_coating_" + mode.name(), BASE.format(), mode, 4096, false, false,
                    () -> {
                        BASE.setupRenderState();
                        RenderSystem.setShaderTexture(0, texture);
                        RenderSystem.depthMask(false);
                        GlStateManager._enablePolygonOffset();
                        GlStateManager._polygonOffset(-1.0f, -10.0f);
                    },
                    () -> {
                        GlStateManager._disablePolygonOffset();
                        RenderSystem.depthMask(true);
                        BASE.clearRenderState();
                    });
        }
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || VISUALS.isEmpty()) return;
        var mc = Minecraft.getInstance();
        var iterator = VISUALS.entrySet().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            LivingEntity entity = entry.getKey();
            Visual visual = entry.getValue();
            if (entity.level() == mc.level && !entity.isRemoved() && frozen(entity)) continue;
            if (visual.skin != null) mc.getTextureManager().release(visual.skin.id);
            iterator.remove();
        }
    }

    @Mod.EventBusSubscriber(modid = "genshinerc", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class Reload {
        @SubscribeEvent
        public static void register(RegisterClientReloadListenersEvent event) {
            event.registerReloadListener((ResourceManagerReloadListener) manager -> {
                sheet = null;
                generation++;
            });
        }
    }

    private static MaskSheet masks() {
        if (sheet == null) sheet = readMasks(Minecraft.getInstance().getResourceManager());
        return sheet;
    }

    private static MaskSheet readMasks(ResourceManager manager) {
        try (InputStream input = manager.open(MASKS); NativeImage image = NativeImage.read(input)) {
            int width = image.getWidth();
            int height = image.getHeight();
            if (width < 4 || height < 8 || width % COLS != 0 || height % ROWS != 0) {
                throw new IllegalArgumentException("Invalid 2 x 4 crack mask sheet: " + MASKS);
            }

            byte[] alpha = new byte[width * height];
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    alpha[y * width + x] = (byte) (image.getPixelRGBA(x, y) >>> 24);
                }
            }
            return new MaskSheet(width, width / COLS, height / ROWS, alpha);
        } catch (IOException ex) {
            throw new UncheckedIOException(MASKS.toString(), ex);
        }
    }

    private record MaskSheet(int stride, int width, int height, byte[] alpha) {
        float sample(int variant, double x, double y) {
            if (x < 0 || y < 0 || x >= width - 1 || y >= height - 1) return 0f;
            int ix = (int) x;
            int iy = (int) y;
            int index = ((variant / COLS) * height + iy) * stride + (variant % COLS) * width + ix;
            float tx = (float) (x - ix);
            float ty = (float) (y - iy);
            float a = Mth.lerp(tx, alpha[index] & 255, alpha[index + 1] & 255);
            float b = Mth.lerp(tx, alpha[index + stride] & 255, alpha[index + stride + 1] & 255);
            return Mth.lerp(ty, a, b) / 255f;
        }
    }

    private static int rgb(int red, int green, int blue) {
        return 0xff000000 | blue << 16 | green << 8 | red;
    }

    private static int blend(int from, int to, float weight) {
        int red = Math.round(Mth.lerp(weight, from & 255, to & 255));
        int green = Math.round(Mth.lerp(weight, from >>> 8 & 255, to >>> 8 & 255));
        int blue = Math.round(Mth.lerp(weight, from >>> 16 & 255, to >>> 16 & 255));
        return rgb(red, green, blue);
    }

    private static void paint(NativeImage image, int band, long seed, int deepStep, MaskSheet masks) {
        float strength = (band - 1) / 15f;
        float deepT = deepStep / (float) DEEP_FREEZE_STEPS;

        int base = rgb(181 - Math.round(22 * strength), 221 - Math.round(12 * strength), 250);
        int dark = rgb(99 - Math.round(75 * strength), 151 - Math.round(90 * strength),
                197 - Math.round(88 * strength));
        int edge = rgb(226, 246, 255);

        int deepBase = rgb(86, 132, 196);
        int deepDark = rgb(42, 74, 132);

        base = blend(base, deepBase, deepT);
        dark = blend(dark, deepDark, deepT);

        image.fillRect(0, 0, SIZE, SIZE, base);

        Random random = new Random(seed);
        int count = 14 + 3 * band;
        for (int n = 0; n < count; n++) {
            int variant = random.nextInt(COLS * ROWS);
            int cx = random.nextInt(SIZE);
            int cy = random.nextInt(SIZE);
            double angle = random.nextDouble() * Math.PI * 2.0;
            double cos = Math.cos(angle);
            double sin = Math.sin(angle);
            double width = 90.0 + random.nextDouble() * 105.0;
            double height = width * (0.29 + random.nextDouble() * 0.23);
            double sx = masks.width / width;
            double sy = masks.height / height;
            int rx = (int) Math.ceil(0.5 * (Math.abs(cos) * width + Math.abs(sin) * height)) + 2;
            int ry = (int) Math.ceil(0.5 * (Math.abs(sin) * width + Math.abs(cos) * height)) + 2;

            for (int dy = -ry; dy <= ry; dy++) {
                for (int dx = -rx; dx <= rx; dx++) {
                    double u = (cos * dx + sin * dy) * sx + masks.width * 0.5;
                    double v = (-sin * dx + cos * dy) * sy + masks.height * 0.5;
                    float line = masks.sample(variant, u, v);
                    float rim = masks.sample(variant, u - 0.7, v - 1.8);
                    if (line + rim < 0.004f) continue;

                    int x = (cx + dx) & WRAP;
                    int y = (cy + dy) & WRAP;
                    int color = image.getPixelRGBA(x, y);
                    if (rim > 0f) color = blend(color, edge, rim * 0.76f);
                    if (line > 0f) color = blend(color, dark, line);
                    image.setPixelRGBA(x, y, color);
                }
            }
        }
    }
}