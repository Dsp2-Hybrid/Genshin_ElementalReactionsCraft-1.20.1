package com.dsp2.genshinerc.common.registry.block;

import com.dsp2.genshinerc.GenshinERC;
import com.dsp2.genshinerc.common.world.block.EleIceBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlocks {
    public static final DeferredRegister<Block> REGISTRY = DeferredRegister.create(ForgeRegistries.BLOCKS, GenshinERC.MODID);
    public static final RegistryObject<Block> SPECIAL_ICE = REGISTRY.register("special_ice", () -> new EleIceBlock(BlockBehaviour.Properties.of()
            .mapColor(MapColor.ICE).friction(0.98F).strength(0.5F).sound(SoundType.GLASS).noOcclusion().noLootTable()
            .isRedstoneConductor((s, l, p) -> false).isViewBlocking((s, l, p) -> false).isSuffocating((s, l, p) -> false)));
    private ModBlocks() {}
}
