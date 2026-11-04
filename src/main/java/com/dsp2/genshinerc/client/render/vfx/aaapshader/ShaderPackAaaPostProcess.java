package com.dsp2.genshinerc.client.render.vfx.aaapshader;

import com.guhao.vix.client.aaaeffect.AAAEffectPostProcessContext;
import com.guhao.vix.client.aaaeffect.AAAEffectPostProcessor;
import com.guhao.vix.client.compat.oculus.OculusShaderCompat;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.logging.LogUtils;
import mod.chloeprime.aaaparticles.api.client.EffectDefinition;
import mod.chloeprime.aaaparticles.api.client.EffectHolder;
import mod.chloeprime.aaaparticles.api.client.EffectRegistry;
import mod.chloeprime.aaaparticles.api.client.effekseer.EffekseerManager;
import mod.chloeprime.aaaparticles.api.client.effekseer.ParticleEmitter;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class ShaderPackAaaPostProcess {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int LAYER_DEFAULT = 0;
    private static final int LAYER_ISOLATED = 30;
    private static final int MASK_DEFAULT = 1;
    private static final int MASK_ISOLATED = 1 << 30;

    private static final Map<ResourceLocation, AAAEffectPostProcessor> ROUTES = new ConcurrentHashMap<>();
    private static final Set<AAAEffectPostProcessor> FAILED = Collections.newSetFromMap(new IdentityHashMap<>());
    private static final ThreadLocal<Captured> ACTIVE = new ThreadLocal<>();

    private static RenderTarget before;
    private static RenderTarget after;
    private static RenderTarget scene;
    private static int lastLoggedFbo = -1;

    private record Captured(EffectDefinition definition, ParticleEmitter.Type type, float partialTick) {}
    private record Source(ResourceLocation id, EffectDefinition definition) {}
    private record Group(AAAEffectPostProcessor processor, Source source, List<ParticleEmitter> emitters) {}

    private ShaderPackAaaPostProcess() {}

    public static void register(ResourceLocation effectId, AAAEffectPostProcessor processor) {
        ROUTES.put(effectId, processor);
    }

    public static void begin(EffectDefinition definition, ParticleEmitter.Type type, float partialTick) {
        ACTIVE.set(new Captured(definition, type, partialTick));
    }

    public static void end() {
        ACTIVE.remove();
    }

    public static void invalidate() {
        FAILED.clear();
        RenderTarget b = before, a = after, s = scene;
        before = null;
        after = null;
        scene = null;
        com.mojang.blaze3d.systems.RenderSystem.recordRenderCall(() -> {
            if (b != null) b.destroyBuffers();
            if (a != null) a.destroyBuffers();
            if (s != null) s.destroyBuffers();
        });
    }

    public static boolean tryDraw(EffekseerManager manager, List<ParticleEmitter> activeEmitters) {
        Captured cap = ACTIVE.get();
        if (cap == null || cap.type() != ParticleEmitter.Type.WORLD || ROUTES.isEmpty() || !VixInternals.AVAILABLE) {
            return false;
        }
        if (!OculusShaderCompat.isShaderPackActive() || OculusShaderCompat.isShadowPass()) {
            return false;
        }

        List<Source> sources = resolveSources(cap);
        if (sources.isEmpty()) return false;

        Map<ParticleEmitter, Source> owner = new IdentityHashMap<>();
        List<ParticleEmitter> discovered = new ArrayList<>();
        for (Source s : sources) {
            s.definition().emitters(cap.type()).forEach(e -> {
                if (owner.putIfAbsent(e, s) == null) discovered.add(e);
            });
        }
        List<ParticleEmitter> all = merge(activeEmitters, discovered);
        List<Group> groups = buildGroups(all, owner);
        if (groups.isEmpty()) return false;

        int[] vp = new int[4];
        GL11.glGetIntegerv(GL11.GL_VIEWPORT, vp);
        if (vp[0] != 0 || vp[1] != 0 || vp[2] <= 0 || vp[3] <= 0) return false;
        int w = vp[2];
        int h = vp[3];
        int fbo = GL11.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING);

        Object state = VixInternals.capture();
        try {
            logTarget(fbo);
            ensureTargets(w, h);
        } catch (RuntimeException ex) {
            LOGGER.error("shader-pack AAA post-process setup failed", ex);
            VixInternals.restore(state);
            return false;
        }
        VixInternals.restore(state);

        Set<ParticleEmitter> rendered = Collections.newSetFromMap(new IdentityHashMap<>());
        try {
            for (Group g : groups) {
                if (processGroup(cap, manager, all, g, fbo, state, w, h)) {
                    rendered.addAll(g.emitters());
                }
            }
            VixInternals.restore(state);
            if (hasRest(all, rendered)) {
                for (ParticleEmitter e : all) {
                    boolean original = renderable(e) && !rendered.contains(e);
                    manager.getImpl().SetLayer(e.handle, original ? LAYER_DEFAULT : LAYER_ISOLATED);
                }
                draw(manager, MASK_DEFAULT);
            }
        } finally {
            try {
                for (ParticleEmitter e : all) manager.getImpl().SetLayer(e.handle, LAYER_DEFAULT);
            } finally {
                VixInternals.restore(state);
            }
        }
        return true;
    }

    private static boolean processGroup(Captured cap, EffekseerManager manager, List<ParticleEmitter> all,
                                        Group g, int fbo, Object state, int w, int h) {
        boolean drawn = false;
        try {
            boolean any = false;
            for (ParticleEmitter e : g.emitters()) any |= renderable(e);
            if (!any) return false;

            Set<ParticleEmitter> selected = Collections.newSetFromMap(new IdentityHashMap<>());
            selected.addAll(g.emitters());
            for (ParticleEmitter e : all) {
                manager.getImpl().SetLayer(e.handle, selected.contains(e) ? LAYER_ISOLATED : LAYER_DEFAULT);
            }

            blitColor(fbo, before.frameBufferId, w, h);
            VixInternals.restore(state);
            draw(manager, MASK_ISOLATED);
            drawn = true;

            blitColor(fbo, after.frameBufferId, w, h);
            blitColor(fbo, scene.frameBufferId, w, h);
            for (ParticleEmitter e : g.emitters()) manager.getImpl().SetLayer(e.handle, LAYER_DEFAULT);

            RenderTarget isolated = VixInternals.extract(before, after);
            VixInternals.restore(state);

            AAAEffectPostProcessContext ctx = VixInternals.context(
                    g.source().id(), g.source().definition(), cap.type(), cap.partialTick(), scene,
                    List.copyOf(g.emitters()));

            Object processorState = VixInternals.capture();
            try {
                g.processor().processScene(ctx, before, after, isolated, scene);
            } finally {
                VixInternals.restore(processorState);
            }

            VixInternals.restore(state);
            blitColor(scene.frameBufferId, fbo, w, h);
            return true;
        } catch (RuntimeException ex) {
            if (FAILED.add(g.processor())) {
                LOGGER.error("shader-pack AAA post processor failed for {}", g.source().id(), ex);
            }
            VixInternals.restore(state);
            return drawn;
        }
    }

    private static void blitColor(int readFbo, int drawFbo, int w, int h) {
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        GL11.glColorMask(true, true, true, true);

        GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, readFbo);
        int prevRead = GL11.glGetInteger(GL11.GL_READ_BUFFER);
        if (readFbo != 0) GL11.glReadBuffer(GL30.GL_COLOR_ATTACHMENT0);

        GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, drawFbo);
        int[] prevDraw = null;
        if (drawFbo != 0) {
            int max = Math.min(8, GL11.glGetInteger(GL20.GL_MAX_DRAW_BUFFERS));
            prevDraw = new int[max];
            for (int i = 0; i < max; i++) prevDraw[i] = GL11.glGetInteger(GL20.GL_DRAW_BUFFER0 + i);
            GL20.glDrawBuffers(GL30.GL_COLOR_ATTACHMENT0);
        }

        GL30.glBlitFramebuffer(0, 0, w, h, 0, 0, w, h, GL11.GL_COLOR_BUFFER_BIT, GL11.GL_NEAREST);

        if (prevDraw != null) GL20.glDrawBuffers(prevDraw);
        if (readFbo != 0) {
            GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, readFbo);
            GL11.glReadBuffer(prevRead);
        }
    }

    private static void draw(EffekseerManager manager, int mask) {
        manager.getImpl().DrawBack(mask);
        manager.getImpl().DrawFront(mask);
    }

    private static void ensureTargets(int w, int h) {
        before = ensure(before, w, h);
        after = ensure(after, w, h);
        scene = ensure(scene, w, h);
    }

    private static RenderTarget ensure(RenderTarget t, int w, int h) {
        if (t == null) {
            t = new TextureTarget(w, h, false, Minecraft.ON_OSX);
            t.setClearColor(0f, 0f, 0f, 0f);
            t.setFilterMode(GL11.GL_LINEAR);
        } else if (t.width != w || t.height != h) {
            t.resize(w, h, Minecraft.ON_OSX);
            t.setFilterMode(GL11.GL_LINEAR);
        }
        return t;
    }

    private static void logTarget(int fbo) {
        if (fbo == lastLoggedFbo) return;
        lastLoggedFbo = fbo;
        int mainFbo = Minecraft.getInstance().getMainRenderTarget().frameBufferId;
        String format = "n/a";
        if (fbo != 0) {
            int type = GL30.glGetFramebufferAttachmentParameteri(GL30.GL_DRAW_FRAMEBUFFER,
                    GL30.GL_COLOR_ATTACHMENT0, GL30.GL_FRAMEBUFFER_ATTACHMENT_OBJECT_TYPE);
            if (type == GL11.GL_TEXTURE) {
                int name = GL30.glGetFramebufferAttachmentParameteri(GL30.GL_DRAW_FRAMEBUFFER,
                        GL30.GL_COLOR_ATTACHMENT0, GL30.GL_FRAMEBUFFER_ATTACHMENT_OBJECT_NAME);
                int prev = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
                GL11.glBindTexture(GL11.GL_TEXTURE_2D, name);
                format = "0x" + Integer.toHexString(
                        GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_INTERNAL_FORMAT));
                GL11.glBindTexture(GL11.GL_TEXTURE_2D, prev);
            }
        }
        LOGGER.info("AAA shader-pack pass: bound FBO={} mainTargetFBO={} isMain={} colour0 internalFormat={} (0x8058 = RGBA8)",
                fbo, mainFbo, fbo == mainFbo, format);
    }

    private static List<Source> resolveSources(Captured cap) {
        List<Source> out = new ArrayList<>();
        if (cap.definition() != null) {
            ResourceLocation id = cap.definition().getId();
            if (id != null && ROUTES.containsKey(id)) out.add(new Source(id, cap.definition()));
        } else {
            for (ResourceLocation id : ROUTES.keySet()) {
                EffectHolder holder = EffectRegistry.get(id);
                if (holder != null) holder.lazyGet().ifPresent(d -> out.add(new Source(id, d)));
            }
        }
        return out;
    }

    private static List<Group> buildGroups(List<ParticleEmitter> all, Map<ParticleEmitter, Source> owner) {
        Map<AAAEffectPostProcessor, Group> map = new IdentityHashMap<>();
        for (ParticleEmitter e : all) {
            Source s = owner.get(e);
            if (s == null || !renderable(e)) continue;
            AAAEffectPostProcessor p = ROUTES.get(s.id());
            if (p == null || !p.isEnabled() || FAILED.contains(p)) continue;
            map.computeIfAbsent(p, k -> new Group(p, s, new ArrayList<>())).emitters().add(e);
        }
        return new ArrayList<>(map.values());
    }

    private static boolean renderable(ParticleEmitter e) {
        return e.isVisible() && e.exists();
    }

    private static boolean hasRest(List<ParticleEmitter> all, Set<ParticleEmitter> rendered) {
        for (ParticleEmitter e : all) if (renderable(e) && !rendered.contains(e)) return true;
        return false;
    }

    private static List<ParticleEmitter> merge(List<ParticleEmitter> active, List<ParticleEmitter> discovered) {
        Set<ParticleEmitter> seen = Collections.newSetFromMap(new IdentityHashMap<>());
        List<ParticleEmitter> out = new ArrayList<>();
        if (active != null) for (ParticleEmitter e : active) if (e != null && seen.add(e)) out.add(e);
        for (ParticleEmitter e : discovered) if (e != null && seen.add(e)) out.add(e);
        return out;
    }
}