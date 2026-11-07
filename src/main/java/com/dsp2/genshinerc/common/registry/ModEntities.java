package com.dsp2.genshinerc.common.registry;

import com.dsp2.genshinerc.GenshinERC;
import com.dsp2.genshinerc.common.core.reaction.DendroCoreEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> REGISTRY =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, GenshinERC.MODID);

    public static final RegistryObject<EntityType<DendroCoreEntity>> DENDRO_CORE = REGISTRY.register("dendro_core",
            () -> EntityType.Builder.<DendroCoreEntity>of(DendroCoreEntity::new, MobCategory.MISC)
                    .sized(0.4f, 0.4f).clientTrackingRange(8).updateInterval(20).fireImmune().noSave()
                    .build("dendro_core"));

    private ModEntities() {}
}
