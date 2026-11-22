package com.dsp2.genshinerc.common.core.damage;

import com.dsp2.genshinerc.common.core.reaction.ReactionDamage;
import com.dsp2.genshinerc.common.level.EntityLevelManager;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "genshinerc")
public final class StatusDamageScaling {
    private static final float LEVEL_SCALE = 0.035f;
    private static final float MAX_SCALE = 6.0f;

    private StatusDamageScaling() {}

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onHurt(LivingHurtEvent event) {
        LivingEntity victim = event.getEntity();
        DamageSource source = event.getSource();

        if (victim.level().isClientSide()) {
            return;
        }

        if (event.getAmount() <= 0f) {
            return;
        }

        if (ReactionDamage.isDealing()) {
            return;
        }

        if (!isStatusDamage(source)) {
            return;
        }

        if (!(source.getEntity() instanceof LivingEntity attacker)) {
            return;
        }

        int level = Math.max(
                0,
                Math.min(120, EntityLevelManager.getLevel(attacker)));

        float multiplier = Math.min(
                MAX_SCALE,
                1f + level * LEVEL_SCALE);

        event.setAmount(event.getAmount() * multiplier);
    }

    private static boolean isStatusDamage(DamageSource source) {
        return source.is(DamageTypes.MAGIC)
                || source.is(DamageTypes.WITHER)
                || source.is(DamageTypes.DROWN)
                || source.is(DamageTypeTags.IS_LIGHTNING)
                || source.is(DamageTypeTags.IS_FIRE);
    }
}