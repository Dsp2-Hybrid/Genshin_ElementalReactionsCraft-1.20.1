package com.dsp2.genshinerc.common.core.reaction;

import com.dsp2.genshinerc.common.core.element.ElementType;
import net.minecraft.world.entity.LivingEntity;

public final class DrownReaction {
    private static final float HYDRO_UNITS = 1.0f;

    private DrownReaction() {}

    public static float trigger(LivingEntity entity) {
        ReactionResult result = ElementReactions.trigger(entity, null, ElementType.HYDRO, HYDRO_UNITS);
        return (float) Math.max(1.0, result.amplify());
    }
}