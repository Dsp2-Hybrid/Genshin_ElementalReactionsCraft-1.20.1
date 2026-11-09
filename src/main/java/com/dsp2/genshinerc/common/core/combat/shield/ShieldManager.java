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
        for (ShieldInstance s : list) {
            if (s.sameAttribute(kind, element)) { s.stack(amount, durationTicks); DIRTY.add(entity); return; }
        }
        list.add(new ShieldInstance(kind, element, amount, durationTicks));
        DIRTY.add(entity);
    }
    public record AbsorbResult(float blocked, float shieldSpent, boolean broke, @Nullable ElementType shieldElement) {
        public static final AbsorbResult NONE = new AbsorbResult(0f, 0f, false, null);
    }
    public static AbsorbResult absorbElemental(LivingEntity entity, float damage, DamageKind kind) {
        List<ShieldInstance> list = SHIELDS.get(entity);
        if (list == null || list.isEmpty() || damage <= 0f) return AbsorbResult.NONE;
        ShieldInstance best = null;
        for (ShieldInstance s : list) {
            if (s.kind() == ShieldKind.ELEMENTAL && s.element() != null && DamageKind.fromElement(s.element()) == kind) { best = s; break; }
        }
        if (best == null) for (ShieldInstance s : list) if (s.kind() == ShieldKind.SPECIAL) { best = s; break; }
        if (best == null) best = list.get(0);
        boolean sameElement = best.kind() == ShieldKind.ELEMENTAL && best.element() != null && DamageKind.fromElement(best.element()) == kind;
        float costPerDamage = sameElement ? 0.4f : 1.0f;
        float available = best.amount();
        float needed = damage * costPerDamage;
        float blocked;
        boolean broke;
        if (available >= needed) { best.damage(needed); blocked = damage; broke = false; }
        else { blocked = available / costPerDamage; best.damage(available); broke = true; }
        list.removeIf(s -> s.amount() <= 0f);
        HIT.add(entity);
        DIRTY.add(entity);
        return new AbsorbResult(blocked, Math.min(available, needed), broke, best.element());
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
            if (entry.getValue().removeIf(ShieldInstance::tickExpired)) DIRTY.add(e);
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
        List<SyncShieldPacket.Entry> entries = new ArrayList<>();
        for (ShieldInstance s : get(e)) entries.add(new SyncShieldPacket.Entry(s.kind(), s.element(), s.amount(), s.max()));
        return new SyncShieldPacket(e.getId(), hit, entries);
    }
    public static void reset() { SHIELDS.clear(); DIRTY.clear(); HIT.clear(); }
}