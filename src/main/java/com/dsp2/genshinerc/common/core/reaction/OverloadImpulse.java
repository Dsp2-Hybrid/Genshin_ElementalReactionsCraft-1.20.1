package com.dsp2.genshinerc.common.core.reaction;

import com.dsp2.genshinerc.common.config.ServerConfig;
import com.dsp2.genshinerc.common.core.combat.zhenmie.ClashResult;
import com.dsp2.genshinerc.common.core.combat.zhenmie.ZhenMieManager;
import com.dsp2.genshinerc.common.core.combat.epicfight.EfCombatBridge;
import com.dsp2.genshinerc.common.core.damage.ElementalStats;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.WeakHashMap;

public final class OverloadImpulse {

    private record Pending(LivingEntity entity, long landTick, float elapsed) {}
    private static final Map<LivingEntity, Pending> AIRBORNE = new WeakHashMap<>();

    private OverloadImpulse() {}

    public static void apply(LivingEntity center, @Nullable Entity attacker, ElementalStats stats) {
        double radius = ServerConfig.OVERLOAD_RADIUS.get();
        Vec3 dir = attackDirection(center, attacker);
        double emFactor = 1.0 + stats.mastery() / (stats.mastery() + 1200.0);
        emFactor = Math.min(ServerConfig.OVERLOAD_EM_CAP.get(), emFactor);

        java.util.List<LivingEntity> targets = new java.util.ArrayList<>(ReactionTargets.nearbyEnemies(center, attacker, radius)); if (ReactionTargets.isEnemy(attacker, center)) targets.add(center); for (LivingEntity t : targets) {
            if (t instanceof net.minecraft.world.entity.animal.IronGolem) continue; double d = t.position().distanceTo(center.position());
            double falloff = 1.0 - Mth.clamp(d / radius, 0.0, 1.0);

            double up = (ServerConfig.OVERLOAD_UP_BASE.get()
                    + ServerConfig.OVERLOAD_UP_SCALE.get() * falloff) * emFactor;
            double side = (ServerConfig.OVERLOAD_SIDE_BASE.get()
                    + ServerConfig.OVERLOAD_SIDE_SCALE.get() * falloff) * emFactor;

            Vec3 push = (dir != null ? dir : randomDir(t))
                    .multiply(side, 0.0, side).add(0.0, up, 0.0);

            t.setDeltaMovement(t.getDeltaMovement().add(push));
            t.hurtMarked = true;
            t.hasImpulse = true;

            if (t.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.KNOCKBACK_RESISTANCE) >= 0.9) {
                ZhenMieManager.add(t, 0f, 1f);
            } else {
                AIRBORNE.put(t, new Pending(t, t.level().getGameTime(), 0f));
            }
        }
    }

    public static void tick() {
        AIRBORNE.entrySet().removeIf(e -> {
            LivingEntity t = e.getKey();
            if (t.isRemoved() || !t.isAlive()) return true;
            boolean landed = t.level().getGameTime() - e.getValue().landTick() >= 3 && (t.onGround() || t.isInWater() || t.isInLava());
            if (!landed) {
                if (t.level().getGameTime() - e.getValue().landTick() > 100) {
                    ZhenMieManager.add(t, 0f, 1f);
                    return true;
                }
                return false;
            }
            ZhenMieManager.add(t, 0f, 1f);
            EfCombatBridge.applyStun(t, ClashResult.CRUSH);
            return true;
        });
    }

    public static void reset() { AIRBORNE.clear(); }

    @Nullable
    private static Vec3 attackDirection(LivingEntity center, @Nullable Entity attacker) {
        if (attacker == null) return null;
        Vec3 v = center.position().subtract(attacker.position());
        v = new Vec3(v.x, 0.0, v.z);
        return v.lengthSqr() < 1.0e-4 ? null : v.normalize();
    }

    private static Vec3 randomDir(LivingEntity t) {
        double a = t.level().getRandom().nextDouble() * Math.PI * 2.0;
        return new Vec3(Math.cos(a), 0.0, Math.sin(a));
    }
}