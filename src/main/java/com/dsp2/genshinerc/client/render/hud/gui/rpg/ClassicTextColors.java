package com.dsp2.genshinerc.client.render.hud.gui.rpg;

public final class ClassicTextColors {
    private ClassicTextColors() {}

    public static int health(double current, double max) {
        double p = max <= 0 ? 0 : current / max;
        if (p >= 0.45) return 0xFFFFFF;
        if (p >= 0.10) {
            int gb = (int) (255 * (p - 0.10) / 0.35);
            return (255 << 16) | (gb << 8) | gb;
        }
        return ((int) (100 + 155 * (p / 0.10))) << 16;
    }

    public static int hunger(double hunger) {
        double f = Math.min(hunger, 20.0) / 20.0;
        if (f >= 1.0) return 0xFFFFFF;
        int r = 152 + (int) (103 * f), g = 101 + (int) (154 * f), b = 74 + (int) (181 * f);
        return (r << 16) | (g << 8) | b;
    }

    public static int saturation(double sat) {
        double s = Math.min(sat, 20.0);
        if (s <= 3.0) return 0xFFFFFF;
        if (s <= 10.0) return (255 << 16) | ((130 - (int) (90 * (s - 3.0) / 7.0)) << 8);
        return 0xFF2800;
    }

    public static int exhaustion(double exhaustion) {
        int v = (int) (15 + 240 * (1 - Math.min(exhaustion, 4.0) / 4.0));
        return (v << 16) | (v << 8) | v;
    }
}