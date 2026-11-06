package com.dsp2.genshinerc.common.world.entity.registry;
import com.dsp2.genshinerc.common.world.entity.UnitBootstrap;
import com.dsp2.genshinerc.common.world.entity.unit.TrainingUnit;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
public class UnitRegistry {
    public static final DeferredRegister<EntityType<?>> TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, UnitBootstrap.NS);
    public static final RegistryObject<EntityType<TrainingUnit>> TRAINING =
            TYPES.register("training",
                    () -> EntityType.Builder.of(TrainingUnit::new, MobCategory.MONSTER)
                            .sized(0.8F, 2.0F).clientTrackingRange(10).build("training"));
}