package com.dsp2.genshinerc.common.core.combat.zhenmie;

import com.dsp2.genshinerc.common.network.GenshinercNetwork;
import com.dsp2.genshinerc.common.network.base.combat.SyncZhenMiePacket;
import com.dsp2.genshinerc.common.core.combat.epicfight.EfCombatBridge;
import com.dsp2.genshinerc.common.level.EntityLevelManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.util.*;

public final class ZhenMieManager {
    private static final Logger LOGGER = LogUtils.getLogger();
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
        long staggerUntil;
        long resistanceUntil;
        int recentClashes;

        boolean idle() { return zhen <= 0f && mie <= 0f && !zhenBuff && !mieBuff && staggerUntil == 0L && recentClashes == 0; }
    }

    private ZhenMieManager() {}

    private static int getLevelSafe(LivingEntity entity) {
        if (entity instanceof Player p) return EntityLevelManager.getPlayerLevel(p);
        return EntityLevelManager.getLevel(entity);
    }

    public static void applyDefense(LivingEntity defender, AttackIntensity intensity, DefenseOutcome outcome) {
        applyDefense(defender, null, intensity, outcome, 0f);
    }

    public static void applyDefense(LivingEntity defender, @Nullable LivingEntity attacker, AttackIntensity intensity, DefenseOutcome outcome, float damage) {
        int atkLv = attacker != null ? getLevelSafe(attacker) : 0;
        int defLv = getLevelSafe(defender);
        float cap = ZhenMieRules.maxCapacity(defender.getMaxHealth(), defLv);
        ZhenMieRules.Delta d = ZhenMieRules.forDefense(intensity, outcome, atkLv, defLv, damage, cap);
        
        
                
        add(defender, d.zhen(), d.mie());
    }

    public static boolean staggered(LivingEntity entity) {
        State s = STATES.get(entity);
        return !entity.level().isClientSide() && s != null && entity.isAlive()
                && s.staggerUntil > entity.level().getGameTime();
    }

    public static float incomingMultiplier(LivingEntity entity) {
        State s = STATES.get(entity);
        return s != null && staggered(entity) ? 1f + ZhenMieRules.vulnerability(s.clashPoint) : 1f;
    }

    public static void fillMie(LivingEntity entity) {
        add(entity, 0f, ZhenMieRules.maxCapacity(entity.getMaxHealth(), getLevelSafe(entity)));
    }

    public static void add(LivingEntity entity, float dZhen, float dMie) {
        if (entity.level().isClientSide() || !entity.isAlive() || staggered(entity)
                || !Float.isFinite(dZhen) || !Float.isFinite(dMie)
                || (dZhen == 0f && dMie == 0f)) return;
        State s = STATES.computeIfAbsent(entity, k -> new State());
        s.lastCombatTick = entity.level().getGameTime();
        s.decaying = false;

        float oldZ = s.zhen, oldM = s.mie;
        int defLv = getLevelSafe(entity);
        float maxCap = ZhenMieRules.maxCapacity(entity.getMaxHealth(), defLv);
        
        
        float z = Mth.clamp(oldZ + dZhen, 0f, maxCap);
        float m = Mth.clamp(oldM + dMie, 0f, maxCap);

        if (maxCap > EPS && z + m >= maxCap - EPS && (dZhen > 0f || dMie > 0f)) {
            float point = Mth.clamp(dZhen >= dMie ? maxCap - oldM : oldZ, 0f, maxCap);
            float normalizedPoint = point / maxCap;
            
            ClashResult result = ZhenMieRules.classify(normalizedPoint);
            s.zhen = 0f;
            s.mie = m * ZhenMieRules.MIE_KEPT_AFTER_CLASH;
            s.pendingClash = result;
            s.clashPoint = normalizedPoint;
            onClash(entity, s, result);
        } else {
            s.zhen = z;
            s.mie = m;
        }
        DIRTY.add(entity);
    }

    private static void recoverResistance(State s, long now) {
        while (s.recentClashes > 0 && now >= s.resistanceUntil) {
            s.recentClashes--;
            s.resistanceUntil += ZhenMieRules.STAGGER_MEMORY_STEP;
        }
        if (s.recentClashes == 0) s.resistanceUntil = 0L;
    }

    private static void onClash(LivingEntity entity, State s, ClashResult result) {
        if (entity.level() instanceof ServerLevel level) {
            var center = entity.getBoundingBox().getCenter();
            level.sendParticles(ParticleTypes.CRIT, center.x, center.y, center.z, 18, 0.35, 0.45, 0.35, 0.12);
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, center.x, center.y, center.z, 14, 0.42, 0.48, 0.42, 0.10);
            level.sendParticles(ParticleTypes.FLASH, center.x, center.y, center.z, 1, 0.15, 0.15, 0.15, 0);
            level.playSound(null, entity.blockPosition(), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 0.85f, 0.75f);
            level.playSound(null, entity.blockPosition(), SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.PLAYERS, 0.45f, 1.2f);
        }
        long now = entity.level().getGameTime();
        recoverResistance(s, now);
        int ticks = ZhenMieRules.staggerTicks(result, s.recentClashes);
        boolean first = s.recentClashes == 0;
        s.recentClashes = Math.min(4, s.recentClashes + 1);
        s.staggerUntil = now + ticks;
        s.resistanceUntil = s.staggerUntil + ZhenMieRules.STAGGER_MEMORY_STEP;
        com.dsp2.genshinerc.common.core.element.FrozenControl.refresh(entity);
        EfCombatBridge.applyStun(entity, first ? result.stunType() : yesman.epicfight.world.damagesource.StunType.SHORT,
                ticks / 20f);
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
        s.staggerUntil = s.resistanceUntil = 0L;
        s.recentClashes = 0;
        s.pendingClash = null;
        com.dsp2.genshinerc.common.core.element.FrozenControl.refresh(entity);
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
            int oldResistance = s.recentClashes;
            recoverResistance(s, time);
            if (oldResistance != s.recentClashes) DIRTY.add(e);
            if (s.staggerUntil != 0L && (time >= s.staggerUntil || !e.isAlive())) {
                s.staggerUntil = 0L;
                com.dsp2.genshinerc.common.core.element.FrozenControl.refresh(e);
                DIRTY.add(e);
            }
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
        float cap = ZhenMieRules.maxCapacity(e.getMaxHealth(), getLevelSafe(e));
        float normalizedZhen = cap > 0f ? s.zhen / cap : 0f;
        float normalizedMie = cap > 0f ? s.mie / cap : 0f;
        
                
        return new SyncZhenMiePacket(e.getId(), normalizedZhen, normalizedMie, s.zhenBuff, s.mieBuff, s.decaying,
                withClash || staggered(e) ? s.pendingClash != null ? s.pendingClash
                        : staggered(e) ? ZhenMieRules.classify(s.clashPoint) : null : null,
                s.clashPoint, (int) Math.max(0L, s.staggerUntil - e.level().getGameTime()));
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
            tag.putLong("staggerUntil", s.staggerUntil);
            tag.putLong("resistanceUntil", s.resistanceUntil);
            tag.putInt("recentClashes", s.recentClashes);
            tag.putFloat("clashPoint", s.clashPoint);
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
            long now = entity.level().getGameTime();
            s.staggerUntil = Math.min(now + 60L, tag.getLong("staggerUntil"));
            if (s.staggerUntil <= now) s.staggerUntil = 0L;
            s.resistanceUntil = Math.min(now + 60L + ZhenMieRules.STAGGER_MEMORY_STEP, tag.getLong("resistanceUntil"));
            s.recentClashes = Mth.clamp(tag.getInt("recentClashes"), 0, 4);
            s.clashPoint = Mth.clamp(tag.getFloat("clashPoint"), 0f, 1f);
            recoverResistance(s, now);
            com.dsp2.genshinerc.common.core.element.FrozenControl.refresh(entity);
            s.decaying = true;
            DIRTY.add(entity);
        }
    }

    public static void reset() {
        STATES.clear();
        DIRTY.clear();
    }
}