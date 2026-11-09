package com.dsp2.genshinerc.client.render.vfx.feature.light;

import com.dsp2.genshinerc.client.render.vfx.config.VFXConfig;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.guhao.vix.client.compat.oculus.OculusShaderCompat;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EffectInstance;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class ElementLightRenderer {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int MAX_LIGHTS = 16;
    private static final String SHADER = "genshinerc:aaa_particle/light/element_light";

    private static final Matrix4f VIEW = new Matrix4f();
    private static final Matrix4f PROJ = new Matrix4f();
    private static Vec3 camera = Vec3.ZERO;
    private static boolean captured;
    private static boolean failed;
    private static EffectInstance shader;
    private static TextureTarget snapshot;

    private record ViewLight(float x, float y, float z, float radius, float r, float g, float b,
                             int[] box, float importance) {}

    private ElementLightRenderer() {}

    public static void capture(PoseStack pose, Camera cam, Matrix4f projection) {
        VIEW.set(pose.last().pose());
        PROJ.set(projection);
        camera = cam.getPosition();
        captured = true;
    }

    public static void invalidate() {
        EffectInstance old = shader;
        shader = null;
        failed = false;
        if (old != null) RenderSystem.recordRenderCall(old::close);
    }

        public static boolean isActive() {
        return OculusShaderCompat.isShaderPackActive() && !OculusShaderCompat.isShadowPass();
    }

    public static void apply() {
        if (!captured || failed) return;
        captured = false;
        if (!isActive()) return;
        VFXConfig cfg = VFXConfig.get();
        if (!cfg.elementShellEnabled || !cfg.elementLightEnabled) return;

        Minecraft mc = Minecraft.getInstance();
        RenderTarget main = mc.getMainRenderTarget();
        int w = main.width, h = main.height;
        List<ViewLight> lights = collect(w, h);
        if (lights.isEmpty()) return;

        int[] vp = new int[4];
        GL11.glGetIntegerv(GL11.GL_VIEWPORT, vp);
        try {
            ensureSnapshot(main, w, h);
            if (shader == null) shader = new EffectInstance(mc.getResourceManager(), SHADER);

            GlStateManager._glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, main.frameBufferId);
            GlStateManager._glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, snapshot.frameBufferId);
            GlStateManager._glBlitFrameBuffer(0, 0, w, h, 0, 0, w, h,
                    GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT, GL11.GL_NEAREST);
            GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);

            main.bindWrite(false);
            RenderSystem.viewport(0, 0, w, h);
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.disableCull();
            RenderSystem.colorMask(true, true, true, false);

            shader.setSampler("DepthSampler", snapshot::getDepthTextureId);
            shader.setSampler("ColorSampler", snapshot::getColorTextureId);
            shader.safeGetUniform("ProjMat").set(new Matrix4f().setOrtho(0f, w, 0f, h, 0.1f, 1000f));
            shader.safeGetUniform("OutSize").set((float) w, (float) h);
            shader.safeGetUniform("IProjMat").set(new Matrix4f(PROJ).invert());
            try {
                for (ViewLight l : lights) {
                    RenderSystem.enableScissor(l.box()[0], l.box()[1], l.box()[2], l.box()[3]);
                    shader.safeGetUniform("LightPos").set(l.x(), l.y(), l.z(), l.radius());
                    shader.safeGetUniform("LightColor").set(l.r(), l.g(), l.b(), 0f);
                    shader.apply();
                    drawQuad(w, h);
                }
            } finally {
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
        } catch (IOException | RuntimeException ex) {
            failed = true;
            LOGGER.error("[genshinerc] aura light pass disabled after error", ex);
        }
    }

    private static List<ViewLight> collect(int w, int h) {
        VFXConfig cfg = VFXConfig.get();
        List<ViewLight> out = new ArrayList<>();
        Vector3f p = new Vector3f();
        for (ElementLightRegistry.Light l : ElementLightRegistry.all()) {
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
        return out.size() > MAX_LIGHTS ? new ArrayList<>(out.subList(0, MAX_LIGHTS)) : out;
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

    private static void ensureSnapshot(RenderTarget main, int w, int h) {
        if (snapshot == null) {
            snapshot = new TextureTarget(w, h, true, Minecraft.ON_OSX);
            snapshot.setFilterMode(GL11.GL_NEAREST);
        } else if (snapshot.width != w || snapshot.height != h) {
            snapshot.resize(w, h, Minecraft.ON_OSX);
        }
        if (main.isStencilEnabled() && !snapshot.isStencilEnabled()) snapshot.enableStencil();
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