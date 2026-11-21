package com.dsp2.genshinerc.client.render.hud.bar.shield;

import com.dsp2.genshinerc.client.render.hud.HudDraw;
import com.dsp2.genshinerc.client.render.hud.bar.ClientCombatBars;
import com.dsp2.genshinerc.client.render.hud.bar.config.BarConfig;
import com.dsp2.genshinerc.client.render.hud.bar.layout.BarAnchor;
import com.dsp2.genshinerc.client.render.hud.bar.layout.BarTextureInfo;
import com.dsp2.genshinerc.client.render.hud.bar.layout.BarTextures;
import com.dsp2.genshinerc.client.render.hud.bar.layout.SlicedBar;
import com.dsp2.genshinerc.client.render.hud.gui.rpg.anim.WhiteOverlay;
import com.dsp2.genshinerc.common.core.combat.shield.ShieldKind;
import net.minecraft.Util;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Matrix4f;

import java.util.HashMap;
import java.util.Map;

public final class ShieldBarRenderer {
    private ShieldBarRenderer() {}

    private static final Map<Integer, WhiteOverlay> O1_MAP = new HashMap<>();
    private static final Map<Integer, WhiteOverlay> O2_MAP = new HashMap<>();
    private static final Map<Integer, WhiteOverlay> O3_MAP = new HashMap<>();
    
    private static final Map<Integer, Float> LAST_L1 = new HashMap<>();
    private static final Map<Integer, Float> LAST_L2 = new HashMap<>();
    private static final Map<Integer, Float> LAST_L3 = new HashMap<>();

    public static void render(LivingEntity entity, Matrix4f m, MultiBufferSource buffers,
                              BarConfig cfg, BarAnchor anchor, float alpha) {
        ClientShieldState s = ClientCombatBars.shield(entity.getId());
        if (s == null) return;
        long now = Util.getMillis();
        s.bar.update(now, cfg.subBarFillSpeed, cfg.subBarWhiteDrainSpeed);
        final float renderAlpha = alpha * s.warningAlpha(now);

        float hpMax = entity.getMaxHealth();
        if (hpMax <= 0f) return;

        float main3 = s.bar.main() / Math.max(1f, hpMax);

        if (s.bar.isIdle(now, cfg.subBarFlashTime) && main3 <= 0.001f) {
            ClientCombatBars.removeShield(entity.getId());
            O1_MAP.remove(entity.getId());
            O2_MAP.remove(entity.getId());
            O3_MAP.remove(entity.getId());
            LAST_L1.remove(entity.getId());
            LAST_L2.remove(entity.getId());
            LAST_L3.remove(entity.getId());
            return;
        }

        float w = anchor.width();
        float h = anchor.height() * cfg.shieldBarHeight / 100f;
        float vx1 = anchor.centerX() - w / 2f, vx2 = vx1 + w;
        float vy1 = anchor.top() + anchor.height() * cfg.subBarGap / 100f, vy2 = vy1 + h;

        BarTextureInfo fill = BarTextureInfo.get(BarTextures.FILL);
        float[] q = fill.quadForVisible(vx1, vy1, vx2, vy2);
        float z = anchor.z();

        int bgColor = (s.kind() == ShieldKind.ELEMENTAL)
                ? HudDraw.scaleRgb(s.color(cfg, hpMax, 0), 0.3f)
                : 0xFF404040;
        SlicedBar.draw(m, buffers, BarTextures.FILL, fill, q, vx1, vx2, z, bgColor, renderAlpha);

        float l1 = Mth.clamp(main3, 0f, 1f);
        float l2 = Mth.clamp(main3 - 1f, 0f, 1f);
        float l3 = Mth.clamp(main3 - 2f, 0f, 1f);

        WhiteOverlay o1 = O1_MAP.computeIfAbsent(entity.getId(), k -> new WhiteOverlay());
        WhiteOverlay o2 = O2_MAP.computeIfAbsent(entity.getId(), k -> new WhiteOverlay());
        WhiteOverlay o3 = O3_MAP.computeIfAbsent(entity.getId(), k -> new WhiteOverlay());

        float lastL1 = LAST_L1.getOrDefault(entity.getId(), 0f);
        float lastL2 = LAST_L2.getOrDefault(entity.getId(), 0f);
        float lastL3 = LAST_L3.getOrDefault(entity.getId(), 0f);

        if (Math.abs(l1 - lastL1) > 0.001f) {
            o1.trigger(lastL1, l1, now);
            LAST_L1.put(entity.getId(), l1);
        }
        if (Math.abs(l2 - lastL2) > 0.001f) {
            o2.trigger(lastL2, l2, now);
            LAST_L2.put(entity.getId(), l2);
        }
        if (Math.abs(l3 - lastL3) > 0.001f) {
            o3.trigger(lastL3, l3, now);
            LAST_L3.put(entity.getId(), l3);
        }

        o1.update(now, l1, cfg);
        o2.update(now, l2, cfg);
        o3.update(now, l3, cfg);

        if (l1 > 0.001f) {
            SlicedBar.draw(m, buffers, BarTextures.FILL, fill, q, vx1, vx1 + w * l1, z, s.color(cfg, hpMax, 0), renderAlpha);
        }
        if (l2 > 0.001f) {
            SlicedBar.draw(m, buffers, BarTextures.FILL, fill, q, vx1, vx1 + w * l2, z, s.color(cfg, hpMax, 1), renderAlpha);
        }
        if (l3 > 0.001f) {
            SlicedBar.draw(m, buffers, BarTextures.FILL, fill, q, vx1, vx1 + w * l3, z, s.color(cfg, hpMax, 2), renderAlpha);
        }

        float sa = s.bar.whiteness(now, cfg.subBarFlashTime);
        if (sa > 0.01f) {
            SlicedBar.draw(m, buffers, BarTextures.FILL, fill, q, vx1, vx1 + w * l1, z, 0xFFFFFF, renderAlpha * sa);
        }

        o1.emit((lo, hi, wa) -> SlicedBar.draw(m, buffers, BarTextures.FILL, fill, q, vx1 + w * lo, vx1 + w * hi, z, 0xFFFFFF, renderAlpha * wa));
        o2.emit((lo, hi, wa) -> SlicedBar.draw(m, buffers, BarTextures.FILL, fill, q, vx1 + w * lo, vx1 + w * hi, z, 0xFFFFFF, renderAlpha * wa));
        o3.emit((lo, hi, wa) -> SlicedBar.draw(m, buffers, BarTextures.FILL, fill, q, vx1 + w * lo, vx1 + w * hi, z, 0xFFFFFF, renderAlpha * wa));
    }
}