package com.dsp2.genshinerc.client.render.vfx.element.elevfx.fadeout;

import com.dsp2.genshinerc.client.render.vfx.aaapshader.ShaderPackAaaPostProcess;
import com.guhao.vix.client.aaaeffect.AAAEffectPostProcessRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import java.util.List;

@SuppressWarnings({"deprecation", "removal"})
@Mod.EventBusSubscriber(modid = "genshinerc", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class FadeOutSetup {
    private static final List<ResourceLocation> ELEMENT_EFFECTS = List.of(
            new ResourceLocation("genshinerc", "hydro_eff")
    );

    private FadeOutSetup() {}

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ElementOpacityProcessor alphaProcessor = new ElementOpacityProcessor();
            for (ResourceLocation id : ELEMENT_EFFECTS) {
                AAAEffectPostProcessRegistry.register(id, alphaProcessor);
                ShaderPackAaaPostProcess.register(id, alphaProcessor);
            }
        });
    }
}