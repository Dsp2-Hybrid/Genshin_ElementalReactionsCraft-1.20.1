package com.dsp2.genshinerc.client.render.damageindicator.event;

import com.dsp2.genshinerc.client.render.damageindicator.api.DamageColorPreset;
import net.minecraft.world.entity.LivingEntity;

public class DamageInfo {
    public final LivingEntity entity;
    public final float amount;
    public final boolean crit;
    public final DamageColorPreset preset;

    public DamageInfo(LivingEntity entity, float amount) {
        this(entity, amount, false, DamageColorPreset.WHITE);
    }

    public DamageInfo(LivingEntity entity, float amount, boolean crit, DamageColorPreset preset) {
        this.entity = entity;
        this.amount = amount;
        this.crit = crit;
        this.preset = preset;
    }

    public boolean isCrit() { return crit; }
    public boolean isHeal() { return amount < 0; }
}