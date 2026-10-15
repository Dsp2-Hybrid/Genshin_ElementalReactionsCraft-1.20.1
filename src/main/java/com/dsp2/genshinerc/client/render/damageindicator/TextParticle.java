package com.dsp2.genshinerc.client.render.damageindicator;

import com.dsp2.genshinerc.client.render.damageindicator.api.CustomColor;
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
    private final float gravity;
    private final float velocityMult;
    private final boolean isKill;
    private final DamageIndicatorConfig cfg;
    private final CustomColor altColor;

    public TextParticle(double x, double y, double z, double vx, double vy, double vz,
                        Component text, CustomColor color, float velocityMult, float gravity,
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
    }

    public boolean tick() {
        px = x; py = y; pz = z;
        vy += gravity;
        vx *= velocityMult; vy *= velocityMult; vz *= velocityMult;
        x += vx; y += vy; z += vz;
        return ++age >= maxAge;
    }

    private float currentScale() {
        float s = scale;
        if (isKill && cfg.killScaleEndTick > cfg.killScaleStartTick
                && age >= cfg.killScaleStartTick && age <= cfg.killScaleEndTick) {
            float t = (age - cfg.killScaleStartTick) / (float) (cfg.killScaleEndTick - cfg.killScaleStartTick);
            s = scale * (1f - t);
        }
        if (cfg.lifeShrinkEnabled && maxAge > cfg.lifeShrinkThreshold) {
            int start = maxAge - cfg.lifeShrinkDuration;
            if (age >= start) {
                float t = (age - start) / (float) cfg.lifeShrinkDuration;
                s *= (1f - t);
            }
        }
        return s;
    }

    private float currentAlpha() {
        if (cfg.lifeFadeEnabled && age >= maxAge - cfg.lifeFadeDuration) {
            float t = (age - (maxAge - cfg.lifeFadeDuration)) / (float) cfg.lifeFadeDuration;
            return 1f - t;
        }
        return 1f;
    }

    private CustomColor currentColor() {
        if (!isKill || !cfg.killFlashEnabled) return color;
        if (age < cfg.killFlashTicks) return (age % 2 == 0) ? color : altColor;
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