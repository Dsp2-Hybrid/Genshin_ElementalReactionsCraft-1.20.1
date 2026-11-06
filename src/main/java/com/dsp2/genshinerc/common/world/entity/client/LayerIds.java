package com.dsp2.genshinerc.common.world.entity.client;
import com.dsp2.genshinerc.common.world.entity.UnitBootstrap;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

@SuppressWarnings({"deprecation", "removal"})
public class LayerIds {
    public static final ModelLayerLocation UNIT = new ModelLayerLocation(new ResourceLocation(UnitBootstrap.NS, "unit_layer"), "main");
}