package com.dsp2.genshinerc.common.core.reaction;

// Assembled from fragments Claude supplied across multiple replies.
// Includes: trigger, natural, run, blunt, attachable, effect, partner, shatter, swirl, shred, crystallize, tick, reset.
// If Claude later posts a full replacement, use that instead.

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
    private static final UUID SUPERCONDUCT_SHRED = UUID.fromString("5b9c2a52-7a3e-4a1d-9d0c-3f6a7e1b8c01");
    private static boolean naturalMode;

    private ElementReactions() {}

    public static boolean isNatural() { return naturalMode; }

    public static void tick() {
        ElectroChargedTracker.tick();
        BurningTracker.tick();
        ResistanceShred.tick();
    }

    public static void reset() {
        ElectroChargedTracker.reset();
        BurningTracker.reset();
        ResistanceShred.reset();
        ReactionDamage.reset();
        ReactionText.reset();
    }

    public static ReactionResult trigger(LivingEntity target, @Nullable Entity attacker, ElementType trigger, float units) {
        return run(target, attacker, trigger, units, false);
    }

    public static float natural(LivingEntity target, ElementType type, float units) {
        return run(target, null, type, units, true).remaining();
    }

    private static ReactionResult run(LivingEntity target, @Nullable Entity attacker, ElementType trigger,
                                      float units, boolean natural) {
        ReactionResult result = new ReactionResult();
        result.setRemaining(units);
        if (target.level().isClientSide() || !target.isAlive()) return result;
        boolean previous = naturalMode;
        naturalMode = natural;
        try {
            ElementalStats stats = ElementalStats.of(attacker);

            if (!natural && (trigger == ElementType.ELECTRO || trigger == ElementType.PYRO)) {
                DendroCores.detonateNear(target, attacker, trigger);
            }

            EnumSet<ElementType> before = EnumSet.noneOf(ElementType.class);
            before.addAll(ElementAuraManager.getAuras(target).keySet());

            float gauge = units;
            boolean consumedAny = false;
            boolean force = false;

            for (ElementType aura : ReactionRules.order(trigger)) {
                if (ElementAuraManager.amount(target, aura) <= EPS) continue;
                Reaction reaction = ReactionRules.rule(trigger, aura);
                if (reaction == null) continue;

                float used = 0f;
                float triggerUsed = 0f;
                if (reaction.consumes()) {
                    if (gauge <= EPS) continue;
                    float ratio = ReactionRules.ratio(trigger, aura);
                    used = Math.min(ElementAuraManager.amount(target, aura), gauge * ratio);
                    triggerUsed = used / ratio;
                    gauge -= triggerUsed;
                    ElementAuraManager.consume(target, aura, used, true);
                    consumedAny = true;
                } else if (!before.contains(aura)) {
                    continue;
                }

                boolean first = result.add(reaction);
                if (!first && reaction.once()) continue;
                force |= effect(reaction, target, attacker, stats, trigger, aura, used, triggerUsed, result);
                if (!natural) ReactionText.send(target, reaction, null, partner(reaction, trigger, aura));
            }
            result.setRemaining(gauge);

            if (!natural && attachable(trigger) && (!consumedAny || force)) {
                ElementAuraManager.applyUnits(target, trigger, units);
            }
            if (result.has(Reaction.BURNING)) BurningTracker.start(target, attacker);
            if (result.has(Reaction.ELECTRO_CHARGED)) ElectroChargedTracker.start(target, attacker);
        } finally {
            naturalMode = previous;
        }
        return result;
    }

    public static boolean blunt(LivingEntity target, @Nullable Entity attacker) {
        if (target.level().isClientSide() || !ElementAuraManager.hasAura(target, ElementType.FROZEN)) return false;
        shatter(target, attacker, ElementalStats.of(attacker));
        ReactionText.send(target, Reaction.SHATTER, null, null);
        return true;
    }

    private static boolean attachable(ElementType e) {
        return switch (e) {
            case PYRO, HYDRO, ELECTRO, CRYO, DENDRO -> true;
            default -> false;
        };
    }

    private static boolean effect(Reaction r, LivingEntity t, @Nullable Entity atk, ElementalStats s,
                                  ElementType trigger, ElementType aura, float used, float triggerUsed,
                                  ReactionResult res) {
        switch (r) {
            case MELT -> {
                if (res.amplify() == 1f) res.setAmplify(ElementalMath.amplify(trigger == ElementType.PYRO ? 2f : 1.5f, s.mastery()));
            }
            case VAPORIZE -> {
                if (res.amplify() == 1f) res.setAmplify(ElementalMath.amplify(trigger == ElementType.HYDRO ? 2f : 1.5f, s.mastery()));
            }
            case OVERLOAD -> ReactionDamage.deal(t, atk, DamageKind.PYRO, r, s);
            case SUPERCONDUCT -> {
                ReactionDamage.deal(t, atk, DamageKind.CRYO, r, s);
                ResistanceShred.apply(t, ModAttributes.PHYSICAL_RESISTANCE, SUPERCONDUCT_SHRED,
                        ServerConfig.SUPERCONDUCT_RES_SHRED.get(),
                        Math.round(ServerConfig.SUPERCONDUCT_RES_SECONDS.get().floatValue() * 20f));
            }
            case FREEZE -> ElementAuraManager.freeze(t, 2f * Math.min(triggerUsed, used));
            case SHATTER -> shatter(t, atk, s);
            case SWIRL -> swirl(t, atk, s, aura);
            case CRYSTALLIZE -> crystallize(atk, aura, s);
            case BLOOM -> { if (!naturalMode) DendroCores.spawn(t, atk, s); }
            case CATALYZE -> ElementAuraManager.quicken(t, used);
            case AGGRAVATE -> res.raiseFlat(ElementalMath.catalyze(1.15f, s));
            case SPREAD -> res.raiseFlat(ElementalMath.catalyze(1.25f, s));
            case ELECTRO_CHARGED, BURNING -> { return true; }
            default -> { }
        }
        return false;
    }

    @Nullable
    private static ElementType partner(Reaction r, ElementType trigger, ElementType aura) {
        return switch (r) {
            case SWIRL -> aura == ElementType.FROZEN ? ElementType.CRYO : aura;
            case CRYSTALLIZE -> aura;
            case SHATTER -> trigger == ElementType.GEO ? ElementType.GEO : null;
            default -> null;
        };
    }

    private static void shatter(LivingEntity t, @Nullable Entity atk, ElementalStats s) {
        ElementAuraManager.remove(t, ElementType.FROZEN, true);
        ReactionDamage.deal(t, atk, DamageKind.PHYSICAL, Reaction.SHATTER, s);
    }

    private static void swirl(LivingEntity t, @Nullable Entity atk, ElementalStats s, ElementType aura) {
        ElementType swirled = aura == ElementType.FROZEN ? ElementType.CRYO : aura;
        DamageKind kind = DamageKind.fromElement(swirled);
        ReactionDamage.deal(t, atk, kind, Reaction.SWIRL, s);
        shred(t, kind);
        float spread = ServerConfig.SWIRL_SPREAD_UNITS.get().floatValue();
        for (LivingEntity near : ReactionTargets.nearbyEnemies(t, atk, ServerConfig.SWIRL_RADIUS.get())) {
            ReactionDamage.deal(near, atk, kind, Reaction.SWIRL, s);
            shred(near, kind);
            trigger(near, atk, swirled, spread);
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
}
