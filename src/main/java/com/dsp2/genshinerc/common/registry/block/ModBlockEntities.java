package com.dsp2.genshinerc.common.registry.block;

import com.dsp2.genshinerc.GenshinERC;
import com.dsp2.genshinerc.common.world.block.entity.SpecialIceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> REGISTRY =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, GenshinERC.MODID);

    public static final RegistryObject<BlockEntityType<SpecialIceBlockEntity>> SPECIAL_ICE =
            REGISTRY.register("special_ice", () -> BlockEntityType.Builder
                    .of(SpecialIceBlockEntity::new, ModBlocks.SPECIAL_ICE.get())
                    .build(null));

    private ModBlockEntities() {}
}