package com.dsp2.genshinerc.mixin.mods.core.combat.epicfight;

import com.dsp2.genshinerc.common.core.combat.ef.ZhenMieCombatEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yesman.epicfight.skill.guard.GuardSkill;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.entity.eventlistener.TakeDamageEvent;

@Mixin(value = GuardSkill.class, remap = false)
public abstract class MixinGuardSkill {
    @Inject(method = "dealEvent", at = @At("HEAD"))
    private void genshinerc$onGuard(PlayerPatch<?> playerpatch, TakeDamageEvent.Attack event, boolean advanced, CallbackInfo ci) {
        ZhenMieCombatEvents.onGuard(playerpatch.getOriginal(), event);
    }
}