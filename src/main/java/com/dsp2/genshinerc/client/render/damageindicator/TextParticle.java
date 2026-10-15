package com.dsp2.genshinerc.client.render.damageindicator;

import com.dsp2.genshinerc.client.render.damageindicator.api.CustomColor;
import com.dsp2.genshinerc.client.render.damageindicator.api.EasingType;
import com.dsp2.genshinerc.client.render.damageindicator.config.DamageIndicatorConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public class TextParticle {
    private double x, y, z, px, py, pz, vx, vy, vz;
    private int age;
    private final int maxAge;
    private final Component text;
    private final CustomColor color;
    private final float scale;
    private final boolean drawShadow;
    private final double gravity;
    private final double velocityMult;
    private final boolean isKill;
    private final DamageIndicatorConfig cfg;
    private final CustomColor altColor;
    private final EasingType shrinkEasing;
    private final EasingType fadeEasing;

    public TextParticle(double x, double y, double z, double vx, double vy, double vz,
                        Component text, CustomColor color, double velocityMult, double gravity,
                        int maxAge, float scale, boolean drawShadow, boolean isKill,
                        DamageIndicatorConfig cfg) {
        this.x = x; this.y = y; this.z = z;
        this.px = x; this.py = y; this.pz = z;
        this.vx = vx; this.vy = vy; this.vz = vz;
        this.text = text; this.color = color;
        this.velocityMult = velocityMult; this.gravity = gravity;
        this.maxAge = maxAge; this.scale = scale;
        this.drawShadow = drawShadow; this.isKill = isKill; this.cfg = cfg;
        boolean isWhite = color.getR() > 0.99f && color.getG() > 0.99f && color.getB() > 0.99f;
        this.altColor = isWhite ? CustomColor.hex(0x000000) : CustomColor.hex(0xFFFFFF);
        this.shrinkEasing = EasingType.fromName(cfg.lifeShrinkEasing.name());
        this.fadeEasing = EasingType.fromName(cfg.lifeFadeEasing.name());
    }

    public boolean tick() {
        px = x; py = y; pz = z;
        vy -= gravity;
        vx *= velocityMult; vy *= velocityMult; vz *= velocityMult;
        x += vx; y += vy; z += vz;
        return ++age >= maxAge;
    }

    private float currentScale() {
        float s = scale;
        if (cfg.lifeShrinkEnabled) {
            int start = Math.max(0, maxAge - cfg.lifeShrinkStartTick);
            if (age >= start) {
                float t = (age - start) / (float) Math.max(1, maxAge - start);
                s *= (1f - shrinkEasing.apply(t));
            }
        }
        return Math.max(0f, s);
    }

    private float currentAlpha() {
        if (cfg.lifeFadeEnabled) {
            int start = Math.max(0, maxAge - cfg.lifeFadeStartTick);
            if (age >= start) {
                float t = (age - start) / (float) Math.max(1, maxAge - start);
                return 1f - fadeEasing.apply(t);
            }
        }
        return 1f;
    }

    private CustomColor currentColor() {
        if (!isKill || !cfg.killFlashEnabled) return color;
        int interval = Math.max(1, cfg.killFlashInterval);
        if (age < cfg.killFlashTicks) {
            int phase = (age / interval) % 2;
            return phase == 0 ? color : altColor;
        }
        return color;
    }

    public void render(PoseStack ps, MultiBufferSource buf, Camera cam, float partialTick) {
        double dx = Mth.lerp(partialTick, px, x) - cam.getPosition().x;
        double dy = Mth.lerp(partialTick, py, y) - cam.getPosition().y;
        double dz = Mth.lerp(partialTick, pz, z) - cam.getPosition().z;

        ps.pushPose();
        ps.translate(dx, dy, dz);
        ps.mulPose(cam.rotation());
        float s = 0.03f * currentScale();
        ps.scale(-s, -s, s);

        Font font = Minecraft.getInstance().font;
        CustomColor c = currentColor();
        int argb = c.argb();
        float alpha = currentAlpha();
        int a = (int) (((argb >> 24) & 0xFF) * alpha);
        int r = (argb >> 16) & 0xFF, g = (argb >> 8) & 0xFF, b = argb & 0xFF;
        int finalArgb = (a << 24) | (r << 16) | (g << 8) | b;

        font.drawInBatch(text, font.width(text) / -2.0f, 0f, finalArgb, drawShadow,
                ps.last().pose(), buf, Font.DisplayMode.NORMAL, 0, 15728880);
        ps.popPose();
    }
}