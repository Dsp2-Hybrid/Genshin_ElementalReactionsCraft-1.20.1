package com.dsp2.genshinerc.common.core.reaction;

import com.dsp2.genshinerc.common.config.ServerConfig;
import com.dsp2.genshinerc.common.core.combat.zhenmie.ClashResult;
import com.dsp2.genshinerc.common.core.combat.zhenmie.ZhenMieManager;
import com.dsp2.genshinerc.common.core.combat.epicfight.EfCombatBridge;
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

public final class OverloadImpulse {

    private record Pending(long launchTick) {}
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
        if (center.level() instanceof ServerLevel level) applyAt(level, center.position(), attacker, stats, targets);
    }

    public static void applyAt(ServerLevel level, Vec3 center, @Nullable Entity attacker, ElementalStats stats,
                               List<LivingEntity> targets) {
        long now = level.getGameTime();
        double radius = ServerConfig.OVERLOAD_RADIUS.get();
        Vec3 dir = attackDirection(center, attacker);
        double emFactor = stats.mastery() > 0.0
                ? 1.0 + stats.mastery() / (stats.mastery() + 1200.0)
                : 1.0 + WorldLevel.get() * 0.15;
        emFactor = Math.min(ServerConfig.OVERLOAD_EM_CAP.get(), emFactor);

        for (LivingEntity t : targets) {
            if (!ReactionTargets.isEnemy(attacker, t)) continue;
            Long last = COOLDOWN.get(t);
            if (last != null && now - last < COOLDOWN_TICKS) continue;
            if (t instanceof net.minecraft.world.entity.animal.IronGolem) continue;
            double d = t.getBoundingBox().getCenter().distanceTo(center);
            double falloff = 1.0 - Math.pow(Mth.clamp(d / radius, 0.0, 1.0), 1.5);
            double up = (ServerConfig.OVERLOAD_UP_BASE.get() + ServerConfig.OVERLOAD_UP_SCALE.get() * falloff) * emFactor;
            double side = (ServerConfig.OVERLOAD_SIDE_BASE.get() + ServerConfig.OVERLOAD_SIDE_SCALE.get() * falloff) * emFactor;
            Vec3 push = (dir != null ? dir : randomDir(t)).multiply(side, 0.0, side).add(0.0, up, 0.0);
            COOLDOWN.put(t, now);
            if (t.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.KNOCKBACK_RESISTANCE) >= 0.9) {
                ZhenMieManager.add(t, 0f, 1f);
            } else {
                QUEUED.merge(t, push, Vec3::add);
            }
        }
    }

    public static void tick() {
        QUEUED.forEach((target, push) -> {
            if (!target.isAlive() || target.isRemoved()) return;
            target.setDeltaMovement(target.getDeltaMovement().add(push));
            target.hurtMarked = true;
            target.hasImpulse = true;
            AIRBORNE.put(target, new Pending(target.level().getGameTime()));
        });
        QUEUED.clear();
        AIRBORNE.entrySet().removeIf(e -> {
            LivingEntity t = e.getKey();
            if (t.isRemoved() || !t.isAlive()) return true;
            long air = t.level().getGameTime() - e.getValue().launchTick();
            boolean landed = air >= 3 && (t.onGround() || t.isInWater() || t.isInLava());
            if (!landed) {
                if (air > MAX_AIRBORNE_TICKS) { ZhenMieManager.add(t, 0f, 1f); return true; }
                return false;
            }
            ZhenMieManager.add(t, 0f, 1f);
            EfCombatBridge.applyStun(t, ClashResult.CRUSH);
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
        return v.lengthSqr() < 1.0e-4 ? null : v.normalize();
    }

    private static Vec3 randomDir(LivingEntity t) {
        double a = t.level().getRandom().nextDouble() * Math.PI * 2.0;
        return new Vec3(Math.cos(a), 0.0, Math.sin(a));
    }
}