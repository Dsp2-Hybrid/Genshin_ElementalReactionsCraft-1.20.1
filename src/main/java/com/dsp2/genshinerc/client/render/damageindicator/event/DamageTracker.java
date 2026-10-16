package com.dsp2.genshinerc.client.render.damageindicator.event;

import com.dsp2.genshinerc.client.render.damageindicator.TextParticle;
import com.dsp2.genshinerc.client.render.damageindicator.api.CustomColor;
import com.dsp2.genshinerc.client.render.damageindicator.api.DamageColorProvider;
import com.dsp2.genshinerc.client.render.damageindicator.api.DamageSizeProvider;
import com.dsp2.genshinerc.client.render.damageindicator.config.DamageIndicatorConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DamageTracker {
    private static final List<TextParticle> PARTICLES = new ArrayList<>();
    private static final Map<Integer, Vector3d> LAST_SPAWN = new HashMap<>();
    private static DamageColorProvider colorProvider = DamageColorProvider.DEFAULT;
    private static DamageSizeProvider sizeProvider = DamageSizeProvider.DEFAULT;

    public static void setColorProvider(DamageColorProvider p) { colorProvider = p; }
    public static void setSizeProvider(DamageSizeProvider p) { sizeProvider = p; }

    public static void addParticle(DamageInfo info) {
        DamageIndicatorConfig cfg = DamageIndicatorConfig.get();
        if (!cfg.enabled || info.entity == null) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        float scale = info.hasOverrideSize() ? info.overrideSize : sizeProvider.getSize(info);
        CustomColor color = info.hasOverrideColor()
                ? CustomColor.fromHexString(info.overrideColor)
                : colorProvider.getColor(info);

        double sizeOffset = (scale - (float) cfg.normalSize) * cfg.sizeYFactorK;
        double heightOffset = info.entity.getBbHeight() * cfg.heightYFactor;
        double yBase = info.entity.getY() + info.entity.getBbHeight() + cfg.offsetY + sizeOffset + heightOffset;

        Vector3d basePos = new Vector3d(info.entity.getX(), yBase, info.entity.getZ());

        Vector3d last = LAST_SPAWN.get(info.entity.getId());
        if (last != null) {
            double dist = basePos.distance(last);
            if (dist < cfg.particleMinSpacing) {
                double angle = Math.random() * Math.PI * 2;
                basePos.x += Math.cos(angle) * cfg.particleMinSpacing;
                basePos.z += Math.sin(angle) * cfg.particleMinSpacing;
            }
        }
        LAST_SPAWN.put(info.entity.getId(), new Vector3d(basePos));

        Vector3d velBase = new Vector3d(0, 0, 0);
        if (cfg.randomVelocityXZ) {
            velBase.x = (Math.random() - 0.5) * 2 * cfg.randomVelocityXZAmount;
            velBase.z = (Math.random() - 0.5) * 2 * cfg.randomVelocityXZAmount;
        }

        if (info.hasAbsorb()) {
            double offX = 0.35;
            Vector3d p1 = new Vector3d(basePos.x - offX, basePos.y, basePos.z);
            Vector3d p2 = new Vector3d(basePos.x + offX, basePos.y, basePos.z);
            String absorbText = "-" + trim(info.absorbed, cfg.numberFormat);
            addOne(p1, velBase, absorbText, CustomColor.fromHexString(cfg.absorbColor), scale, cfg, false, false);
            if (info.amount > 0.001f) {
                String realText = trim(info.amount, cfg.numberFormat);
                addOne(p2, velBase, realText, color, scale, cfg, false, true);
            }
        } else if (info.absorbDelta) {
            String text = (info.amount >= 0 ? "+" : "") + trim(Math.abs(info.amount), cfg.numberFormat);
            addOne(basePos, velBase, text, CustomColor.fromHexString(cfg.absorbColor), scale, cfg, false, false);
        } else {
            addOne(basePos, velBase, formatDamage(info, cfg.numberFormat), color, scale, cfg, info.kill, false);
        }
    }

    private static void addOne(Vector3d pos, Vector3d vel, String text, CustomColor color,
                               float scale, DamageIndicatorConfig cfg, boolean kill, boolean secondary) {
        Vector3d velCopy = new Vector3d(vel);
        if (secondary) {
            velCopy.x += (Math.random() - 0.5) * 0.2;
            velCopy.z += (Math.random() - 0.5) * 0.2;
        }
        PARTICLES.add(new TextParticle(
                pos.x, pos.y, pos.z, velCopy.x, velCopy.y, velCopy.z,
                Component.literal(text), color, cfg.velocityMultiplier, cfg.gravityStrength,
                cfg.lifeTime, scale, cfg.drawShadow, kill, cfg));
    }

    private static String formatDamage(DamageInfo i, int digits) {
        String s = trim(Math.abs(i.amount), digits);
        if (i.isHeal()) return "+" + s + (i.fullHeal ? "!" : "");
        if (i.kill) return s + "!";
        return s;
    }

    private static String trim(float v, int digits) {
        if (digits <= 0) return String.valueOf(Math.round(v));
        if (digits == 1) {
            String s = String.format("%.1f", v);
            return s.endsWith(".0") ? s.substring(0, s.length() - 2) : s;
        }
        return String.format("%.2f", v);
    }

    public static void tickAll() {
        PARTICLES.removeIf(TextParticle::tick);
    }

    public static void renderAll(PoseStack ps, MultiBufferSource buf, Camera cam, float pt) {
        for (TextParticle p : PARTICLES) p.render(ps, buf, cam, pt);
    }
}