package com.dsp2.genshinerc.common.core.reaction;

import com.dsp2.genshinerc.common.core.element.ElementGauge;
import com.dsp2.genshinerc.common.core.element.FrozenControl;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import com.dsp2.genshinerc.common.config.ServerConfig;
import com.dsp2.genshinerc.common.core.combat.shield.ShieldKind;
import com.dsp2.genshinerc.common.core.combat.shield.ShieldManager;
import com.dsp2.genshinerc.common.core.damage.DamageKind;
import com.dsp2.genshinerc.common.core.damage.ElementalMath;
import com.dsp2.genshinerc.common.core.damage.ElementalStats;
import com.dsp2.genshinerc.common.core.element.ElementAuraManager;
import com.dsp2.genshinerc.common.core.element.ElementType;
import com.dsp2.genshinerc.common.registry.ModAttributes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;
import java.nio.charset.StandardCharsets;
import java.util.EnumSet;
import java.util.UUID;

public final class ElementReactions {
    private static final float EPS = 1.0e-4f;
    private static final java.util.Map<LivingEntity, Long> EMBER_REFILL_AT = new java.util.WeakHashMap<>();
    private static final java.util.Map<LivingEntity, Long> CRYSTALLIZE_AT = new java.util.WeakHashMap<>();

    private static void consumeEmbers(LivingEntity entity, float amount) {
        int remaining = entity.getRemainingFireTicks();
        if (remaining <= 0) return;
        int loss = Math.min(Math.max(0, remaining / 3), Math.round(18f * amount));
        if (loss > 0) entity.setRemainingFireTicks(remaining - loss);
        long now = entity.level().getGameTime();
        EMBER_REFILL_AT.put(entity, now + Math.min(40, 16 + Math.round(12f * amount)));
    }
    public static float calcAmplify(ElementType trigger, ElementType aura) {
        if (trigger == ElementType.PYRO && aura == ElementType.HYDRO) return 1.5f;
        if (trigger == ElementType.HYDRO && aura == ElementType.PYRO) return 2.0f;
        if (trigger == ElementType.PYRO && (aura == ElementType.CRYO || aura == ElementType.FROZEN)) return 2.0f;
        if (trigger == ElementType.CRYO && aura == ElementType.PYRO) return 1.5f;
        return 1.0f;
    }

    public static boolean isAmpReaction(ElementType t, ElementType a) {
        return calcAmplify(t, a) > 1.0f;
    }
    private static final UUID SUPERCONDUCT_SHRED = UUID.fromString("5b9c2a52-7a3e-4a1d-9d0c-3f6a7e1b8c01");
    private static boolean naturalMode;
    public static final float TNT_GAUGE_CENTER = 3.0f;
    public static final float TNT_GAUGE_EDGE = 0.4f;

    private ElementReactions() {}

    public static boolean isNatural() { return naturalMode; }

    public static void tick() {
        ElectroChargedTracker.tick();
        WorldElementFields.tick();
        BurningTracker.tick();
        ResistanceShred.tick();
        OverloadImpulse.tick();
        ReactionText.tick();
    }
    public static void reset() {
        ElectroChargedTracker.reset();
        BurningTracker.reset();
        ResistanceShred.reset();
        ReactionDamage.reset();
        ReactionText.reset();
        OverloadImpulse.reset();
        EMBER_REFILL_AT.clear();
        CRYSTALLIZE_AT.clear();
    }

    public static float tntGauge(double distance, double radius) {
        if (radius <= 1.0e-4) return TNT_GAUGE_CENTER;
        double t = Math.max(0.0, Math.min(1.0, distance / radius));
        return (float) (TNT_GAUGE_CENTER + (TNT_GAUGE_EDGE - TNT_GAUGE_CENTER) * t);
    }

    public static ReactionResult trigger(LivingEntity target, @Nullable Entity attacker, ElementType trigger, float units) {
        return run(target, attacker, trigger, units, false, false);
    }

    public static void forceSendText(LivingEntity target, ReactionResult rr) {
        if (rr == null || rr.reactions().isEmpty()) return;
        for (Reaction r : rr.reactions()) {
            ReactionText.send(target, r, r.first(), r.second(), true);
        }
    }

