package com.dsp2.genshinerc.common.core.element;

import com.dsp2.genshinerc.common.config.ServerConfig;
import com.dsp2.genshinerc.common.core.reaction.ElementReactions;
import com.dsp2.genshinerc.common.core.reaction.ReactionParticles;
import com.dsp2.genshinerc.common.core.reaction.ReactionSoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "genshinerc")
public final class FrozenWaterControl {
    public static final float SHATTER_RATIO = 0.5f;
    public static final double SINK = 0.15;
    private static final float BASE_HEALTH = 20f;

    private FrozenWaterControl() {}

    public static float fallRatio(LivingEntity entity, float distance, float multiplier) {
        MobEffectInstance jump = entity.getEffect(MobEffects.JUMP);
        float boost = jump == null ? 0f : jump.getAmplifier() + 1f;
        int damage = Mth.ceil((distance - 3.0f - boost) * multiplier);
        return damage <= 0 ? 0f : damage / BASE_HEALTH;
    }

    public static double findSurface(Level level, AABB box, double fromFeet, double lowest) {
        double best = Double.NEGATIVE_INFINITY;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos above = new BlockPos.MutableBlockPos();
        int top = Mth.floor(fromFeet + 0.002);
        int bottom = Mth.floor(lowest);
        for (int x = Mth.floor(box.minX + 1.0E-4); x <= Mth.floor(box.maxX - 1.0E-4); x++) {
            for (int z = Mth.floor(box.minZ + 1.0E-4); z <= Mth.floor(box.maxZ - 1.0E-4); z++) {
                for (int y = top; y >= bottom; y--) {
                    cursor.set(x, y, z);
                    if (!level.hasChunkAt(cursor)) continue;
                    FluidState fluid = level.getFluidState(cursor);
                    if (fluid.isEmpty() || !fluid.is(FluidTags.WATER)) continue;
                    above.set(x, y + 1, z);
                    if (level.getFluidState(above).is(FluidTags.WATER)) continue;
                    double surface = y + fluid.getHeight(level, cursor);
                    if (surface <= fromFeet + 0.002 && surface >= lowest) {
                        best = Math.max(best, surface);
                    }
                    break;
                }
            }
        }
        return best;
    }

    private static void shatter(LivingEntity entity) {
        ElementAuraManager.remove(entity, ElementType.FROZEN, true);
        ReactionSoundEvents.playShatter(entity);
        ReactionParticles.spawnShatter(entity);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onFall(LivingFallEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide() || !ElementAuraManager.hasAura(entity, ElementType.FROZEN)) return;
        if (fallRatio(entity, event.getDistance(), event.getDamageMultiplier()) >= SHATTER_RATIO) {
            shatter(entity);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (!(entity.level() instanceof ServerLevel level) || !entity.isAlive() || entity.isSpectator()
                || entity.isInWater() || entity.onGround() || entity.isPassenger() || entity.isNoGravity()) return;
        if (entity instanceof Player player && player.getAbilities().flying) return;

        boolean frozen = ElementAuraManager.hasAura(entity, ElementType.FROZEN);
        boolean cryo = ElementAuraManager.hasAura(entity, ElementType.CRYO) && FrozenControl.resistance(entity) < 1.0;
        if (!frozen && !cryo) return;

        double moved = entity.getY() - entity.yo;
        double vy = entity.getDeltaMovement().y;
        if (Math.min(moved, vy) > -0.02) return;

        AABB box = entity.getBoundingBox();
        double feet = box.minY;
        double fromFeet = Math.max(feet, feet - moved);
        double lowest = Math.min(feet, feet + vy) - 0.12;
        double surface = findSurface(level, box, fromFeet, lowest);
        if (surface == Double.NEGATIVE_INFINITY) return;

        double target = surface - SINK;
        float distance = entity.fallDistance + (float) Math.max(0.0, feet - target);
        if (frozen && fallRatio(entity, distance, 1.0f) >= SHATTER_RATIO) {
            shatter(entity);
            return;
        }

        if (!level.noCollision(entity, box.move(0.0, target - feet, 0.0))) return;

        if (entity instanceof ServerPlayer player && player.connection != null) {
            player.connection.teleport(entity.getX(), target, entity.getZ(), player.getYRot(), player.getXRot());
        } else {
            entity.setPos(entity.getX(), target, entity.getZ());
        }
        entity.fallDistance = 0f;
        Vec3 velocity = entity.getDeltaMovement();
        entity.setDeltaMovement(velocity.x * 0.25, 0.0, velocity.z * 0.25);
        entity.hasImpulse = true;
        entity.hurtMarked = true;

        if (!frozen) {
            double seconds = ServerConfig.WATER_SECONDS.get();
            int ticks = Math.max(1, (int) Math.round(seconds * 20.0));
            float add = 2.0f / ticks;
            float rest = ElementReactions.natural(entity, ElementType.HYDRO, add);
            ElementAuraManager.hold(entity, ElementType.HYDRO, rest, 2.0f, 1);
        }
        FrozenControl.refresh(entity);
    }
}