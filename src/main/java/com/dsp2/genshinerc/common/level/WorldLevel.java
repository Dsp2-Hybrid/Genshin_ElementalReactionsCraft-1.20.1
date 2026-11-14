package com.dsp2.genshinerc.common.level;

import com.dsp2.genshinerc.common.config.ServerConfig;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "genshinerc")
public final class WorldLevel {
    private static volatile int level;

    private WorldLevel() {}

    public static int get() { return level; }

    public static void set(int v) { level = Mth.clamp(v, 0, ServerConfig.WORLD_LEVEL_MAX.get()); }

    public static void loadFromConfig() {
        try { set(ServerConfig.WORLD_LEVEL.get()); } catch (Exception ignored) {}
    }

    public static void saveToConfig() {
        try { ServerConfig.WORLD_LEVEL.set(level); } catch (Exception ignored) {}
    }

    public static int rollLevel(LivingEntity entity) {
        int wl = level;
        if (wl <= 0) return 0;
        int base = wl * 10;
        int jitter = entity.getRandom().nextInt(11) - 5;
        return Mth.clamp(base + jitter, 0, 120);
    }

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) { loadFromConfig(); }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) { saveToConfig(); }

    @SubscribeEvent
    public static void onJoin(EntityJoinLevelEvent e) {
        if (e.getLevel().isClientSide() || !(e.getEntity() instanceof Mob mob)) return;
        if (mob.getPersistentData().contains("GenshinLevel")) return;
        int l = rollLevel(mob);
        if (l > 0) EntityLevelManager.setLevel(mob, l);
    }
}