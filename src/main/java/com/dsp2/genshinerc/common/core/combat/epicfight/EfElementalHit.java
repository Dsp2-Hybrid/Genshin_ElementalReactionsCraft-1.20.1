package com.dsp2.genshinerc.common.core.combat.epicfight;

import com.dsp2.genshinerc.common.core.damage.DamageKind;
import com.dsp2.genshinerc.common.core.damage.DamageMapper;
import com.dsp2.genshinerc.common.core.element.AttachCooldown;
import com.dsp2.genshinerc.common.core.element.ElementAttachment;
import com.dsp2.genshinerc.common.core.element.ElementAuraManager;
import com.dsp2.genshinerc.common.core.element.ElementType;
import com.dsp2.genshinerc.common.core.element.apply.probe.ElementImbue;
import com.dsp2.genshinerc.common.core.reaction.ElementReactions;
import com.dsp2.genshinerc.common.core.reaction.ReactionResult;
import com.dsp2.genshinerc.mixin.core.damage.DamageSourceTypeAccessor;
import com.dsp2.genshinerc.server.render.damageindicator.DamageServerEventHandler;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.AxeItem;
import org.jetbrains.annotations.Nullable;
import yesman.epicfight.api.animation.types.AttackAnimation;
import yesman.epicfight.world.damagesource.EpicFightDamageSource;
import yesman.epicfight.world.damagesource.StunType;

import java.util.Map;
import java.util.WeakHashMap;

public final class EfElementalHit {
    public enum AttachMode { STANDARD, REFRESH, SKIP }

    public record PhaseRule(String slot, AttachCooldown cooldown, AttachMode mode,
                            @Nullable ElementType element, float units) {
        public PhaseRule {
            if (slot == null || slot.isBlank() || cooldown == null || mode == null
                    || !Float.isFinite(units) || units < 0f
                    || (element != null && (!ElementAttachment.isTrigger(element) || units <= 0f))) {
                throw new IllegalArgumentException("Invalid Epic Fight attachment phase");
            }
        }
    }

    private static final class Cast {
        final AttackAnimation animation;
        final Map<LivingEntity, java.util.BitSet> refreshed = new WeakHashMap<>();

        Cast(AttackAnimation animation) {
            this.animation = animation;
        }

        boolean firstRefresh(LivingEntity target, int phase) {
            var phases = refreshed.computeIfAbsent(target, ignored -> new java.util.BitSet());
            if (phases.get(phase)) return false;
            phases.set(phase);
            return true;
        }
    }

    private record HitContext(ElementImbue.State imbue, @Nullable PhaseRule rule,
                              @Nullable Cast cast, int phase) {}

    private static final Map<DamageSource, HitContext> IMBUED = new WeakHashMap<>();
    private static final Map<LivingEntity, Cast> CASTS = new WeakHashMap<>();
    private static final Map<AttackAnimation, Map<Integer, PhaseRule>> PROFILES = new java.util.IdentityHashMap<>();

    public static synchronized void registerPhase(AttackAnimation animation, int phase, PhaseRule rule) {
        java.util.Objects.requireNonNull(animation);
        java.util.Objects.requireNonNull(rule);
        if (phase < 0 || phase >= animation.phases.length) throw new IllegalArgumentException("Invalid phase index");
        PROFILES.computeIfAbsent(animation, ignored -> new java.util.HashMap<>()).put(phase, rule);
    }

    @Nullable
    private static synchronized PhaseRule phaseRule(AttackAnimation animation, int phase) {
        var phases = PROFILES.get(animation);
        return phases == null ? null : phases.get(phase);
    }

    public static void beginCast(Entity entity, AttackAnimation animation) {
        if (entity instanceof LivingEntity attacker && !attacker.level().isClientSide()) {
            CASTS.put(attacker, new Cast(animation));
        }
    }

    private EfElementalHit() {}

    public static void convert(Entity entity, EpicFightDamageSource source) {
        convert(entity, source, null, null);
    }

