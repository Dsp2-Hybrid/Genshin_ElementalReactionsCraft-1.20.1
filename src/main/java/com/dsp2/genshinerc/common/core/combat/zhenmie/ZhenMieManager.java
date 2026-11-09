package com.dsp2.genshinerc.common.core.combat.zhenmie;

import com.dsp2.genshinerc.common.network.GenshinercNetwork;
import com.dsp2.genshinerc.common.network.base.combat.SyncZhenMiePacket;
import com.dsp2.genshinerc.common.core.combat.epicfight.EfCombatBridge;
import com.dsp2.genshinerc.common.level.EntityLevelManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public final class ZhenMieManager {
    private static final float EPS = 1.0e-4f;
    private static final String NBT_KEY = "GenshinZhenMie";
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

    private static int getLevelSafe(LivingEntity entity) {
        if (entity instanceof Player p) return EntityLevelManager.getPlayerLevel(p);
        return EntityLevelManager.getLevel(entity);
    }

    public static void applyDefense(LivingEntity defender, AttackIntensity intensity, DefenseOutcome outcome) {
        applyDefense(defender, null, intensity, outcome, ZhenMieRules.DMG_REF);
    }

    public static void applyDefense(LivingEntity defender, @Nullable LivingEntity attacker, AttackIntensity intensity, DefenseOutcome outcome) {
        applyDefense(defender, attacker, intensity, outcome, ZhenMieRules.DMG_REF);
    }

    public static void applyDefense(LivingEntity defender, @Nullable LivingEntity attacker, AttackIntensity intensity, DefenseOutcome outcome, float damage) {
        int atkLv = attacker != null ? getLevelSafe(attacker) : 0;
        int defLv = getLevelSafe(defender);
        ZhenMieRules.Delta d = ZhenMieRules.forDefense(intensity, outcome, atkLv, defLv, damage);
        add(defender, d.zhen(), d.mie());
    }

    public static void add(LivingEntity entity, float dZhen, float dMie) {
        if (entity.level().isClientSide() || (dZhen == 0f && dMie == 0f)) return;
        State s = STATES.computeIfAbsent(entity, k -> new State());
        s.lastCombatTick = entity.level().getGameTime();
        s.decaying = false;

        float oldZ = s.zhen, oldM = s.mie;
        int defLv = getLevelSafe(entity);

        float maxCap = ZhenMieRules.maxCapacity(entity.getMaxHealth(), defLv);

        float z = Mth.clamp(oldZ + dZhen, 0f, maxCap);
        float m = Mth.clamp(oldM + dMie, 0f, maxCap);

        if (z + m >= maxCap - EPS && (dZhen > 0f || dMie > 0f)) {
            float point = Mth.clamp(dZhen >= dMie ? maxCap - oldM : oldZ, 0f, maxCap);
            float normalizedPoint = point / maxCap;
            
            ClashResult result = ZhenMieRules.classify(normalizedPoint);
            s.zhen = 0f;
            s.mie = m * ZhenMieRules.MIE_KEPT_AFTER_CLASH;
            s.pendingClash = result;
            s.clashPoint = normalizedPoint;
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
            
            boolean zDecay = idle > ZhenMieRules.ZHEN_DECAY_DELAY_TICKS && s.zhen > 0f;
            boolean mDecay = idle > ZhenMieRules.MIE_DECAY_DELAY_TICKS && s.mie > 0f;
            if (zDecay || mDecay) {
                int lv = getLevelSafe(e);
                float cap = ZhenMieRules.maxCapacity(e.getMaxHealth(), lv);
                if (zDecay) s.zhen = Math.max(0f, s.zhen - ZhenMieRules.zhenDecayPerTick(cap, lv));
                if (mDecay) s.mie = Math.max(0f, s.mie - ZhenMieRules.mieDecayPerTick(cap, lv));
                s.decaying = true;
                boolean empty = s.zhen <= 0f && s.mie <= 0f;
                if (empty || time % ZhenMieRules.DECAY_SYNC_INTERVAL == 0) DIRTY.add(e);
            }
        }

        for (LivingEntity e : DIRTY) {
            State s = STATES.get(e);
            if (s == null || e.isRemoved()) continue;
            save(e);
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

    public static void save(LivingEntity entity) {
        State s = STATES.get(entity);
        CompoundTag nbt = entity.getPersistentData();
        if (s == null || s.idle()) {
            nbt.remove(NBT_KEY);
        } else {
            CompoundTag tag = new CompoundTag();
            tag.putFloat("zhen", s.zhen);
            tag.putFloat("mie", s.mie);
            tag.putBoolean("zhenBuff", s.zhenBuff);
            tag.putBoolean("mieBuff", s.mieBuff);
            tag.putLong("lastCombatTick", s.lastCombatTick);
            nbt.put(NBT_KEY, tag);
        }
    }

    public static void load(LivingEntity entity) {
        CompoundTag nbt = entity.getPersistentData();
        if (nbt.contains(NBT_KEY)) {
            CompoundTag tag = nbt.getCompound(NBT_KEY);
            State s = STATES.computeIfAbsent(entity, k -> new State());
            s.zhen = tag.getFloat("zhen");
            s.mie = tag.getFloat("mie");
            s.zhenBuff = tag.getBoolean("zhenBuff");
            s.mieBuff = tag.getBoolean("mieBuff");
            s.lastCombatTick = tag.getLong("lastCombatTick");
            s.decaying = true;
            DIRTY.add(entity);
        }
    }

    public static void reset() {
        STATES.clear();
        DIRTY.clear();
    }
}