package com.dsp2.genshinerc.patch.gui.render;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = "genshinerc",
        value = Dist.CLIENT,
        bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class GuiRenderContext {

    private static int depth = 0;

    public static boolean isInGui() { return depth > 0; }
    public static void enter() { depth++; }
    public static void exit()  { if (depth > 0) depth--; }

    @SubscribeEvent
    public static void onScreenPre(ScreenEvent.Render.Pre event)   { enter(); }

    @SubscribeEvent
    public static void onScreenPost(ScreenEvent.Render.Post event) { exit(); }

    @SubscribeEvent
    public static void onHudPre(RenderGuiEvent.Pre event)   { enter(); }

    @SubscribeEvent
    public static void onHudPost(RenderGuiEvent.Post event) { exit(); }
}