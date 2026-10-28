package com.dsp2.genshinerc.client.render.hud.gui.rpg;

import com.dsp2.genshinerc.client.render.hud.HudDraw;
import com.dsp2.genshinerc.client.render.hud.bar.config.BarConfig;
import com.dsp2.genshinerc.client.render.hud.gui.rpg.RpgHudElement.Box;
import com.dsp2.genshinerc.client.render.hud.gui.rpg.anim.BarTrack;
import com.dsp2.genshinerc.client.render.hud.gui.rpg.anim.NumberFx;
import com.dsp2.genshinerc.client.render.hud.gui.rpg.anim.OxygenAnim;
import com.dsp2.genshinerc.client.render.hud.gui.rpg.anim.PointAnim;
import com.dsp2.genshinerc.client.render.hud.gui.rpg.anim.TextAnim;
import com.dsp2.genshinerc.client.render.hud.gui.rpg.render.HudGeom;
import com.dsp2.genshinerc.client.render.hud.gui.rpg.render.HudMirror;
import com.dsp2.genshinerc.client.render.hud.gui.rpg.render.Strip;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.food.FoodProperties;

import static com.dsp2.genshinerc.client.render.hud.gui.rpg.RpgHudOverlay.*;

final class RpgStatBars {
    private static final float LAYER = 20f, FOOD_MAX = 20f, EXH_MAX = 4f, ROW_FADE_S = 0.2f;
    private static final float PREVIEW_ALPHA = 0.6f, EXH_BG_ALPHA = 0.55f, FOOD_COLOR_SPEED = 8f;
    private static final int PREVIEW_RGB = 0x7CFF6A, EXH_BG = 0x101010;

    private static final PointAnim ARMOR = new PointAnim(PointAnim.Style.LAYERED, 700L),
            TOUGH = new PointAnim(PointAnim.Style.LAYERED, 700L),
            FOOD = new PointAnim(PointAnim.Style.CLASSIC, 700L),
            SAT = new PointAnim(PointAnim.Style.PLAIN, 900L),
            EXH = new PointAnim(PointAnim.Style.PLAIN, 300L);
    private static final NumberFx N_FOOD = new NumberFx(500L, 200L, 300L), N_SAT = new NumberFx(500L, 200L, 300L);
    private static final TextAnim T_ARMOR = new TextAnim(), T_TOUGH = new TextAnim(), T_MOUNT = new TextAnim(), T_AIR = new TextAnim();
    private static final BarTrack B_MOUNT = new BarTrack();
    private static final Fade F_MOUNT = new Fade(), F_AIR = new Fade();
    private static final RpgPalette.Tween C_FOOD = new RpgPalette.Tween(), C_SAT = new RpgPalette.Tween();
    private static OxygenAnim oxygen = new OxygenAnim();
    private static int mountId = -1;
    private static float mountMax = 1f;

    private RpgStatBars() {}

    static void reset() {
        ARMOR.reset(); TOUGH.reset(); FOOD.reset(); SAT.reset(); EXH.reset();
        N_FOOD.reset(); N_SAT.reset();
        T_ARMOR.reset(); T_TOUGH.reset(); T_MOUNT.reset(); T_AIR.reset();
        B_MOUNT.reset(); C_FOOD.reset(); C_SAT.reset();
        F_MOUNT.v = 0f; F_AIR.v = 0f;
        oxygen = new OxygenAnim();
        mountId = -1;
    }

    static void tick(Snap s, long now, BarConfig cfg) {
        ARMOR.update(now, s.armor(), LAYER);
        TOUGH.update(now, s.toughness(), LAYER);
        FOOD.update(now, s.food(), FOOD_MAX);
        SAT.update(now, s.sat(), FOOD_MAX);
        EXH.update(now, Mth.clamp(s.exhaustion(), 0f, EXH_MAX), EXH_MAX);
        N_FOOD.set(s.food(), now); N_FOOD.update(now);
        N_SAT.set(s.sat(), now); N_SAT.update(now);
        C_FOOD.step(s.hungerEffect() ? RpgPalette.HUNGER_DEBUFF : RpgPalette.HUNGER, now, FOOD_COLOR_SPEED);
        C_SAT.step(s.hungerEffect() ? RpgPalette.SATURATION_DEBUFF : RpgPalette.SATURATION, now, FOOD_COLOR_SPEED);
        T_ARMOR.set(s.armor(), now);
        T_TOUGH.set(s.toughness(), now);

        if (s.mount() != null) {
            if (s.mount().getId() != mountId) { B_MOUNT.reset(); T_MOUNT.reset(); mountId = s.mount().getId(); }
            mountMax = s.mount().getMaxHealth();
            B_MOUNT.update(now, s.mount().getHealth(), 0f, mountMax, cfg, false, false);
            T_MOUNT.set(s.mount().getHealth(), now);
        }
        F_MOUNT.update(now, s.mount() != null, ROW_FADE_S);

        float air = s.maxAir() > 0 ? Math.max(0, s.air()) / (float) s.maxAir() : 1f;
        oxygen.update(now, air, cfg);
        F_AIR.update(now, oxygen.visible(s.underwater()), ROW_FADE_S);
        T_AIR.set(Math.round(air * 100f), now);

        T_ARMOR.update(now, cfg.rpgCountSpeed);
        T_TOUGH.update(now, cfg.rpgCountSpeed);
        T_MOUNT.update(now, cfg.rpgCountSpeed);
        T_AIR.update(now, cfg.rpgCountSpeed);
    }

