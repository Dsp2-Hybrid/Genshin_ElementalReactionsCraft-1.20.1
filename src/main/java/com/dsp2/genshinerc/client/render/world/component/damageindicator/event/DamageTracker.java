package com.dsp2.genshinerc.client.render.world.component.damageindicator.event;

import com.dsp2.genshinerc.client.render.world.component.damageindicator.TextParticle;
import com.dsp2.genshinerc.client.render.world.component.damageindicator.api.CustomColor;
import com.dsp2.genshinerc.client.render.world.component.damageindicator.api.DamageColorProvider;
import com.dsp2.genshinerc.client.render.world.component.damageindicator.api.DamageSizeProvider;
import com.dsp2.genshinerc.client.render.world.component.damageindicator.config.DamageIndicatorConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
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

    private static int forceLifeTime = 0;

    public static void addCustomText(LivingEntity entity, String text, String hexColor, String hexColor2, float scale) {
        forceLifeTime = 24;
        addParticle(new DamageInfo(entity, "physical", 0, false, false, false, 0, hexColor, scale, false, false, text, hexColor2));
        forceLifeTime = 0;
    }

    public static void addParticle(DamageInfo info) {
        DamageIndicatorConfig cfg = DamageIndicatorConfig.get();
        if (!cfg.serverEnabled || info.entity == null) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        if (mc.player.distanceTo(info.entity) > cfg.serverMaxDistance) return;

        float scale = info.hasOverrideSize() ? info.overrideSize : sizeProvider.getSize(info);
        CustomColor color = info.hasOverrideColor()
                ? CustomColor.fromHexString(info.overrideColor)
                : colorProvider.getColor(info);
        
        CustomColor color2 = info.overrideColor2 != null ? CustomColor.fromHexString(info.overrideColor2) : null;

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

        Vector3d toCamera = new Vector3d(
                mc.player.getX() - basePos.x,
                mc.player.getY() + mc.player.getEyeHeight() - basePos.y,
                mc.player.getZ() - basePos.z
        );
        double camLen = toCamera.length();
        if (camLen > 0.0001) {
            toCamera.div(camLen);
            double lo = Math.min(cfg.zRandomMin, cfg.zRandomMax);
            double hi = Math.max(cfg.zRandomMin, cfg.zRandomMax);
            double randomZ = lo + Math.random() * (hi - lo);
            if (info.entity.isOnFire()) {
                randomZ += cfg.zOffsetBurning;
            }
            if (Math.abs(randomZ) > 0.0001) {
                basePos.fma(randomZ, toCamera);
            }
        }

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
            addOne(p1, velBase, absorbText, "physical".equals(info.typeKey) ? CustomColor.fromHexString(cfg.absorbColor) : color, null, scale, cfg, false, false);
            if (info.amount > 0.001f) {
                String realText = trim(info.amount, cfg.numberFormat);
                addOne(p2, velBase, realText, color, null, scale, cfg, false, true);
            }
        } else if (info.absorbDelta) {
            String text = (info.amount >= 0 ? "+" : "") + trim(Math.abs(info.amount), cfg.numberFormat);
            addOne(basePos, velBase, text, CustomColor.fromHexString(cfg.absorbColor), null, scale, cfg, false, false);
        } else {
            String txt = info.customText != null ? info.customText : formatDamage(info, cfg.numberFormat);
            addOne(basePos, velBase, txt, color, color2, scale, cfg, info.kill, false);
        }
    }

    private static void addOne(Vector3d pos, Vector3d vel, String text, CustomColor color, CustomColor color2,
                               float scale, DamageIndicatorConfig cfg, boolean kill, boolean secondary) {
        Vector3d velCopy = new Vector3d(vel);
        if (secondary) {
            velCopy.x += (Math.random() - 0.5) * 0.2;
            velCopy.z += (Math.random() - 0.5) * 0.2;
        }
        int life = forceLifeTime > 0 ? forceLifeTime : cfg.lifeTime;
        PARTICLES.add(new TextParticle(
                pos.x, pos.y, pos.z, velCopy.x, velCopy.y, velCopy.z,
                Component.literal(text), color, color2, cfg.velocityMultiplier, cfg.gravityStrength,
                life, scale, cfg.drawShadow, kill, cfg));
    }

    private static String formatDamage(DamageInfo i, int digits) {
        String s = trim(Math.abs(i.amount), digits);
        if (i.isHeal()) return "+" + s + (i.fullHeal ? "!" : "");
        if (i.kill) return s + "!";
        return s;
    }

    private static String trim(float v, int digits) {
        double value = v;
        double abs = Math.abs(value);
        if (digits <= 0) return String.valueOf(Math.round(value));
        if (digits == 1) {
            if (abs >= 1000) return String.valueOf(Math.round(value));
            String s = String.format("%.1f", value);
            return s.endsWith(".0") ? s.substring(0, s.length() - 2) : s;
        }
        if (abs >= 10000) return String.valueOf(Math.round(value));
        if (abs >= 1000) return String.format("%.1f", value);
        return String.format("%.2f", value);
    }

    public static void tickAll() {
        PARTICLES.removeIf(TextParticle::tick);
    }

    public static void renderAll(PoseStack ps, MultiBufferSource buf, Camera cam, float pt) {
        for (TextParticle p : PARTICLES) p.render(ps, buf, cam, pt);
    }
}