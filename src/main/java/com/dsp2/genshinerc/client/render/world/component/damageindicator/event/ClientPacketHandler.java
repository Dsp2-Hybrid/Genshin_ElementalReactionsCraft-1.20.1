package com.dsp2.genshinerc.client.render.world.component.damageindicator.event;

import com.dsp2.genshinerc.client.render.world.component.damageindicator.config.DamageIndicatorConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class ClientPacketHandler {
    public static void handleDamage(int entityId, String typeKey, float amount,
                                    boolean crit, boolean kill, boolean fullHeal, float absorbed,
                                    String overrideColor, Float overrideSize,
                                    boolean absorbDelta, boolean secondary) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        Entity e = mc.level.getEntity(entityId);
        if (!(e instanceof LivingEntity living)) return;
        String second = null;
        if (overrideColor != null && overrideColor.startsWith("@reaction:")) {
            String[] keys = overrideColor.substring("@reaction:".length()).split(",", -1);
            overrideColor = null;
            if (keys.length == 2) {
                var cfg = DamageIndicatorConfig.get();
                if ("anemo".equals(keys[0]) && !"anemo".equals(keys[1])) {
                    String main = cfg.colorFor(keys[1]);
                    String wind = cfg.colorFor("anemo");
                    overrideColor = main;
                    second = mix(main, wind, 0.5f);
                } else {
                    overrideColor = cfg.colorFor(keys[0]);
                    second = cfg.colorFor(keys[1]);
                }
            }
        }
        DamageTracker.addParticle(new DamageInfo(living, typeKey, amount, crit, kill, fullHeal, absorbed,
                overrideColor, overrideSize, absorbDelta, secondary, null, second));
    }

    private static String mix(String a, String b, float t) {
        try {
            String pa = a.startsWith("#") ? a.substring(1) : a;
            String pb = b.startsWith("#") ? b.substring(1) : b;
            if (pa.length() != pb.length() || pa.length() % 2 != 0) return a;
            StringBuilder sb = new StringBuilder("#");
            for (int i = 0; i < pa.length(); i += 2) {
                int x = Integer.parseInt(pa.substring(i, i + 2), 16);
                int y = Integer.parseInt(pb.substring(i, i + 2), 16);
                sb.append(String.format("%02X", Math.round(x + (y - x) * t)));
            }
            return sb.toString();
        } catch (RuntimeException ex) {
            return a;
        }
    }
}