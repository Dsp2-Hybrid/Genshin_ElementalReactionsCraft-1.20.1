package com.dsp2.genshinerc.common.world.entity;
import com.dsp2.genshinerc.common.world.entity.audio.Sounds;
import com.dsp2.genshinerc.common.world.entity.fx.Particles;
import com.dsp2.genshinerc.common.world.entity.net.NetChannel;
import com.dsp2.genshinerc.common.world.entity.registry.AvatarBinding;
import com.dsp2.genshinerc.common.world.entity.registry.StatBinding;
import com.dsp2.genshinerc.common.world.entity.registry.UnitRegistry;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
@Mod.EventBusSubscriber(modid = UnitBootstrap.NS, bus = Mod.EventBusSubscriber.Bus.MOD)
public class UnitBootstrap {
    public static final String NS = "genshinerc";
    @SubscribeEvent public static void onConstruct(FMLConstructModEvent event) {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        UnitRegistry.TYPES.register(bus);
        Sounds.REGISTRY.register(bus);
        Particles.REGISTRY.register(bus);
    }
    @SubscribeEvent public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> { AvatarBinding.bind(); NetChannel.init(); });
    }
    @SubscribeEvent public static void onAttributeCreation(EntityAttributeCreationEvent event) { StatBinding.onCreation(event); }
    @SubscribeEvent public static void onAttributeModification(EntityAttributeModificationEvent event) { StatBinding.onModification(event); }
}