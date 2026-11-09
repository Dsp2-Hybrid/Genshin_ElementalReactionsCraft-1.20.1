package com.dsp2.genshinerc.client.render.vfx.aura.elevfx;

import com.dsp2.genshinerc.client.render.hud.bar.element.ClientAuraCache;
import com.dsp2.genshinerc.client.render.hud.bar.element.EntityAuras;
import com.dsp2.genshinerc.client.render.vfx.config.VFXConfig;
import com.dsp2.genshinerc.common.core.element.ElementType;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

@SuppressWarnings({"deprecation", "removal"})
@Mod.EventBusSubscriber(modid = "genshinerc", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class ElementCryoVisuals {

    private static final ResourceLocation POWDER_SNOW_OUTLINE =
            new ResourceLocation("minecraft", "textures/misc/powder_snow_outline.png");

    private static float frostAlpha;
    private static long lastFrameNanos;

    private ElementCryoVisuals() {}

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) return;
        VFXConfig cfg = VFXConfig.get();
        if (!cfg.cryoTintEnabled) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        long nowMs = System.currentTimeMillis();
        double phase = (nowMs % (long) (1000.0 * cfg.cryoPulseSeconds)) / (1000.0 * cfg.cryoPulseSeconds);
        float pulse = 0.5f + 0.5f * (float) Math.sin(phase * Math.PI * 2.0);

        float partialTick = event.getPartialTick();
        Vec3 cam = event.getCamera().getPosition();
        PoseStack ps = event.getPoseStack();
        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();

        for (var entity : mc.level.entitiesForRendering()) {
            if (!(entity instanceof LivingEntity living) || !living.isAlive()) continue;
            EntityAuras auras = ClientAuraCache.get(entity.getId());
            if (auras == null) continue;
            boolean frozen = auras.has(ElementType.FROZEN);
            if (!frozen && !auras.has(ElementType.CRYO)) continue;

            float gauge = Math.max(auras.gauge(ElementType.FROZEN), auras.gauge(ElementType.CRYO));
            float gaugeFactor = Mth.clamp(gauge / 3f, 0f, 1f);
            float reactionDim = ElementReactionDim.factor(entity.getId(), cfg);
            float alpha = cfg.cryoTintAlpha * gaugeFactor * reactionDim
                    * (cfg.cryoPulseMin + (1f - cfg.cryoPulseMin) * pulse);
            if (alpha <= 0.004f) continue;

            double x = Mth.lerp(partialTick, living.xo, living.getX()) - cam.x;
            double y = Mth.lerp(partialTick, living.yo, living.getY()) - cam.y;
            double z = Mth.lerp(partialTick, living.zo, living.getZ()) - cam.z;

            drawBox(ps, buffers, x, y, z, living.getBbWidth(), living.getBbHeight(), 0x8FD8FF, alpha);
        }
        buffers.endBatch();
    }

    private static void drawBox(PoseStack ps, MultiBufferSource buffers, double x, double y, double z,
                                float width, float height, int rgb, float alpha) {
        float h = width * 0.5f;
        float x1 = (float) x - h, x2 = (float) x + h;
        float y1 = (float) y, y2 = (float) y + height;
        float z1 = (float) z - h, z2 = (float) z + h;

        int r = (rgb >> 16) & 255, g = (rgb >> 8) & 255, b = rgb & 255;
        int a = (int) (Mth.clamp(alpha, 0f, 1f) * 255f);
        if (a <= 2) return;

        ps.pushPose();
        Matrix4f m = ps.last().pose();
        VertexConsumer vc = buffers.getBuffer(com.dsp2.genshinerc.client.render.hud.HudRenderTypes.overlay(
                net.minecraft.client.renderer.texture.TextureAtlas.LOCATION_BLOCKS));

        quad(vc, m, x1, y1, z1, x2, y1, z1, x2, y2, z1, x1, y2, z1, r, g, b, a);
        quad(vc, m, x2, y1, z2, x1, y1, z2, x1, y2, z2, x2, y2, z2, r, g, b, a);
        quad(vc, m, x1, y1, z2, x1, y1, z1, x1, y2, z1, x1, y2, z2, r, g, b, a);
        quad(vc, m, x2, y1, z1, x2, y1, z2, x2, y2, z2, x2, y2, z1, r, g, b, a);
        ps.popPose();
    }

    private static void quad(VertexConsumer vc, Matrix4f m,
                             float ax, float ay, float az, float bx, float by, float bz,
                             float cx, float cy, float cz, float dx, float dy, float dz,
                             int r, int g, int b, int a) {
        vc.vertex(m, ax, ay, az).color(r, g, b, a).endVertex();
        vc.vertex(m, bx, by, bz).color(r, g, b, a).endVertex();
        vc.vertex(m, cx, cy, cz).color(r, g, b, a).endVertex();
        vc.vertex(m, dx, dy, dz).color(r, g, b, a).endVertex();
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiOverlayEvent.Post event) {
        if (event.getOverlay() != VanillaGuiOverlay.HOTBAR.type()) return;

        VFXConfig cfg = VFXConfig.get();
        if (!cfg.frostOverlayEnabled) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;

        EntityAuras auras = ClientAuraCache.get(mc.player.getId());
        boolean frozen = auras != null && auras.has(ElementType.FROZEN);

        long now = System.nanoTime();
        float dt = lastFrameNanos == 0 ? 0f : Math.min(0.1f, (now - lastFrameNanos) / 1_000_000_000f);
        lastFrameNanos = now;

        float target = frozen ? cfg.frostMaxAlpha : 0f;
        float k = 1f - (float) Math.exp(-dt * (frozen ? cfg.frostEaseInSpeed : cfg.frostEaseOutSpeed));
        frostAlpha += (target - frostAlpha) * k;
        if (frostAlpha <= 0.002f) return;

        float t = Mth.clamp(frostAlpha / Math.max(0.001f, cfg.frostMaxAlpha), 0f, 1f);
        float eased = t * t * (3f - 2f * t);

        GuiGraphics g = event.getGuiGraphics();
        int w = g.guiWidth();
        int h = g.guiHeight();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1f, 1f, 1f, eased * cfg.frostMaxAlpha);

        g.blit(POWDER_SNOW_OUTLINE, 0, 0, 0, 0, w, h, w, h);

        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.disableBlend();
    }
}