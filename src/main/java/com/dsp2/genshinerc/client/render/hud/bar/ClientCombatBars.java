package com.dsp2.genshinerc.client.render.hud.bar;

import com.dsp2.genshinerc.client.render.hud.bar.config.BarConfig;
import com.dsp2.genshinerc.client.render.hud.bar.shield.ClientShieldState;
import com.dsp2.genshinerc.client.render.hud.bar.zhenmie.ClientZhenMieState;
import com.dsp2.genshinerc.common.network.base.combat.SyncShieldPacket;
import com.dsp2.genshinerc.common.network.base.combat.SyncZhenMiePacket;
import net.minecraft.Util;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = "genshinerc", value = Dist.CLIENT)
public final class ClientCombatBars {
    private static final Map<Integer, ClientShieldState> SHIELDS = new HashMap<>();
    private static final Map<Integer, ClientZhenMieState> ZHEN_MIE = new HashMap<>();

    private ClientCombatBars() {}

    public static void applyShield(SyncShieldPacket msg) {
        SHIELDS.computeIfAbsent(msg.entityId(), k -> new ClientShieldState()).apply(msg, Util.getMillis(), BarConfig.get());
    }

    public static void applyZhenMie(SyncZhenMiePacket msg) {
        ZHEN_MIE.computeIfAbsent(msg.entityId(), k -> new ClientZhenMieState()).apply(msg, Util.getMillis(), BarConfig.get());
    }

    @Nullable public static ClientZhenMieState zhenMie(int id) { return ZHEN_MIE.get(id); }
    public static void removeShield(int id)  { SHIELDS.remove(id); }
    public static void removeZhenMie(int id) { ZHEN_MIE.remove(id); }

    @SubscribeEvent
    public static void onLeave(EntityLeaveLevelEvent event) {
        if (!event.getLevel().isClientSide()) return;
        int id = event.getEntity().getId();
        SHIELDS.remove(id);
        ZHEN_MIE.remove(id);
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        SHIELDS.clear();
        ZHEN_MIE.clear();
    }

    public static ClientShieldState shield(int entityId) { return SHIELDS.get(entityId); }
}