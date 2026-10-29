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
    private static final Map<Integer, Float> LAST_HPMAX = new HashMap<>();

    public static void render(LivingEntity entity, Matrix4f m, MultiBufferSource buffers,
                              BarConfig cfg, BarAnchor anchor, float alpha) {
        ClientShieldState s = ClientCombatBars.shield(entity.getId());
        if (s == null) return;
        long now = Util.getMillis();
        s.bar.update(now, cfg.subBarFillSpeed, cfg.subBarWhiteDrainSpeed);

        float hpMax = entity.getMaxHealth();
        if (hpMax <= 0f) return;

        float main3 = s.bar.main() * 3f;
        float target3 = s.bar.target() * 3f;

        if (s.bar.isIdle(now, cfg.subBarFlashTime) && main3 <= 0.001f) {
            ClientCombatBars.removeShield(entity.getId());
            OVERLAYS.remove(entity.getId());
            LASTS.remove(entity.getId());
            LAST_HPMAX.remove(entity.getId());
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

        Float lastHpMax = LAST_HPMAX.get(entity.getId());
        if (lastHpMax == null || Math.abs(hpMax - lastHpMax) > 0.001f) {
            LAST_HPMAX.put(entity.getId(), hpMax);
            LASTS.put(entity.getId(), target3);
            OVERLAYS.computeIfAbsent(entity.getId(), k -> new WhiteOverlay()).reset();
        }

        float lastTarget = LASTS.getOrDefault(entity.getId(), target3);
        WhiteOverlay overlay = OVERLAYS.computeIfAbsent(entity.getId(), k -> new WhiteOverlay());

        if (Math.abs(target3 - lastTarget) > 0.001f) {
            float delta = Math.abs(target3 - lastTarget) / 3f;
            boolean small = cfg.smallChangeEnabled && delta < cfg.smallChangeThreshold;
            if (!small) {
                overlay.trigger(lastTarget / 3f, target3 / 3f, now);
            } else {
                s.bar.flash(now);
            }
        }
        LASTS.put(entity.getId(), target3);
        overlay.update(now, main3 / 3f, cfg);

        float l1 = Mth.clamp(main3, 0f, 1f);
        float l2 = Mth.clamp(main3 - 1f, 0f, 1f);
        float l3 = Mth.clamp(main3 - 2f, 0f, 1f);

        if (l1 > 0.001f) {
            int c = s.color(cfg, hpMax, 0);
            SlicedBar.draw(m, buffers, BarTextures.FILL, fill, q, vx1, vx1 + w * (l1 / 3f), z, c, alpha);
        }
        if (l2 > 0.001f) {
            int c = s.color(cfg, hpMax, 1);
            SlicedBar.draw(m, buffers, BarTextures.FILL, fill, q, vx1 + w * (1f / 3f), vx1 + w * (1f / 3f + l2 / 3f), z, c, alpha);
        }
        if (l3 > 0.001f) {
            int c = s.color(cfg, hpMax, 2);
            SlicedBar.draw(m, buffers, BarTextures.FILL, fill, q, vx1 + w * (2f / 3f), vx1 + w * (2f / 3f + l3 / 3f), z, c, alpha);
        }

        float sa = s.bar.whiteness(now, cfg.subBarFlashTime);
        if (sa > 0.01f) {
            SlicedBar.draw(m, buffers, BarTextures.FILL, fill, q, vx1, vx1 + w * (main3 / 3f), z, 0xFFFFFF, alpha * sa);
        }

        overlay.emit((lo, hi, wa) -> {
            SlicedBar.draw(m, buffers, BarTextures.FILL, fill, q, vx1 + w * lo, vx1 + w * hi, z, 0xFFFFFF, alpha * wa);
        });
    }
}