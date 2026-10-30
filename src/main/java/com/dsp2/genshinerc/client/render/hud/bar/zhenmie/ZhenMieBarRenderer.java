package com.dsp2.genshinerc.client.render.hud.bar.zhenmie;

import com.dsp2.genshinerc.client.render.hud.bar.ClientCombatBars;
import com.dsp2.genshinerc.client.render.hud.bar.config.BarConfig;
import com.dsp2.genshinerc.client.render.hud.bar.layout.BarAnchor;
import com.dsp2.genshinerc.client.render.hud.bar.layout.BarTextureInfo;
import com.dsp2.genshinerc.client.render.hud.bar.layout.BarTextures;
import com.dsp2.genshinerc.client.render.hud.bar.layout.SlicedBar;
import com.dsp2.genshinerc.client.render.hud.HudDraw;
import com.dsp2.genshinerc.client.render.hud.gui.rpg.anim.WhiteOverlay;
import net.minecraft.Util;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Matrix4f;

import java.util.HashMap;
import java.util.Map;

public final class ZhenMieBarRenderer {
    private static final float CLASH_OVERLAY_ALPHA = 0.85f;
    private static final long KNOCKDOWN_SWAP_MS = 90L;
    private static final float MARKER_WIDTH = 0.6f;

    private static final float MIE_RIGHT_OFFSET = 0.003f;
    private static final float MIE_TOP_OFFSET = 0.10f;
    private static final float MIE_BOTTOM_OFFSET = 0.088f;
    private static final float SWEEP_WIDTH = 0.18f;
    private static final float SWEEP_ALPHA = 0.45f;

    private static final Map<Integer, WhiteOverlay> ZHEN_OVERLAYS = new HashMap<>();
    private static final Map<Integer, WhiteOverlay> MIE_OVERLAYS = new HashMap<>();
    private static final Map<Integer, Float> ZHEN_LASTS = new HashMap<>();
    private static final Map<Integer, Float> MIE_LASTS = new HashMap<>();

    private ZhenMieBarRenderer() {}

