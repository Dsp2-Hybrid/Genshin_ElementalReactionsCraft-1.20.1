package com.dsp2.genshinerc.mixin.damageindicator;

import com.dsp2.genshinerc.client.render.damageindicator.event.DamageInfo;
import com.dsp2.genshinerc.client.render.damageindicator.event.DamageTracker;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


/*@Mixin(LivingEntity.class)
public class LivingEntityTickMixin {
    @Unique
    private float genshinerc$lastHealth = -1f;

    @Inject(method = "tick()V", at = @At("TAIL"))
    private void genshinerc$onTick(CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (self.level().isClientSide()) {
            if (genshinerc$lastHealth < 0f) {
                genshinerc$lastHealth = self.getHealth();
                return;
            }
            if (genshinerc$lastHealth != self.getHealth()) {
                float delta = genshinerc$lastHealth - self.getHealth();
                genshinerc$lastHealth = self.getHealth();
                if (Math.abs(delta) > 0.001f) {
                    DamageTracker.reportDamage(new DamageInfo(self, delta));
                }
            }
        }
    }
}
*/