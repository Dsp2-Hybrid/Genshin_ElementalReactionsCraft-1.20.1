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

    public static float tintWeight(LivingEntity entity, float partialTick) {
        float ratio = gaugeRatio(entity);
        float weak = net.minecraft.util.Mth.clamp(1f - 2f * ratio, 0f, 1f);
        float time = (entity.tickCount % 600 + partialTick) * 0.05f;
        float wave = 0.5f + 0.5f * (float) Math.sin(time * (7f + 14f * weak));
        return net.minecraft.util.Mth.clamp((float) Math.sqrt(ratio) - 0.16f * weak * wave, 0f, 1f);
    }

    public static float alpha(LivingEntity entity) {
        Minecraft mc = Minecraft.getInstance();
        VFXConfig cfg = VFXConfig.get();
        if (!cfg.cryoTintEnabled || GuiRenderContext.isInGui() || entity.isInvisible() || entity.isSpectator()
                || (entity == mc.getCameraEntity() && mc.options.getCameraType().isFirstPerson()) || !frozen(entity)) return 0f;
        return (float) Math.max(0.0, Math.min(0.8, cfg.cryoTintAlpha)) * (0.55f + 0.45f * gaugeRatio(entity));
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
