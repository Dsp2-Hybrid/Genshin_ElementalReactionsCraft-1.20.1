package com.dsp2.genshinerc.client.render.vfx.trail.aaap.weapons;

import javax.annotation.Nullable;
import mod.chloeprime.aaaparticles.api.client.effekseer.ParticleEmitter;

final class SwingTracker {
    int lastTickCount = Integer.MIN_VALUE;
    float lastPartial;
    @Nullable
    BladeSegment lastSegment;
    float spawnBudget;
    int spawnedThisFrame;
    long lastSeenFrame;
    boolean followRequested;
    int swingId;
    @Nullable
    ParticleEmitter follow;

    @Nullable
    com.dsp2.genshinerc.client.render.vfx.feature.light.AaapTransientLights.Handle light;

    void endSwing() {
        this.swingId++;
        this.lastSegment = null;
        this.lastTickCount = Integer.MIN_VALUE;
        this.spawnBudget = 0.0F;
        this.followRequested = false;
        if (this.follow != null) {
            this.follow.stop();
            this.follow = null;
        }
        
        if (this.light != null) {
            this.light.release(0.25f);
            this.light = null;
        }
    }
}