package com.dsp2.genshinerc.common.combat.shield;

import com.dsp2.genshinerc.common.element.ElementType;
import com.dsp2.genshinerc.common.network.GenshinercNetwork;
import com.dsp2.genshinerc.common.network.combat.SyncShieldPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public final class ShieldManager {
    private static final Map<LivingEntity, List<ShieldInstance>> SHIELDS = new WeakHashMap<>();
    private static final Set<LivingEntity> DIRTY = Collections.newSetFromMap(new WeakHashMap<>());
    private static final Set<LivingEntity> HIT = Collections.newSetFromMap(new WeakHashMap<>());

    private ShieldManager() {}

    public static void add(LivingEntity entity, ShieldKind kind, @Nullable ElementType element, float amount, int durationTicks) {
        if (entity.level().isClientSide() || amount <= 0f) return;
        List<ShieldInstance> list = SHIELDS.computeIfAbsent(entity, k -> new ArrayList<>());
        for (ShieldInstance s : list) {
            if (s.sameAttribute(kind, element)) {
                s.stack(amount, durationTicks);
                DIRTY.add(entity);
                return;
            }
        }
        list.add(new ShieldInstance(kind, element, amount, durationTicks));
        DIRTY.add(entity);
    }

    public static float absorb(LivingEntity entity, float damage) {
        List<ShieldInstance> list = SHIELDS.get(entity);
        if (list == null || list.isEmpty() || damage <= 0f) return 0f;
        float largest = 0f;
        for (ShieldInstance s : list) {
            largest = Math.max(largest, s.amount());
            s.damage(damage);
        }
        list.removeIf(s -> s.amount() <= 0f);
        HIT.add(entity);
        DIRTY.add(entity);
        return Math.min(damage, largest);
    }

    public static List<ShieldInstance> get(LivingEntity entity) {
        List<ShieldInstance> list = SHIELDS.get(entity);
        return list == null ? List.of() : Collections.unmodifiableList(list);
    }

    public static float largest(LivingEntity entity) {
        float max = 0f;
        for (ShieldInstance s : get(entity)) max = Math.max(max, s.amount());
        return max;
    }

    public static void clear(LivingEntity entity) {
        if (SHIELDS.remove(entity) != null) DIRTY.add(entity);
    }

    public static void tick() {
        Iterator<Map.Entry<LivingEntity, List<ShieldInstance>>> it = SHIELDS.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<LivingEntity, List<ShieldInstance>> entry = it.next();
            LivingEntity e = entry.getKey();
            if (e.isRemoved()) {
                it.remove();
                continue;
            }
            if (entry.getValue().removeIf(ShieldInstance::tickExpired)) DIRTY.add(e);
        }

        for (LivingEntity e : DIRTY) {
            if (e.isRemoved()) continue;
            GenshinercNetwork.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> e), snapshot(e, HIT.contains(e)));
            List<ShieldInstance> list = SHIELDS.get(e);
            if (list != null && list.isEmpty()) SHIELDS.remove(e);
        }
        DIRTY.clear();
        HIT.clear();
    }

    public static void sendTo(ServerPlayer player, LivingEntity entity) {
        if (SHIELDS.containsKey(entity)) {
            GenshinercNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), snapshot(entity, false));
        }
    }

    private static SyncShieldPacket snapshot(LivingEntity e, boolean hit) {
        List<SyncShieldPacket.Entry> entries = new ArrayList<>();
        for (ShieldInstance s : get(e)) entries.add(new SyncShieldPacket.Entry(s.kind(), s.element(), s.amount(), s.max()));
        return new SyncShieldPacket(e.getId(), hit, entries);
    }

    public static void reset() {
        SHIELDS.clear();
        DIRTY.clear();
        HIT.clear();
    }
}