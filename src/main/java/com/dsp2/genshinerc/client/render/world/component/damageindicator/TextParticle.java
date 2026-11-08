package com.dsp2.genshinerc.client.render.world.component.damageindicator;

import com.dsp2.genshinerc.client.render.world.component.damageindicator.api.CustomColor;
import com.dsp2.genshinerc.client.render.world.component.damageindicator.api.EasingType;
import com.dsp2.genshinerc.client.render.world.component.damageindicator.config.DamageIndicatorConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

public class TextParticle {
    private double x, y, z, px, py, pz, vx, vy, vz;
    private int age;
    private final int maxAge;
    private final Component text;
    private final CustomColor color;
    @Nullable private final CustomColor color2;
    private final float baseScale;
    private final boolean drawShadow;
    private final float shadowLuma;
    private final float shadowAlpha;
    private final float shadowOffsetX;
    private final float shadowOffsetY;
    private final float zOffset;
    private final double gravity;
    private final double velocityMult;
    private final boolean isKill;
    private final DamageIndicatorConfig cfg;
    private final CustomColor altColor;
    private final EasingType shrinkEasing;
    private final EasingType fadeEasing;
    private final EasingType birthScaleEasing;
    private final EasingType birthFadeEasing;
    private final int gradientDir;

    public TextParticle(double x, double y, double z, double vx, double vy, double vz,
                        Component text, CustomColor color, @Nullable CustomColor color2, double velocityMult, double gravity,
                        int maxAgeBase, float scale, boolean drawShadow, boolean isKill,
                        DamageIndicatorConfig cfg) {
        this.x = x; this.y = y; this.z = z;
        this.px = x; this.py = y; this.pz = z;
        this.vx = vx; this.vy = vy; this.vz = vz;
        this.text = text; this.color = color; this.color2 = color2;
        this.velocityMult = velocityMult; this.gravity = gravity;
        this.maxAge = isKill ? maxAgeBase + cfg.killExtraTicks : maxAgeBase;
        this.drawShadow = drawShadow; this.isKill = isKill; this.cfg = cfg;
        this.gradientDir = (int) (Math.random() * 3);

        double randFactor = 1.0 + (Math.random() * 2 - 1) * cfg.birthSizeRandomPercent;
        this.baseScale = (float) (scale * randFactor);

        int hex;
        try { hex = Integer.parseInt(cfg.shadowColor.replace("#", "").trim(), 16); }
        catch (Exception e) { hex = 0x929292; }
        int sr = (hex >> 16) & 0xFF;
        int sg = (hex >> 8) & 0xFF;
        int sb = hex & 0xFF;
        this.shadowLuma = Math.max(0f, Math.min(1f, (sr + sg + sb) / (3f * 255f)));
        this.shadowAlpha = (float) Math.max(0.0, Math.min(1.0, cfg.shadowAlpha));
        this.shadowOffsetX = (float) cfg.shadowOffsetX;
        this.shadowOffsetY = (float) cfg.shadowOffsetY;
        this.zOffset = (float) cfg.zOffset;

        boolean isWhite = color.getR() > 0.99f && color.getG() > 0.99f && color.getB() > 0.99f;
        this.altColor = isWhite ? CustomColor.hex(0x000000) : CustomColor.hex(0xFFFFFF);
        this.shrinkEasing = EasingType.fromName(cfg.lifeShrinkEasing.name());
        this.fadeEasing = EasingType.fromName(cfg.lifeFadeEasing.name());
        this.birthScaleEasing = EasingType.fromName(cfg.birthScaleEasing.name());
        this.birthFadeEasing = EasingType.fromName(cfg.birthFadeEasing.name());
    }

    public boolean tick() {
        px = x; py = y; pz = z;
        vy -= gravity;
        vx *= velocityMult; vy *= velocityMult; vz *= velocityMult;
        x += vx; y += vy; z += vz;
        return ++age >= maxAge;
    }

    private boolean skipBirthScale() {
        if (!cfg.lifeShrinkEnabled) return false;
        return Math.max(0, maxAge - cfg.lifeShrinkStartTick) < cfg.birthScaleDuration;
    }
    private boolean skipBirthFade() {
        if (!cfg.lifeFadeEnabled) return false;
        return Math.max(0, maxAge - cfg.lifeFadeStartTick) < cfg.birthFadeDuration;
    }

