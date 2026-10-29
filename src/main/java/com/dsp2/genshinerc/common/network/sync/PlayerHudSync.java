package com.dsp2.genshinerc.common.network.sync;

import com.dsp2.genshinerc.common.level.EntityLevelManager;
import com.dsp2.genshinerc.common.network.GenshinercNetwork;
import com.dsp2.genshinerc.common.network.sync.packet.SyncPlayerHudPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = "genshinerc")
public final class PlayerHudSync {
    private static final float EXHAUSTION_STEP = 0.02f;
    private static final Map<UUID, float[]> LAST_SENT = new HashMap<>();

    private static volatile int clientLevel;
    private static volatile float clientExhaustion;

    private PlayerHudSync() {}

    public static int clientLevel() { return clientLevel; }
    public static float clientExhaustion() { return clientExhaustion; }

    public static void applyOnClient(int level, float exhaustion) {
        clientLevel = level;
        clientExhaustion = exhaustion;
    }

    public static void clientReset() { applyOnClient(0, 0f); }

    public static void markDirty(ServerPlayer player) { LAST_SENT.remove(player.getUUID()); }

    @SubscribeEvent
    public static void onTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer sp)) return;
        int level = EntityLevelManager.getPlayerLevel(sp);
        float exhaustion = sp.getFoodData().getExhaustionLevel();
        float[] last = LAST_SENT.get(sp.getUUID());
        if (last != null && last[0] == level && Math.abs(last[1] - exhaustion) < EXHAUSTION_STEP) return;
        LAST_SENT.put(sp.getUUID(), new float[]{level, exhaustion});
        GenshinercNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> sp), new SyncPlayerHudPacket(level, exhaustion));
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent e) {
        if (e.getEntity() instanceof ServerPlayer sp) {
            markDirty(sp);
            float stamina = sp.getPersistentData().getFloat("GenshinStamina");
            if (stamina > 0) {
                PlayerPatch<?> patch = EpicFightCapabilities.getEntityPatch(sp, PlayerPatch.class);
                if (patch != null) patch.setStamina(stamina);
            }
        }
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent e) {
        if (e.getEntity() instanceof ServerPlayer sp) markDirty(sp);
    }

    @SubscribeEvent
    public static void onDimension(PlayerEvent.PlayerChangedDimensionEvent e) {
        if (e.getEntity() instanceof ServerPlayer sp) markDirty(sp);
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent e) {
        LAST_SENT.remove(e.getEntity().getUUID());
        if (e.getEntity() instanceof ServerPlayer sp) {
            PlayerPatch<?> patch = EpicFightCapabilities.getEntityPatch(sp, PlayerPatch.class);
            if (patch != null) sp.getPersistentData().putFloat("GenshinStamina", patch.getStamina());
        }
    }

    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone e) {
        if (!e.isWasDeath()) return;
        int level = e.getOriginal().getPersistentData().getInt("GenshinPlayerLevel");
        if (level > 0) e.getEntity().getPersistentData().putInt("GenshinPlayerLevel", level);
        float stamina = e.getOriginal().getPersistentData().getFloat("GenshinStamina");
        if (stamina > 0) e.getEntity().getPersistentData().putFloat("GenshinStamina", stamina);
    }
}