package com.dsp2.genshinerc.common.core.combat;

import com.dsp2.genshinerc.common.core.combat.shield.ShieldManager;
import com.dsp2.genshinerc.common.core.damage.DamageKind;
import com.dsp2.genshinerc.common.core.damage.ElementalMath;
import com.dsp2.genshinerc.common.core.element.FrozenControl;
import com.dsp2.genshinerc.common.core.reaction.ElementReactions;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "genshinerc")
public final class FinalCombatHooks {
    private static final float VANILLA_BASE_HEALTH = 20f;

    private FinalCombatHooks() {}

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onKnockBack(LivingKnockBackEvent event) {
        LivingEntity entity = event.getEntity();
        if (!ShieldManager.protectsFromImpact(entity)) return;
        event.setCanceled(true);
        Vec3 velocity = entity.getDeltaMovement();
        entity.setDeltaMovement(velocity.x * 0.08, velocity.y, velocity.z * 0.08);
        entity.hurtMarked = true;
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onFall(LivingFallEvent event) {
        LivingEntity entity = event.getEntity();
        if (!FrozenControl.active(entity)) return;
        float vanillaDamage = Mth.ceil(Math.max(0f, event.getDistance() - 3f) * event.getDamageMultiplier());
        var attribute = DamageKind.PHYSICAL.resistanceAttribute();
        var instance = attribute == null ? null : entity.getAttribute(attribute);
        double resistance = instance == null ? 1.0 : ElementalMath.resistanceMultiplier(instance.getValue());
        double expectedDamage = vanillaDamage / VANILLA_BASE_HEALTH * entity.getMaxHealth() * resistance;
        if (expectedDamage > entity.getMaxHealth() * 0.5 && ElementReactions.blunt(entity, null)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onFrozenWaterTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide() || !FrozenControl.active(entity)
                || (!entity.isInWater() && !entity.isUnderWater())) return;
        Vec3 velocity = entity.getDeltaMovement();
        entity.setDeltaMovement(velocity.x * 0.25, 0.0, velocity.z * 0.25);
        entity.setNoGravity(true);
        entity.hasImpulse = true;
        entity.hurtMarked = true;
    }
}