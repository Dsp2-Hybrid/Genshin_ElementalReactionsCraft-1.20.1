package com.dsp2.genshinerc.common.registry;

import com.dsp2.genshinerc.GenshinERC;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = GenshinERC.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ResistanceAttributeEvents {
    private ResistanceAttributeEvents() {}

    @SubscribeEvent
    public static void onModification(EntityAttributeModificationEvent event) {
        for (EntityType<? extends LivingEntity> type : event.getTypes()) {
            for (RegistryObject<Attribute> attribute : ModAttributes.LIVING_ATTRIBUTES) {
                event.add(type, attribute.get());
            }
        }
    }
}
