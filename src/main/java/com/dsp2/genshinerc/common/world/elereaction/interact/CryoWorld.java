package com.dsp2.genshinerc.common.world.elereaction.interact;

import com.dsp2.genshinerc.common.core.element.ElementAuraManager;
import com.dsp2.genshinerc.common.core.element.ElementType;
import com.dsp2.genshinerc.common.core.reaction.ElementReactions;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "genshinerc")
public final class CryoWorld {
    private CryoWorld() {}
    @SubscribeEvent
    public static void onTick(LivingEvent.LivingTickEvent event) {
        LivingEntity e = event.getEntity();
        if (e.level().isClientSide() || e.tickCount % 10 != 0) return;
        Level lv = e.level();
        BlockPos p = e.blockPosition();
        if (ElementAuraManager.hasAura(e, ElementType.CRYO) && lv.getFluidState(p).is(Fluids.WATER)) {
            for (int dx = -1; dx <= 1; dx++)
                for (int dz = -1; dz <= 1; dz++) {
                    BlockPos q = p.offset(dx, 0, dz);
                    if (lv.getBlockState(q).is(Blocks.WATER)) lv.setBlockAndUpdate(q, Blocks.ICE.defaultBlockState());
                }
        }
        if (e.isOnFire() && lv.getBlockState(p.below()).is(Blocks.ICE)) {
            lv.setBlockAndUpdate(p.below(), Blocks.WATER.defaultBlockState());
            if (ElementAuraManager.hasAura(e, ElementType.PYRO)) ElementReactions.trigger(e, null, ElementType.CRYO, 1.0f);
        }
    }
}