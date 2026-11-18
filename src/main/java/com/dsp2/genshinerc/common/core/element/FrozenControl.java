package com.dsp2.genshinerc.common.core.element;

import com.dsp2.genshinerc.common.config.ServerConfig;
import com.dsp2.genshinerc.common.core.reaction.ReactionDamage;
import com.dsp2.genshinerc.common.registry.ModAttributes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = "genshinerc")
public final class FrozenControl {
    public static final float MAX_GAUGE = 8f;
    private static final UUID SPEED_LOCK = UUID.fromString("c473a997-b0a7-4f7e-9b95-d0641937ddf2");
    private static final TagKey<EntityType<?>> IMMUNE = TagKey.create(
            Registries.ENTITY_TYPE, new ResourceLocation("genshinerc", "freeze_immune"));

    private FrozenControl() {}

    public static double resistance(LivingEntity entity) {
        if (entity.getType().is(IMMUNE)) return 1.0;
        AttributeInstance attr = entity.getAttribute(ModAttributes.FREEZE_RESISTANCE.get());
        return attr == null ? 0.0 : Math.max(0.0, Math.min(1.0, attr.getValue()));
    }

    public static boolean active(LivingEntity entity) {
        return entity.isAlive() && ElementAuraManager.hasAura(entity, ElementType.FROZEN);
    }

    static void apply(LivingEntity entity) {
        AttributeInstance speed = entity.getAttribute(Attributes.MOVEMENT_SPEED);
        double slow = -Math.min(1.0, (ServerConfig.FREEZE_SLOW_AMPLIFIER.get() + 1) * 0.15);
        if (speed != null) {
            AttributeModifier existing = speed.getModifier(SPEED_LOCK);
            if (existing == null || existing.getAmount() != slow) {
                if (existing != null) speed.removeModifier(SPEED_LOCK);
                speed.addTransientModifier(new AttributeModifier(
                        SPEED_LOCK, "genshinerc_frozen", slow, AttributeModifier.Operation.MULTIPLY_TOTAL));
            }
        }
        if (entity instanceof Mob mob) mob.getNavigation().stop();
        if (entity.isSprinting()) entity.setSprinting(false);
    }

    static void release(LivingEntity entity) {
        AttributeInstance speed = entity.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null && speed.getModifier(SPEED_LOCK) != null) speed.removeModifier(SPEED_LOCK);
    }

    @SubscribeEvent
    public static void onJump(LivingEvent.LivingJumpEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide() || !active(entity)) return;
        var velocity = entity.getDeltaMovement();
        entity.setDeltaMovement(velocity.x, Math.min(0.0, velocity.y), velocity.z);
        entity.hurtMarked = true;
    }

    @SubscribeEvent
    public static void onAttack(LivingAttackEvent event) {
        if (event.getEntity().level().isClientSide() || ReactionDamage.isDealing()) return;
        var source = event.getSource();
        if (source.getEntity() instanceof LivingEntity attacker
                && source.getDirectEntity() == attacker && attacker != event.getEntity() && active(attacker)) {
            event.setCanceled(true);
        }
    }
}
