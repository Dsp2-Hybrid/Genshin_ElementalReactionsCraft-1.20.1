package com.dsp2.genshinerc.mixin.client.render;

import com.dsp2.genshinerc.common.world.block.EleIceBlock;
import net.minecraft.client.renderer.chunk.RenderChunkRegion;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RenderChunkRegion.class)
public abstract class IceTerrainWaterMixin {
    @Inject(method = "getBlockState", at = @At("RETURN"), cancellable = true)
    private void genshinerc$originalWater(BlockPos pos, CallbackInfoReturnable<BlockState> callback) {
        BlockState state = callback.getReturnValue();
        if (state.getBlock() instanceof EleIceBlock) {
            callback.setReturnValue(EleIceBlock.originalWater(state));
        }
    }
}
