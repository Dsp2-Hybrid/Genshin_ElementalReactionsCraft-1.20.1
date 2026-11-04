package com.dsp2.genshinerc.client.render.vfx.aaapshader;

import com.guhao.vix.client.aaaeffect.AAAEffectPostProcessContext;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.logging.LogUtils;
import mod.chloeprime.aaaparticles.api.client.EffectDefinition;
import mod.chloeprime.aaaparticles.api.client.effekseer.ParticleEmitter;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

final class VixInternals {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String PKG = "com.guhao.vix.client.aaaeffect.";
    private static final Method CAPTURE;
    private static final Method RESTORE;
    private static final Method EXTRACT;
    private static final Constructor<?> CONTEXT;
    static final boolean AVAILABLE;

    static {
        Method capture = null, restore = null, extract = null;
        Constructor<?> context = null;
        try {
            Class<?> stateClass = Class.forName(PKG + "AAAEffectFullscreenPass$RenderState");
            capture = stateClass.getDeclaredMethod("capture");
            restore = stateClass.getDeclaredMethod("restore");
            extract = Class.forName(PKG + "AAAEffectSceneDifference")
                    .getDeclaredMethod("extract", RenderTarget.class, RenderTarget.class);
            context = AAAEffectPostProcessContext.class.getDeclaredConstructor(
                    ResourceLocation.class, EffectDefinition.class, ParticleEmitter.Type.class,
                    float.class, RenderTarget.class, List.class);
            capture.setAccessible(true);
            restore.setAccessible(true);
            extract.setAccessible(true);
            context.setAccessible(true);
        } catch (ReflectiveOperationException | RuntimeException e) {
            LOGGER.error("VIX internals not found; shader-pack AAA post-process disabled", e);
            capture = null;
        }
        CAPTURE = capture;
        RESTORE = restore;
        EXTRACT = extract;
        CONTEXT = context;
        AVAILABLE = capture != null;
    }

    private VixInternals() {}

    static Object capture() {
        return invoke(CAPTURE, null);
    }

    static void restore(Object state) {
        invoke(RESTORE, state);
    }

    static RenderTarget extract(RenderTarget beforeTarget, RenderTarget afterTarget) {
        return (RenderTarget) invoke(EXTRACT, null, beforeTarget, afterTarget);
    }

    static AAAEffectPostProcessContext context(ResourceLocation id, EffectDefinition def, ParticleEmitter.Type type,
                                               float partial, RenderTarget scene, List<ParticleEmitter> emitters) {
        try {
            return (AAAEffectPostProcessContext) CONTEXT.newInstance(id, def, type, partial, scene, emitters);
        } catch (InvocationTargetException e) {
            throw unwrap(e);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    private static Object invoke(Method m, Object target, Object... args) {
        try {
            return m.invoke(target, args);
        } catch (InvocationTargetException e) {
            throw unwrap(e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    private static RuntimeException unwrap(InvocationTargetException e) {
        Throwable c = e.getCause();
        return c instanceof RuntimeException re ? re : new IllegalStateException(c);
    }
}