package com.dsp2.genshinerc.client.render.entity.elereaction.overlay;

import com.dsp2.genshinerc.GenshinERC;
import com.dsp2.genshinerc.common.registry.block.ModBlocks;
import com.dsp2.genshinerc.common.world.block.EleIceBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = GenshinERC.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class SpecialIceColors {
    private SpecialIceColors() {}

    @SubscribeEvent
    public static void register(RegisterColorHandlersEvent.Block event) {
        event.register(SpecialIceColors::color, ModBlocks.SPECIAL_ICE.get());
    }

    private static int color(BlockState state, BlockAndTintGetter level, BlockPos pos, int tintIndex) {
        if (tintIndex != 0) return -1;
        int age = state.getValue(EleIceBlock.AGE);
        int alpha = switch (age) {
            case 0 -> 220;
            case 1 -> 185;
            case 2 -> 145;
            case 3 -> 105;
            default -> 70;
        };
        int red = Math.min(255, 220 + age * 7);
        int green = Math.min(255, 244 + age * 2);
        return (alpha << 24) | (red << 16) | (green << 8) | 255;
    }
}