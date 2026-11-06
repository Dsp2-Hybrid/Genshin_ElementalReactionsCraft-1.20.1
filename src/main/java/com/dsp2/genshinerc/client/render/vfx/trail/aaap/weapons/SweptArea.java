package com.dsp2.genshinerc.client.render.vfx.trail.aaap.weapons;

import net.minecraft.world.phys.Vec3;

public final class SweptArea {
    private SweptArea() {}

    public static double area(BladeSegment prev, BladeSegment cur) {
        return 0.5 * (triangle2x(prev.base(), prev.tip(), cur.base()) + triangle2x(prev.tip(), cur.tip(), cur.base()));
    }

    public static Vec3 point(BladeSegment prev, BladeSegment cur, double bladeFraction, double timeFraction) {
        return prev.at(bladeFraction).lerp(cur.at(bladeFraction), timeFraction);
    }

    private static double triangle2x(Vec3 a, Vec3 b, Vec3 c) {
        return b.subtract(a).cross(c.subtract(a)).length();
    }
}