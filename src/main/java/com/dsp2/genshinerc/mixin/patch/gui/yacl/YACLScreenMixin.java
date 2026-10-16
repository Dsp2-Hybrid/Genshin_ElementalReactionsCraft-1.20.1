package com.dsp2.genshinerc.mixin.patch.gui.yacl;

import com.dsp2.genshinerc.client.render.damageindicator.config.ConfigSelectScreen;
import dev.isxander.yacl3.gui.YACLScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(YACLScreen.class)
public abstract class YACLScreenMixin extends Screen {

    protected YACLScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "shouldCloseOnEsc", at = @At("HEAD"))
    private void genshinerc$markEsc(CallbackInfoReturnable<Boolean> cir) {
        ConfigSelectScreen.escPressed = true;
    }

    @Inject(method = "onClose", at = @At("HEAD"), cancellable = true)
    private void genshinerc$onClose(CallbackInfo ci) {
        boolean isEsc = ConfigSelectScreen.escPressed;
        ConfigSelectScreen.escPressed = false;
        if (!isEsc && ConfigSelectScreen.lastModListParent != null) {
            this.minecraft.setScreen(ConfigSelectScreen.lastModListParent);
            ci.cancel();
        }
    }
}