    public static void render(LivingEntity entity, Matrix4f m, MultiBufferSource buffers,
                              BarConfig cfg, BarAnchor anchor, float alpha) {
        float w = anchor.width() * cfg.subBarWidth / 100f;
        float h = anchor.height() * cfg.zhenMieBarHeight / 100f;
        float vx1 = anchor.centerX() - w / 2f, vx2 = vx1 + w;
        float vy2 = anchor.bottom() - anchor.height() * cfg.subBarGap / 100f, vy1 = vy2 - h;

        BarTextureInfo frame = BarTextureInfo.get(BarTextures.FRAME);
        BarTextureInfo fill = BarTextureInfo.get(BarTextures.FILL);
        float[] q = frame.quadForVisible(vx1, vy1, vx2, vy2);
        float z = anchor.z();

        SlicedBar.draw(m, buffers, BarTextures.FRAME, frame, q, q[0], q[2], z, 0xFFFFFF, alpha);

        ClientZhenMieState s = ClientCombatBars.zhenMie(entity.getId());
        if (s == null) return;

        long now = Util.getMillis();
        s.update(now, cfg);

        float l = frame.visibleLeft(q);
        float r = frame.visibleRight(q);
        float span = r - l;
        float topY = frame.visibleTop(q);
        float bottomY = frame.visibleBottom(q);

        float uLeft = fill.left / (float) fill.width;
        float uRight = fill.right / (float) fill.width;
        float uSpan = uRight - uLeft;

        int zhenBase = cfg.zhenColor.getRGB() & 0xFFFFFF;
        int mieBase = cfg.mieColor.getRGB() & 0xFFFFFF;
        int zc = s.zhenBuff ? cycle(zhenBase, cfg.zhenBuffColor.getRGB() & 0xFFFFFF, cfg.zhenMieBuffCycleHz, now) : zhenBase;
        int mc = s.mieBuff ? cycle(mieBase, cfg.mieBuffColor.getRGB() & 0xFFFFFF, cfg.zhenMieBuffCycleHz, now) : mieBase;

        float zm = Mth.clamp(s.zhen.main(), 0f, 1f);
        float mm = Mth.clamp(s.mie.main(), 0f, 1f);

        float zt = Mth.clamp(s.zhenTarget(), 0f, 1f);
        float mt = Mth.clamp(s.mieTarget(), 0f, 1f);

        WhiteOverlay zhOverlay = ZHEN_OVERLAYS.computeIfAbsent(entity.getId(), k -> new WhiteOverlay());
        WhiteOverlay miOverlay = MIE_OVERLAYS.computeIfAbsent(entity.getId(), k -> new WhiteOverlay());
        float zhLast = ZHEN_LASTS.getOrDefault(entity.getId(), 0f);
        float miLast = MIE_LASTS.getOrDefault(entity.getId(), 0f);

        boolean isDecaying = s.decaying();
        if (Math.abs(zt - zhLast) > 0.001f && !isDecaying) zhOverlay.trigger(zhLast, zt, now);
        if (Math.abs(mt - miLast) > 0.001f && !isDecaying) miOverlay.trigger(miLast, mt, now);
        ZHEN_LASTS.put(entity.getId(), zt);
        MIE_LASTS.put(entity.getId(), mt);

        zhOverlay.update(now, zm, cfg);
        miOverlay.update(now, mm, cfg);

        int bgColor = 0xFF404040;
        int zBase = HudDraw.lerpRgb(zc, 0xFFFFFF, s.zhen.whiteness(now, cfg.subBarFlashTime));
        int mBase = HudDraw.lerpRgb(mc, 0xFFFFFF, s.mie.whiteness(now, cfg.subBarFlashTime));

        int zFinal = HudDraw.lerpRgb(bgColor, applyBlend(bgColor, zBase, cfg.zhenMieBlendMode), cfg.zhenMieBlendStrength);
        int mFinal = HudDraw.lerpRgb(bgColor, applyBlend(bgColor, mBase, cfg.zhenMieBlendMode), cfg.zhenMieBlendStrength);

        if (zm > 0f) SlicedBar.draw(m, buffers, BarTextures.FILL, fill, q, l, l + span * zm, z, zFinal, alpha);

        float rMie = r + MIE_RIGHT_OFFSET;
        float topMie = topY - MIE_TOP_OFFSET;
        float bottomMie = bottomY + MIE_BOTTOM_OFFSET;

        if (mm > 0f) {
            HudDraw.quad(m, buffers, BarTextures.FILL, rMie - span * mm, bottomMie, rMie, topMie,
                    uRight - uSpan * mm, 0f, uRight, 1f, z, mFinal, alpha);
        }

        if (s.clash != null) {
            float t = (now - s.clashStartMs) / (s.clash.visualSeconds() * 1000f);
            float fade = 1f - Mth.clamp(t, 0f, 1f);
            int color;
            float ca = 1f;
            switch (s.clash) {
                case CRUSH -> {
                    float periodMs = 1000f / cfg.zhenMieClashHueHz;
                    color = Mth.hsvToRgb((now % (long)periodMs) / periodMs, 0.85f, 1f);
                    ca = 0.5f + 0.5f * Mth.cos((now / 1000f) * cfg.zhenMieClashHueHz * Mth.TWO_PI);
                }
                case KNOCKDOWN -> color = (now / KNOCKDOWN_SWAP_MS) % 2 == 0 ? zhenBase : mieBase;
                default -> color = 0xFFFFFF;
            }

            int clashFinal = HudDraw.lerpRgb(bgColor, applyBlend(bgColor, color, cfg.zhenMieBlendMode), cfg.zhenMieBlendStrength);

            if (zm > 0.001f) {
                HudDraw.quad(m, buffers, BarTextures.FILL, l, bottomY, l + span * zm, topY, uLeft, 0f, uLeft + uSpan * zm, 1f, z, clashFinal, alpha * fade * ca * CLASH_OVERLAY_ALPHA);
            }
            if (mm > 0.001f) {
                HudDraw.quad(m, buffers, BarTextures.FILL, rMie - span * mm, bottomMie, rMie, topMie, uRight - uSpan * mm, 0f, uRight, 1f, z, clashFinal, alpha * fade * ca * CLASH_OVERLAY_ALPHA);
            }

            float x = Mth.lerp(s.clashPoint, l, r);
            float half = (topY - bottomY) * MARKER_WIDTH / 2f;
            HudDraw.rect(m, buffers, x - half, bottomY, x + half, topY, z, 0xFFFFFF, alpha * fade);
        } else {
            zhOverlay.emit((lo, hi, wa) -> {
                HudDraw.quad(m, buffers, BarTextures.FILL, l + span * lo, bottomY, l + span * hi, topY,
                        uLeft + uSpan * lo, 0f, uLeft + uSpan * hi, 1f, z, 0xFFFFFF, alpha * wa);
            });

            miOverlay.emit((lo, hi, wa) -> {
                HudDraw.quad(m, buffers, BarTextures.FILL, rMie - span * hi, bottomMie, rMie - span * lo, topMie,
                        uRight - uSpan * hi, 0f, uRight - uSpan * lo, 1f, z, 0xFFFFFF, alpha * wa);
            });

            if (mm > 0.01f) {
                int breathColor = cfg.zhenMieBreathColor.getRGB() & 0xFFFFFF;
                sweep(m, buffers, fill, l, span, uLeft, uSpan, bottomMie, topMie, z, 1f - mm, 1f, now, alpha, breathColor, cfg);
            }
        }
    }

