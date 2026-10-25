package com.dsp2.genshinerc.mixin.mods.core.combat.epicfight;

import com.dsp2.genshinerc.client.render.hud.bar.config.BarConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yesman.epicfight.client.gui.BattleModeGui;

@Mixin(value = BattleModeGui.class, remap = false)
public abstract class MixinBattleModeGui {

    @Inject(method = "renderStaminaBar", at = @At("HEAD"), cancellable = true)
    private void genshinerc$hideStamina(CallbackInfo ci) {
        BarConfig cfg = BarConfig.get();
        if (cfg.rpgHudEnabled && cfg.rpgHideEfHud) ci.cancel();
    }

    @Inject(method = "renderNormalSkills", at = @At("HEAD"), cancellable = true)
    private void genshinerc$hideSkills(CallbackInfo ci) {
        BarConfig cfg = BarConfig.get();
        if (cfg.rpgHudEnabled && cfg.rpgHideEfHud) ci.cancel();
    }

    @Inject(method = "renderWeaponInnateSkill", at = @At("HEAD"), cancellable = true)
    private void genshinerc$hideInnate(CallbackInfo ci) {
        BarConfig cfg = BarConfig.get();
        if (cfg.rpgHudEnabled && cfg.rpgHideEfHud) ci.cancel();
    }

    @Inject(method = "renderCharingBar", at = @At("HEAD"), cancellable = true)
    private void genshinerc$hideCharging(CallbackInfo ci) {
        BarConfig cfg = BarConfig.get();
        if (cfg.rpgHudEnabled && cfg.rpgHideEfHud) ci.cancel();
    }
}