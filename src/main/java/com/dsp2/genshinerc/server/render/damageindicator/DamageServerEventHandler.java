package com.dsp2.genshinerc.server.render.damageindicator;

import com.dsp2.genshinerc.common.network.damageindicator.DamageIndicatorConfigSyncPacket;
import com.dsp2.genshinerc.common.network.damageindicator.DamageIndicatorNetworkHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.TickEvent;
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

    private static class PreDamageState {
        LivingEntity entity;
        float preHealth;
        float preAbsorb;
    }
    private static final Map<Integer, PreDamageState> PRE_STATE = new HashMap<>();

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingHurtPre(LivingHurtEvent event) {
        LivingEntity e = event.getEntity();
        if (e.level().isClientSide()) return;

        if (!PRE_STATE.containsKey(e.getId())) {
            PreDamageState state = new PreDamageState();
            state.entity = e;
            state.preHealth = e.getHealth();
            state.preAbsorb = e.getAbsorptionAmount();
            PRE_STATE.put(e.getId(), state);
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (PRE_STATE.isEmpty()) return;

        for (PreDamageState state : PRE_STATE.values()) {
            LivingEntity e = state.entity;
            if (e == null || e.isRemoved()) continue;

            float healthLoss = state.preHealth - e.getHealth();
            float absorbLoss = state.preAbsorb - e.getAbsorptionAmount();

            if (healthLoss < 0.001f && absorbLoss < 0.001f) continue;

            float actualDamage = Math.max(0f, healthLoss);
            float absorbed = Math.max(0f, absorbLoss);
            boolean kill = e.getHealth() <= 0.001f;

            DamageIndicatorNetworkHandler.sendDamage(e, "physical", actualDamage, false, kill, false, absorbed, null, null, false, false);
        }
        PRE_STATE.clear();
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

        DamageIndicatorNetworkHandler.sendDamage(e, "physical", -actualHeal, false, false, fullHeal, 0f, null, null, false, false);
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
                com.dsp2.genshinerc.client.render.damageindicator.config.DamageIndicatorConfig.get()));
    }
}