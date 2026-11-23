package com.dsp2.genshinerc.common.core.reaction;

import com.dsp2.genshinerc.common.config.ServerConfig;
import com.dsp2.genshinerc.common.core.combat.zhenmie.ZhenMieManager;

import com.dsp2.genshinerc.common.core.damage.ElementalStats;
import com.dsp2.genshinerc.common.level.WorldLevel;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

@net.minecraftforge.fml.common.Mod.EventBusSubscriber(modid = "genshinerc")
public final class OverloadImpulse {
    @net.minecraftforge.eventbus.api.SubscribeEvent
    public static void onKnockBack(net.minecraftforge.event.entity.living.LivingKnockBackEvent event) {
        if (!event.getEntity().level().isClientSide() && ReactionDamage.isOverloadHit()) {
            event.setCanceled(true);
        }
    }

    private record Pending(long launchTick, boolean leftGround) {}
    private static final Map<LivingEntity, Pending> AIRBORNE = new WeakHashMap<>();
    private static final Map<LivingEntity, Vec3> QUEUED = new WeakHashMap<>();
    private static final Map<LivingEntity, Long> COOLDOWN = new WeakHashMap<>();
    private static final int COOLDOWN_TICKS = 10;
    private static final int MAX_AIRBORNE_TICKS = 100;

    private OverloadImpulse() {}

    public static void apply(LivingEntity center, @Nullable Entity attacker, ElementalStats stats) {
        if (center == null) return;
        List<LivingEntity> targets = new ArrayList<>(ReactionTargets.nearbyEnemies(center, attacker, ServerConfig.OVERLOAD_RADIUS.get()));
        if (ReactionTargets.isEnemy(attacker, center)) targets.add(center);
        if (center.level() instanceof ServerLevel level) applyAt(level, center.getBoundingBox().getCenter(), attacker, stats, targets);
    }

    public static void applyAt(ServerLevel level, Vec3 center, @Nullable Entity attacker, ElementalStats stats,
                               List<LivingEntity> targets) {
        long now = level.getGameTime();
        double radius = Math.max(0.01, ServerConfig.OVERLOAD_RADIUS.get());
        Vec3 dir = attackDirection(center, attacker);
        double mastery = Math.max(0.0, stats.mastery());
        double emFactor = attacker == null
                ? 1.0 + WorldLevel.get() * 0.15
                : 1.0 + mastery / (mastery + 1200.0);
        emFactor = Math.min(ServerConfig.OVERLOAD_EM_CAP.get(), emFactor);

        for (LivingEntity t : targets) {
            if (!ReactionTargets.isEnemy(attacker, t)) continue;
            Long last = COOLDOWN.get(t);
            if (last != null && now - last < COOLDOWN_TICKS) continue;
            if (AIRBORNE.containsKey(t) || QUEUED.containsKey(t)) continue;
            if (ZhenMieManager.staggered(t)) continue;
            double resistance = Mth.clamp(t.getAttributeValue(
                    net.minecraft.world.entity.ai.attributes.Attributes.KNOCKBACK_RESISTANCE), 0.0, 1.0);
            double d = t.getBoundingBox().getCenter().distanceTo(center);
            double falloff = 1.0 - 0.26 * Math.pow(Mth.clamp(d / radius, 0.0, 1.0), 1.6);
            double up = (ServerConfig.OVERLOAD_UP_BASE.get() * 1.25 + ServerConfig.OVERLOAD_UP_SCALE.get() * falloff) * emFactor;
            double side = (ServerConfig.OVERLOAD_SIDE_BASE.get() * 1.35 + ServerConfig.OVERLOAD_SIDE_SCALE.get() * falloff) * emFactor;
            double movement = 1.0 - resistance;
            double launchY = up > 0.0 ? Math.max(up * movement * 1.12, 0.25 + 0.17 * movement) : 0.0;
            Vec3 push = (dir != null ? dir : randomDir(t)).scale(side * movement * 1.12).add(0.0, launchY, 0.0);
            COOLDOWN.put(t, now);
            if (t instanceof net.minecraft.world.entity.animal.IronGolem
                    || resistance >= 0.9 || launchY <= 0.0 || t.isInWater() || t.isInLava()) {
                ZhenMieManager.fillMie(t);
            } else {
                QUEUED.put(t, push);
            }
        }
    }

    public static void tick() {
        QUEUED.forEach((target, push) -> {
            if (!target.isAlive() || target.isRemoved()) return;
            Vec3 velocity = target.getDeltaMovement();
            target.setDeltaMovement(velocity.x + push.x, Math.max(0.0, velocity.y) + push.y, velocity.z + push.z);
            target.hurtMarked = true;
            target.hasImpulse = true;
            AIRBORNE.put(target, new Pending(target.level().getGameTime(), !target.onGround()));
        });
        QUEUED.clear();
        AIRBORNE.entrySet().removeIf(e -> {
            LivingEntity t = e.getKey();
            if (t.isRemoved() || !t.isAlive()) return true;
            long air = t.level().getGameTime() - e.getValue().launchTick();
            if (air <= 0) return false;
            boolean landed = t.onGround() || t.isInWater() || t.isInLava();
            if (!landed) {
                if (air > MAX_AIRBORNE_TICKS) return true;
                if (!e.getValue().leftGround()) e.setValue(new Pending(e.getValue().launchTick(), true));
                return false;
            }
            if (!e.getValue().leftGround()) return air >= 6;
            ZhenMieManager.fillMie(t);
            if (t.level() instanceof ServerLevel sl)
                sl.sendParticles(ParticleTypes.EXPLOSION, t.getX(), t.getY(), t.getZ(), 1, 0, 0, 0, 0);
            return true;
        });
    }

    public static void reset() { AIRBORNE.clear(); COOLDOWN.clear(); QUEUED.clear(); }

    @Nullable
    private static Vec3 attackDirection(Vec3 center, @Nullable Entity attacker) {
        if (attacker == null) return null;
        Vec3 v = center.subtract(attacker.position());
        v = new Vec3(v.x, 0.0, v.z);
        if (v.lengthSqr() < 1.0e-4) {
            Vec3 look = attacker.getLookAngle();
            v = new Vec3(look.x, 0.0, look.z);
        }
        return v.lengthSqr() < 1.0e-4 ? null : v.normalize().scale(-1.0);
    }

    private static Vec3 randomDir(LivingEntity t) {
        double a = t.level().getRandom().nextDouble() * Math.PI * 2.0;
        return new Vec3(Math.cos(a), 0.0, Math.sin(a));
    }
}