    public static float natural(LivingEntity target, ElementType type, float units) {
        if (type == ElementType.PYRO && target.isOnFire() && !target.isInLava()
                && !target.level().getBlockState(target.blockPosition()).is(net.minecraft.tags.BlockTags.FIRE)) {
            long now = target.level().getGameTime();
            long resume = EMBER_REFILL_AT.getOrDefault(target, Long.MIN_VALUE);
            if (now < resume) return 0f;
            EMBER_REFILL_AT.remove(target);
        }
        return run(target, null, type, units, true, false).remaining();
    }

    public static ReactionResult run(LivingEntity target, @Nullable Entity attacker, ElementType trigger, float units, boolean natural, boolean applyWear) {
        return run(target, attacker, trigger, units, natural, applyWear, true);
    }

    public static ReactionResult run(LivingEntity target, @Nullable Entity attacker, ElementType trigger, float units,
                                     boolean natural, boolean applyWear, boolean allowElectroCharged) {
        ReactionResult result = new ReactionResult();
        result.setRemaining(units);
        if (target.level().isClientSide() || !target.isAlive() || !Float.isFinite(units) || units <= EPS) return result;
        boolean previous = naturalMode;
        naturalMode = natural;
        try {
            ElementalStats stats = ElementalStats.of(attacker);
            if (!natural && (trigger == ElementType.ELECTRO || trigger == ElementType.PYRO)) DendroCores.detonateNear(target, attacker, trigger);

            EnumSet<ElementType> before = EnumSet.noneOf(ElementType.class);
            before.addAll(ElementAuraManager.getAuras(target).keySet());

            float gauge = units;
            boolean consumedAny = false;
            boolean force = false;
            EnumSet<ElementType> swirled = EnumSet.noneOf(ElementType.class);

            for (ElementType aura : ReactionRules.order(trigger)) {
                if (gauge <= EPS) break;
                if (ElementAuraManager.amount(target, aura) <= EPS) continue;
                Reaction reaction = ReactionRules.rule(trigger, aura);
                if (reaction == null || (!allowElectroCharged && reaction == Reaction.ELECTRO_CHARGED)) continue;
                if (reaction == Reaction.FREEZE && FrozenControl.resistance(target) >= 1.0) continue;
                if (target.isUnderWater() && (reaction == Reaction.SWIRL || reaction == Reaction.CRYSTALLIZE)) continue;
                if (reaction == Reaction.CRYSTALLIZE
                        && target.level().getGameTime() < CRYSTALLIZE_AT.getOrDefault(target, Long.MIN_VALUE)) continue;
                if (reaction == Reaction.CRYSTALLIZE
                        && target.level().getGameTime() < CRYSTALLIZE_AT.getOrDefault(target, Long.MIN_VALUE)) continue;
                ElementType swirlElement = aura == ElementType.FROZEN ? ElementType.CRYO : aura;
                if (reaction == Reaction.SWIRL && swirled.contains(swirlElement)) continue;

                float used = 0f;
                float triggerUsed = 0f;
                if (reaction.consumes()) {
                    if (gauge <= EPS) continue;
                    float ratio = ReactionRules.ratio(trigger, aura);
                    used = ElementAuraManager.consume(target, aura, gauge * ratio, true);
                    if (used <= EPS) continue;
                    triggerUsed = used / ratio;
                    gauge = Math.max(0f, gauge - triggerUsed);
                    consumedAny = true;
                    if (reaction == Reaction.CRYSTALLIZE) {
                        CRYSTALLIZE_AT.put(target, target.level().getGameTime() + 20L);
                    }
                    if (reaction == Reaction.OVERLOAD && aura == ElementType.PYRO) consumeEmbers(target, used);
                } else if (!before.contains(aura)) continue;

                if (reaction == Reaction.SWIRL) swirled.add(swirlElement);
                boolean first = result.add(reaction);
                if (!first && reaction.once()) continue;
                force |= effect(reaction, target, attacker, stats, trigger, aura, used, triggerUsed, result);

                ReactionText.send(target, reaction, trigger, aura);
            }
            result.setRemaining(gauge);

            if (!natural && gauge > EPS && attachable(trigger) && (!consumedAny || force)) {
                if (consumedAny) {
                    ElementAuraManager.apply(target, trigger, gauge, ElementGauge.durationTicksFromUnits(gauge));
                } else {
                    ElementAuraManager.applyUnits(target, trigger, units);
                }
            }

            if (result.has(Reaction.BURNING)) BurningTracker.start(target, attacker);
            if (result.has(Reaction.ELECTRO_CHARGED)) ElectroChargedTracker.start(target, attacker, natural);
        } finally {
            naturalMode = previous;
        }
        return result;
    }

