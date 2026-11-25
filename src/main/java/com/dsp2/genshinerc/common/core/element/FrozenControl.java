package com.dsp2.genshinerc.common.core.element;

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
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Witch;
import net.minecraftforge.event.entity.EntityTeleportEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

@SuppressWarnings({"deprecation", "removal"})
@Mod.EventBusSubscriber(modid = "genshinerc")
public final class FrozenControl {
    public static final float MAX_GAUGE = 8f;

    private static final UUID SPEED_LOCK =
            UUID.fromString("c473a997-b0a7-4f7e-9b95-d0641937ddf2");
    private static final UUID CRYO_SLOW =
            UUID.fromString("44ddcab4-aa3c-4e4e-b422-f67d38157f65");
    private static final double CRYO_SPEED_PENALTY = -0.15;
    private static final String MOB_AI_MEMORY = "GenshinERC_FrozenMobPreviousNoAI";
    private static final String NO_GRAVITY_MEMORY =
            "GenshinERC_FrozenWaterNoGravityBefore";

    private static final TagKey<EntityType<?>> IMMUNE =
            TagKey.create(
                    Registries.ENTITY_TYPE,
                    new ResourceLocation("genshinerc", "freeze_immune"));

    private FrozenControl() {}

    public static double resistance(LivingEntity entity) {
        if (entity.getType().is(IMMUNE)) {
            return 1.0;
        }

        AttributeInstance attribute =
                entity.getAttribute(ModAttributes.FREEZE_RESISTANCE.get());

        return attribute == null
                ? 0.0
                : Math.max(0.0, Math.min(1.0, attribute.getValue()));
    }

    public static boolean active(LivingEntity entity) {
        return entity.isAlive()
                && ElementAuraManager.hasAura(entity, ElementType.FROZEN);
    }

    public static boolean locked(LivingEntity entity) {
        if (!entity.isAlive() || entity.isSpectator()) {
            return false;
        }

        if (entity.level().isClientSide()) {
            AttributeInstance speed =
                    entity.getAttribute(Attributes.MOVEMENT_SPEED);
            return speed != null && speed.getModifier(SPEED_LOCK) != null;
        }

        return active(entity)
                || com.dsp2.genshinerc.common.core.combat.zhenmie
                .ZhenMieManager.staggered(entity);
    }

    public static void refresh(LivingEntity entity) {
        if (entity.level().isClientSide()) {
            return;
        }

        boolean isLocked = locked(entity);
        AttributeInstance speed =
                entity.getAttribute(Attributes.MOVEMENT_SPEED);

        if (speed != null) {
            modifier(
                    speed,
                    SPEED_LOCK,
                    "genshinerc_action_lock",
                    -1.0,
                    isLocked);

            modifier(
                    speed,
                    CRYO_SLOW,
                    "genshinerc_cryo_aura",
                    CRYO_SPEED_PENALTY,
                    entity.isAlive()
                            && !isLocked
                            && ElementAuraManager.hasAura(entity, ElementType.CRYO));
        }

        updateWaterGravity(entity);
        restoreMobAi(entity);

        if (isLocked) {
            suppress(entity);
        }
    }

