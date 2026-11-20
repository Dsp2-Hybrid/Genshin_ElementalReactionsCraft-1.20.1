package com.dsp2.genshinerc.mixin.mods.core.combat.epicfight;

import com.dsp2.genshinerc.common.core.combat.epicfight.EfElementalHit;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yesman.epicfight.api.animation.types.AttackAnimation;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.damagesource.EpicFightDamageSource;

@Mixin(value = AttackAnimation.class, remap = false)
public abstract class MixinAttackAnimationImbue {
    @Inject(method = "begin(Lyesman/epicfight/world/capabilities/entitypatch/LivingEntityPatch;)V", at = @At("HEAD"))
    private void genshinerc$beginAttachmentCast(LivingEntityPatch<?> patch, CallbackInfo ci) {
        EfElementalHit.beginCast(patch.getOriginal(), (AttackAnimation) (Object) this);
    }

    @Inject(method = "getEpicFightDamageSource(Lyesman/epicfight/world/capabilities/entitypatch/LivingEntityPatch;Lnet/minecraft/world/entity/Entity;Lyesman/epicfight/api/animation/types/AttackAnimation$Phase;)Lyesman/epicfight/world/damagesource/EpicFightDamageSource;",
            at = @At("RETURN"))
    private void genshinerc$elementalType(LivingEntityPatch<?> patch, Entity target, AttackAnimation.Phase phase,
                                        CallbackInfoReturnable<EpicFightDamageSource> cir) {
        EfElementalHit.convert(patch.getOriginal(), cir.getReturnValue(), (AttackAnimation) (Object) this, phase);
    }
}
