package com.dsp2.genshinerc.client.render.entity.elereaction.overlay;

import com.dsp2.genshinerc.client.render.hud.bar.element.ClientAuraCache;
import com.dsp2.genshinerc.client.render.vfx.config.VFXConfig;
import com.dsp2.genshinerc.common.core.element.ElementType;
import com.dsp2.genshinerc.patch.core.gui.render.GuiRenderContext;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import org.lwjgl.opengl.GL11;

@SuppressWarnings({"deprecation", "removal"})
public final class FrozenSurface {
    private static final ResourceLocation ICE = new ResourceLocation("minecraft", "textures/block/ice.png");
    private static final RenderType BASE = RenderType.entityTranslucent(ICE, false);
    public static final RenderType VANILLA = new SurfaceType(VertexFormat.Mode.QUADS);
    public static final RenderType EPIC_FIGHT = new SurfaceType(VertexFormat.Mode.TRIANGLES);

    private FrozenSurface() {}

    public static boolean frozen(LivingEntity entity) {
        var auras = ClientAuraCache.get(entity.getId());
        return entity.isAlive() && auras != null && auras.hasActive(ElementType.FROZEN);
    }

    public static float gaugeRatio(LivingEntity entity) {
        var auras = ClientAuraCache.get(entity.getId());
        return auras == null || !entity.isAlive() ? 0f : net.minecraft.util.Mth.clamp(
                auras.gauge(ElementType.FROZEN) / com.dsp2.genshinerc.common.core.element.FrozenControl.MAX_GAUGE, 0f, 1f);
    }

    private static final java.util.Map<LivingEntity, Pulse> PULSES = new java.util.WeakHashMap<>();

    private static final class Pulse {
        double time;
        double phase;
        int band;
    }

    private static float gauge(LivingEntity entity) {
        return gaugeRatio(entity) * com.dsp2.genshinerc.common.core.element.FrozenControl.MAX_GAUGE;
    }

    private static float pulseStrength(LivingEntity entity, float partialTick) {
        float amount = gauge(entity);
        int band = amount < 1.2f ? -1 : amount > 2.8f ? 1 : 0;
        if (band == 0) {
            PULSES.remove(entity);
            return 0f;
        }

        double time = entity.tickCount + (double) partialTick;
        Pulse pulse = PULSES.computeIfAbsent(entity, ignored -> new Pulse());
        if (pulse.band != band || time < pulse.time || time - pulse.time > 5.0) {
            pulse.band = band;
            pulse.time = time;
            pulse.phase = 0.0;
        }

        double frequency = band < 0
                ? 1.1 + 2.3 * (1.0 - amount / 1.2)
                : 1.05 + 2.35 * Math.min(1.0, (amount - 2.8) / 5.2);
        if (time > pulse.time) {
            pulse.phase = (pulse.phase + (time - pulse.time) * frequency / 20.0) % 1.0;
            pulse.time = time;
        }

        double wave = (Math.sin(pulse.phase * Math.PI * 2.0) + 1.0) * 0.5;
        double smooth = wave * wave * (3.0 - 2.0 * wave);
        double envelope = band < 0
                ? net.minecraft.util.Mth.clamp((1.2 - amount) / 0.24, 0.0, 1.0)
                : net.minecraft.util.Mth.clamp((amount - 2.8) / 0.4, 0.0, 1.0);
        return (float) (smooth * envelope);
    }

    public static float whiteFlashStrength(LivingEntity entity, float partialTick) {
        float amount = gauge(entity);
        return amount > 2.8f ? pulseStrength(entity, partialTick) : 0f;
    }

    public static boolean whiteFlash(LivingEntity entity, float partialTick) {
        return whiteFlashStrength(entity, partialTick) >= 0.98f;
    }

    public static float tintWeight(LivingEntity entity, float partialTick) {
        return (float) (0.18 + 0.22 * Math.pow(gaugeRatio(entity), 0.65));
    }

    public static float alpha(LivingEntity entity) {
        return alpha(entity, Minecraft.getInstance().getFrameTime());
    }

    public static float alpha(LivingEntity entity, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        VFXConfig cfg = VFXConfig.get();
        if (!cfg.cryoTintEnabled || GuiRenderContext.isInGui() || entity.isInvisible() || entity.isSpectator()
                || (entity == mc.getCameraEntity() && mc.options.getCameraType().isFirstPerson()) || !frozen(entity)) {
            PULSES.remove(entity);
            return 0f;
        }

        float ratio = gaugeRatio(entity);
        float amount = ratio * com.dsp2.genshinerc.common.core.element.FrozenControl.MAX_GAUGE;
        float pulse = pulseStrength(entity, partialTick);
        float base = net.minecraft.util.Mth.clamp(cfg.cryoTintAlpha, 0f, 1f);
        double opacity = base * (0.78 + 0.72 * Math.pow(ratio, 0.55));
        if (amount < 1.2f) opacity *= 1.0 - pulse * 0.92;
        return (float) Math.min(0.72, opacity);
    }
    private static final class SurfaceType extends RenderType {
        private SurfaceType(VertexFormat.Mode mode) {
            super("genshinerc_frozen_surface_" + mode.name().toLowerCase(java.util.Locale.ROOT),
                    BASE.format(), mode, 256, false, false,
                    () -> {
                        BASE.setupRenderState();
                        RenderSystem.enableDepthTest();
                        RenderSystem.depthFunc(GL11.GL_EQUAL);
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
