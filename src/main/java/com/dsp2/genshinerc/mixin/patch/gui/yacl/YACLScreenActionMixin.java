package com.dsp2.genshinerc.mixin.patch.gui.yacl;

import com.dsp2.genshinerc.client.render.damageindicator.config.DamageIndicatorConfigYaclActionTracker;
import dev.isxander.yacl3.gui.YACLScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = YACLScreen.class, remap = false)
public class YACLScreenActionMixin {

    @Inject(method = "finishOrSave", at = @At("HEAD"), remap = false)
    private void genshinerc$onFinishOrSave(CallbackInfo ci) {
        DamageIndicatorConfigYaclActionTracker.setAction("finish");
    }

    @Inject(method = "cancelOrReset", at = @At("HEAD"), remap = false)
    private void genshinerc$onCancelOrReset(CallbackInfo ci) {
        DamageIndicatorConfigYaclActionTracker.setAction("cancel");
    }
}