package com.dsp2.genshinerc.client.render.vfx.aura.elevfx;

import com.dsp2.genshinerc.client.render.hud.bar.element.ClientAuraCache;
import com.dsp2.genshinerc.client.render.hud.bar.element.EntityAuras;
import com.dsp2.genshinerc.client.render.vfx.config.VFXConfig;
import com.dsp2.genshinerc.common.core.element.ElementType;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@SuppressWarnings({"deprecation", "removal"})
@Mod.EventBusSubscriber(modid = "genshinerc", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class ElementCryoVisuals {

    private static final ResourceLocation POWDER_SNOW_OUTLINE = new ResourceLocation("minecraft", "textures/misc/powder_snow_outline.png");
    private static final int TINT_RGB = 0x8FD8FF;

    private static final Map<Integer, Float> TINT = new HashMap<>();
    private static final Set<Integer> SEEN = new HashSet<>();
    private static float frostAlpha;
    private static long lastFrameNanos;
    private static long lastLevelMs;

    private ElementCryoVisuals() {}

    private static boolean has(int id, ElementType type) {
        EntityAuras a = ClientAuraCache.get(id);
        return a != null && a.has(type);
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) return;
        VFXConfig cfg = VFXConfig.get();
        Minecraft mc = Minecraft.getInstance();
        if (!cfg.cryoTintEnabled || mc.level == null) { TINT.clear(); return; }

        long nowMs = Util.getMillis();
        float dt = lastLevelMs == 0 ? 0f : Math.min(0.1f, (nowMs - lastLevelMs) / 1000f);
        lastLevelMs = nowMs;
        float periodMs = Math.max(100f, cfg.cryoPulseSeconds * 1000f);
        float pulse = 0.5f + 0.5f * Mth.sin((nowMs % (long) periodMs) / periodMs * Mth.TWO_PI);
        float ease = 1f - (float) Math.exp(-dt * 8f);

        float pt = event.getPartialTick();
        Vec3 cam = event.getCamera().getPosition();
        Matrix4f m = event.getPoseStack().last().pose();
        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        VertexConsumer vc = null;
        SEEN.clear();

        for (Entity entity : mc.level.entitiesForRendering()) {
            if (!(entity instanceof LivingEntity living) || !living.isAlive()) continue;
            int id = entity.getId();
            float goal = 0f;
            EntityAuras auras = ClientAuraCache.get(id);
            if (auras != null) {
                if (auras.has(ElementType.FROZEN)) goal = 1f;
                else if (auras.has(ElementType.CRYO)) goal = 0.35f * Mth.clamp(auras.gauge(ElementType.CRYO) / 3f, 0f, 1f);
            }
            float cur = TINT.getOrDefault(id, 0f);
            cur += (goal - cur) * ease;
            if (cur <= 0.004f && goal <= 0f) { TINT.remove(id); continue; }
            TINT.put(id, cur);
            SEEN.add(id);

            float alpha = cfg.cryoTintAlpha * cur * ElementReactionDim.factor(id, cfg)
                    * (cfg.cryoPulseMin + (1f - cfg.cryoPulseMin) * pulse);
            int a = (int) (Mth.clamp(alpha, 0f, 1f) * 255f);
            if (a <= 2) continue;

            float x = (float) (Mth.lerp(pt, living.xo, living.getX()) - cam.x);
            float y = (float) (Mth.lerp(pt, living.yo, living.getY()) - cam.y);
            float z = (float) (Mth.lerp(pt, living.zo, living.getZ()) - cam.z);
            float h = living.getBbWidth() * 0.5f + 0.05f;
            if (vc == null) vc = buffers.getBuffer(RenderType.debugQuads());
            box(vc, m, x - h, y - 0.02f, z - h, x + h, y + living.getBbHeight() + 0.05f, z + h,
                    (TINT_RGB >> 16) & 255, (TINT_RGB >> 8) & 255, TINT_RGB & 255, a);
        }
        TINT.keySet().retainAll(SEEN);
        if (vc != null) buffers.endBatch(RenderType.debugQuads());
    }

    private static void box(VertexConsumer vc, Matrix4f m, float x1, float y1, float z1, float x2, float y2, float z2, int r, int g, int b, int a) {
        quad(vc, m, x1, y1, z1, x2, y1, z1, x2, y2, z1, x1, y2, z1, r, g, b, a);
        quad(vc, m, x2, y1, z2, x1, y1, z2, x1, y2, z2, x2, y2, z2, r, g, b, a);
        quad(vc, m, x1, y1, z2, x1, y1, z1, x1, y2, z1, x1, y2, z2, r, g, b, a);
        quad(vc, m, x2, y1, z1, x2, y1, z2, x2, y2, z2, x2, y2, z1, r, g, b, a);
        quad(vc, m, x1, y1, z1, x2, y1, z1, x2, y1, z2, x1, y1, z2, r, g, b, a);
        quad(vc, m, x1, y2, z1, x1, y2, z2, x2, y2, z2, x2, y2, z1, r, g, b, a);
    }

    private static void quad(VertexConsumer vc, Matrix4f m, float ax, float ay, float az, float bx, float by, float bz, float cx, float cy, float cz, float dx, float dy, float dz, int r, int g, int b, int a) {
        vc.vertex(m, ax, ay, az).color(r, g, b, a).endVertex();
        vc.vertex(m, bx, by, bz).color(r, g, b, a).endVertex();
        vc.vertex(m, cx, cy, cz).color(r, g, b, a).endVertex();
        vc.vertex(m, dx, dy, dz).color(r, g, b, a).endVertex();
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.isPaused() || mc.level.getGameTime() % 2L != 0L) return;
        RandomSource rnd = mc.level.random;
        for (Entity e : mc.level.entitiesForRendering()) {
            if (!(e instanceof LivingEntity l) || !l.isAlive() || !has(e.getId(), ElementType.FROZEN)) continue;
            double x = l.getX() + (rnd.nextDouble() - 0.5) * l.getBbWidth() * 1.2;
            double y = l.getY() + rnd.nextDouble() * l.getBbHeight();
            double z = l.getZ() + (rnd.nextDouble() - 0.5) * l.getBbWidth() * 1.2;
            mc.level.addParticle(ParticleTypes.SNOWFLAKE, x, y, z, 0.0, 0.02, 0.0);
        }
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiOverlayEvent.Post event) {
        if (event.getOverlay() != VanillaGuiOverlay.HOTBAR.type()) return;
        VFXConfig cfg = VFXConfig.get();
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) { frostAlpha = 0f; return; }
        if (!cfg.frostOverlayEnabled || mc.options.hideGui) return;

        boolean frozen = has(mc.player.getId(), ElementType.FROZEN);
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

        int a = (int) (eased * 255f);
        if (a > 4) {
            g.pose().pushPose();
            g.pose().translate(w / 2f, h / 3f, 0f);
            g.pose().scale(2f, 2f, 1f);
            g.drawCenteredString(mc.font, Component.literal("Frozen"), 0, 0, (a << 24) | TINT_RGB);
            g.pose().popPose();
        }
        RenderSystem.disableBlend();
    }
}