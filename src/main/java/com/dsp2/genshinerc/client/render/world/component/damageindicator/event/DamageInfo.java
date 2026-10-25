package com.dsp2.genshinerc.client.render.world.component.damageindicator.event;

import net.minecraft.world.entity.LivingEntity;

public class DamageInfo {
    public final LivingEntity entity;
    public final String typeKey;
    public final float amount;
    public final boolean crit, kill, fullHeal;
    public final float absorbed;
    public final String overrideColor;
    public final Float overrideSize;
    public final boolean absorbDelta;
    public final boolean secondary;

    public DamageInfo(LivingEntity e, String t, float a, boolean c, boolean k, boolean fh, float ab,
                      String oc, Float os, boolean absorbDelta, boolean secondary) {
        this.entity = e; this.typeKey = t; this.amount = a;
        this.crit = c; this.kill = k; this.fullHeal = fh;
        this.absorbed = ab;
        this.overrideColor = oc; this.overrideSize = os;
        this.absorbDelta = absorbDelta; this.secondary = secondary;
    }

    public boolean isHeal() { return amount < 0; }
    public boolean hasAbsorb() { return absorbed > 0.001f; }
    public boolean hasOverrideColor() { return overrideColor != null; }
    public boolean hasOverrideSize() { return overrideSize != null; }
}