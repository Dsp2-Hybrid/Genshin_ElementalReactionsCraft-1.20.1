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
    private static final float CRUSH_FLICKER_HZ = 10f;
    private static final long CRUSH_HUE_PERIOD_MS = 600L;
    private static final long KNOCKDOWN_SWAP_MS = 90L;
    private static final float MARKER_WIDTH = 0.6f;

    private static final float MIE_RIGHT_OFFSET = 0.003f;
    private static final float MIE_TOP_OFFSET = 0.10f;
    private static final float MIE_BOTTOM_OFFSET = 0.088f;

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

        WhiteOverlay zhOverlay = ZHEN_OVERLAYS.computeIfAbsent(entity.getId(), k -> new WhiteOverlay());
        WhiteOverlay miOverlay = MIE_OVERLAYS.computeIfAbsent(entity.getId(), k -> new WhiteOverlay());
        float zhLast = ZHEN_LASTS.getOrDefault(entity.getId(), zm);
        float miLast = MIE_LASTS.getOrDefault(entity.getId(), mm);

        if (Math.abs(zm - zhLast) > 0.001f) {
            zhOverlay.trigger(zhLast, zm, now);
        }
        if (Math.abs(mm - miLast) > 0.001f) {
            miOverlay.trigger(miLast, mm, now);
        }
        ZHEN_LASTS.put(entity.getId(), zm);
        MIE_LASTS.put(entity.getId(), mm);

        zhOverlay.update(now, zm, cfg);
        miOverlay.update(now, mm, cfg);

        if (zm > 0f) SlicedBar.draw(m, buffers, BarTextures.FILL, fill, q, l, l + span * zm, z,
                HudDraw.lerpRgb(zc, 0xFFFFFF, s.zhen.whiteness(now, cfg.subBarFlashTime)), alpha);

        float rMie = r + MIE_RIGHT_OFFSET;
        float topMie = topY - MIE_TOP_OFFSET;
        float bottomMie = bottomY + MIE_BOTTOM_OFFSET;

        if (mm > 0f) {
            int c = HudDraw.lerpRgb(mc, 0xFFFFFF, s.mie.whiteness(now, cfg.subBarFlashTime));
            HudDraw.quad(m, buffers, BarTextures.FILL, rMie - span * mm, bottomMie, rMie, topMie,
                    uRight - uSpan * mm, 0f, uRight, 1f, z, c, alpha);
        }

        zhOverlay.emit((lo, hi, wa) -> {
            HudDraw.quad(m, buffers, BarTextures.FILL, l + span * lo, bottomY, l + span * hi, topY,
                    uLeft + uSpan * lo, 0f, uLeft + uSpan * hi, 1f, z, 0xFFFFFF, alpha * wa);
        });

        miOverlay.emit((lo, hi, wa) -> {
            HudDraw.quad(m, buffers, BarTextures.FILL, rMie - span * hi, bottomMie, rMie - span * lo, topMie,
                    uRight - uSpan * hi, 0f, uRight - uSpan * lo, 1f, z, 0xFFFFFF, alpha * wa);
        });

        if (s.clash != null) {
            drawClash(m, buffers, s, fill, l, rMie, bottomMie, topMie, z, zhenBase, mieBase, uLeft, uRight, alpha, now, mm, span);
        }
    }

    private static void drawClash(Matrix4f m, MultiBufferSource buffers, ClientZhenMieState s, BarTextureInfo fill,
                                  float l, float r, float bottomY, float topY, float z,
                                  int zhenBase, int mieBase, float uLeft, float uRight, float alpha, long now, float mm, float span) {
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

        float rightX = (s.clash == com.dsp2.genshinerc.common.core.combat.zhenmie.ClashResult.KNOCKDOWN) ? r - span * mm : r;

        HudDraw.quad(m, buffers, BarTextures.FILL, l, bottomY, rightX, topY, uLeft, 0f, uRight, 1f, z, color, alpha * fade * a * CLASH_OVERLAY_ALPHA);

        float x = Mth.lerp(s.clashPoint, l, r);
        float half = (topY - bottomY) * MARKER_WIDTH / 2f;
        HudDraw.rect(m, buffers, x - half, bottomY, x + half, topY, z, 0xFFFFFF, alpha * fade);
    }

    private static int cycle(int a, int b, float hz, long now) {
        float wave = 0.5f - 0.5f * Mth.cos((now / 1000f) * hz * Mth.TWO_PI);
        return HudDraw.lerpRgb(a, b, wave);
    }
}