    private float currentScale(float renderAge) {
        float s = baseScale;
        if (cfg.birthScaleEnabled && !skipBirthScale() && renderAge < cfg.birthScaleDuration) {
            s *= birthScaleEasing.apply(renderAge / (float) cfg.birthScaleDuration);
        }
        if (cfg.lifeShrinkEnabled) {
            int start = Math.max(0, maxAge - cfg.lifeShrinkStartTick);
            if (renderAge >= start) {
                float t = (renderAge - start) / (float) Math.max(1, maxAge - start);
                s *= (1f - shrinkEasing.apply(t));
            }
        }
        return Math.max(0f, s);
    }

    private float currentAlpha(float renderAge) {
        float a = 1f;
        if (cfg.birthFadeEnabled && !skipBirthFade() && renderAge < cfg.birthFadeDuration) {
            a *= birthFadeEasing.apply(renderAge / (float) cfg.birthFadeDuration);
        }
        if (cfg.lifeFadeEnabled) {
            int start = Math.max(0, maxAge - cfg.lifeFadeStartTick);
            if (renderAge >= start) {
                float t = (renderAge - start) / (float) Math.max(1, maxAge - start);
                a *= (1f - fadeEasing.apply(t));
            }
        }
        return Math.max(0f, a);
    }

    private CustomColor currentColor() {
        if (!isKill || !cfg.killFlashEnabled) return color;
        int interval = Math.max(1, cfg.killFlashInterval);
        if (age < cfg.killFlashTicks) {
            return (age / interval) % 2 == 0 ? color : altColor;
        }
        return color;
    }

    public void render(PoseStack ps, MultiBufferSource buf, Camera cam, float partialTick) {
        float renderAge = Math.min(maxAge, age + partialTick);
        float s = 0.03f * currentScale(renderAge);

        CustomColor c = currentColor();
        int argb = c.argb();
        int a = (int) (((argb >>> 24) & 0xFF) * currentAlpha(renderAge));
        
        if (s <= 0f || a < 4) return;

        double dx = Mth.lerp(partialTick, px, x) - cam.getPosition().x;
        double dy = Mth.lerp(partialTick, py, y) - cam.getPosition().y;
        double dz = Mth.lerp(partialTick, pz, z) - cam.getPosition().z;

        ps.pushPose();
        ps.translate(dx, dy, dz);
        ps.mulPose(cam.rotation());
        ps.translate(0f, 0f, zOffset);
        ps.scale(-s, -s, s);

        Font font = Minecraft.getInstance().font;
        Matrix4f pose = ps.last().pose();
        float textWidth = font.width(text);
        float textX = -textWidth / 2f;
        float textY = -font.lineHeight / 2f;

        int rgb1 = argb & 0xFFFFFF;
        int sa = (int) (a * shadowAlpha);
        boolean shadow = drawShadow && sa >= 4;

        if (color2 != null) {
            int rgb2 = color2.argb() & 0xFFFFFF;

            if (shadow) {
                MultiBufferSource shadowBuf = new GradientTextBuffer(
                        buf, pose, textX, textX + textWidth, textY, textY + font.lineHeight,
                        shade(rgb1, shadowLuma), shade(rgb2, shadowLuma), sa, gradientDir);
                font.drawInBatch(text, textX + shadowOffsetX, shadowOffsetY, 0xFFFFFFFF, false,
                        pose, shadowBuf, Font.DisplayMode.NORMAL, 0, 15728880);
            }

            MultiBufferSource mainBuf = new GradientTextBuffer(
                    buf, pose, textX, textX + textWidth, textY, textY + font.lineHeight,
                    rgb1, rgb2, a, gradientDir);
            font.drawInBatch(text, textX, 0f, 0xFFFFFFFF, false,
                    pose, mainBuf, Font.DisplayMode.NORMAL, 0, 15728880);
        } else {
            if (shadow) {
                int shadowArgb = (sa << 24) | shade(rgb1, shadowLuma);
                font.drawInBatch(text, textX + shadowOffsetX, shadowOffsetY, shadowArgb, false,
                        pose, buf, Font.DisplayMode.NORMAL, 0, 15728880);
            }
            font.drawInBatch(text, textX, 0f, (a << 24) | rgb1, false,
                    pose, buf, Font.DisplayMode.NORMAL, 0, 15728880);
        }

        ps.popPose();
    }

    private static int shade(int rgb, float luma) {
        int r = (int) (((rgb >> 16) & 0xFF) * luma);
        int g = (int) (((rgb >> 8) & 0xFF) * luma);
        int b = (int) ((rgb & 0xFF) * luma);
        return (r << 16) | (g << 8) | b;
    }
}