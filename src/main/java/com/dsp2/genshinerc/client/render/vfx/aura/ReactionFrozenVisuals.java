package com.dsp2.genshinerc.client.render.vfx.aura;

import com.dsp2.genshinerc.common.core.element.ElementAuraManager;
import com.dsp2.genshinerc.common.core.element.ElementType;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "genshinerc", value = Dist.CLIENT)
public final class ReactionFrozenVisuals {
    private ReactionFrozenVisuals() {}

    @SubscribeEvent
    public static void onRenderPre(RenderLivingEvent.Pre<?, ?> event) {
        if (event.getEntity().level().isClientSide() && ElementAuraManager.hasAura(event.getEntity(), ElementType.FROZEN)) {
            float time = (System.currentTimeMillis() % 2000) / 2000f;
            float pulse = Mth.sin(time * 3.14159f * 2) * 0.15f + 0.85f;
            RenderSystem.setShaderColor(0.5f * pulse, 0.7f * pulse, 1.0f, 1.0f);
        }
    }

    @SubscribeEvent
    public static void onRenderPost(RenderLivingEvent.Post<?, ?> event) {
        if (event.getEntity().level().isClientSide() && ElementAuraManager.hasAura(event.getEntity(), ElementType.FROZEN)) {
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        }
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && ElementAuraManager.hasAura(mc.player, ElementType.FROZEN)) {
            int w = mc.getWindow().getGuiScaledWidth();
            int h = mc.getWindow().getGuiScaledHeight();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShaderColor(0.6f, 0.8f, 1.0f, 0.4f);
            event.getGuiGraphics().fill(0, 0, w, h, 0xAA99CCFF);
            RenderSystem.disableBlend();
        }
    }
}