    static void draw(GuiGraphics g, Snap s, long now, BarConfig cfg, float a, boolean preview) {
        if (preview || s.armor() > 0 || ARMOR.busy())
            layered(g, RpgHudElement.ARMOR.get(cfg), ARMOR, RpgPalette.ARMOR, Math.round(T_ARMOR.shown()), now, a);
        if (preview || s.toughness() > 0 || TOUGH.busy())
            layered(g, RpgHudElement.TOUGHNESS.get(cfg), TOUGH, RpgPalette.TOUGHNESS, Math.round(T_TOUGH.shown()), now, a);
        hunger(g, s, RpgHudElement.HUNGER.get(cfg), now, cfg, a);
        exhaustion(g, RpgHudElement.EXHAUSTION.get(cfg), a);

        HudMirror.draw(g, RpgHudElement.ARMOR_ICON.get(cfg),
                b -> HudGeom.icon(g, HudGeom.ICONS, 43, 9, b, argb(a, WHITE)));
        HudMirror.draw(g, RpgHudElement.TOUGHNESS_ICON.get(cfg),
                b -> HudGeom.icon(g, HudGeom.ICONS, 43, 9, b, argb(a, 0xAAAAAA)));
        HudMirror.draw(g, RpgHudElement.HUNGER_ICON.get(cfg),
                b -> HudGeom.icon(g, HudGeom.ICONS, 52, 27, b, argb(a, WHITE)));

        drawMount(g, cfg, now, preview ? a : a * F_MOUNT.v);
        drawOxygen(g, cfg, preview ? a : a * F_AIR.v);
    }

    private static void layered(GuiGraphics g, Box signed, PointAnim anim, int[] colors, int number, long now, float a) {
        Box b = signed.abs();
        Strip st = Strip.of(BAR_STAT);
        int cap = colors.length;
        float v = Mth.clamp(anim.shown(), 0f, cap * LAYER);
        int full = (int) Math.floor(v / LAYER + 1.0e-4f);
        float rem = Math.max(0f, v - full * LAYER);
        if (full >= cap) { full = cap; rem = 0f; }

        st.background(g, b, HudDraw.scaleRgb(colors[0], RpgPalette.BG_DARKEN), a * BAR_BG_ALPHA);
        if (full > 0) st.seg(g, st.i.tint(), b, 0f, 1f, argb(a, colors[full - 1]));
        if (rem > 0.001f) st.seg(g, st.i.tint(), b, 0f, rem / LAYER, argb(a, colors[full]));

        int cur = rem > 0.001f ? full : full - 1;
        if (cur >= 0 && cur < cap) {
            float base = cur * LAYER;
            anim.emitWhite(now, (lo, hi, wa) -> {
                float s0 = Math.max(lo, base), s1 = Math.min(hi, base + LAYER);
                if (s1 > s0) st.seg(g, st.i.white(), b, (s0 - base) / LAYER, (s1 - base) / LAYER, argb(a * wa * 0.4f, WHITE));
            });
        }
        st.outline(g, b, a * OUTLINE_ALPHA);
        label(g, signed, Component.literal(String.valueOf(number)), 1f, a);
    }

