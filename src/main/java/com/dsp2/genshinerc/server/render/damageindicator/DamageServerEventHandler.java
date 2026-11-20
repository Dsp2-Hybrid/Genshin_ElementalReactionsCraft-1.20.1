package com.dsp2.genshinerc.server.render.damageindicator;

import net.minecraft.world.damagesource.DamageSource;
import java.util.ArrayDeque;

import com.dsp2.genshinerc.common.config.ServerConfig;
import com.dsp2.genshinerc.common.core.damage.DamageMapper;
import com.dsp2.genshinerc.common.core.reaction.ReactionDamage;
import com.dsp2.genshinerc.common.network.base.damageindicator.DamageIndicatorNetworkHandler;
import com.dsp2.genshinerc.common.network.base.damageindicator.DamageIndicatorConfigSyncPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = "genshinerc", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class DamageServerEventHandler {

    private static final Map<UUID, Float> LAST_ABSORPTION = new HashMap<>();
    private static final ThreadLocal<ArrayDeque<Hit>> HITS = ThreadLocal.withInitial(ArrayDeque::new);

    private record PendingPalette(DamageSource source, String palette) {}
    private static final ThreadLocal<PendingPalette> PENDING_PALETTE = new ThreadLocal<>();

    public static void withPalette(DamageSource source, String palette, Runnable action) {
        if (palette == null) {
            action.run();
            return;
        }
        PendingPalette previous = PENDING_PALETTE.get();
        PENDING_PALETTE.set(new PendingPalette(source, palette));
        try {
            action.run();
        } finally {
            if (previous == null) PENDING_PALETTE.remove();
            else PENDING_PALETTE.set(previous);
        }
    }

    private static final class Hit {
        final LivingEntity entity;
        final DamageSource source;
        final float health;
        final float absorption;
        String key;
        String palette;
        float nestedHealth;
        float nestedAbsorption;
        float shield;
        boolean critical;

        Hit(LivingEntity entity, DamageSource source) {
            this.entity = entity;
            this.source = source;
            this.health = entity.getHealth();
            this.absorption = entity.getAbsorptionAmount();
            this.key = source.is(DamageTypeTags.IS_EXPLOSION) ? "pyro" : DamageMapper.classify(source).indicatorKey();
        }
    }

    private static Hit find(LivingEntity entity) {
        for (Hit hit : HITS.get()) {
            if (hit.entity == entity) return hit;
        }
        return null;
    }

    public static void begin(LivingEntity entity, DamageSource source) {
        if (entity.level().isClientSide() || ReactionDamage.isDealing() || !ServerConfig.ENABLED.get()) return;
        Hit hit = new Hit(entity, source);
        PendingPalette pending = PENDING_PALETTE.get();
        if (pending != null && pending.source() == source) hit.palette = pending.palette();
        HITS.get().push(hit);
    }

    public static void end(LivingEntity entity, DamageSource source, boolean accepted) {
        if (entity.level().isClientSide() || ReactionDamage.isDealing()) return;
        ArrayDeque<Hit> stack = HITS.get();
        Hit hit = stack.peek();
        if (hit == null || hit.entity != entity || hit.source != source) return;
        stack.pop();
        float rawHealth = Math.max(0f, hit.health - entity.getHealth());
        float rawAbsorption = Math.max(0f, hit.absorption - entity.getAbsorptionAmount());
        discount(entity, rawHealth, rawAbsorption);
        if (stack.isEmpty()) HITS.remove();
        float damage = accepted ? Math.max(0f, rawHealth - hit.nestedHealth) : 0f;
        float absorbed = hit.shield + (accepted ? Math.max(0f, rawAbsorption - hit.nestedAbsorption) : 0f);
        if (damage < 0.001f && absorbed < 0.001f) return;
        DamageIndicatorNetworkHandler.sendDamage(entity, hit.key, damage, hit.critical,
                damage > 0f && !entity.isAlive(), false, absorbed, hit.palette, null, false, false);
    }

    public static void discount(LivingEntity entity, float health, float absorb) {
        Hit hit = find(entity);
        if (hit != null) {
            hit.nestedHealth += Math.max(0f, health);
            hit.nestedAbsorption += Math.max(0f, absorb);
        }
    }

    public static void shieldAbsorb(LivingEntity entity, float blocked, String key) {
        if (blocked <= 0f || !ServerConfig.ENABLED.get()) return;
        Hit hit = ReactionDamage.isDealing() ? null : find(entity);
        if (hit != null) {
            hit.shield += blocked;
        } else {
            DamageIndicatorNetworkHandler.sendDamage(entity, ReactionDamage.indicatorKey(key), 0f,
                    false, false, false, blocked, ReactionDamage.indicatorPalette(), null, false, false);
        }
    }

    public static void tag(LivingEntity entity, String key) {
        Hit hit = find(entity);
        if (hit != null) hit.key = key;
    }

    public static void tagPalette(LivingEntity entity, String palette) {
        Hit hit = find(entity);
        if (hit != null && palette != null) hit.palette = palette;
    }

    public static void markCrit(LivingEntity entity) {
        Hit hit = find(entity);
        if (hit != null) hit.critical = true;
    }
    @SubscribeEvent
    public static void onLivingHeal(LivingHealEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        if (!ServerConfig.ENABLED.get()) return;

        LivingEntity e = event.getEntity();
        float healAmount = event.getAmount();
        if (healAmount <= 0) return;

        float missing = e.getMaxHealth() - e.getHealth();
        if (missing < 0) missing = 0;
        float actualHeal = Math.min(healAmount, missing);
        if (actualHeal <= 0) return;

        boolean fullHeal = actualHeal >= missing - 0.001f;

        DamageIndicatorNetworkHandler.sendDamage(e, "physical", -actualHeal, false, false, fullHeal, 0f, null, null, false, false);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer sp)) return;
        if (sp.level().isClientSide()) return;
        if (!ServerConfig.ENABLED.get()) return;

        float current = sp.getAbsorptionAmount();
        Float last = LAST_ABSORPTION.get(sp.getUUID());
        if (last == null) { LAST_ABSORPTION.put(sp.getUUID(), current); return; }
        float diff = current - last;
        LAST_ABSORPTION.put(sp.getUUID(), current);
        if (diff > 0.001f) {
            DamageIndicatorNetworkHandler.sendDamage(sp, "physical", diff, false, false, false, 0f, null, null, true, false);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        LAST_ABSORPTION.remove(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer sp)) return;

        DamageIndicatorNetworkHandler.syncConfigTo(sp, new DamageIndicatorConfigSyncPacket(
                ServerConfig.ENABLED.get(),
                ServerConfig.MAX_DISTANCE.get()
        ));
    }
}