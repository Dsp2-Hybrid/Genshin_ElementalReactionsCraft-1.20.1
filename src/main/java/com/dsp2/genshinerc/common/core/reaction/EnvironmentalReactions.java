package com.dsp2.genshinerc.common.core.reaction;

import com.dsp2.genshinerc.common.core.element.ElementAuraManager;
import com.dsp2.genshinerc.common.core.element.ElementType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "genshinerc")
public final class EnvironmentalReactions {
    private EnvironmentalReactions() {}
    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide() || entity.tickCount % 10 != 0) return;
        Level level = entity.level();
        BlockPos pos = entity.blockPosition();
        if (level.getFluidState(pos).is(Fluids.LAVA) && ElementAuraManager.hasAura(entity, ElementType.ELECTRO))
            ElementReactions.trigger(entity, null, ElementType.PYRO, 2.0f);
        if (entity.isOnFire() && level.getFluidState(pos).is(Fluids.WATER) && ElementAuraManager.hasAura(entity, ElementType.PYRO))
            ElementReactions.trigger(entity, null, ElementType.HYDRO, 2.0f);
    }
}