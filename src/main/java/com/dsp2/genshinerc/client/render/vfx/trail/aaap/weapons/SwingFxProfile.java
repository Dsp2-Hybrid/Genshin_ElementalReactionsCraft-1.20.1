package com.dsp2.genshinerc.client.render.vfx.trail.aaap.weapons;

import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import yesman.epicfight.api.utils.math.Vec3f;

public record SwingFxProfile(
        @Nullable ResourceLocation sweptEffect,
        @Nullable ResourceLocation followEffect,
        Vec3f baseOffset,
        Vec3f tipOffset,
        float sweptDensity,
        int maxSweptPerFrame,
        float sweptScale,
        float followScale,
        float followAnchor,
        float minTipStep,
        int subSamples) {
}