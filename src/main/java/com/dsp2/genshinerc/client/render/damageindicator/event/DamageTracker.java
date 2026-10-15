package com.dsp2.genshinerc.client.render.damageindicator.event;

import com.dsp2.genshinerc.client.render.damageindicator.api.CustomColor;
import com.dsp2.genshinerc.client.render.damageindicator.api.DamageColorProvider;
import com.dsp2.genshinerc.client.render.damageindicator.api.DamageSizeProvider;
import com.dsp2.genshinerc.client.render.damageindicator.config.DamageIndicatorConfig;
import com.dsp2.genshinerc.client.render.damageindicator.TextParticle;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;

public class DamageTracker {
    private static final List<TextParticle> PARTICLES = new ArrayList<>();
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

        Vector3d pos = new Vector3d(info.entity.getX(),
                info.entity.getY() + info.entity.getBbHeight() + cfg.offsetY,
                info.entity.getZ());

        Vector3d vel = new Vector3d(cfg.randomVelocityXZ ? cfg.randomVelocityXZAmount : 0, cfg.velocityY, 0);
        if (cfg.randomVelocityXZ) {
            vel.x = (Math.random() - 0.5) * 2 * cfg.randomVelocityXZAmount;
            vel.z = (Math.random() - 0.5) * 2 * cfg.randomVelocityXZAmount;
        }

        if (info.hasAbsorb()) {
            String absorbText = "-" + trim(info.absorbed);
            addOne(pos, vel, absorbText, CustomColor.fromHexString(cfg.absorbColor), scale, cfg, false);
            if (info.amount > 0.001f) {
                String realText = "-" + trim(info.amount);
                addOne(pos, vel, realText, color, scale, cfg, false);
            }
        } else {
            addOne(pos, vel, formatDamage(info), color, scale, cfg, info.kill);
        }
    }

    private static void addOne(Vector3d pos, Vector3d vel, String text, CustomColor color,
                               float scale, DamageIndicatorConfig cfg, boolean kill) {
        PARTICLES.add(new TextParticle(
                pos.x, pos.y, pos.z, vel.x, vel.y, vel.z,
                Component.literal(text), color, cfg.velocityMultiplier, cfg.gravityStrength,
                (int) cfg.lifeTime, scale, cfg.drawShadow, kill, cfg));
    }

    private static String formatDamage(DamageInfo i) {
        String s = trim(Math.abs(i.amount));
        if (i.isHeal()) return "+" + s;
        if (i.kill) return "-" + s + "!";
        return "-" + s;
    }
    private static String trim(float v) {
        String s = String.format("%.1f", v);
        return s.endsWith(".0") ? s.substring(0, s.length() - 2) : s;
    }

    public static void tickAll() { PARTICLES.removeIf(TextParticle::tick); }

    public static void renderAll(PoseStack ps, MultiBufferSource buf, Camera cam, float pt) {
        for (TextParticle p : PARTICLES) p.render(ps, buf, cam, pt);
    }
}