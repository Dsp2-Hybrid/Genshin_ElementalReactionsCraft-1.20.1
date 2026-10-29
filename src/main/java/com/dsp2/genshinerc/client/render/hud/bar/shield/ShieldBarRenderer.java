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

        float hpMax = entity.getMaxHealth();
        if (hpMax <= 0f) return;

        float totalMax = hpMax * 3f;
        float target = Math.min(s.bar.target(), totalMax);
        float main = Math.min(s.bar.main(), totalMax);

        if (s.bar.isIdle(now, cfg.subBarFlashTime) && main <= 0.001f) {
            ClientCombatBars.removeShield(entity.getId());
            OVERLAYS.remove(entity.getId());
            LASTS.remove(entity.getId());
            return;
        }

        float w = anchor.width();
        float h = anchor.height() * cfg.shieldBarHeight / 100f;
        float vx1 = anchor.centerX() - w / 2f, vx2 = vx1 + w;
        float vy1 = anchor.top() + anchor.height() * cfg.subBarGap / 100f, vy2 = vy1 + h;

        BarTextureInfo fill = BarTextureInfo.get(BarTextures.FILL);
        float[] q = fill.quadForVisible(vx1, vy1, vx2, vy2);
        float z = anchor.z();

        SlicedBar.draw(m, buffers, BarTextures.FILL, fill, q, vx1, vx2, z, 0xFF404040, alpha);

        float mainNorm = main / totalMax;
        float targetNorm = target / totalMax;
        float lastTarget = LASTS.getOrDefault(entity.getId(), targetNorm);

        WhiteOverlay overlay = OVERLAYS.computeIfAbsent(entity.getId(), k -> new WhiteOverlay());

        if (Math.abs(targetNorm - lastTarget) > 0.001f) {
            float delta = Math.abs(targetNorm - lastTarget);
            boolean small = cfg.smallChangeEnabled && delta < cfg.smallChangeThreshold;
            if (!small) {
                overlay.trigger(lastTarget, targetNorm, now);
            }
        }
        LASTS.put(entity.getId(), targetNorm);
        overlay.update(now, mainNorm, cfg);

        int layers = (int) Math.ceil(mainNorm * 3f);
        for (int i = 0; i < Math.min(3, layers); i++) {
            float layerStart = i / 3f;
            float layerEnd = Math.min((i + 1) / 3f, mainNorm);
            if (layerEnd > layerStart) {
                int c = s.color(cfg, hpMax, i);
                SlicedBar.draw(m, buffers, BarTextures.FILL, fill, q, vx1 + w * layerStart, vx1 + w * layerEnd, z, c, alpha);
            }
        }

        overlay.emit((lo, hi, wa) -> {
            SlicedBar.draw(m, buffers, BarTextures.FILL, fill, q, vx1 + w * lo, vx1 + w * hi, z, 0xFFFFFF, alpha * wa * 0.5f);
        });
    }
}