    public static boolean blunt(LivingEntity target, @Nullable Entity attacker) {
        if (target.level().isClientSide() || !ElementAuraManager.hasAura(target, ElementType.FROZEN)) return false;
        ReactionText.send(target, Reaction.SHATTER, ElementType.FROZEN, null);
        shatter(target, attacker, ElementalStats.of(attacker));
        return true;
    }

    private static boolean attachable(ElementType e) {
        return switch (e) { case PYRO, HYDRO, ELECTRO, CRYO, DENDRO -> true; default -> false; };
    }

    private static boolean effect(Reaction r, LivingEntity t, @Nullable Entity atk, ElementalStats s, ElementType trigger, ElementType aura, float used, float triggerUsed, ReactionResult res) {
        switch (r) {
            case MELT -> {
                if (res.amplify() == 1f) res.setAmplify(ElementalMath.amplify(trigger == ElementType.PYRO ? 2f : 1.5f, s.mastery()));
                ReactionSoundEvents.playMelt(t);
                ReactionParticles.spawnMelt(t);
            }
            case VAPORIZE -> {
                if (res.amplify() == 1f) res.setAmplify(ElementalMath.amplify(trigger == ElementType.HYDRO ? 2f : 1.5f, s.mastery()));
                ReactionSoundEvents.playVaporize(t);
                ReactionParticles.spawnVaporize(t);
            }
            case OVERLOAD -> {
                overload(t, atk, s);
                OverloadVFX.play(t);
            }
            case SUPERCONDUCT -> {
                superconduct(t, atk, s);
            }
            case FREEZE -> {
                if (FrozenControl.resistance(t) >= 1.0) break;
                ElementAuraManager.freeze(t, 2f * Math.min(triggerUsed, used));
                if (FrozenControl.active(t)) {
                    ReactionSoundEvents.playFreeze(t);
                    ReactionParticles.spawnFreeze(t);
                }
            }
            case SHATTER -> {
                shatter(t, atk, s);
            }
            case SWIRL -> {
                swirl(t, atk, s, aura, used);
            }
            case CRYSTALLIZE -> {
                res.noteCrystallized(aura);
                crystallize(atk, aura, s);
                ReactionSoundEvents.playCrystallize(t);
                ReactionParticles.spawnCrystallize(t);
            }
            case BLOOM -> {
                if (!naturalMode) DendroCores.spawn(t, atk, s);
                ReactionSoundEvents.playBloom(t);
                ReactionParticles.spawnBloom(t);
            }
            case CATALYZE -> ElementAuraManager.quicken(t, used);
            case AGGRAVATE -> res.raiseFlat(ElementalMath.catalyze(1.15f, s));
            case SPREAD -> res.raiseFlat(ElementalMath.catalyze(1.25f, s));
            case ELECTRO_CHARGED -> {
                return true;
            }
            case BURNING -> {
                ReactionSoundEvents.playBurning(t);
                ReactionParticles.spawnBurning(t);
                return true;
            }
            default -> { }
        }
        return false;
    }

    private static void superconduct(LivingEntity target, @Nullable Entity attacker, ElementalStats stats) {
        double radius = 5.0;
        int ticks = Math.round(ServerConfig.SUPERCONDUCT_RES_SECONDS.get().floatValue() * 20f);
        ReactionDamage.deal(target, attacker, DamageKind.CRYO, Reaction.SUPERCONDUCT, stats);
        ResistanceShred.apply(target, ModAttributes.PHYSICAL_RESISTANCE, SUPERCONDUCT_SHRED,
                ServerConfig.SUPERCONDUCT_RES_SHRED.get(), ticks);
        ReactionSoundEvents.playSuperconduct(target);
        ReactionParticles.spawnSuperconduct(target);

        for (LivingEntity near : ReactionTargets.nearbyEnemies(target, attacker, radius)) {
            ReactionDamage.deal(near, attacker, DamageKind.CRYO, Reaction.SUPERCONDUCT, stats);
            ResistanceShred.apply(near, ModAttributes.PHYSICAL_RESISTANCE, SUPERCONDUCT_SHRED,
                    ServerConfig.SUPERCONDUCT_RES_SHRED.get(), ticks);
            ReactionParticles.spawnSuperconduct(near);
        }
    }

