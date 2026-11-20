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
                overrideColor = cfg.colorFor(keys[0]);
                second = cfg.colorFor(keys[1]);
            }
        }
        DamageTracker.addParticle(new DamageInfo(living, typeKey, amount, crit, kill, fullHeal, absorbed,
                overrideColor, overrideSize, absorbDelta, secondary, null, second));
    }
}