    private static void sweep(Matrix4f m, MultiBufferSource buffers, BarTextureInfo fill, float l, float span, float uLeft, float uSpan, float bottom, float top, float z, float loN, float hiN, long now, float a, int color, BarConfig cfg) {
        if (hiN - loN <= 0.02f) return;
        float t = (now / 1000f * cfg.zhenMieBreathHz) % 1f;
        float c = Mth.lerp(t, loN - SWEEP_WIDTH, hiN + SWEEP_WIDTH);
        int steps = 6;
        for (int i = 0; i < steps; i++) {
            float s0 = c - SWEEP_WIDTH + 2f * SWEEP_WIDTH * i / steps;
            float s1 = c - SWEEP_WIDTH + 2f * SWEEP_WIDTH * (i + 1) / steps;
            float w = 1f - Math.abs((s0 + s1) / 2f - c) / SWEEP_WIDTH;
            float drawL = Math.max(loN, s0), drawR = Math.min(hiN, s1);
            if (drawR > drawL) {
                float x1 = l + span * drawL;
                float x2 = l + span * drawR;
                float u1 = uLeft + uSpan * drawL;
                float u2 = uLeft + uSpan * drawR;
                HudDraw.quad(m, buffers, BarTextures.FILL, x1, bottom, x2, top, u1, 0f, u2, 1f, z, color, a * SWEEP_ALPHA * w);
            }
        }
    }

    private static int applyBlend(int base, int blend, BarConfig.ZhenMieBlendMode mode) {
        if (mode == BarConfig.ZhenMieBlendMode.NORMAL) return blend;
        int br = (base >> 16) & 0xFF, bg = (base >> 8) & 0xFF, bb = base & 0xFF;
        int lr = (blend >> 16) & 0xFF, lg = (blend >> 8) & 0xFF, lb = blend & 0xFF;
        int r, g, b;
        switch (mode) {
            case MULTIPLY -> { r = br * lr / 255; g = bg * lg / 255; b = bb * lb / 255; }
            case SCREEN -> { r = 255 - (255 - br) * (255 - lr) / 255; g = 255 - (255 - bg) * (255 - lg) / 255; b = 255 - (255 - bb) * (255 - lb) / 255; }
            case ADD -> { r = Math.min(255, br + lr); g = Math.min(255, bg + lg); b = Math.min(255, bb + lb); }
            case OVERLAY -> {
                r = br < 128 ? (2 * br * lr / 255) : (255 - 2 * (255 - br) * (255 - lr) / 255);
                g = bg < 128 ? (2 * bg * lg / 255) : (255 - 2 * (255 - bg) * (255 - lg) / 255);
                b = bb < 128 ? (2 * bb * lb / 255) : (255 - 2 * (255 - bb) * (255 - lb) / 255);
            }
            default -> { r = lr; g = lg; b = lb; }
        }
        return (r << 16) | (g << 8) | b;
    }

    private static int cycle(int a, int b, float hz, long now) {
        float wave = 0.5f - 0.5f * Mth.cos((now / 1000f) * hz * Mth.TWO_PI);
        return HudDraw.lerpRgb(a, b, wave);
    }
}