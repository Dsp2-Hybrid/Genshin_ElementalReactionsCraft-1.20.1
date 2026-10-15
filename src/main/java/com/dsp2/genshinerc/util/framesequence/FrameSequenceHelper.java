package com.dsp2.genshinerc.util.framesequence;

import com.dsp2.genshinerc.util.framesequence.render.FrameRenderer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

/**
 * 库对外的唯一入口。
 */
public final class FrameSequenceHelper {

    private FrameSequenceHelper() {}

    public static FrameSequenceBuilder startBuilder(ResourceLocation id) {
        return FrameSequence.startBuilder(id);
    }

    public static void render(GuiGraphics guiGraphics, FrameSequence sequence, int x, int y) {
        FrameRenderer.render(guiGraphics, sequence, x, y);
    }
}