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

        float tr = 0f, tg = 0f, tb = 0f, ta = 0f;
        if (shield != null && shield.amount() > 0f && shield.initial() > 0f) {
            int color = shield.color(BarConfig.get(), entity.getMaxHealth(), 0);
            tr = ((color >> 16) & 255) / 255f;
            tg = ((color >> 8) & 255) / 255f;
            tb = (color & 255) / 255f;
            float ratio = Mth.clamp(shield.amount() / shield.initial(), 0f, 1f);
            ta = 0.12f + 0.30f * ratio;
            float flash = shield.bar.whiteness(now, BarConfig.get().subBarFlashTime);
            tr += (1f - tr) * flash;
            tg += (1f - tg) * flash;
            tb += (1f - tb) * flash;
        }

        float dt = blend.last == 0L ? 0.05f : Mth.clamp((now - blend.last) / 1000f, 0f, 0.2f);
        blend.last = now;
        float k = 1f - (float) Math.exp(-8f * dt);
        blend.red += (tr - blend.red) * k;
        blend.green += (tg - blend.green) * k;
        blend.blue += (tb - blend.blue) * k;
        blend.alpha += (ta - blend.alpha) * k;
        return new Overlay(blend.red, blend.green, blend.blue, blend.alpha);
    }
}