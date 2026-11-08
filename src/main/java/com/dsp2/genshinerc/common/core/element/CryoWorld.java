package com.dsp2.genshinerc.common.core.element;

import com.dsp2.genshinerc.GenshinERC;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = GenshinERC.MODID)
public final class CryoWorld {
    private static final int R = 2;
    private static final int PERIOD = 10;

    private CryoWorld() {}

    @SubscribeEvent
    public static void onTick(LivingEvent.LivingTickEvent event) {
        LivingEntity e = event.getEntity();
        if (e.level().isClientSide() || !e.isAlive()) return;
        if ((e.tickCount + e.getId()) % PERIOD != 0) return;

        boolean cryo = ElementAuraManager.hasAura(e, ElementType.CRYO)
                || ElementAuraManager.hasAura(e, ElementType.FROZEN);
        boolean pyro = ElementAuraManager.hasAura(e, ElementType.PYRO)
                || ElementAuraManager.hasAura(e, ElementType.BURNING);
        if (!cryo && !pyro) return;

        Level level = e.level();
        BlockPos feet = e.blockPosition();
        for (BlockPos p : BlockPos.betweenClosed(feet.offset(-R, -1, -R), feet.offset(R, 1, R))) {
            BlockState s = level.getBlockState(p);
            if (cryo && s.is(Blocks.WATER)) {
                level.setBlockAndUpdate(p.immutable(), Blocks.ICE.defaultBlockState());
            } else if (pyro && (s.is(Blocks.ICE) || s.is(Blocks.FROSTED_ICE))) {
                level.setBlockAndUpdate(p.immutable(), Blocks.WATER.defaultBlockState());
            } else if (pyro && ElementAuraManager.hasAura(e, ElementType.FROZEN) && s.is(Blocks.SNOW)) {
                level.setBlockAndUpdate(p.immutable(), Blocks.WATER.defaultBlockState());
            }
        }
    }
}