package com.dsp2.genshinerc.client.render.hud.bar.element;

import com.dsp2.genshinerc.client.render.hud.bar.config.BarConfig;
import com.dsp2.genshinerc.client.render.vfx.config.VFXConfig;
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
import net.minecraft.world.entity.player.Player;
import org.joml.Matrix4f;

import java.util.List;

public final class ElementHudRenderer {
    private ElementHudRenderer() {}

    public static void render(LivingEntity entity, Matrix4f matrix, MultiBufferSource buffers,
                              BarConfig cfg, float hudX, float hudY, float globalAlpha) {

        VFXConfig vfx = VFXConfig.get();
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

        boolean isPlayer = entity instanceof Player;
        int baseElementY = isPlayer ? (int)cfg.playerElementOffsetY : cfg.elementOffsetY;
        int gaugeYOffset = 0;

        float box = (cfg.elementBoxSize / 100f) * cfg.elementScale;
        float step = box + (cfg.elementSpacing / 100f) * cfg.elementScale;
        float rowCenterX = hudX + cfg.elementOffsetX / 100f;
        float centerY = hudY + baseElementY / 100f  + box / 2f;
        float centerSlot = (auras.layoutCount() - 1f) / 2f;

        for (ClientAura aura : visible) {
            float cx = rowCenterX + (aura.slot() - centerSlot) * step;
            drawAura(matrix, buffers, cfg, vfx, aura, cx, centerY, box, globalAlpha, nowTicks, nowMs, gaugeYOffset);
        }
    }

    private static void drawAura(Matrix4f m, MultiBufferSource buffers, BarConfig cfg, VFXConfig vfx, ClientAura aura,
                                 float cx, float cy, float box, float globalAlpha, double nowTicks, long nowMs, int gaugeYOffset) {
        float gauge = aura.display(nowTicks, nowMs);
        float white = aura.whiteness(nowMs, cfg);
        float alpha = globalAlpha * aura.opacity(nowTicks, nowMs, gauge, cfg, vfx);

        boolean middleAura = aura.type() == ElementType.FROZEN
                || aura.type() == ElementType.QUICKEN
                || aura.type() == ElementType.BURNING;
        if (middleAura) {
            alpha = globalAlpha * (gauge >= 1f ? 1f : 0.8f + 0.2f * Mth.clamp(gauge, 0f, 1f));
        }

        if (aura.type() == ElementType.QUICKEN) {
            white = Math.max(white, (float) (0.5 + 0.5 * Math.sin(nowTicks * 0.36)));
        }
        if (alpha <= 0.004f) return;

        float z = cfg.elementOffsetZ;
        float half = box / 2f;
        int color = elementColor(aura.type());

        float barW = box * cfg.elementGaugeWidth / 100f;
        float barH = box * cfg.elementGaugeHeight / 100f;
        float barBottom = cy + half + box * cfg.elementGaugeGap / 100f + gaugeYOffset / 100f;
        float bx1 = cx - barW / 2f;
        HudDraw.rect(m, buffers, bx1, barBottom, bx1 + barW, barBottom + barH, z,
                HudDraw.scaleRgb(color, cfg.elementGaugeBgBrightness), alpha * cfg.elementGaugeBgAlpha * 0.85f);
        float fill = Mth.clamp(gauge / ElementGauge.MAX_GAUGE, 0f, 1f);
        if (fill > 0.001f) {
            if (middleAura) {
                int segments = 16;
                float width = barW * fill / segments;
                float phase = (float) (nowTicks * 0.36);
                for (int i = 0; i < segments; i++) {
                    float t = (i + 0.5f) / segments;
                    int animatedColor = middleAuraColor(aura.type(), t + phase);
                    int finalColor = HudDraw.lerpRgb(animatedColor, 0xFFFFFF, white);
                    float x0 = bx1 + width * i;
                    float x1 = i == segments - 1 ? bx1 + barW * fill : x0 + width + 0.002f;
                    HudDraw.rect(m, buffers, x0, barBottom, x1, barBottom + barH, z,
                            finalColor, alpha * 0.88f);
                }
            } else {
                HudDraw.rect(m, buffers, bx1, barBottom, bx1 + barW * fill, barBottom + barH, z,
                        HudDraw.lerpRgb(color, 0xFFFFFF, white), alpha * 0.7f);
            }
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

    private static int middleAuraColor(ElementType type, float phase) {
        float t = phase - (float) Math.floor(phase);
        int[] colors;

        if (type == ElementType.FROZEN) {
            colors = new int[]{0x82DFFF, 0xEAFBFF, 0x174A9C, 0xFFFFFF};
        } else if (type == ElementType.QUICKEN) {
            colors = new int[]{0xA9FF55, 0xFFFFFF, 0x3677C8, 0xFFFFFF};
        } else {
            colors = new int[]{0xFF7438, 0xFFFFFF, 0x5BE34C, 0xFFFFFF};
        }

        float scaled = t * colors.length;
        int index = (int) scaled;
        float local = scaled - index;
        int first = colors[index % colors.length];
        int second = colors[(index + 1) % colors.length];

        int red = Mth.lerpInt(local, (first >> 16) & 255, (second >> 16) & 255);
        int green = Mth.lerpInt(local, (first >> 8) & 255, (second >> 8) & 255);
        int blue = Mth.lerpInt(local, first & 255, second & 255);

        return (red << 16) | (green << 8) | blue;
    }
    private static int interpolateColor(int first, int second, float amount) {
        float t = Mth.clamp(amount, 0f, 1f);
        int red = Mth.lerpInt(t, (first >> 16) & 255, (second >> 16) & 255);
        int green = Mth.lerpInt(t, (first >> 8) & 255, (second >> 8) & 255);
        int blue = Mth.lerpInt(t, first & 255, second & 255);
        return (red << 16) | (green << 8) | blue;
    }
    public static int elementColor(ElementType type) {
        return BarConfig.toAwt(DamageIndicatorConfig.get().colorFor(type.colorKey())).getRGB() & 0xFFFFFF;
    }
}
