package com.dsp2.genshinerc.common.world.entity.asset;
import com.dsp2.genshinerc.common.world.entity.UnitBootstrap;
import yesman.epicfight.api.animation.AnimationManager;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
@Mod.EventBusSubscriber(modid = UnitBootstrap.NS, bus = Mod.EventBusSubscriber.Bus.MOD)
public class AssetLoader {
    @SubscribeEvent
    public static void onAnimationRegistry(AnimationManager.AnimationRegistryEvent event) {
        event.newBuilder(UnitBootstrap.NS, Motions::build);
    }
}