    private static void suppress(LivingEntity entity) {
        if (entity instanceof Mob mob) {
            mob.getNavigation().stop();
            mob.setAggressive(false);
        }

        entity.xxa = 0f;
        entity.yya = 0f;
        entity.zza = 0f;
        entity.setJumping(false);
        entity.swinging = false;
        entity.swingTime = 0;

        if (entity.isUsingItem()) {
            entity.stopUsingItem();
        }

        if (entity.isSprinting()) {
            entity.setSprinting(false);
        }

        if (entity instanceof Creeper creeper) {
            creeper.setSwellDir(-1);
        }

        if (entity instanceof Witch witch && witch.isDrinkingPotion()) {
            witch.setUsingItem(false);
            witch.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND,
                    net.minecraft.world.item.ItemStack.EMPTY);
        }
    }

    private static void restoreMobAi(LivingEntity entity) {
        if (!(entity instanceof Mob mob)) return;
        if (entity.getPersistentData().contains(MOB_AI_MEMORY)) {
            boolean previous = entity.getPersistentData().getBoolean(MOB_AI_MEMORY);
            entity.getPersistentData().remove(MOB_AI_MEMORY);
            mob.setNoAi(previous);
        }
    }

    private static void updateWaterGravity(LivingEntity entity) {
        boolean frozenInWater = active(entity)
                && (entity.isInWater() || entity.isUnderWater());

        if (frozenInWater) {
            if (!entity.getPersistentData().contains(NO_GRAVITY_MEMORY)) {
                entity.getPersistentData().putBoolean(
                        NO_GRAVITY_MEMORY,
                        entity.isNoGravity());
            }

            entity.setNoGravity(true);
            entity.setDeltaMovement(
                    entity.getDeltaMovement().x * 0.25,
                    0.0,
                    entity.getDeltaMovement().z * 0.25);
            entity.hasImpulse = true;
            entity.hurtMarked = true;
            return;
        }

        if (entity.getPersistentData().contains(NO_GRAVITY_MEMORY)) {
            boolean previous = entity.getPersistentData()
                    .getBoolean(NO_GRAVITY_MEMORY);
            entity.getPersistentData().remove(NO_GRAVITY_MEMORY);
            entity.setNoGravity(previous);
        }
    }

    private static void modifier(
            AttributeInstance attribute,
            UUID id,
            String name,
            double amount,
            boolean enabled) {
        AttributeModifier current = attribute.getModifier(id);

        if (current != null
                && (!enabled || current.getAmount() != amount)) {
            attribute.removeModifier(id);
            current = null;
        }

        if (enabled && current == null) {
            attribute.addTransientModifier(
                    new AttributeModifier(
                            id,
                            name,
                            amount,
                            AttributeModifier.Operation.MULTIPLY_TOTAL));
        }
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();

        if (entity.level().isClientSide()) {
            return;
        }

        if (locked(entity)
                || entity.getPersistentData().contains(NO_GRAVITY_MEMORY)
                || entity.getPersistentData().contains(MOB_AI_MEMORY)) {
            refresh(entity);
        }
    }

    @SubscribeEvent
    public static void onInteract(
            net.minecraftforge.event.entity.player.PlayerInteractEvent event) {
        if (event.isCancelable() && locked(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onPlayerAttack(
            net.minecraftforge.event.entity.player.AttackEntityEvent event) {
        if (locked(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onUse(LivingEntityUseItemEvent.Start event) {
        if (locked(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onUseTick(LivingEntityUseItemEvent.Tick event) {
        if (locked(event.getEntity())) {
            event.setCanceled(true);
            event.getEntity().stopUsingItem();
        }
    }

    @SubscribeEvent
    public static void onEnderTeleport(EntityTeleportEvent.EnderEntity event) {
        if (event.getEntity() instanceof LivingEntity living && locked(living)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onChorusTeleport(EntityTeleportEvent.ChorusFruit event) {
        if (event.getEntity() instanceof LivingEntity living && locked(living)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onJump(LivingEvent.LivingJumpEvent event) {
        LivingEntity entity = event.getEntity();

        if (!locked(entity)) {
            return;
        }

        var velocity = entity.getDeltaMovement();
        entity.setDeltaMovement(
                velocity.x,
                Math.min(0.0, velocity.y),
                velocity.z);
        entity.hurtMarked = true;
    }

    @SubscribeEvent
    public static void onAttack(LivingAttackEvent event) {
        if (event.getEntity().level().isClientSide()
                || ReactionDamage.isDealing()) {
            return;
        }

        var source = event.getSource();

        if (source.getEntity() instanceof LivingEntity attacker
                && source.getDirectEntity() == attacker
                && attacker != event.getEntity()
                && locked(attacker)) {
            event.setCanceled(true);
        }
    }
}