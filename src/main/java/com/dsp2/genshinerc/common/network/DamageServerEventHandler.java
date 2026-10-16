package com.dsp2.genshinerc.common.network;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = "genshinerc", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class DamageServerEventHandler {

    private static final Map<UUID, Float> LAST_ABSORPTION = new HashMap<>();
    private static final Map<UUID, Float> PRE_ABSORPTION = new HashMap<>();
    private static final Map<UUID, Float> PRE_HEALTH = new HashMap<>();

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingHurtPre(LivingHurtEvent event) {
        LivingEntity e = event.getEntity();
        if (e.level().isClientSide()) return;
        PRE_ABSORPTION.put(e.getUUID(), e.getAbsorptionAmount());
        PRE_HEALTH.put(e.getUUID(), e.getHealth());
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingDamage(LivingDamageEvent event) {
        LivingEntity e = event.getEntity();
        if (e.level().isClientSide()) return;

        Float preAbsorbBoxed = PRE_ABSORPTION.remove(e.getUUID());
        Float preHealthBoxed = PRE_HEALTH.remove(e.getUUID());
        if (preAbsorbBoxed == null || preHealthBoxed == null) return;

        float preAbsorb = preAbsorbBoxed;
        float preHealth = preHealthBoxed;
        float postAbsorb = e.getAbsorptionAmount();
        float actualDamage = Math.max(0f, event.getAmount());
        float absorbed = Math.max(0f, preAbsorb - postAbsorb);

        if (actualDamage < 0.001f && absorbed < 0.001f) return;

        boolean kill = (preHealth - actualDamage) <= 0;
        if (kill) actualDamage = Math.min(actualDamage, preHealth);

        NetworkHandler.sendDamage(e, "physical", actualDamage, false, kill, false, absorbed, null, null, false, false);
    }

    @SubscribeEvent
    public static void onLivingHeal(LivingHealEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        LivingEntity e = event.getEntity();
        float healAmount = event.getAmount();
        if (healAmount <= 0) return;

        float missing = e.getMaxHealth() - e.getHealth();
        if (missing < 0) missing = 0;
        float actualHeal = Math.min(healAmount, missing);
        if (actualHeal <= 0) return;

        boolean fullHeal = actualHeal >= missing - 0.001f;

        NetworkHandler.sendDamage(e, "physical", -actualHeal, false, false, fullHeal, 0f, null, null, false, false);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer sp)) return;
        if (sp.level().isClientSide()) return;
        float current = sp.getAbsorptionAmount();
        Float last = LAST_ABSORPTION.get(sp.getUUID());
        if (last == null) { LAST_ABSORPTION.put(sp.getUUID(), current); return; }
        float diff = current - last;
        LAST_ABSORPTION.put(sp.getUUID(), current);
        if (diff > 0.001f) {
            NetworkHandler.sendDamage(sp, "physical", diff, false, false, false, 0f, null, null, true, false);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        LAST_ABSORPTION.remove(event.getEntity().getUUID());
        PRE_ABSORPTION.remove(event.getEntity().getUUID());
        PRE_HEALTH.remove(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer sp)) return;
        NetworkHandler.syncConfigTo(sp, new DamageIndicatorConfigSyncPacket(
                com.dsp2.genshinerc.client.render.damageindicator.config.DamageIndicatorConfig.get()));
    }
}