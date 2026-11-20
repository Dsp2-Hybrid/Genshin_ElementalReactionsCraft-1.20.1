package com.dsp2.genshinerc.client.render.vfx.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import mod.chloeprime.aaaparticles.api.client.effekseer.ParticleEmitter;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class AaaEntityOcclusion {
    private static final Map<ParticleEmitter, Vec3> POSITIONS = new WeakHashMap<>();
    private static final List<ParticleEmitter> HIDDEN = new ArrayList<>();

    private AaaEntityOcclusion() {}

    public static void position(ParticleEmitter emitter, float x, float y, float z) {
        if (emitter.type != ParticleEmitter.Type.WORLD
                || !Float.isFinite(x) || !Float.isFinite(y) || !Float.isFinite(z)) return;
        synchronized (POSITIONS) {
            POSITIONS.put(emitter, new Vec3(x, y, z));
        }
    }

    public static void beforeDraw(List<?> active) {
        afterDraw();
        Minecraft mc = Minecraft.getInstance();
        var camera = mc.gameRenderer.getMainCamera();
        if (active == null || mc.level == null || mc.player == null || !camera.isInitialized()) return;
        Vec3 eye = camera.getPosition();
        float partial = mc.getFrameTime();
        for (Object item : active) {
            if (!(item instanceof ParticleEmitter emitter) || emitter.type != ParticleEmitter.Type.WORLD
                    || !emitter.exists() || !emitter.isVisible()) continue;
            Vec3 target;
            synchronized (POSITIONS) {
                target = POSITIONS.get(emitter);
            }
            if (target == null || eye.distanceToSqr(target) < 0.01) continue;
            var nearby = mc.level.getEntitiesOfClass(LivingEntity.class, new AABB(eye, target).inflate(0.8),
                    e -> e.isAlive() && !e.isSpectator() && !e.isInvisibleTo(mc.player)
                            && (e != mc.getCameraEntity() || !mc.options.getCameraType().isFirstPerson()));
            for (LivingEntity blocker : nearby) {
                AABB box = blocker.getBoundingBox().move(blocker.getPosition(partial).subtract(blocker.position()));
                if (box.contains(eye) || box.inflate(0.08).contains(target)) continue;
                if (box.clip(eye, target).isPresent()) {
                    HIDDEN.add(emitter);
                    emitter.setVisibility(false);
                    break;
                }
            }
        }
    }

    public static void afterDraw() {
        try {
            for (ParticleEmitter emitter : HIDDEN) if (emitter.exists()) emitter.setVisibility(true);
        } finally {
            HIDDEN.clear();
        }
    }
}
