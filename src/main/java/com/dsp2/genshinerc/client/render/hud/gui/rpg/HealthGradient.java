package com.dsp2.genshinerc.client.render.hud.gui.rpg;

import com.dsp2.genshinerc.client.render.hud.HudDraw;
import net.minecraft.util.Mth;

public final class HealthGradient {
    private static final float[] STOPS  = {0f,       0.25f,    0.5f,     0.75f,    1f};
    private static final int[]   COLORS = {0x6E0A0A, 0xE53935, 0xFF9800, 0xFFE03B, 0x4CC152};

    private HealthGradient() {}

    public static int sample(float fraction) {
        float f = Mth.clamp(fraction, 0f, 1f);
        for (int i = 1; i < STOPS.length; i++) {
            if (f <= STOPS[i]) {
                float t = (f - STOPS[i - 1]) / (STOPS[i] - STOPS[i - 1]);
                return HudDraw.lerpRgb(COLORS[i - 1], COLORS[i], t);
            }
        }
        return COLORS[COLORS.length - 1];
    }
}