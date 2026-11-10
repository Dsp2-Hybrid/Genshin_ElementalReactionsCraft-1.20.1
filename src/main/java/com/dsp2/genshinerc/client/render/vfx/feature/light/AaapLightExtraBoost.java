package com.dsp2.genshinerc.client.render.vfx.feature.light;

public class AaapLightExtraBoost {
    public static float instantIntensity = 0.7f;
    public static float instantRadius = 1.25f;
    public static float trailExtraIntensity = 0.7f;
    public static float trailRadius = 0.9f;

    public static float getIntensityMultiplier(boolean isTrail) {
        return isTrail ? (instantIntensity * trailExtraIntensity) : instantIntensity;
    }

    public static float getRadiusMultiplier(boolean isTrail) {
        return isTrail ? trailRadius : instantRadius;
    }
}