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
        float progress = state.getValue(EleIceBlock.AGE) / 4f;
        float fade = progress * progress * (3f - 2f * progress);
        int alpha = Math.round(220f - 150f * fade);
        int red = Math.round(190f + 58f * progress);
        int green = Math.round(224f + 29f * progress);
        return (alpha << 24) | (red << 16) | (green << 8) | 255;
    }
}