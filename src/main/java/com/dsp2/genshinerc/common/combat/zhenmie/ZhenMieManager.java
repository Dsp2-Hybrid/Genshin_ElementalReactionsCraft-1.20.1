package com.dsp2.genshinerc.common.combat.zhenmie;

import com.dsp2.genshinerc.common.network.GenshinercNetwork;
import com.dsp2.genshinerc.common.network.combat.SyncZhenMiePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public final class ZhenMieManager {
    private static final float EPS = 1.0e-4f;
    private static final Map<LivingEntity, State> STATES = new WeakHashMap<>();
    private static final Set<LivingEntity> DIRTY = Collections.newSetFromMap(new WeakHashMap<>());

    private static final class State {
        float zhen, mie;
        boolean zhenBuff, mieBuff;
        long lastCombatTick;
        boolean decaying;
        @Nullable ClashResult pendingClash;
        float clashPoint;

        boolean idle() { return zhen <= 0f && mie <= 0f && !zhenBuff && !mieBuff; }
    }

    private ZhenMieManager() {}

    public static void applyDefense(LivingEntity defender, AttackIntensity intensity, DefenseOutcome outcome) {
        ZhenMieRules.Delta d = ZhenMieRules.forDefense(intensity, outcome);
        add(defender, d.zhen(), d.mie());
    }

    public static void add(LivingEntity entity, float dZhen, float dMie) {
        if (entity.level().isClientSide() || (dZhen == 0f && dMie == 0f)) return;
        State s = STATES.computeIfAbsent(entity, k -> new State());
        s.lastCombatTick = entity.level().getGameTime();
        s.decaying = false;

        float oldZ = s.zhen, oldM = s.mie;
        float z = Mth.clamp(oldZ + dZhen, 0f, 1f);
        float m = Mth.clamp(oldM + dMie, 0f, 1f);

        if (z + m >= 1f - EPS && (dZhen > 0f || dMie > 0f)) {
            float point = Mth.clamp(dZhen >= dMie ? 1f - oldM : oldZ, 0f, 1f);
            ClashResult result = ZhenMieRules.classify(point);
            s.zhen = 0f;
            s.mie = m * ZhenMieRules.MIE_KEPT_AFTER_CLASH;
            s.pendingClash = result;
            s.clashPoint = point;
            onClash(entity, result);
        } else {
            s.zhen = z;
            s.mie = m;
        }
        DIRTY.add(entity);
    }

    private static void onClash(LivingEntity entity, ClashResult result) {
        EfCombatBridge.applyStun(entity, result);
    }

    public static void setBuffs(LivingEntity entity, boolean zhenBuff, boolean mieBuff) {
        if (entity.level().isClientSide()) return;
        State s = STATES.computeIfAbsent(entity, k -> new State());
        s.zhenBuff = zhenBuff;
        s.mieBuff = mieBuff;
        DIRTY.add(entity);
    }

    public static void markCombat(LivingEntity entity) {
        State s = STATES.get(entity);
        if (s != null) s.lastCombatTick = entity.level().getGameTime();
    }

    public static float zhen(LivingEntity entity) { State s = STATES.get(entity); return s == null ? 0f : s.zhen; }
    public static float mie(LivingEntity entity)  { State s = STATES.get(entity); return s == null ? 0f : s.mie; }

    public static void clear(LivingEntity entity) {
        State s = STATES.get(entity);
        if (s == null) return;
        s.zhen = s.mie = 0f;
        s.zhenBuff = s.mieBuff = false;
        DIRTY.add(entity);
    }

    public static void tick() {
        Iterator<Map.Entry<LivingEntity, State>> it = STATES.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<LivingEntity, State> entry = it.next();
            LivingEntity e = entry.getKey();
            if (e.isRemoved()) {
                it.remove();
                continue;
            }
            State s = entry.getValue();
            long time = e.level().getGameTime();
            long idle = time - s.lastCombatTick;
            boolean changed = false;
            if (idle > ZhenMieRules.ZHEN_DECAY_DELAY_TICKS && s.zhen > 0f) {
                s.zhen = Math.max(0f, s.zhen - ZhenMieRules.ZHEN_DECAY_PER_TICK);
                changed = true;
            }
            if (idle > ZhenMieRules.MIE_DECAY_DELAY_TICKS && s.mie > 0f) {
                s.mie = Math.max(0f, s.mie - ZhenMieRules.MIE_DECAY_PER_TICK);
                changed = true;
            }
            if (changed) {
                s.decaying = true;
                boolean empty = s.zhen <= 0f && s.mie <= 0f;
                if (empty || time % ZhenMieRules.DECAY_SYNC_INTERVAL == 0) DIRTY.add(e);
            }
        }

        for (LivingEntity e : DIRTY) {
            State s = STATES.get(e);
            if (s == null || e.isRemoved()) continue;
            GenshinercNetwork.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> e), snapshot(e, s, true));
            s.pendingClash = null;
            if (s.idle()) STATES.remove(e);
        }
        DIRTY.clear();
    }

    public static void sendTo(ServerPlayer player, LivingEntity entity) {
        State s = STATES.get(entity);
        if (s != null) GenshinercNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), snapshot(entity, s, false));
    }

    private static SyncZhenMiePacket snapshot(LivingEntity e, State s, boolean withClash) {
        return new SyncZhenMiePacket(e.getId(), s.zhen, s.mie, s.zhenBuff, s.mieBuff, s.decaying,
                withClash ? s.pendingClash : null, s.clashPoint);
    }

    public static void reset() {
        STATES.clear();
        DIRTY.clear();
    }
}