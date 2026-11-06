package com.dsp2.genshinerc.client.render.vfx.trail.aaap.weapons;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class AaaRotation {
    private AaaRotation() {}

    public static float[] fromForward(Vec3 forward) {
        double xz = Math.sqrt(forward.x * forward.x + forward.z * forward.z);
        double rx = -wrap(Mth.atan2(forward.y, xz) - Math.PI / 2.0);
        double ry = -wrap(Mth.atan2(forward.z, forward.x) - Math.PI / 2.0);
        return new float[] {(float) wrap(-Math.PI / 2.0 - rx), (float) wrap(ry + Math.PI)};
    }

    private static double wrap(double radians) {
        return Math.toRadians(Mth.wrapDegrees(Math.toDegrees(radians)));
    }
}