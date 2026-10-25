package com.dsp2.genshinerc.client.render.bar.zhenmie;

import com.dsp2.genshinerc.client.render.bar.ClientCombatBars;
import com.dsp2.genshinerc.client.render.bar.config.BarConfig;
import com.dsp2.genshinerc.client.render.bar.layout.BarAnchor;
import com.dsp2.genshinerc.client.render.bar.layout.BarTextureInfo;
import com.dsp2.genshinerc.client.render.bar.layout.BarTextures;
import com.dsp2.genshinerc.client.render.bar.layout.SlicedBar;
import com.dsp2.genshinerc.client.render.hud.HudDraw;
import net.minecraft.Util;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Matrix4f;

public final class ZhenMieBarRenderer {
    private static final float CLASH_OVERLAY_ALPHA = 0.85f;
    private static final float CRUSH_FLICKER_HZ = 10f;
    private static final long CRUSH_HUE_PERIOD_MS = 600L;
    private static final long KNOCKDOWN_SWAP_MS = 90L;
    private static final float MARKER_WIDTH = 0.6f;

    private ZhenMieBarRenderer() {}

    public static void render(LivingEntity entity, Matrix4f m, MultiBufferSource buffers,
                              BarConfig cfg, BarAnchor anchor, float alpha) {
        ClientZhenMieState s = ClientCombatBars.zhenMie(entity.getId());
        if (s == null) return;
        long now = Util.getMillis();
        s.update(now, cfg);
        if (s.isIdle(now, cfg)) {
            ClientCombatBars.removeZhenMie(entity.getId());
            return;
        }

        float w = anchor.width() * cfg.subBarWidth / 100f;
        float h = anchor.height() * cfg.zhenMieBarHeight / 100f;
        float vx1 = anchor.centerX() - w / 2f, vx2 = vx1 + w;
        float vy2 = anchor.bottom() - anchor.height() * cfg.subBarGap / 100f, vy1 = vy2 - h;

        BarTextureInfo frame = BarTextureInfo.get(BarTextures.FRAME);
        BarTextureInfo fill = BarTextureInfo.get(BarTextures.FILL);
        float[] q = frame.quadForVisible(vx1, vy1, vx2, vy2);
        float z = anchor.z();

        SlicedBar.draw(m, buffers, BarTextures.FRAME, frame, q, q[0], q[2], z, 0xFFFFFF, alpha);

        float l = fill.visibleLeft(q), r = fill.visibleRight(q), span = r - l;

        int zhenBase = cfg.zhenColor.getRGB() & 0xFFFFFF;
        int mieBase = cfg.mieColor.getRGB() & 0xFFFFFF;
        int zc = s.zhenBuff ? cycle(zhenBase, cfg.zhenBuffColor.getRGB() & 0xFFFFFF, cfg.zhenMieBuffCycleHz, now) : zhenBase;
        int mc = s.mieBuff ? cycle(mieBase, cfg.mieBuffColor.getRGB() & 0xFFFFFF, cfg.zhenMieBuffCycleHz, now) : mieBase;

        float zm = Mth.clamp(s.zhen.main(), 0f, 1f), zt = Mth.clamp(s.zhen.trail(), 0f, 1f);
        float mm = Mth.clamp(s.mie.main(), 0f, 1f), mt = Mth.clamp(s.mie.trail(), 0f, 1f);

        if (zt > zm) SlicedBar.draw(m, buffers, BarTextures.FILL, fill, q, l, l + span * zt, z, 0xFFFFFF, alpha);
        if (zm > 0f) SlicedBar.draw(m, buffers, BarTextures.FILL, fill, q, l, l + span * zm, z,
                HudDraw.lerpRgb(zc, 0xFFFFFF, s.zhen.whiteness(now, cfg.subBarFlashTime)), alpha);

        if (mt > mm) SlicedBar.draw(m, buffers, BarTextures.FILL, fill, q, r - span * mt, r, z, 0xFFFFFF, alpha);
        if (mm > 0f) SlicedBar.draw(m, buffers, BarTextures.FILL, fill, q, r - span * mm, r, z,
                HudDraw.lerpRgb(mc, 0xFFFFFF, s.mie.whiteness(now, cfg.subBarFlashTime)), alpha);

        if (s.clash != null) drawClash(m, buffers, s, fill, q, l, r, vy1, vy2, z, zhenBase, mieBase, alpha, now);
    }

    private static void drawClash(Matrix4f m, MultiBufferSource buffers, ClientZhenMieState s, BarTextureInfo fill,
                                  float[] q, float l, float r, float vy1, float vy2, float z,
                                  int zhenBase, int mieBase, float alpha, long now) {
        float t = (now - s.clashStartMs) / (s.clash.visualSeconds() * 1000f);
        float fade = 1f - Mth.clamp(t, 0f, 1f);
        int color;
        float a = 1f;
        switch (s.clash) {
            case CRUSH -> {
                color = Mth.hsvToRgb((now % CRUSH_HUE_PERIOD_MS) / (float) CRUSH_HUE_PERIOD_MS, 0.85f, 1f);
                a = 0.5f + 0.5f * Mth.cos((now / 1000f) * CRUSH_FLICKER_HZ * Mth.TWO_PI);
            }
            case KNOCKDOWN -> color = (now / KNOCKDOWN_SWAP_MS) % 2 == 0 ? zhenBase : mieBase;
            default -> color = 0xFFFFFF;
        }
        SlicedBar.draw(m, buffers, BarTextures.FILL, fill, q, l, r, z, color, alpha * fade * a * CLASH_OVERLAY_ALPHA);

        float x = Mth.lerp(s.clashPoint, l, r);
        float half = (vy2 - vy1) * MARKER_WIDTH / 2f;
        HudDraw.rect(m, buffers, x - half, vy1, x + half, vy2, z, 0xFFFFFF, alpha * fade);
    }

    private static int cycle(int a, int b, float hz, long now) {
        float wave = 0.5f - 0.5f * Mth.cos((now / 1000f) * hz * Mth.TWO_PI);
        return HudDraw.lerpRgb(a, b, wave);
    }
}