package com.dsp2.genshinerc.common.core.damage;

import com.dsp2.genshinerc.common.core.reaction.ReactionDamage;
import com.dsp2.genshinerc.common.level.EntityLevelManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Fireball;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "genshinerc")
public final class StatusDamageScaling {
    private static final float LEVEL_SCALE = 0.035f;
    private static final float MAX_SCALE = 6.0f;
    private static final String ROOT = "GenshinStatusSources";
    private static final int FIRE_TRACK_TICKS = 240;

    private StatusDamageScaling() {}

    @SubscribeEvent
    public static void onEffectAdded(MobEffectEvent.Added event) {
        LivingEntity victim = event.getEntity();
        if (victim.level().isClientSide()) return;
        MobEffect effect = event.getEffectInstance().getEffect();
        String key = effect == MobEffects.POISON ? "poison" : effect == MobEffects.WITHER ? "wither" : null;
        if (key == null) return;
        Entity source = event.getEffectSource();
        if (source instanceof LivingEntity living && living != victim) {
            record(victim, key, living, event.getEffectInstance().getDuration() + 40);
        } else {
            forget(victim, key);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onHurt(LivingHurtEvent event) {
        LivingEntity victim = event.getEntity();
        DamageSource source = event.getSource();
        if (victim.level().isClientSide() || event.getAmount() <= 0f || ReactionDamage.isDealing()) return;

        if (source.getEntity() instanceof LivingEntity attacker) {
            trackFire(victim, source, attacker);
            if (!isStatusDamage(source)) return;
            int level = Math.max(0, Math.min(120, EntityLevelManager.getLevel(attacker)));
            event.setAmount(event.getAmount() * Math.min(MAX_SCALE, 1f + level * LEVEL_SCALE));
            return;
        }

        String key = trackKey(source);
        if (key == null) return;
        int level = trackedLevel(victim, key);
        if (level <= 0) return;
        event.setAmount((float) (event.getAmount() * Math.pow(200.0, Math.min(120, level) / 100.0)));
    }

    private static void trackFire(LivingEntity victim, DamageSource source, LivingEntity attacker) {
        if (attacker == victim) return;
        Entity direct = source.getDirectEntity();
        boolean ignites = source.is(DamageTypeTags.IS_FIRE)
                || direct instanceof Fireball
                || (direct instanceof AbstractArrow arrow && arrow.isOnFire())
                || (direct == attacker && EnchantmentHelper.getFireAspect(attacker) > 0);
        if (ignites) record(victim, "fire", attacker, FIRE_TRACK_TICKS);
    }

    private static String trackKey(DamageSource source) {
        if (source.is(DamageTypes.WITHER)) return "wither";
        if (source.is(DamageTypes.MAGIC)) return "poison";
        if (source.is(DamageTypes.ON_FIRE)) return "fire";
        return null;
    }

    private static void record(LivingEntity victim, String key, LivingEntity source, int ticks) {
        CompoundTag data = victim.getPersistentData();
        CompoundTag root = data.getCompound(ROOT);
        CompoundTag entry = new CompoundTag();
        entry.putUUID("id", source.getUUID());
        entry.putInt("level", EntityLevelManager.getLevel(source));
        entry.putLong("until", victim.level().getGameTime() + ticks);
        root.put(key, entry);
        data.put(ROOT, root);
    }

    private static void forget(LivingEntity victim, String key) {
        CompoundTag data = victim.getPersistentData();
        if (!data.contains(ROOT, Tag.TAG_COMPOUND)) return;
        data.getCompound(ROOT).remove(key);
    }

    private static int trackedLevel(LivingEntity victim, String key) {
        CompoundTag root = victim.getPersistentData().getCompound(ROOT);
        if (!root.contains(key, Tag.TAG_COMPOUND)) return 0;
        CompoundTag entry = root.getCompound(key);
        if (victim.level().getGameTime() > entry.getLong("until")) return 0;
        int level = entry.getInt("level");
        if (entry.hasUUID("id") && victim.level() instanceof ServerLevel server
                && server.getEntity(entry.getUUID("id")) instanceof LivingEntity owner && owner.isAlive()) {
            level = EntityLevelManager.getLevel(owner);
        }
        return level;
    }

    private static boolean isStatusDamage(DamageSource source) {
        return source.is(DamageTypes.MAGIC)
                || source.is(DamageTypes.WITHER)
                || source.is(DamageTypes.DROWN)
                || source.is(DamageTypeTags.IS_LIGHTNING)
                || source.is(DamageTypeTags.IS_FIRE);
    }
}