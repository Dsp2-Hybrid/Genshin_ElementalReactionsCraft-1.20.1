package com.dsp2.genshinerc.mixin.patch.gui.yacl;

import com.dsp2.genshinerc.client.render.bar.config.BarConfig;
import com.dsp2.genshinerc.common.config.ConfigListFormatErrorScreen;
import dev.isxander.yacl3.gui.YACLScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(YACLScreen.class)
public class YACLListFormatErrorScreenEventCallbackMixin {

    private boolean genshinerc$shouldIntercept() {
        YACLScreen self = (YACLScreen) (Object) this;
        if (BarConfig.PENDING_LIST_SCREEN != self || BarConfig.PENDING_LIST_OPTION == null) {
            return false;
        }

        self.setFocused(null);

        return BarConfig.hasInvalidEntityYOffset(BarConfig.PENDING_LIST_OPTION.pendingValue());
    }

    private void genshinerc$showError() {
        Minecraft mc = Minecraft.getInstance();
        mc.execute(() -> {
            mc.setScreen(new ConfigListFormatErrorScreen(
                    BarConfig.PENDING_LIST_SCREEN,
                    BarConfig.PENDING_LIST_PARENT,
                    Component.translatable("genshinerc.bar.config.entity_y_offsets.error_message"),
                    Component.translatable("genshinerc.bar.config.entity_y_offsets.error_continue"),
                    Component.translatable("genshinerc.bar.config.entity_y_offsets.error_exit")
            ));
        });
    }

    @Inject(method = "onClose", at = @At("HEAD"), cancellable = true, remap = false)
    private void genshinerc$onClose(CallbackInfo ci) {
        if (genshinerc$shouldIntercept()) {
            ci.cancel();
            genshinerc$showError();
        }
    }

    @Inject(method = "finishOrSave", at = @At("HEAD"), cancellable = true, remap = false)
    private void genshinerc$onFinishOrSave(CallbackInfo ci) {
        if (genshinerc$shouldIntercept()) {
            ci.cancel();
            genshinerc$showError();
        }
    }
}