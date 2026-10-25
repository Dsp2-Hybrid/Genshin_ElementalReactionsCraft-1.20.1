package com.dsp2.genshinerc.client.render.hud.bar.element;

import com.dsp2.genshinerc.client.render.hud.bar.config.BarConfig;
import com.dsp2.genshinerc.client.render.world.component.damageindicator.config.DamageIndicatorConfig;
import com.dsp2.genshinerc.client.render.hud.HudDraw;
import com.dsp2.genshinerc.client.render.hud.WhiteMaskTextures;
import com.dsp2.genshinerc.common.core.element.ElementGauge;
import com.dsp2.genshinerc.common.core.element.ElementType;
import net.minecraft.Util;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Matrix4f;

import java.util.List;

public final class ElementHudRenderer {
    private ElementHudRenderer() {}

    public static void render(LivingEntity entity, Matrix4f matrix, MultiBufferSource buffers,
                              BarConfig cfg, float hudX, float hudY, float globalAlpha) {
        EntityAuras auras = ClientAuraCache.get(entity.getId());
        if (auras == null) return;

        long nowMs = Util.getMillis();
        double nowTicks = ClientAura.nowTicks();
        List<ClientAura> visible = auras.layout(nowMs, nowTicks, cfg);
        if (auras.isEmpty()) {
            ClientAuraCache.remove(entity.getId());
            return;
        }
        if (visible.isEmpty()) return;

        float box = (cfg.elementBoxSize / 100f) * cfg.elementScale;
        float step = box + (cfg.elementSpacing / 100f) * cfg.elementScale;
        float rowCenterX = hudX + cfg.elementOffsetX / 100f;
        float centerY = hudY + cfg.elementOffsetY / 100f + box / 2f;
        float centerSlot = (auras.layoutCount() - 1f) / 2f;

        for (ClientAura aura : visible) {
            float cx = rowCenterX + (aura.slot() - centerSlot) * step;
            drawAura(matrix, buffers, cfg, aura, cx, centerY, box, globalAlpha, nowTicks, nowMs);
        }
    }

    private static void drawAura(Matrix4f m, MultiBufferSource buffers, BarConfig cfg, ClientAura aura,
                                 float cx, float cy, float box, float globalAlpha, double nowTicks, long nowMs) {
        float gauge = aura.display(nowTicks, nowMs);
        float white = aura.whiteness(nowMs, cfg);
        float alpha = globalAlpha * aura.opacity(nowTicks, nowMs, gauge, cfg);
        if (alpha <= 0.004f) return;

        float z = cfg.elementOffsetZ;
        float half = box / 2f;
        int color = elementColor(aura.type());

        float barW = box * cfg.elementGaugeWidth / 100f;
        float barH = box * cfg.elementGaugeHeight / 100f;
        float barBottom = cy + half + box * cfg.elementGaugeGap / 100f;
        float bx1 = cx - barW / 2f;
        HudDraw.rect(m, buffers, bx1, barBottom, bx1 + barW, barBottom + barH, z,
                HudDraw.scaleRgb(color, cfg.elementGaugeBgBrightness), alpha * cfg.elementGaugeBgAlpha * 0.85f);
        float fill = Mth.clamp(gauge / ElementGauge.MAX_GAUGE, 0f, 1f);
        if (fill > 0.001f) {
            HudDraw.rect(m, buffers, bx1, barBottom, bx1 + barW * fill, barBottom + barH, z,
                    HudDraw.lerpRgb(color, 0xFFFFFF, white), alpha * 0.7f);
        }

        ResourceLocation icon = ElementTextures.currentFrame(aura.type(), nowMs);
        HudDraw.quad(m, buffers, icon, cx - half, cy - half, cx + half, cy + half, 0, 0, 1, 1, z, 0xFFFFFF, alpha);
        if (white > 0.004f) {
            ResourceLocation mask = WhiteMaskTextures.get(icon);
            if (mask != null) {
                HudDraw.quad(m, buffers, mask, cx - half, cy - half, cx + half, cy + half, 0, 0, 1, 1, z, 0xFFFFFF, alpha * white);
            }
        }
    }

    public static int elementColor(ElementType type) {
        return BarConfig.toAwt(DamageIndicatorConfig.get().colorFor(type.colorKey())).getRGB() & 0xFFFFFF;
    }
}