    private static void hunger(GuiGraphics g, Snap s, Box signed, long now, BarConfig cfg, float a) {
        int fc = C_FOOD.rgb(), sc = C_SAT.rgb();
        FoodProperties held = s.held();
        float pulse = held == null ? 0f : 0.5f - 0.5f * Mth.cos(now / 1000f * cfg.rpgFoodPreviewHz * Mth.TWO_PI);
        int pf = held == null ? s.food() : Math.min(20, s.food() + held.getNutrition());
        float psat = held == null ? s.sat() : Math.min(pf, s.sat() + held.getNutrition() * held.getSaturationModifier() * 2f);

        Box b = signed.abs();
        Strip st = Strip.of(BAR_STAT);
        float food = FOOD.shown() / FOOD_MAX, sat = SAT.shown() / FOOD_MAX;
        st.background(g, b, HudDraw.scaleRgb(fc, RpgPalette.BG_DARKEN), a * BAR_BG_ALPHA);
        if (held != null) st.seg(g, st.i.tint(), b, food, pf / FOOD_MAX, argb(a * pulse * PREVIEW_ALPHA, fc));
        st.seg(g, st.i.tint(), b, 0f, food, argb(a, fc));
        FOOD.emitWhite(now, (lo, hi, wa) -> st.seg(g, st.i.white(), b, lo / FOOD_MAX, hi / FOOD_MAX, argb(a * wa * 0.4f, WHITE)));
        if (held != null) st.seg(g, st.i.tint(), b, sat, psat / FOOD_MAX, argb(a * pulse * PREVIEW_ALPHA, sc));
        st.seg(g, st.i.tint(), b, 0f, sat, argb(a, sc));
        st.outline(g, b, a * OUTLINE_ALPHA);

        float ta = a * Math.max(N_FOOD.alpha(), N_SAT.alpha());
        float pop = Math.max(N_FOOD.scale(now), N_SAT.scale(now));
        Component cur = foodLabel(N_FOOD.shown(), N_SAT.shown(), s.exhaustion());
        if (held == null) { label(g, signed, cur, pop, ta); return; }
        label(g, signed, cur, pop, ta * (1f - pulse));
        label(g, signed, colored(oneDecimal(pf) + "+" + oneDecimal(psat), PREVIEW_RGB), pop, ta * pulse);
    }

    private static Component foodLabel(float food, float sat, float exh) {
        return Component.empty()
                .append(colored(oneDecimal(food), RpgPalette.foodText(food)))
                .append(colored("+", RpgPalette.exhaustText(exh)))
                .append(colored(oneDecimal(sat), RpgPalette.satText(sat)));
    }

    private static void exhaustion(GuiGraphics g, Box signed, float a) {
        float f = Mth.clamp(EXH.shown() / EXH_MAX, 0f, 1f);
        HudMirror.draw(g, signed, b -> {
            HudGeom.rect(g, b.x(), b.y(), b.x2(), b.y2(), argb(a * EXH_BG_ALPHA, EXH_BG));
            HudGeom.rect(g, b.x(), b.y(), b.x() + b.w() * f, b.y2(),
                    argb(a, HudDraw.lerpRgb(RpgPalette.EXH_LIGHT, RpgPalette.EXH_DARK, f)));
        });
    }

    private static void drawMount(GuiGraphics g, BarConfig cfg, long now, float a) {
        if (a <= 0.01f) return;
        Box bs = RpgHudElement.MOUNT.get(cfg);
        HudMirror.draw(g, bs, b -> drawBar(g, BAR_MOUNT, b, B_MOUNT, RpgPalette.MOUNT, false, now, cfg, a));
        label(g, bs, Component.literal(ceil(T_MOUNT.shown()) + "/" + ceil(mountMax)), 1f, a);
        HudMirror.draw(g, RpgHudElement.MOUNT_ICON.get(cfg), b -> {
            HudGeom.icon(g, HudGeom.ICONS, 52, 9, b, argb(a, WHITE));
            HudGeom.icon(g, HudGeom.ICONS, 88, 9, b, argb(a, WHITE));
        });
    }

    private static void drawOxygen(GuiGraphics g, BarConfig cfg, float a) {
        if (a <= 0.01f) return;
        Box bs = RpgHudElement.OXYGEN.get(cfg);
        HudMirror.draw(g, bs, b -> {
            Strip st = Strip.of(BAR_OXYGEN);
            st.full(g, st.i.core(), b, argb(a * BAR_BG_ALPHA, HudDraw.scaleRgb(WHITE, RpgPalette.BG_DARKEN)));
            st.seg(g, st.i.core(), b, oxygen.bar(), oxygen.gainTop(), argb(a * 0.35f, WHITE));
            st.seg(g, st.i.core(), b, 0f, oxygen.bar(), argb(a, WHITE));
            if (oxygen.ghostAlpha() > 0.01f)
                st.seg(g, st.i.white(), b, oxygen.bar(), oxygen.ghostTop(), argb(a * oxygen.ghostAlpha() * 0.4f, WHITE));
            st.outline(g, b, a * OUTLINE_ALPHA);
            float ic = Math.max(5f, b.h() * 1.6f);
            HudGeom.icon(g, HudGeom.ICONS, 16, 18, new Box(b.x() - ic - 1f, b.cy() - ic / 2f, ic, ic), argb(a, WHITE));
        });
        label(g, bs, Component.literal(Math.round(T_AIR.shown()) + "/100"), 1f, a);
    }
}