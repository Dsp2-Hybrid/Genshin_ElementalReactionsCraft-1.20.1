package com.dsp2.genshinerc.client.render.damageindicator.event;

import com.dsp2.genshinerc.client.render.damageindicator.TextParticle;
import com.dsp2.genshinerc.client.render.damageindicator.api.DamageColorProvider;
import com.dsp2.genshinerc.client.render.damageindicator.api.DamageSizeProvider;
import com.dsp2.genshinerc.client.render.damageindicator.config.DamageIndicatorConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;

public class DamageTracker {
    private static final List<TextParticle> PARTICLES = new ArrayList<>();
    private static DamageColorProvider colorProvider = DamageColorProvider.CONFIG_DEFAULT;
    private static DamageSizeProvider sizeProvider = DamageSizeProvider.DEFAULT;

    public static void setColorProvider(DamageColorProvider p) { colorProvider = p; }
    public static void setSizeProvider(DamageSizeProvider p) { sizeProvider = p; }
    public static DamageColorProvider getColorProvider() { return colorProvider; }
    public static DamageSizeProvider getSizeProvider() { return sizeProvider; }

    public static void reportDamage(DamageInfo info) {
        System.out.println("[DI] reportDamage called: amount=" + info.amount + " crit=" + info.crit
                + " entity=" + (info.entity != null ? info.entity.getName().getString() : "null"));

        DamageIndicatorConfig cfg = DamageIndicatorConfig.get();
        if (cfg == null) { System.out.println("[DI] cfg is NULL"); return; }
        if (!cfg.enabled) { System.out.println("[DI] disabled in config"); return; }
        if (info.entity == null) { System.out.println("[DI] entity null"); return; }

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) { System.out.println("[DI] mc.player null"); return; }
        if (info.entity == mc.player && !cfg.showSelfDamage) { System.out.println("[DI] self damage hidden"); return; }
        if (mc.player.distanceTo(info.entity) > cfg.maxDistance) {
            System.out.println("[DI] too far: " + mc.player.distanceTo(info.entity) + " > " + cfg.maxDistance);
            return;
        }

        float scale = sizeProvider.getSize(info);
        var color = colorProvider.getColor(info);
        String text = formatText(info, cfg);

        var pos = info.entity.position().add(cfg.offsetX, info.entity.getBbHeight() + cfg.offsetY, cfg.offsetZ);
        var vel = new Vector3d(cfg.velocityX, cfg.velocityY, cfg.velocityZ);

        if (cfg.randomVelocityXZ) {
            vel.x = Math.random() * cfg.velocityX;
            vel.z = Math.random() * cfg.velocityZ;
        }

        PARTICLES.add(new TextParticle(pos.x, pos.y, pos.z, vel.x, vel.y, vel.z,
                Component.literal(text), color, cfg.velocityMultiplier, cfg.gravityStrength,
                cfg.lifeTime, scale, cfg.drawShadow));

        System.out.println("[DI] particle added: " + text + " at " + pos);
    }

    private static String formatText(DamageInfo info, DamageIndicatorConfig cfg) {
        float dmg = info.amount;
        if (cfg.showPercentageText) {
            float pct = Math.abs(dmg) / info.entity.getMaxHealth() * 100f;
            String s = String.format("%.1f", pct);
            if (s.endsWith(".0")) s = s.substring(0, s.length() - 2);
            return (dmg > 0 ? "" : "+") + s + "%";
        } else {
            String s = String.format("%.1f", Math.abs(dmg));
            if (s.endsWith(".0")) s = s.substring(0, s.length() - 2);
            return (dmg > 0 ? "" : "+") + s;
        }
    }

    public static void tickAll() {
        PARTICLES.removeIf(TextParticle::tick);
    }

    public static void renderAll(com.mojang.blaze3d.vertex.PoseStack poseStack,
                                 net.minecraft.client.renderer.MultiBufferSource buffer,
                                 net.minecraft.client.Camera camera, float partialTick) {
        if (!PARTICLES.isEmpty()) {
            System.out.println("[DI] rendering " + PARTICLES.size() + " particles");
        }
        for (TextParticle p : PARTICLES) p.render(poseStack, buffer, camera, partialTick);
    }
}