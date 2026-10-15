package com.dsp2.genshinerc.common.network;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = "genshinerc", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class DamageServerEventHandler {
    private static final Map<UUID, Float> LAST_ABSORPTION = new HashMap<>();

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        LivingEntity e = event.getEntity();
        float original = event.getAmount();
        if (original <= 0) return;

        float amount = original;
        float absorbed = 0f;
        if (e.getAbsorptionAmount() > 0) {
            absorbed = Math.min(e.getAbsorptionAmount(), amount);
            amount -= absorbed;
            if (amount < 0) amount = 0;
        }
        boolean kill = (e.getHealth() - original) <= 0;

        if (absorbed > 0) {
            NetworkHandler.sendDamage(e, "physical", amount, false, kill, absorbed, null, null, false, false);
        } else {
            NetworkHandler.sendDamage(e, "physical", amount, false, kill, 0f, null, null, false, false);
        }
    }

    @SubscribeEvent
    public static void onLivingHeal(LivingHealEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        float a = event.getAmount();
        if (a <= 0) return;
        NetworkHandler.sendDamage(event.getEntity(), "physical", -a, false, false, 0f, null, null, false, false);
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
            NetworkHandler.sendDamage(sp, "physical", diff, false, false, 0f, null, null, true, false);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        LAST_ABSORPTION.remove(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer sp)) return;
        NetworkHandler.syncConfigTo(sp, new ConfigSyncPacket(
                com.dsp2.genshinerc.client.render.damageindicator.config.DamageIndicatorConfig.get()));
    }
}