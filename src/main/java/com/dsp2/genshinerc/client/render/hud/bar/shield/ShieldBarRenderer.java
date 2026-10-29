package com.dsp2.genshinerc.client.render.hud.bar.shield;

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

public final class ShieldBarRenderer {
    private ShieldBarRenderer() {}

    private static final Map<Integer, WhiteOverlay> OVERLAYS = new HashMap<>();
    private static final Map<Integer, Float> LASTS = new HashMap<>();

    public static void render(LivingEntity entity, Matrix4f m, MultiBufferSource buffers,
                              BarConfig cfg, BarAnchor anchor, float alpha) {
        ClientShieldState s = ClientCombatBars.shield(entity.getId());
        if (s == null) return;
        long now = Util.getMillis();
        s.bar.update(now, cfg.subBarFillSpeed, cfg.subBarWhiteDrainSpeed);
        if (s.bar.isIdle(now, cfg.subBarFlashTime)) {
            ClientCombatBars.removeShield(entity.getId());
            OVERLAYS.remove(entity.getId());
            LASTS.remove(entity.getId());
            return;
        }

        float denom = Math.max(entity.getMaxHealth(), s.max);
        if (denom <= 0f) return;

        float w = anchor.width();
        float h = anchor.height() * cfg.shieldBarHeight / 100f;
        float vx1 = anchor.centerX() - w / 2f, vx2 = vx1 + w;
        float vy1 = anchor.top() + anchor.height() * cfg.subBarGap / 100f, vy2 = vy1 + h;

        BarTextureInfo fill = BarTextureInfo.get(BarTextures.FILL);
        float[] q = fill.quadForVisible(vx1, vy1, vx2, vy2);
        float z = anchor.z();
        int color = s.color(cfg);

        float main = Mth.clamp(s.bar.main() / denom, 0f, 1f);
        float lastMain = LASTS.getOrDefault(entity.getId(), main);

        WhiteOverlay overlay = OVERLAYS.computeIfAbsent(entity.getId(), k -> new WhiteOverlay());

        if (Math.abs(main - lastMain) > 0.001f) {
            float delta = Math.abs(main - lastMain);
            boolean small = cfg.smallChangeEnabled && delta < cfg.smallChangeThreshold;
            if (!small) {
                overlay.trigger(lastMain, main, now);
            }
        }
        LASTS.put(entity.getId(), main);
        overlay.update(now, main, cfg);

        SlicedBar.draw(m, buffers, BarTextures.FILL, fill, q, vx1, vx2, z,
                HudDraw.scaleRgb(color, cfg.shieldBgBrightness), alpha * cfg.shieldBgAlpha);

        if (main > 0f) {
            int c = HudDraw.lerpRgb(color, 0xFFFFFF, s.bar.whiteness(now, cfg.subBarFlashTime));
            SlicedBar.draw(m, buffers, BarTextures.FILL, fill, q, vx1, vx1 + w * main, z, c, alpha);
        }

        overlay.emit((lo, hi, wa) -> {
            SlicedBar.draw(m, buffers, BarTextures.FILL, fill, q, vx1 + w * lo, vx1 + w * hi, z, 0xFFFFFF, alpha * wa * 0.5f);
        });
    }
}