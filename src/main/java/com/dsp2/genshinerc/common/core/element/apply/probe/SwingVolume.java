package com.dsp2.genshinerc.common.core.element.apply.probe;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import yesman.epicfight.api.utils.math.OpenMatrix4f;

public final class SwingVolume {
    private static final double MARGIN = 0.55;
    public final Vec3 worldCenter;
    public final double radius;
    private final OpenMatrix4f inverse;
    private final double cx, cy, cz, hx, hy, hz, mx, my, mz;

    public SwingVolume(OpenMatrix4f world, double cx, double cy, double cz, double hx, double hy, double hz) {
        this.cx = cx;
        this.cy = cy;
        this.cz = cz;
        this.hx = Math.abs(hx);
        this.hy = Math.abs(hy);
        this.hz = Math.abs(hz);
        this.inverse = new OpenMatrix4f(world).invert();
        Vec3 flipped = OpenMatrix4f.transform(world, new Vec3(cx, cy, cz));
        this.worldCenter = new Vec3(-flipped.x, flipped.y, -flipped.z);
        OpenMatrix4f linear = world.removeTranslation();
        double sx = Math.max(0.05, len(linear.m00, linear.m01, linear.m02));
        double sy = Math.max(0.05, len(linear.m10, linear.m11, linear.m12));
        double sz = Math.max(0.05, len(linear.m20, linear.m21, linear.m22));
        this.mx = MARGIN / sx;
        this.my = MARGIN / sy;
        this.mz = MARGIN / sz;
        this.radius = Math.sqrt(this.hx * this.hx * sx * sx + this.hy * this.hy * sy * sy + this.hz * this.hz * sz * sz) + MARGIN + 0.2;
    }

    public AABB bounds() { return new AABB(worldCenter, worldCenter).inflate(radius); }

    public boolean contains(double x, double y, double z) {
        Vec3 local = OpenMatrix4f.transform(inverse, new Vec3(-x, y, -z));
        return Math.abs(local.x - cx) <= hx + mx && Math.abs(local.y - cy) <= hy + my && Math.abs(local.z - cz) <= hz + mz;
    }

    public boolean intersects(AABB box) {
        if (contains(Mth.clamp(worldCenter.x, box.minX, box.maxX), Mth.clamp(worldCenter.y, box.minY, box.maxY), Mth.clamp(worldCenter.z, box.minZ, box.maxZ))) return true;
        return contains((box.minX + box.maxX) * 0.5, (box.minY + box.maxY) * 0.5, (box.minZ + box.maxZ) * 0.5);
    }

    private static double len(float x, float y, float z) { return Math.sqrt(x * x + y * y + z * z); }
}
