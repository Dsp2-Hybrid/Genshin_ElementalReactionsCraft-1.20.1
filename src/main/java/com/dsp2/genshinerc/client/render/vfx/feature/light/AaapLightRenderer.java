package com.dsp2.genshinerc.client.render.vfx.feature.light;

import com.dsp2.genshinerc.client.render.vfx.config.VFXConfig;
import com.guhao.vix.client.compat.oculus.OculusShaderCompat;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EffectInstance;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL30;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class AaapLightRenderer {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String SHADER = "genshinerc:aaa_particle/light/element_light";
    private static final int RETRY_COOLDOWN_FRAMES = 300;
    private static final int MAX_LOGGED_FAILURES = 3;

    private static final Matrix4f VIEW = new Matrix4f();
    private static final Matrix4f PROJ = new Matrix4f();
    private static Vec3 camera = Vec3.ZERO;
    private static boolean captured;
    private static int failures;
    private static int cooldownFrames;
    private static EffectInstance shader;
    private static TextureTarget snapshot;
    private static int lightFbo = -1;

    private record ViewLight(float x, float y, float z, float radius, float r, float g, float b,
                             int[] box, float importance) {}

    private AaapLightRenderer() {}

    public static boolean isActive() {
        return OculusShaderCompat.isShaderPackActive() && !OculusShaderCompat.isShadowPass();
    }

    public static void capture(PoseStack pose, Camera cam, Matrix4f projection) {
        VIEW.set(pose.last().pose());
        PROJ.set(projection);
        camera = cam.getPosition();
        captured = true;
    }

    public static void invalidate() {
        EffectInstance old = shader;
        shader = null;
        failures = 0;
        cooldownFrames = 0;
        if (old != null) RenderSystem.recordRenderCall(old::close);
    }

    public static void apply() {
        AaapTransientLights.tick();
        if (!captured) return;
        captured = false;
        if (cooldownFrames > 0) { cooldownFrames--; return; }
        if (!isActive()) return;

        VFXConfig cfg = VFXConfig.get();
        if (!cfg.aaaLightEnabled) return;

        Minecraft mc = Minecraft.getInstance();
        RenderTarget main = mc.getMainRenderTarget();
        int w = main.width, h = main.height;
        
        int[] vp = new int[4];
        GL11.glGetIntegerv(GL11.GL_VIEWPORT, vp);
        boolean touchedGl = false;
        Throwable error = null;
        try {
            List<ViewLight> lights = collect(w, h);
            if (lights.isEmpty()) return;

            ensureSnapshot(w, h);
            if (shader == null) shader = new EffectInstance(mc.getResourceManager(), SHADER);
            touchedGl = true;

            GlStateManager._glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, main.frameBufferId);
            GlStateManager._glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, snapshot.frameBufferId);
            GlStateManager._glBlitFrameBuffer(0, 0, w, h, 0, 0, w, h, GL11.GL_COLOR_BUFFER_BIT, GL11.GL_NEAREST);

            if (!bindLightTarget(main)) throw new IllegalStateException("light FBO incomplete");

            RenderSystem.viewport(0, 0, w, h);
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.disableCull();
            RenderSystem.colorMask(true, true, true, false);
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GL11.GL_ONE, GL11.GL_ONE);

            shader.setSampler("DepthSampler", main::getDepthTextureId);
            shader.setSampler("ColorSampler", snapshot::getColorTextureId);
            shader.safeGetUniform("ProjMat").set(new Matrix4f().setOrtho(0f, w, 0f, h, 0.1f, 1000f));
            shader.safeGetUniform("OutSize").set((float) w, (float) h);
            shader.safeGetUniform("IProjMat").set(new Matrix4f(PROJ).invert());

            for (ViewLight l : lights) {
                RenderSystem.enableScissor(l.box()[0], l.box()[1], l.box()[2], l.box()[3]);
                shader.safeGetUniform("LightPos").set(l.x(), l.y(), l.z(), l.radius());
                shader.safeGetUniform("LightColor").set(l.r(), l.g(), l.b(), 0f);
                shader.apply();
                RenderSystem.enableBlend();
                RenderSystem.blendEquation(GL14.GL_FUNC_ADD);
                RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
                drawQuad(w, h);
            }
            failures = 0;
        } catch (IOException | RuntimeException ex) {
            error = ex;
        } finally {
            if (touchedGl) {
                shader.clear();
                RenderSystem.disableScissor();
                RenderSystem.colorMask(true, true, true, true);
                RenderSystem.depthMask(true);
                RenderSystem.enableDepthTest();
                RenderSystem.enableCull();
                RenderSystem.defaultBlendFunc();
                RenderSystem.disableBlend();
                main.bindWrite(false);
                RenderSystem.viewport(vp[0], vp[1], vp[2], vp[3]);
            }
        }
        if (error != null) onFailure(error);
    }

    private static void onFailure(Throwable ex) {
        failures++;
        cooldownFrames = RETRY_COOLDOWN_FRAMES;
        EffectInstance old = shader;
        shader = null;
        if (old != null) RenderSystem.recordRenderCall(old::close);
        if (failures <= MAX_LOGGED_FAILURES) {
            LOGGER.error("[genshinerc] aura light pass failed (failure #{}), retrying in {} frames",
                    failures, RETRY_COOLDOWN_FRAMES, ex);
        }
    }

    private static boolean bindLightTarget(RenderTarget main) {
        if (lightFbo < 0) lightFbo = GlStateManager.glGenFramebuffers();
        GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, lightFbo);
        GlStateManager._glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, main.getColorTextureId(), 0);
        return GlStateManager.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER) == GL30.GL_FRAMEBUFFER_COMPLETE;
    }

    private static void ensureSnapshot(int w, int h) {
        if (snapshot == null) {
            snapshot = new TextureTarget(w, h, false, Minecraft.ON_OSX);
            snapshot.setFilterMode(GL11.GL_NEAREST);
        } else if (snapshot.width != w || snapshot.height != h) {
            snapshot.resize(w, h, Minecraft.ON_OSX);
        }
    }

    private static List<ViewLight> collect(int w, int h) {
        VFXConfig cfg = VFXConfig.get();
        List<ViewLight> out = new ArrayList<>();
        Vector3f p = new Vector3f();
        for (AaapLightRegistry.Light l : AaapLightRegistry.all()) {
            float intensity = l.intensity();
            if (!(intensity > 0.01f) || !(l.radius() > 0.01f)) continue;
            double dx = l.x() - camera.x, dy = l.y() - camera.y, dz = l.z() - camera.z;
            VIEW.transformPosition((float) dx, (float) dy, (float) dz, p);
            int[] box = screenBox(p, l.radius(), w, h);
            if (box == null) continue;
            float imp = intensity * l.radius() * l.radius() / (float) Math.max(dx * dx + dy * dy + dz * dz, 1.0);
            out.add(new ViewLight(p.x, p.y, p.z, l.radius(), l.r() * intensity, l.g() * intensity,
                    l.b() * intensity, box, imp));
        }
        out.sort(Comparator.comparingDouble((ViewLight v) -> v.importance()).reversed());
        return out.size() > cfg.aaaLightMaxCount ? new ArrayList<>(out.subList(0, cfg.aaaLightMaxCount)) : out;
    }

    private static int[] screenBox(Vector3f p, float r, int w, int h) {
        if (-p.z + r <= 0.05f) return null;
        float near = -p.z - r;
        if (near <= 0.1f) return new int[]{0, 0, w, h};
        float cx = PROJ.m00() * p.x / -p.z;
        float cy = PROJ.m11() * p.y / -p.z;
        float ex = Math.abs(PROJ.m00()) * r / near;
        float ey = Math.abs(PROJ.m11()) * r / near;
        int x0 = Math.max(0, (int) Math.floor((cx - ex) * 0.5f * w + 0.5f * w) - 2);
        int x1 = Math.min(w, (int) Math.ceil((cx + ex) * 0.5f * w + 0.5f * w) + 2);
        int y0 = Math.max(0, (int) Math.floor((cy - ey) * 0.5f * h + 0.5f * h) - 2);
        int y1 = Math.min(h, (int) Math.ceil((cy + ey) * 0.5f * h + 0.5f * h) + 2);
        if (x1 <= x0 || y1 <= y0) return null;
        return new int[]{x0, y0, x1 - x0, y1 - y0};
    }

    private static void drawQuad(int w, int h) {
        BufferBuilder b = Tesselator.getInstance().getBuilder();
        b.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        b.vertex(0.0, 0.0, 500.0).endVertex();
        b.vertex(w, 0.0, 500.0).endVertex();
        b.vertex(w, h, 500.0).endVertex();
        b.vertex(0.0, h, 500.0).endVertex();
        BufferUploader.draw(b.end());
    }
}