package com.dsp2.genshinerc.common.registry;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class ElementEffect extends MobEffect {
    public ElementEffect(int color) {
        super(MobEffectCategory.NEUTRAL, color);
    }
}
