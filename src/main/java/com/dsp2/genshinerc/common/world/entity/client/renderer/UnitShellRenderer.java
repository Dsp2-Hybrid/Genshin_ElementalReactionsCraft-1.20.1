package com.dsp2.genshinerc.common.world.entity.client.renderer;

import com.dsp2.genshinerc.common.world.entity.client.LayerIds;
import com.dsp2.genshinerc.common.world.entity.client.model.ShellModel;
import com.dsp2.genshinerc.common.world.entity.unit.TrainingUnit;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

@SuppressWarnings({"deprecation", "removal"})
public class UnitShellRenderer extends MobRenderer<TrainingUnit, ShellModel> {
    
    public static EntityRendererProvider.Context CONTEXT;
    
    private static final ResourceLocation TEX = new ResourceLocation("minecraft", "textures/entity/zombie/zombie.png");
    
    public UnitShellRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new ShellModel(ctx.bakeLayer(LayerIds.UNIT)), 0.5F);
        CONTEXT = ctx;
    }
    
    @Override 
    public ResourceLocation getTextureLocation(TrainingUnit e) { 
        return TEX; 
    }
}