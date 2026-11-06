package com.dsp2.genshinerc.common.world.entity.registry;
import com.dsp2.genshinerc.common.world.entity.unit.TrainingUnit;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
public class StatBinding {
    public static void onCreation(EntityAttributeCreationEvent event) {
        event.put(UnitRegistry.TRAINING.get(), TrainingUnit.baseStats().build());
    }
    public static void onModification(EntityAttributeModificationEvent event) { }
}