package com.dsp2.genshinerc.util.framesequence.render;

import com.dsp2.genshinerc.util.framesequence.FrameSequence;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public class FrameRenderer {

    public static void render(GuiGraphics guiGraphics, FrameSequence sequence, int x, int y) {
        ResourceLocation frame = sequence.getCurrentFrame();

        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO
        );
        RenderSystem.setShaderColor(1F, 1F, 1F, sequence.getAlpha());

        guiGraphics.blit(
                frame,
                x, y,
                0, 0,
                sequence.getWidth(), sequence.getHeight(),
                sequence.getWidth(), sequence.getHeight()
        );

        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        RenderSystem.disableBlend();
    }
}