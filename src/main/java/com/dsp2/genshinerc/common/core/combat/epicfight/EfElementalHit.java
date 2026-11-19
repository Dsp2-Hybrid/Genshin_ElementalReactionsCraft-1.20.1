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
    private static final Map<DamageSource, ElementImbue.State> IMBUED = new WeakHashMap<>();

    private EfElementalHit() {}

    public static void convert(Entity entity, EpicFightDamageSource source) {
        if (!(entity instanceof LivingEntity attacker) || attacker.level().isClientSide()
                || source == null || DamageMapper.classify(source) != DamageKind.PHYSICAL
                || source.getEntity() != attacker || source.getDirectEntity() != attacker
                || source.is(DamageTypeTags.IS_PROJECTILE)) return;
        ElementImbue.State imbue = ElementImbue.get(attacker);
        if (imbue == null || imbue.element() == null || !ElementAttachment.isTrigger(imbue.element())
                || !Float.isFinite(imbue.units()) || imbue.units() <= 0f) return;
        var holder = attacker.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(DamageKind.fromElement(imbue.element()).typeKey());
        source.typeHolder().tags().forEach(source::addRuntimeTag);
        ((DamageSourceTypeAccessor) (Object) source).genshinerc$setType(holder);
        IMBUED.put(source, imbue);
    }

    public static boolean converted(DamageSource source) {
        return IMBUED.containsKey(source);
    }

    public static double apply(LivingEntity target, DamageSource source, DamageKind kind, double damage) {
        if (damage <= 0.0 || !(source.getEntity() instanceof LivingEntity attacker)
                || source.getDirectEntity() != attacker || source.is(DamageTypeTags.IS_PROJECTILE)
                || source.is(DamageTypeTags.IS_EXPLOSION)) return damage;

        ElementImbue.State imbue = IMBUED.get(source);
        if (source instanceof EpicFightDamageSource ef && ef.getAnimation().get() instanceof AttackAnimation) {
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
        ReactionResult result = ElementAttachment.attach(target, attacker, imbue.element(), imbue.units(),
                "epicfight:imbue:" + imbue.element().name(), AttachCooldown.STANDARD, false);
        DamageServerEventHandler.tag(target, result.indicatorKey(kind.indicatorKey()));
        return (damage + result.catalyzeFlat()) * Math.max(0.0, 1.0 + damageBonus) * result.amplify();
    }


}
