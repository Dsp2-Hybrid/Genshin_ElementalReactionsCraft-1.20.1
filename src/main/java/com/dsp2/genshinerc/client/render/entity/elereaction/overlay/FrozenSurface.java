package com.dsp2.genshinerc.client.render.entity.elereaction.overlay;

import com.dsp2.genshinerc.client.render.hud.bar.element.ClientAuraCache;
import com.dsp2.genshinerc.client.render.vfx.config.VFXConfig;
import com.dsp2.genshinerc.common.core.element.ElementType;
import com.dsp2.genshinerc.patch.core.gui.render.GuiRenderContext;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import org.lwjgl.opengl.GL11;

import java.util.Map;
import java.util.WeakHashMap;

@SuppressWarnings({"deprecation", "removal"})
public final class FrozenSurface {
    private static final ResourceLocation ICE = new ResourceLocation("genshinerc", "textures/entity/frozen_ice_pattern.png");
    private static final RenderType BASE = RenderType.entityTranslucent(ICE, false);
    public static final RenderType VANILLA = new SurfaceType(VertexFormat.Mode.QUADS);
    public static final RenderType EPIC_FIGHT = new SurfaceType(VertexFormat.Mode.TRIANGLES);
    private static final Map<LivingEntity, FreezeVisual> VISUALS = new WeakHashMap<>();
    private static final Map<LivingEntity, Pulse> PULSES = new WeakHashMap<>();

    private FrozenSurface() {}

    private static final class FreezeVisual {
        long seed;
        int band;
        float offsetU;
        float offsetV;
        float scale;
    }

    private static final class Pulse {
        double time;
        double phase;
    }

    public static boolean frozen(LivingEntity entity) {
        var auras = ClientAuraCache.get(entity.getId());
        return entity.isAlive() && auras != null && auras.hasActive(ElementType.FROZEN);
    }

    public static float gaugeRatio(LivingEntity entity) {
        var auras = ClientAuraCache.get(entity.getId());
        return auras == null || !entity.isAlive() ? 0f : net.minecraft.util.Mth.clamp(
                auras.gauge(ElementType.FROZEN)
                        / com.dsp2.genshinerc.common.core.element.FrozenControl.MAX_GAUGE,
                0f, 1f);
    }

    private static float gauge(LivingEntity entity) {
        return gaugeRatio(entity) * com.dsp2.genshinerc.common.core.element.FrozenControl.MAX_GAUGE;
    }

    private static FreezeVisual visual(LivingEntity entity) {
        int band = Math.max(0, Math.min(16, (int) Math.floor(gauge(entity) * 2f)));
        FreezeVisual visual = VISUALS.get(entity);
        if (visual == null) {
            visual = new FreezeVisual();
            visual.seed = entity.getUUID().getMostSignificantBits()
                    ^ entity.getUUID().getLeastSignificantBits()
                    ^ (long) entity.tickCount * 0x9E3779B97F4A7C15L;
            visual.band = band;
            VISUALS.put(entity, visual);
            randomize(visual, band);
        } else if (visual.band != band) {
            visual.band = band;
            randomize(visual, band);
        }
        return visual;
    }

    private static void randomize(FreezeVisual visual, int band) {
        long value = mix(visual.seed ^ (long) band * 0x9E3779B97F4A7C15L);
        visual.offsetU = unit(value);
        visual.offsetV = unit(mix(value + 0x632BE59BD9B4E019L));
        visual.scale = 1f + band * 0.075f;
    }

    private static long mix(long value) {
        value = (value ^ (value >>> 30)) * 0xBF58476D1CE4E5B9L;
        value = (value ^ (value >>> 27)) * 0x94D049BB133111EBL;
        return value ^ (value >>> 31);
    }

    private static float unit(long value) {
        return (float) ((value >>> 40) * (1.0 / 16777216.0));
    }

    public static VertexConsumer randomized(VertexConsumer consumer, LivingEntity entity) {
        FreezeVisual visual = visual(entity);
        return new UvConsumer(consumer, visual.offsetU, visual.offsetV, visual.scale);
    }

    public static MultiBufferSource randomized(MultiBufferSource source, LivingEntity entity) {
        return renderType -> {
            VertexConsumer consumer = source.getBuffer(renderType);
            return renderType == EPIC_FIGHT ? randomized(consumer, entity) : consumer;
        };
    }

    private static final class UvConsumer implements VertexConsumer {
        private final VertexConsumer delegate;
        private final float offsetU;
        private final float offsetV;
        private final float scale;

