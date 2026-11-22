package com.dsp2.genshinerc.client.render.entity.elereaction.overlay;

import com.dsp2.genshinerc.client.render.hud.bar.ClientCombatBars;
import com.dsp2.genshinerc.client.render.hud.bar.config.BarConfig;
import com.dsp2.genshinerc.client.render.hud.bar.shield.ClientShieldState;
import net.minecraft.Util;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

import java.util.HashMap;
import java.util.Map;

public final class ShieldSurface {
    public record Overlay(float red, float green, float blue, float alpha) {}

    private static final class Blend {
        float red;
        float green;
        float blue;
        float alpha;
        long last;
    }

    private static final Map<Integer, Blend> STATES = new HashMap<>();

    private ShieldSurface() {}

    public static Overlay sample(LivingEntity entity) {
        int id = entity.getId();
        ClientShieldState shield = ClientCombatBars.shield(id);
        long now = Util.getMillis();
        Blend blend = STATES.computeIfAbsent(id, ignored -> new Blend());

        float targetRed = 0f;
        float targetGreen = 0f;
        float targetBlue = 0f;
        float targetAlpha = 0f;

        if (shield != null && shield.amount() > 0f && shield.initial() > 0f) {
            int color = shield.color(BarConfig.get(), entity.getMaxHealth(), 0);

            targetRed = ((color >> 16) & 255) / 255f;
            targetGreen = ((color >> 8) & 255) / 255f;
            targetBlue = (color & 255) / 255f;

            float ratio = Mth.clamp(
                    shield.amount() / shield.initial(),
                    0f,
                    1f);

            float warning = shield.warningAlpha(now);
            float flash = shield.bar.whiteness(
                    now,
                    BarConfig.get().subBarFlashTime);

            targetAlpha = (0.10f + ratio * 0.34f) * warning;
            flash = Math.max(flash, 1f - warning);

            targetRed += (1f - targetRed) * flash * 0.72f;
            targetGreen += (1f - targetGreen) * flash * 0.72f;
            targetBlue += (1f - targetBlue) * flash * 0.72f;
        }

        float dt = blend.last == 0L
                ? 0.05f
                : Mth.clamp((now - blend.last) / 1000f, 0f, 0.2f);

        blend.last = now;

        float interpolation = 1f - (float) Math.exp(-8f * dt);

        blend.red += (targetRed - blend.red) * interpolation;
        blend.green += (targetGreen - blend.green) * interpolation;
        blend.blue += (targetBlue - blend.blue) * interpolation;
        blend.alpha += (targetAlpha - blend.alpha) * interpolation;

        return new Overlay(
                Mth.clamp(blend.red, 0f, 1f),
                Mth.clamp(blend.green, 0f, 1f),
                Mth.clamp(blend.blue, 0f, 1f),
                Mth.clamp(blend.alpha, 0f, 0.75f));
    }

    public static float shellRadius(LivingEntity entity) {
        return Math.max(entity.getBbWidth(), entity.getBbHeight() * 0.58f) + 0.18f;
    }

    public static float shellAlpha(LivingEntity entity) {
        Overlay overlay = sample(entity);
        return Mth.clamp(overlay.alpha() * 0.88f, 0f, 0.65f);
    }

    public static int shellColor(LivingEntity entity) {
        Overlay overlay = sample(entity);

        int red = Mth.clamp((int) (overlay.red() * 255f), 0, 255);
        int green = Mth.clamp((int) (overlay.green() * 255f), 0, 255);
        int blue = Mth.clamp((int) (overlay.blue() * 255f), 0, 255);

        return (red << 16) | (green << 8) | blue;
    }
}