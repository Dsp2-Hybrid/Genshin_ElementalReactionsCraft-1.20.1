package com.dsp2.genshinerc.client.render.vfx.trail.aaap.weapons;

import net.minecraft.world.phys.Vec3;

public record BladeSegment(Vec3 base, Vec3 tip) {
    public Vec3 direction() {
        return this.tip.subtract(this.base);
    }
    public Vec3 at(double bladeFraction) {
        return this.base.lerp(this.tip, bladeFraction);
    }
}