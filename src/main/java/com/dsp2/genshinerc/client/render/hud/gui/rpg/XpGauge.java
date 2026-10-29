package com.dsp2.genshinerc.client.render.hud.gui.rpg;

import com.dsp2.genshinerc.client.render.hud.bar.config.BarConfig;
import com.dsp2.genshinerc.client.render.hud.gui.rpg.RpgHudElement.Box;
import com.dsp2.genshinerc.client.render.hud.gui.rpg.render.GuiQuad;
import com.dsp2.genshinerc.client.render.hud.gui.rpg.render.HudGeom;
import com.dsp2.genshinerc.client.render.hud.gui.rpg.render.RpgTextures;
import com.dsp2.genshinerc.client.render.hud.gui.rpg.render.TexScan;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import org.joml.Vector4f;

final class XpGauge {
    private boolean init;
    private float shown;
    private long last;

    void reset() { init = false; }

    void update(long now, int level, float progress) {
        float target = level + Mth.clamp(progress, 0f, 0.9999f);
        if (!init) { init = true; shown = target; last = now; return; }
        float dt = Mth.clamp((now - last) / 1000f, 0f, 0.1f);
        last = now;
        shown = Mth.clamp(shown, target - 1f, target + 1f);
        shown += (target - shown) * (1f - (float) Math.exp(-6f * dt));
        if (Math.abs(target - shown) < 1.0e-3f) shown = target;
    }

    void draw(GuiGraphics g, Box box, float a, long now, BarConfig cfg) {
        Box b = box.abs();
        RpgTextures.Info shadow = RpgTextures.info(RpgHudOverlay.EXP_SHADOW);
        RpgTextures.Info fill = RpgTextures.info(RpgHudOverlay.EXP_FILL);

        float u0 = Math.min(shadow.ax0() / (float) shadow.w(), fill.ax0() / (float) fill.w());
        float u1 = Math.max(shadow.ax1() / (float) shadow.w(), fill.ax1() / (float) fill.w());
        float v0 = Math.min(shadow.vTop(), fill.vTop());
        float v1 = Math.max(shadow.vBottom(), fill.vBottom());
        if (u1 - u0 <= 1.0e-6f || v1 - v0 <= 1.0e-6f) return;

        float qu0 = b.flipX() ? u1 : u0, qu1 = b.flipX() ? u0 : u1;
        float qv0 = b.flipY() ? v1 : v0, qv1 = b.flipY() ? v0 : v1;

        GuiQuad.blit(g, RpgHudOverlay.EXP_SHADOW, b.x(), b.y(), b.x2(), b.y2(),
                qu0, qv0, qu1, qv1, HudGeom.argb(a, 0xFFFFFF));

        float f = shown - (float) Math.floor(shown);
        if (f <= 0.001f) return;

        int rgb = cfg.rpgExpColor.getRGB() & 0xFFFFFF;
        float yCut = b.y2() - b.h() * f;

        if (scissorOn(g, b.x(), yCut, b.x2(), b.y2())) {
            try {
                GuiQuad.blit(g, fill.tint(), b.x(), b.y(), b.x2(), b.y2(),
                        qu0, qv0, qu1, qv1, HudGeom.argb(a, rgb));
            } finally {
                g.disableScissor();
            }
        }

        TexScan.Rows rows = TexScan.rows(RpgHudOverlay.EXP_FILL);
        if (rows.h() <= 0 || rows.w() <= 0) return;

        float ty = 1f - f;
        float vCut = b.flipY() ? v1 - ty * (v1 - v0) : v0 + ty * (v1 - v0);
        float rf = vCut * rows.h() - 0.5f;
        int r0 = (int) Math.floor(rf);
        float t = rf - r0;
        float c0 = rows.centerX(Mth.clamp(r0, 0, rows.h() - 1));
        float c1 = rows.centerX(Mth.clamp(r0 + 1, 0, rows.h() - 1));
        float cx = c0 < 0f ? c1 : c1 < 0f ? c0 : Mth.lerp(t, c0, c1);
        if (cx < 0f) return;

        float uc = (cx + 0.5f) / rows.w();
        float tx = (uc - u0) / (u1 - u0);
        if (b.flipX()) tx = 1f - tx;
        float ox = b.x() + b.w() * tx;

        float pulse = 0.5f + 0.5f * Mth.sin(now / 1000f * cfg.rpgOrbHz * Mth.TWO_PI);
        float r = b.w() * 0.18f * (0.85f + 0.3f * pulse);
        float oa = a * (cfg.rpgOrbMinAlpha + (1f - cfg.rpgOrbMinAlpha) * pulse) * cfg.rpgOrbBrightness;

        HudGeom.glow(g, ox, yCut, r, rgb, oa);
        HudGeom.glow(g, ox, yCut, r * 0.4f, 0xFFFFFF, a * 0.9f);
    }

    private static boolean scissorOn(GuiGraphics g, float x0, float y0, float x1, float y1) {
        Matrix4f m = g.pose().last().pose();
        Vector4f p = new Vector4f(x0, y0, 0f, 1f).mul(m);
        Vector4f q = new Vector4f(x1, y1, 0f, 1f).mul(m);
        int sx0 = (int) Math.floor(Math.min(p.x, q.x)), sx1 = (int) Math.ceil(Math.max(p.x, q.x));
        int sy0 = Math.round(Math.min(p.y, q.y)), sy1 = (int) Math.ceil(Math.max(p.y, q.y));
        if (sx1 <= sx0 || sy1 <= sy0) return false;
        g.enableScissor(sx0, sy0, sx1, sy1);
        return true;
    }
}