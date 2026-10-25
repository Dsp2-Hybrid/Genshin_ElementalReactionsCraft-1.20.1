package com.dsp2.genshinerc.mixin.ef;

import com.dsp2.genshinerc.client.render.bar.config.BarConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yesman.epicfight.client.gui.BattleModeGui;

@Mixin(value = BattleModeGui.class, remap = false)
public abstract class MixinBattleModeGui {
    @Inject(method = "renderGui", at = @At("HEAD"), cancellable = true)
    private void genshinerc$hide(CallbackInfo ci) {
        BarConfig cfg = BarConfig.get();
        if (cfg.rpgHudEnabled && cfg.rpgHideEfHud) ci.cancel();
    }
}