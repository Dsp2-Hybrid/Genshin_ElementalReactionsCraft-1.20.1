package com.dsp2.genshinerc.client.render.hud.gui.rpg.render;

import com.dsp2.genshinerc.client.render.hud.gui.rpg.RpgHudElement.Box;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;

import java.util.function.Consumer;

public final class HudMirror {
    private static int depth;

    private HudMirror() {}

    public static boolean active() { return depth > 0; }

    public static void draw(GuiGraphics g, Box signed, Consumer<Box> body) {
        Box b = signed.abs();
        boolean fx = signed.flipX(), fy = signed.flipY();
        if (!fx && !fy) { body.accept(b); return; }
        PoseStack ps = g.pose();
        ps.pushPose();
        ps.translate(b.cx(), b.cy(), 0f);
        ps.scale(fx ? -1f : 1f, fy ? -1f : 1f, 1f);
        ps.translate(-b.cx(), -b.cy(), 0f);
        if (depth++ == 0) RenderSystem.disableCull();
        try {
            body.accept(b);
        } finally {
            if (--depth == 0) RenderSystem.enableCull();
            ps.popPose();
        }
    }
}