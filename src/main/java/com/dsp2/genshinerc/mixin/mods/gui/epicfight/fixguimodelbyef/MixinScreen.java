package com.dsp2.genshinerc.mixin.mods.gui.epicfight.fixguimodelbyef;

import com.dsp2.genshinerc.patch.core.gui.render.GuiRenderContext;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public class MixinScreen {

    @Inject(method = "render", at = @At("HEAD"))
    private void genshinerc$guiEnter(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        GuiRenderContext.enter();
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void genshinerc$guiExit(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        GuiRenderContext.exit();
    }
}