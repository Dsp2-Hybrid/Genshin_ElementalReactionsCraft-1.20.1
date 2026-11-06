package com.dsp2.genshinerc.common.world.entity.audio;
import com.dsp2.genshinerc.common.world.entity.UnitBootstrap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
public class Sounds {
    public static final DeferredRegister<SoundEvent> REGISTRY = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, UnitBootstrap.NS);
    public static final RegistryObject<SoundEvent> TRAINING_STRIKE = REGISTRY.register("training_strike",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(UnitBootstrap.NS, "training_strike")));
}