package com.dsp2.genshinerc.common.core.combat.shield;

import com.dsp2.genshinerc.common.core.damage.DamageKind;
import com.dsp2.genshinerc.common.core.element.ElementType;
import com.dsp2.genshinerc.common.network.GenshinercNetwork;
import com.dsp2.genshinerc.common.network.base.combat.SyncShieldPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
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
        if (kind == ShieldKind.ELEMENTAL) {
            list.removeIf(shield -> shield.kind() == ShieldKind.ELEMENTAL);
            list.add(new ShieldInstance(kind, element, amount, durationTicks));
            DIRTY.add(entity);
            return;
        }
        for (ShieldInstance shield : list) {
            if (shield.sameAttribute(kind, element)) {
                shield.stack(amount, durationTicks);
                DIRTY.add(entity);
                return;
            }
        }
        list.add(new ShieldInstance(kind, element, amount, durationTicks));
        DIRTY.add(entity);
    }

    public record AbsorbResult(float blocked, float shieldSpent, boolean broke, @Nullable ElementType shieldElement) {
        public static final AbsorbResult NONE = new AbsorbResult(0f, 0f, false, null);
    }

    public static AbsorbResult absorbElemental(LivingEntity entity, float damage, DamageKind kind) {
        return absorbElemental(entity, damage, kind, 1f);
    }

    public static AbsorbResult absorbElemental(LivingEntity entity, float damage, DamageKind kind, float rockCost) {
        List<ShieldInstance> list = SHIELDS.get(entity);
        if (list == null || list.isEmpty() || damage <= 0f) return AbsorbResult.NONE;

        float blocked = 0f;
        float spent = 0f;
        boolean broke = false;
        ElementType displayElement = null;
        float displayAmount = -1f;

        for (ShieldInstance shield : list) {
            boolean sameElement = shield.kind() == ShieldKind.ELEMENTAL
                    && shield.element() != null
                    && DamageKind.fromElement(shield.element()) == kind;
            float costPerDamage = (sameElement ? 0.4f : 1f)
                    * (shield.element() == ElementType.GEO ? rockCost : 1f);
            costPerDamage = Math.max(0.0001f, costPerDamage);

            float available = shield.amount();
            float needed = damage * costPerDamage;
            float consumed = Math.min(available, needed);
            shield.damage(consumed);
            spent += consumed;
            blocked = Math.max(blocked, Math.min(damage, available / costPerDamage));
            if (available < needed) broke = true;

            if (available > displayAmount) {
                displayAmount = available;
                displayElement = shield.element();
            }
        }

        list.removeIf(shield -> shield.amount() <= 0f);
        HIT.add(entity);
        DIRTY.add(entity);
        return new AbsorbResult(blocked, spent, broke, displayElement);
    }

    public static float absorb(LivingEntity entity, float damage) { return absorbElemental(entity, damage, DamageKind.PHYSICAL).blocked(); }

    public static List<ShieldInstance> get(LivingEntity entity) {
        List<ShieldInstance> list = SHIELDS.get(entity);
        return list == null ? List.of() : Collections.unmodifiableList(list);
    }

    public static float largest(LivingEntity entity) {
        float max = 0f;
        for (ShieldInstance s : get(entity)) max = Math.max(max, s.amount());
        return max;
    }

    public static void clear(LivingEntity entity) { if (SHIELDS.remove(entity) != null) { DIRTY.add(entity); save(entity); } }

    public static void tick() {
        Iterator<Map.Entry<LivingEntity, List<ShieldInstance>>> it = SHIELDS.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<LivingEntity, List<ShieldInstance>> entry = it.next();
            LivingEntity e = entry.getKey();
            if (e.isRemoved()) { it.remove(); continue; }
            boolean expired = entry.getValue().removeIf(ShieldInstance::tickExpired);
            boolean warningSync = !expired && entry.getValue().stream()
                    .anyMatch(shield -> shield.ticksLeft() > 0
                            && shield.ticksLeft() <= 80
                            && shield.ticksLeft() % 5 == 0);
            if (expired || warningSync) DIRTY.add(e);
        }
        for (LivingEntity e : DIRTY) {
            if (e.isRemoved()) continue;
            save(e);
            GenshinercNetwork.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> e), snapshot(e, HIT.contains(e)));
            List<ShieldInstance> list = SHIELDS.get(e);
            if (list != null && list.isEmpty()) SHIELDS.remove(e);
        }
        DIRTY.clear();
        HIT.clear();
    }

    public static void syncAll(ServerPlayer player) {
        for (LivingEntity entity : SHIELDS.keySet()) {
            if (!entity.isRemoved() && entity.level() == player.level()) sendTo(player, entity);
        }
    }

    public static void sendTo(ServerPlayer player, LivingEntity entity) {
        if (SHIELDS.containsKey(entity)) GenshinercNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), snapshot(entity, false));
    }

    public static void save(LivingEntity entity) {
        List<ShieldInstance> list = SHIELDS.get(entity);
        if (list == null || list.isEmpty()) { entity.getPersistentData().remove("GenshinShields"); return; }
        ListTag listTag = new ListTag();
        for (ShieldInstance s : list) {
            CompoundTag st = new CompoundTag();
            st.putString("kind", s.kind().name());
            st.putString("aura", s.element() == null ? "" : s.element().name());
            st.putFloat("amount", s.amount());
            st.putFloat("max", s.max());
            st.putInt("ticks", s.ticksLeft());
            listTag.add(st);
        }
        CompoundTag tag = new CompoundTag();
        tag.put("shields", listTag);
        entity.getPersistentData().put("GenshinShields", tag);
    }

    public static void load(LivingEntity entity) {
        CompoundTag nbt = entity.getPersistentData();
        if (nbt.contains("GenshinShields")) {
            CompoundTag tag = nbt.getCompound("GenshinShields");
            ListTag listTag = tag.getList("shields", Tag.TAG_COMPOUND);
            List<ShieldInstance> list = new ArrayList<>();
            for (int i = 0; i < listTag.size(); i++) {
                CompoundTag st = listTag.getCompound(i);
                ShieldKind kind = ShieldKind.valueOf(st.getString("kind"));
                String elStr = st.getString("aura");
                ElementType element = elStr.isEmpty() ? null : ElementType.valueOf(elStr);
                list.add(new ShieldInstance(kind, element, st.getFloat("amount"), st.getFloat("max"), st.getInt("ticks")));
            }
            if (!list.isEmpty()) { SHIELDS.put(entity, list); DIRTY.add(entity); }
        }
    }

    private static SyncShieldPacket snapshot(LivingEntity e, boolean hit) {
        List<ShieldInstance> list = new ArrayList<>(get(e));
        list.sort((a, b) -> Float.compare(b.amount(), a.amount()));
        List<SyncShieldPacket.Entry> entries = new ArrayList<>();
        for (ShieldInstance s : list) {
            entries.add(new SyncShieldPacket.Entry(s.kind(), s.element(), s.amount(), s.max(), s.ticksLeft()));
        }
        return new SyncShieldPacket(e.getId(), hit, entries);
    }

    public static void reset() { SHIELDS.clear(); DIRTY.clear(); HIT.clear(); }
}