package com.dsp2.genshinerc.client.render.hud.bar;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class LineOfSightChecker {

    private LineOfSightChecker() {}

    public static boolean isOccluded(Minecraft mc, LivingEntity entity) {
        if (entity.distanceToSqr(mc.getCameraEntity()) <= 25.0D) {
            return false;
        }

        Camera camera = mc.gameRenderer.getMainCamera();
        if (!camera.isInitialized()) {
            return false;
        }

        Vec3 camPos = camera.getPosition();
        Vec3 targetPos = new Vec3(entity.getX(), entity.getY() + entity.getBbHeight() * 0.5, entity.getZ());

        ClipContext context = new ClipContext(camPos, targetPos, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, mc.player);
        BlockHitResult hitResult = entity.level().clip(context);

        return hitResult.getType() == HitResult.Type.BLOCK;
    }
}