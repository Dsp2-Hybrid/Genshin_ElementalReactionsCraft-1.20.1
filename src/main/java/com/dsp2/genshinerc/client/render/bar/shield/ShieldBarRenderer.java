package com.dsp2.genshinerc.client.render.bar.shield;

import com.dsp2.genshinerc.client.render.bar.ClientCombatBars;
import com.dsp2.genshinerc.client.render.bar.config.BarConfig;
import com.dsp2.genshinerc.client.render.bar.layout.BarAnchor;
import com.dsp2.genshinerc.client.render.bar.layout.BarTextureInfo;
import com.dsp2.genshinerc.client.render.bar.layout.BarTextures;
import com.dsp2.genshinerc.client.render.bar.layout.SlicedBar;
import com.dsp2.genshinerc.client.render.hud.HudDraw;
import net.minecraft.Util;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Matrix4f;

public final class ShieldBarRenderer {
    private ShieldBarRenderer() {}

    public static void render(LivingEntity entity, Matrix4f m, MultiBufferSource buffers,
                              BarConfig cfg, BarAnchor anchor, float alpha) {
        ClientShieldState s = ClientCombatBars.shield(entity.getId());
        if (s == null) return;
        long now = Util.getMillis();
        s.bar.update(now, cfg.subBarFillSpeed, cfg.subBarWhiteDrainSpeed);
        if (s.bar.isIdle(now, cfg.subBarFlashTime)) {
            ClientCombatBars.removeShield(entity.getId());
            return;
        }

        float denom = Math.max(entity.getMaxHealth(), s.max);
        if (denom <= 0f) return;

        float w = anchor.width() * cfg.subBarWidth / 100f;
        float h = anchor.height() * cfg.shieldBarHeight / 100f;
        float vx1 = anchor.centerX() - w / 2f, vx2 = vx1 + w;
        float vy1 = anchor.top() + anchor.height() * cfg.subBarGap / 100f, vy2 = vy1 + h;

        BarTextureInfo fill = BarTextureInfo.get(BarTextures.FILL);
        float[] q = fill.quadForVisible(vx1, vy1, vx2, vy2);
        float z = anchor.z();
        int color = s.color(cfg);

        SlicedBar.draw(m, buffers, BarTextures.FILL, fill, q, vx1, vx2, z,
                HudDraw.scaleRgb(color, cfg.shieldBgBrightness), alpha * cfg.shieldBgAlpha);

        float trail = Mth.clamp(s.bar.trail() / denom, 0f, 1f);
        float main = Mth.clamp(s.bar.main() / denom, 0f, 1f);
        if (trail > main) {
            SlicedBar.draw(m, buffers, BarTextures.FILL, fill, q, vx1, vx1 + w * trail, z, 0xFFFFFF, alpha);
        }
        if (main > 0f) {
            int c = HudDraw.lerpRgb(color, 0xFFFFFF, s.bar.whiteness(now, cfg.subBarFlashTime));
            SlicedBar.draw(m, buffers, BarTextures.FILL, fill, q, vx1, vx1 + w * main, z, c, alpha);
        }
    }
}