package com.dsp2.genshinerc.patch.core.gui.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import java.lang.reflect.Method;
import java.util.Map;

public class GuiRenderHelper {

    private static Map<EntityType<?>, EntityRenderer<?>> vanillaRenderers;
    private static Map<String, EntityRenderer<? extends Player>> vanillaPlayerRenderers;

    @SuppressWarnings("unchecked")
    public static void init() {
        Minecraft mc = Minecraft.getInstance();

        EntityRendererProvider.Context context = new EntityRendererProvider.Context(
                mc.getEntityRenderDispatcher(),
                mc.getItemRenderer(),
                mc.getBlockRenderer(),
                mc.getEntityRenderDispatcher().getItemInHandRenderer(),
                mc.getResourceManager(),
                mc.getEntityModels(),
                mc.font
        );

        try {
            Method createEntityMethod = ObfuscationReflectionHelper.findMethod(
                    EntityRenderers.class, "createEntityRenderers", EntityRendererProvider.Context.class);
            createEntityMethod.setAccessible(true);
            vanillaRenderers = (Map<EntityType<?>, EntityRenderer<?>>) createEntityMethod.invoke(null, context);

            Method createPlayerMethod = ObfuscationReflectionHelper.findMethod(
                    EntityRenderers.class, "createPlayerRenderers", EntityRendererProvider.Context.class);
            createPlayerMethod.setAccessible(true);
            vanillaPlayerRenderers = (Map<String, EntityRenderer<? extends Player>>) createPlayerMethod.invoke(null, context);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static EntityRenderer<Entity> getVanillaRenderer(Entity entity) {
        if (vanillaRenderers == null || vanillaPlayerRenderers == null) {
            init();
        }

        EntityRenderer renderer = null;

        try {
            if (entity instanceof AbstractClientPlayer) {
                if (vanillaPlayerRenderers != null) {
                    renderer = vanillaPlayerRenderers.get("default");
                }
            } else {
                if (vanillaRenderers != null) {
                    renderer = vanillaRenderers.get(entity.getType());
                }
            }
        } catch (Exception ignored) {
        }

        if (renderer == null) {
            return (EntityRenderer<Entity>) (EntityRenderer) Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(entity);
        }
        return (EntityRenderer<Entity>) renderer;
    }
}