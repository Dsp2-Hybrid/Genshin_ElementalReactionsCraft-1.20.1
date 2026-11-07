package com.dsp2.genshinerc.common.core.damage;


import com.dsp2.genshinerc.common.level.EntityLevelManager;
import com.dsp2.genshinerc.common.registry.ModAttributes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public record ElementalStats(int level, double mastery) {
    public static final ElementalStats NONE = new ElementalStats(1, 0.0);

    public static ElementalStats of(@Nullable Entity entity) {
        if (entity instanceof Player p) {
            return new ElementalStats(Math.max(1, EntityLevelManager.getPlayerLevel(p)), mastery(p));
        }
        if (entity instanceof LivingEntity l) {
            return new ElementalStats(Math.max(1, EntityLevelManager.getLevel(l)), mastery(l));
        }
        return NONE;
    }

    private static double mastery(LivingEntity l) {
        AttributeInstance em = l.getAttribute(ModAttributes.ELEMENTAL_MASTERY.get());
        return em == null ? 0.0 : em.getValue();
    }
}
