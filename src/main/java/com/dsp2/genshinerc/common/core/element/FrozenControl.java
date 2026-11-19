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
    private static final UUID CRYO_SLOW = UUID.fromString("44ddcab4-aa3c-4e4e-b422-f67d38157f65");
    private static final double CRYO_SPEED_PENALTY = -0.15;
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

    public static boolean locked(LivingEntity entity) {
        if (!entity.isAlive() || entity.isSpectator()) return false;
        if (entity.level().isClientSide()) {
            AttributeInstance speed = entity.getAttribute(Attributes.MOVEMENT_SPEED);
            return speed != null && speed.getModifier(SPEED_LOCK) != null;
        }
        return active(entity) || com.dsp2.genshinerc.common.core.combat.zhenmie.ZhenMieManager.staggered(entity);
    }

    public static void refresh(LivingEntity entity) {
        if (entity.level().isClientSide()) return;
        boolean locked = locked(entity);
        AttributeInstance speed = entity.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null) {
            modifier(speed, SPEED_LOCK, "genshinerc_action_lock", -1.0, locked);
            modifier(speed, CRYO_SLOW, "genshinerc_cryo_aura", CRYO_SPEED_PENALTY,
                    entity.isAlive() && !locked && ElementAuraManager.hasAura(entity, ElementType.CRYO));
        }
        if (!locked) return;
        if (entity instanceof Mob mob) mob.getNavigation().stop();
        if (entity.isUsingItem()) entity.stopUsingItem();
        if (entity.isSprinting()) entity.setSprinting(false);
    }

    private static void modifier(AttributeInstance attribute, UUID id, String name, double amount, boolean enabled) {
        AttributeModifier current = attribute.getModifier(id);
        if (current != null && (!enabled || current.getAmount() != amount)) {
            attribute.removeModifier(id);
            current = null;
        }
        if (enabled && current == null) {
            attribute.addTransientModifier(new AttributeModifier(id, name, amount, AttributeModifier.Operation.MULTIPLY_TOTAL));
        }
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (!entity.level().isClientSide() && locked(entity)) refresh(entity);
    }

    @SubscribeEvent
    public static void onInteract(net.minecraftforge.event.entity.player.PlayerInteractEvent event) {
        if (event.isCancelable() && locked(event.getEntity())) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onPlayerAttack(net.minecraftforge.event.entity.player.AttackEntityEvent event) {
        if (locked(event.getEntity())) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onUse(net.minecraftforge.event.entity.living.LivingEntityUseItemEvent.Start event) {
        if (locked(event.getEntity())) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onJump(LivingEvent.LivingJumpEvent event) {
        LivingEntity entity = event.getEntity();
        if (!locked(entity)) return;
        var velocity = entity.getDeltaMovement();
        entity.setDeltaMovement(velocity.x, Math.min(0.0, velocity.y), velocity.z);
        entity.hurtMarked = true;
    }

    @SubscribeEvent
    public static void onAttack(LivingAttackEvent event) {
        if (event.getEntity().level().isClientSide() || ReactionDamage.isDealing()) return;
        var source = event.getSource();
        if (source.getEntity() instanceof LivingEntity attacker
                && source.getDirectEntity() == attacker && attacker != event.getEntity() && locked(attacker)) {
            event.setCanceled(true);
        }
    }
}
