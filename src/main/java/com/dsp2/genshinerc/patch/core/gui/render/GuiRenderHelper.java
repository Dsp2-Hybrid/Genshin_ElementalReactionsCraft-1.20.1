package com.dsp2.genshinerc.patch.core.gui.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;

public class GuiRenderHelper {

    @SuppressWarnings("unchecked")
    public static EntityRenderer<Entity> getVanillaRenderer(Entity entity) {
        var dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        EntityRenderer<?> renderer = dispatcher.getRenderer(entity);
        return (EntityRenderer<Entity>) renderer;
    }
}