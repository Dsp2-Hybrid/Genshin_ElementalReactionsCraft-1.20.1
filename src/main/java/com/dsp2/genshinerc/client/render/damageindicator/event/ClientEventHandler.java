package com.dsp2.genshinerc.client.render.damageindicator.event;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "genshinerc", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientEventHandler {

    private static int tickCounter = 0;
    private static int renderCounter = 0;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent e) {
        if (e.phase == TickEvent.Phase.END) {
            DamageTracker.tickAll();
            if (++tickCounter % 100 == 0) {
                System.out.println("[DI] ClientTick alive, tick=" + tickCounter);
            }
        }
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent e) {
        if (e.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        if (++renderCounter % 100 == 0) {
            System.out.println("[DI] RenderLevelStage alive, count=" + renderCounter);
        }

        var cam = mc.gameRenderer.getMainCamera();
        PoseStack poseStack = e.getPoseStack();
        var buffer = mc.renderBuffers().bufferSource();

        DamageTracker.renderAll(poseStack, buffer, cam, e.getPartialTick());
        buffer.endBatch();
    }
}