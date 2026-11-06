package com.dsp2.genshinerc.common.world.entity.fx;
import com.dsp2.genshinerc.common.world.entity.UnitBootstrap;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
public class Particles {
    public static final DeferredRegister<ParticleType<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, UnitBootstrap.NS);
    public static final RegistryObject<SimpleParticleType> STRIKE_IMPACT = REGISTRY.register("strike_impact",
            () -> new SimpleParticleType(false));
}