    private static void overload(LivingEntity t, @Nullable Entity atk, ElementalStats s) {
        double radius = ServerConfig.OVERLOAD_RADIUS.get();
        Vec3 center = t.getBoundingBox().getCenter();
        ReactionDamage.dealOverload(t, atk, s, center);
        for (LivingEntity near : ReactionTargets.nearbyEnemies(t, atk, radius)) {
            ReactionDamage.dealOverload(near, atk, s, center);
        }
        OverloadImpulse.apply(t, atk, s);
    }
    private static void shatter(LivingEntity t, @Nullable Entity atk, ElementalStats s) {
        ElementAuraManager.remove(t, ElementType.FROZEN, true);
        ReactionSoundEvents.playShatter(t);
        ReactionParticles.spawnShatter(t);
        ReactionDamage.deal(t, atk, DamageKind.PHYSICAL, Reaction.SHATTER, s);
    }

    private static void swirl(LivingEntity t, @Nullable Entity atk, ElementalStats s, ElementType aura, float consumed) {
        ElementType swirled = aura == ElementType.FROZEN ? ElementType.CRYO : aura;
        DamageKind kind = DamageKind.fromElement(swirled);
        double radius = ServerConfig.SWIRL_RADIUS.get();
        float spread = Math.min(ServerConfig.SWIRL_SPREAD_UNITS.get().floatValue(), consumed * 2f);

        ReactionSoundEvents.playSwirl(t, swirled);
        ReactionParticles.spawnSwirl(t, swirled, radius);
        WorldElementFields.applySwirlToWorld(t, atk, swirled, spread, radius);
        ReactionDamage.deal(t, atk, kind, Reaction.SWIRL, s);
        shred(t, kind);

        for (LivingEntity near : ReactionTargets.nearbyEnemies(t, atk, radius)) {
            ReactionDamage.deal(near, atk, kind, Reaction.SWIRL, s);
            if (!near.isAlive()) continue;
            shred(near, kind);
            if (spread > EPS) trigger(near, atk, swirled, spread);
        }
    }

    private static void shred(LivingEntity t, DamageKind kind) {
        RegistryObject<Attribute> attribute = kind.resistanceObject();
        if (attribute == null) return;
        UUID id = UUID.nameUUIDFromBytes(("genshinerc:swirl_shred:" + kind.name()).getBytes(StandardCharsets.UTF_8));
        int ticks = Math.round(ServerConfig.SWIRL_SHRED_SECONDS.get().floatValue() * 20f);
        ResistanceShred.apply(t, attribute, id, ServerConfig.SWIRL_SHRED.get(), ticks);
    }

    private static void crystallize(@Nullable Entity atk, ElementType aura, ElementalStats s) {
        if (!(atk instanceof LivingEntity owner)) return;
        float amount = (float) ElementalMath.crystallizeShield(s.level(), s.mastery());
        int ticks = Math.round(ServerConfig.CRYSTALLIZE_SECONDS.get().floatValue() * 20f);
        ShieldManager.add(owner, ShieldKind.ELEMENTAL, aura, amount, ticks);
    }

    public static void overloadAt(ServerLevel level, LivingEntity attacker, Vec3 at) {
        double radius = ServerConfig.OVERLOAD_RADIUS.get();
        var targets = level.getEntitiesOfClass(LivingEntity.class, new AABB(at, at).inflate(radius),
                target -> ReactionTargets.isEnemy(attacker, target)
                        && target.getBoundingBox().getCenter().distanceToSqr(at) <= radius * radius);
        ElementalStats stats = ElementalStats.of(attacker);
        OverloadVFX.play(level, at);
        for (LivingEntity target : targets) {
            ReactionText.send(target, Reaction.OVERLOAD, ElementType.ELECTRO, ElementType.PYRO);
            ReactionDamage.dealOverload(target, attacker, stats, at);
        }
        OverloadImpulse.applyAt(level, at, attacker, stats, targets);
    }
}
