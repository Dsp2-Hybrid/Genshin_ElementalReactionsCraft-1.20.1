package com.dsp2.genshinerc.common.core.combat.api;

import com.dsp2.genshinerc.common.registry.ModAttributes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import java.util.UUID;

public final class AttributeTemplates {
    private AttributeTemplates() {}
    public static void applyGenshinBase(LivingEntity e, double atk, double hp, double def) {
        UUID id = UUID.nameUUIDFromBytes(("genshin_base:" + e.getStringUUID()).getBytes());
        add(e, ModAttributes.BASE_ATTACK.get(), new AttributeModifier(id, "genshin_base_atk", atk, AttributeModifier.Operation.ADDITION));
        add(e, ModAttributes.FLAT_HP_BONUS.get(), new AttributeModifier(id, "genshin_base_hp", hp, AttributeModifier.Operation.ADDITION));
        add(e, ModAttributes.DEFENSE.get(), new AttributeModifier(id, "genshin_base_def", def, AttributeModifier.Operation.ADDITION));
    }
    public static void applyArtifactBonus(LivingEntity e, double flatATK, double flatHP, double flatDEF, double cr, double cd, double em) {
        UUID id = UUID.nameUUIDFromBytes(("artifact:" + e.getStringUUID()).getBytes());
        if (flatATK > 0) add(e, ModAttributes.FLAT_ATTACK_BONUS.get(), new AttributeModifier(id, "artifact_atk", flatATK, AttributeModifier.Operation.ADDITION));
        if (flatHP  > 0) add(e, ModAttributes.FLAT_HP_BONUS.get(),    new AttributeModifier(id, "artifact_hp",  flatHP,  AttributeModifier.Operation.ADDITION));
        if (flatDEF > 0) add(e, ModAttributes.FLAT_DEFENSE_BONUS.get(), new AttributeModifier(id, "artifact_def", flatDEF, AttributeModifier.Operation.ADDITION));
        if (cr > 0) add(e, ModAttributes.CRIT_RATE.get(),   new AttributeModifier(id, "artifact_cr", cr, AttributeModifier.Operation.ADDITION));
        if (cd > 0) add(e, ModAttributes.CRIT_DAMAGE.get(), new AttributeModifier(id, "artifact_cd", cd, AttributeModifier.Operation.ADDITION));
        if (em > 0) add(e, ModAttributes.ELEMENTAL_MASTERY.get(), new AttributeModifier(id, "artifact_em", em, AttributeModifier.Operation.ADDITION));
    }
    private static void add(LivingEntity e, net.minecraft.world.entity.ai.attributes.Attribute a, AttributeModifier m) {
        var inst = e.getAttribute(a); if (inst != null) inst.addPermanentModifier(m);
    }
}