package com.dsp2.genshinerc.client.render.vfx.manager;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

public final class EffekVisibilityManager {
    private static final Map<Integer, Boolean> CACHE = new HashMap<>();
    private static long lastStamp = Long.MIN_VALUE;

    private EffekVisibilityManager() {}

    public static boolean isVisible(Entity entity) {
        ensureFresh();
        return CACHE.computeIfAbsent(entity.getId(), id -> compute(entity));
    }

    private static boolean compute(Entity entity) {
        Minecraft mc = Minecraft.getInstance();

        if (mc.player == null || mc.player.isSpectator()) return true;
        if (!(entity instanceof LivingEntity living)) return true;

        if (living.distanceToSqr(mc.getCameraEntity()) <= 25.0D) {
            return true;
        }

        Camera camera = mc.gameRenderer.getMainCamera();
        if (!camera.isInitialized()) {
            return true;
        }

        Vec3 camPos = camera.getPosition();
        Vec3 targetPos = new Vec3(living.getX(), living.getY() + living.getBbHeight() * 0.5, living.getZ());

        ClipContext context = new ClipContext(camPos, targetPos, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, mc.player);
        BlockHitResult hitResult = living.level().clip(context);

        return hitResult.getType() != HitResult.Type.BLOCK;
    }

    private static void ensureFresh() {
        Minecraft mc = Minecraft.getInstance();
        long stamp = mc.level == null ? Long.MIN_VALUE : mc.level.getGameTime();
        if (stamp != lastStamp) {
            lastStamp = stamp;
            CACHE.clear();
        }
    }

    public static void clear() {
        CACHE.clear();
        lastStamp = Long.MIN_VALUE;
    }
}