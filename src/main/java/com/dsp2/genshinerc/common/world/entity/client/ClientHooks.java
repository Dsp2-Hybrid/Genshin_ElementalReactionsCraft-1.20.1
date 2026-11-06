package com.dsp2.genshinerc.common.world.entity.client;

import com.dsp2.genshinerc.common.world.entity.UnitBootstrap;
import com.dsp2.genshinerc.common.world.entity.client.model.ShellModel;
import com.dsp2.genshinerc.common.world.entity.client.renderer.TrainingAvatarRenderer;
import com.dsp2.genshinerc.common.world.entity.client.renderer.UnitShellRenderer;
import com.dsp2.genshinerc.common.world.entity.registry.UnitRegistry;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import yesman.epicfight.api.client.forgeevent.PatchedRenderersEvent;

@Mod.EventBusSubscriber(modid = UnitBootstrap.NS, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientHooks {
    
    @SubscribeEvent 
    public static void layers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(LayerIds.UNIT, ShellModel::createBodyLayer);
    }
    
    @SubscribeEvent 
    public static void renderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(UnitRegistry.TRAINING.get(), UnitShellRenderer::new);
    }
    
    @SubscribeEvent 
    public static void patched(PatchedRenderersEvent.Add event) {
        event.addPatchedEntityRenderer(UnitRegistry.TRAINING.get(), (type) -> 
            new TrainingAvatarRenderer(UnitShellRenderer.CONTEXT, UnitRegistry.TRAINING.get())
        );
    }
}