    public static void convert(Entity entity, EpicFightDamageSource source,
                               @Nullable AttackAnimation animation, @Nullable AttackAnimation.Phase phase) {
        if (!(entity instanceof LivingEntity attacker) || attacker.level().isClientSide()
                || source == null || DamageMapper.classify(source) != DamageKind.PHYSICAL
                || source.getEntity() != attacker || source.getDirectEntity() != attacker
                || source.is(DamageTypeTags.IS_PROJECTILE)) return;
        int phaseIndex = -1;
        if (animation != null && phase != null) {
            for (int i = 0; i < animation.phases.length; i++) {
                if (animation.phases[i] == phase) {
                    phaseIndex = i;
                    break;
                }
            }
        }
        PhaseRule rule = phaseIndex < 0 ? null : phaseRule(animation, phaseIndex);
        ElementImbue.State imbue = ElementImbue.get(attacker);
        if (rule != null && rule.element() != null) {
            imbue = new ElementImbue.State(rule.element(), rule.units());
        } else if (rule != null && rule.units() > 0f && imbue != null) {
            imbue = new ElementImbue.State(imbue.element(), rule.units());
        }
        if (imbue == null || imbue.element() == null || !ElementAttachment.isTrigger(imbue.element())
                || !Float.isFinite(imbue.units()) || imbue.units() <= 0f) return;
        var holder = attacker.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(DamageKind.fromElement(imbue.element()).typeKey());
        source.typeHolder().tags().forEach(source::addRuntimeTag);
        ((DamageSourceTypeAccessor) (Object) source).genshinerc$setType(holder);
        Cast cast = null;
        if (rule != null && rule.mode() == AttachMode.REFRESH) {
            cast = CASTS.get(attacker);
            if (cast == null || cast.animation != animation) {
                cast = new Cast(animation);
                CASTS.put(attacker, cast);
            }
        }
        IMBUED.put(source, new HitContext(imbue, rule, cast, phaseIndex));
    }

    public static boolean converted(DamageSource source) {
        return IMBUED.containsKey(source);
    }

    public static double apply(LivingEntity target, DamageSource source, DamageKind kind, double damage) {
        if (damage <= 0.0 || !(source.getEntity() instanceof LivingEntity attacker)
                || source.getDirectEntity() != attacker || source.is(DamageTypeTags.IS_PROJECTILE)
                || source.is(DamageTypeTags.IS_EXPLOSION)) return damage;

        HitContext context = IMBUED.get(source);
        ElementImbue.State imbue = context == null ? null : context.imbue();
        if (source instanceof EpicFightDamageSource ef && ef.getAnimation() != null && ef.getAnimation().get() instanceof AttackAnimation) {
            boolean geo = imbue != null && imbue.element() == ElementType.GEO;
            if (geo || ef.getStunType() == StunType.KNOCKDOWN
                    || ef.calculateImpact() >= 2.0f || ef.getUsedItem().getItem() instanceof AxeItem) {
                ElementReactions.blunt(target, attacker);
            }
        } else if (kind == DamageKind.PHYSICAL && attacker.getMainHandItem().getItem() instanceof AxeItem) {
            ElementReactions.blunt(target, attacker);
        }

        if (imbue == null) return damage;
        var bonus = kind.damageBonusObject();
        var modifier = bonus == null ? null : attacker.getAttribute(bonus.get());
        double damageBonus = modifier == null ? 0.0 : modifier.getValue();
        PhaseRule rule = context.rule();
        String slot = rule == null ? "epicfight:imbue:" + imbue.element().name() : "epicfight:skill:" + rule.slot();
        AttachCooldown cooldown = rule == null ? AttachCooldown.STANDARD : rule.cooldown();

        if (context.cast() != null && context.cast().firstRefresh(target, context.phase())) {
            ElementAttachment.refreshWindow(target, attacker, slot);
        }

        ReactionResult result = rule != null && rule.mode() == AttachMode.SKIP
                ? ReactionResult.none()
                : ElementAttachment.attach(target, attacker, imbue.element(), imbue.units(), slot, cooldown, true);
        DamageServerEventHandler.tag(target, result.indicatorKey(kind.indicatorKey()));
        DamageServerEventHandler.tagPalette(target, result.damagePalette(kind));
        return (damage + result.catalyzeFlat()) * Math.max(0.0, 1.0 + damageBonus) * result.amplify();
    }


}
