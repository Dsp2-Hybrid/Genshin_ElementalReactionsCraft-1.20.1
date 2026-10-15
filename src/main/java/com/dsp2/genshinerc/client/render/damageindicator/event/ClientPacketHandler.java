package com.dsp2.genshinerc.client.render.damageindicator.event;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class ClientPacketHandler {
    public static void handleDamage(int entityId, String typeKey, float amount,
                                    boolean crit, boolean kill, float absorbed,
                                    String overrideColor, Float overrideSize,
                                    boolean absorbDelta, boolean secondary) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        Entity e = mc.level.getEntity(entityId);
        if (!(e instanceof LivingEntity living)) return;
        DamageTracker.addParticle(new DamageInfo(living, typeKey, amount, crit, kill, absorbed,
                overrideColor, overrideSize, absorbDelta, secondary));
    }
}