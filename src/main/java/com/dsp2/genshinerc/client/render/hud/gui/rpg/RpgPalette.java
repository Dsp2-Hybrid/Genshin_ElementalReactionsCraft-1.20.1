package com.dsp2.genshinerc.client.render.hud.gui.rpg;

import com.dsp2.genshinerc.client.render.hud.HudDraw;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

final class RpgPalette {
    private RpgPalette() {}

    static final int[] ARMOR = {0xB4B4B4, 0x8C8C8C, 0x646464, 0x505050, 0x434343, 0x353535};
    static final int[] TOUGHNESS = {0x8C8C8C, 0x646464, 0x505050, 0x434343, 0x353535, 0x000000};

    static final int HP_FULL = 0xFF2A2A, HP_LOW = 0x8C0000;
    static final int POISON_FULL = 0x8C7A2A, POISON_LOW = 0x504B00;
    static final int WITHER_FULL = 0x3A3A3A, WITHER_LOW = 0x151515;
    static final int FROZEN = 0x7FAFFF;

    static final int HUNGER = 0xB08A2E, SATURATION = 0xE8D45A;
    static final int HUNGER_DEBUFF = 0x5E9C1E, SATURATION_DEBUFF = 0x87BC00;

    static final int MOUNT = 0x90EE90;
    static final int EXH_LIGHT = 0xC8C8C8, EXH_DARK = 0x3A3A3A;

    static final float BG_DARKEN = 0.28f;

    static int hp(LivingEntity e, float frac) {
        float k = Mth.clamp((frac - 0.1f) / 0.5f, 0f, 1f);
        int base;
        if (e.hasEffect(MobEffects.WITHER)) base = HudDraw.lerpRgb(WITHER_LOW, WITHER_FULL, k);
        else if (e.hasEffect(MobEffects.POISON)) base = HudDraw.lerpRgb(POISON_LOW, POISON_FULL, k);
        else base = HudDraw.lerpRgb(HP_LOW, HP_FULL, k);
        float frozen = e.getPercentFrozen();
        return frozen > 0f ? HudDraw.lerpRgb(base, FROZEN, frozen) : base;
    }

    static int hpText(float frac) {
        if (frac >= 0.45f) return 0xFFFFFF;
        if (frac >= 0.10f) {
            int gb = (int) (255 * (frac - 0.10f) / 0.35f);
            return 0xFF0000 | (gb << 8) | gb;
        }
        return ((int) (100 + 155 * Math.max(0f, frac) / 0.10f)) << 16;
    }

    static int foodText(float food) {
        float f = Math.min(food, 20f);
        if (f >= 20f) return 0xFFFFFF;
        float k = f / 20f;
        int r = 152 + (int) (103 * k), g = 101 + (int) (154 * k), b = 74 + (int) (181 * k);
        return (r << 16) | (g << 8) | b;
    }

    static int satText(float sat) {
        float s = Math.min(sat, 20f);
        if (s <= 3f) return 0xFFFFFF;
        if (s <= 10f) return 0xFF0000 | ((130 - (int) (90 * (s - 3f) / 7f)) << 8);
        return 0xFF2800;
    }

    static int exhaustText(float exh) {
        int v = (int) (15 + 240 * (1f - Math.min(exh, 4f) / 4f));
        return (v << 16) | (v << 8) | v;
    }

    static final class Tween {
        private boolean init;
        private float r, g, b;
        private long last;

        void reset() { init = false; }

        int step(int target, long now, float speed) {
            float tr = (target >> 16) & 0xFF, tg = (target >> 8) & 0xFF, tb = target & 0xFF;
            if (!init) { init = true; r = tr; g = tg; b = tb; last = now; return rgb(); }
            float dt = Mth.clamp((now - last) / 1000f, 0f, 0.1f);
            last = now;
            float k = 1f - (float) Math.exp(-speed * dt);
            r += (tr - r) * k; g += (tg - g) * k; b += (tb - b) * k;
            return rgb();
        }

        int rgb() {
            return (Mth.clamp(Math.round(r), 0, 255) << 16) | (Mth.clamp(Math.round(g), 0, 255) << 8) | Mth.clamp(Math.round(b), 0, 255);
        }
    }
}