        private UvConsumer(VertexConsumer delegate, float offsetU, float offsetV, float scale) {
            this.delegate = delegate;
            this.offsetU = offsetU;
            this.offsetV = offsetV;
            this.scale = scale;
        }

        @Override
        public VertexConsumer vertex(double x, double y, double z) {
            delegate.vertex(x, y, z);
            return this;
        }

        @Override
        public VertexConsumer color(int red, int green, int blue, int alpha) {
            delegate.color(red, green, blue, alpha);
            return this;
        }

        @Override
        public VertexConsumer uv(float u, float v) {
            delegate.uv(fract((u + offsetU) * scale), fract((v + offsetV) * scale));
            return this;
        }

        @Override
        public VertexConsumer overlayCoords(int u, int v) {
            delegate.overlayCoords(u, v);
            return this;
        }

        @Override
        public VertexConsumer uv2(int u, int v) {
            delegate.uv2(u, v);
            return this;
        }

        @Override
        public VertexConsumer normal(float x, float y, float z) {
            delegate.normal(x, y, z);
            return this;
        }

        @Override
        public void endVertex() {
            delegate.endVertex();
        }

        @Override
        public void defaultColor(int red, int green, int blue, int alpha) {
            delegate.defaultColor(red, green, blue, alpha);
        }

        @Override
        public void unsetDefaultColor() {
            delegate.unsetDefaultColor();
        }

        private static float fract(float value) {
            return value - (float) Math.floor(value);
        }
    }

    private static float pulseStrength(LivingEntity entity, float partialTick) {
        float amount = gauge(entity);
        if (amount >= 4f) {
            PULSES.remove(entity);
            return 0f;
        }

        double time = entity.tickCount + partialTick;
        Pulse pulse = PULSES.computeIfAbsent(entity, ignored -> new Pulse());
        if (time < pulse.time || time - pulse.time > 5.0) {
            pulse.time = time;
            pulse.phase = 0.0;
        }

        double frequency = 1.0 + 2.5 * (1.0 - net.minecraft.util.Mth.clamp(amount / 4f, 0f, 1f));
        if (time > pulse.time) {
            pulse.phase = (pulse.phase + (time - pulse.time) * frequency / 20.0) % 1.0;
            pulse.time = time;
        }

        double wave = (Math.sin(pulse.phase * Math.PI * 2.0) + 1.0) * 0.5;
        double smooth = wave * wave * (3.0 - 2.0 * wave);
        double envelope = net.minecraft.util.Mth.clamp(4.0 - amount, 0.0, 1.0);
        return (float) (smooth * envelope);
    }

    public static float whiteFlashStrength(LivingEntity entity, float partialTick) {
        return gauge(entity) > 2.8f ? pulseStrength(entity, partialTick) : 0f;
    }

    public static boolean whiteFlash(LivingEntity entity, float partialTick) {
        return whiteFlashStrength(entity, partialTick) >= 0.98f;
    }

    public static float tintWeight(LivingEntity entity, float partialTick) {
        return 1f - net.minecraft.util.Mth.clamp(gauge(entity) / 4f, 0f, 1f);
    }

    public static float alpha(LivingEntity entity) {
        return alpha(entity, Minecraft.getInstance().getFrameTime());
    }

    public static float alpha(LivingEntity entity, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        if (!VFXConfig.get().cryoTintEnabled || GuiRenderContext.isInGui()
                || entity.isInvisible() || entity.isSpectator()
                || (entity == mc.getCameraEntity() && mc.options.getCameraType().isFirstPerson())
                || !frozen(entity)) {
            PULSES.remove(entity);
            VISUALS.remove(entity);
            return 0f;
        }

        float amount = gauge(entity);
        visual(entity);
        float maximum = 0.85f - 0.10f * net.minecraft.util.Mth.clamp(amount, 0f, 4f);
        return net.minecraft.util.Mth.clamp(maximum * (1f - pulseStrength(entity, partialTick)), 0f, 0.85f);
    }

    private static final class SurfaceType extends RenderType {
        private SurfaceType(VertexFormat.Mode mode) {
            super("genshinerc_frozen_surface_" + mode.name().toLowerCase(java.util.Locale.ROOT),
                    BASE.format(), mode, 256, false, false,
                    () -> {
                        BASE.setupRenderState();
                        RenderSystem.enableDepthTest();
                        RenderSystem.depthFunc(GL11.GL_LEQUAL);
                        RenderSystem.depthMask(false);
                    },
                    () -> {
                        RenderSystem.depthMask(true);
                        RenderSystem.depthFunc(GL11.GL_LEQUAL);
                        BASE.clearRenderState();
                    });
        }
    }
}
