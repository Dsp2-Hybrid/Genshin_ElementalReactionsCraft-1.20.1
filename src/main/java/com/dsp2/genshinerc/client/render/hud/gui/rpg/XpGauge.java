package com.dsp2.genshinerc.client.render.hud.gui.rpg;

import com.dsp2.genshinerc.client.render.hud.gui.rpg.RpgHudElement.Box;
import com.dsp2.genshinerc.client.render.hud.gui.rpg.render.GuiQuad;
import com.dsp2.genshinerc.client.render.hud.gui.rpg.render.HudGeom;
import com.dsp2.genshinerc.client.render.hud.gui.rpg.render.RpgTextures;
import com.dsp2.genshinerc.client.render.hud.gui.rpg.render.TexScan;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;

final class XpGauge {
    private static final float SPEED = 6f, ORB_HZ = 1.2f;
    private static final int ORB_RGB = 0xC8FFC0;

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
        shown += (target - shown) * (1f - (float) Math.exp(-SPEED * dt));
        if (Math.abs(target - shown) < 1.0e-3f) shown = target;
    }

    void draw(GuiGraphics g, Box b, int rgb, float a, long now) {
        RpgTextures.Info track = RpgTextures.info(RpgHudOverlay.EXP_TRACK);
        float u0 = track.ax0() / (float) track.w(), u1 = track.ax1() / (float) track.w();
        float v0 = track.vTop(), v1 = track.vBottom();
        if (u1 - u0 <= 1.0e-6f || v1 - v0 <= 1.0e-6f) return;
        GuiQuad.blit(g, RpgHudOverlay.EXP_TRACK, b.x(), b.y(), b.x2(), b.y2(), u0, v0, u1, v1, HudGeom.argb(a, 0xFFFFFF));

        float f = shown - (float) Math.floor(shown);
        if (f <= 0.001f) return;
        RpgTextures.Info fill = RpgTextures.info(RpgHudOverlay.EXP_FILL);
        float vCut = Mth.lerp(f, fill.vBottom(), fill.vTop());
        float yCut = b.y() + b.h() * (vCut - v0) / (v1 - v0);
        float yBot = b.y() + b.h() * (fill.vBottom() - v0) / (v1 - v0);
        GuiQuad.blit(g, fill.tint(), b.x(), yCut, b.x2(), yBot, u0, vCut, u1, fill.vBottom(), HudGeom.argb(a, rgb));

        TexScan.Rows rows = TexScan.rows(RpgHudOverlay.EXP_FILL);
        if (rows.h() <= 0) return;
        float cx = rows.centerX(Mth.clamp((int) (vCut * rows.h()), 0, rows.h() - 1));
        if (cx < 0f) return;
        float ox = b.x() + b.w() * ((cx + 0.5f) / rows.w() - u0) / (u1 - u0);
        float pulse = 0.5f + 0.5f * Mth.sin(now / 1000f * ORB_HZ * Mth.TWO_PI);
        float r = b.w() * 0.18f * (0.85f + 0.3f * pulse);
        HudGeom.glow(g, ox, yCut, r, ORB_RGB, a * (0.5f + 0.35f * pulse));
        HudGeom.glow(g, ox, yCut, r * 0.4f, 0xFFFFFF, a * 0.9f);
    }
}