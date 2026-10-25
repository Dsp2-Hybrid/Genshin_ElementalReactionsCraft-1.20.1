package com.dsp2.genshinerc.common.core.element;

import com.dsp2.genshinerc.common.network.GenshinercNetwork;
import com.dsp2.genshinerc.common.network.base.element.SyncAuraPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public final class ElementAuraManager {

    public static final class Aura {
        private float amount;
        private final float decayPerTick;

        private Aura(float amount, float decayPerTick) {
            this.amount = amount;
            this.decayPerTick = decayPerTick;
        }

        public float amount() { return amount; }
        public float decayPerTick() { return decayPerTick; }
    }

    private static final Map<LivingEntity, LinkedHashMap<ElementType, Aura>> AURAS = new WeakHashMap<>();
    private static final Map<LivingEntity, EnumSet<ElementType>> DIRTY = new LinkedHashMap<>();

    private ElementAuraManager() {}

    public static void applyUnits(LivingEntity entity, ElementType type, float units) {
        apply(entity, type, ElementGauge.gaugeFromUnits(units), ElementGauge.durationTicksFromUnits(units));
    }

    public static void apply(LivingEntity entity, ElementType type, float gauge, int durationTicks) {
        if (entity.level().isClientSide() || gauge <= 0f || durationTicks <= 0) return;
        float clamped = Math.min(ElementGauge.MAX_GAUGE, gauge);
        LinkedHashMap<ElementType, Aura> auras = AURAS.computeIfAbsent(entity, k -> new LinkedHashMap<>());
        Aura existing = auras.get(type);
        if (existing == null) {
            auras.put(type, new Aura(clamped, clamped / durationTicks));
        } else {
            existing.amount = Math.max(existing.amount, clamped);
        }
        markDirty(entity, null);
    }

    public static float consume(LivingEntity entity, ElementType type, float amount, boolean flash) {
        if (entity.level().isClientSide()) return 0f;
        LinkedHashMap<ElementType, Aura> auras = AURAS.get(entity);
        Aura aura = auras == null ? null : auras.get(type);
        if (aura == null) return 0f;

        float used = Math.min(aura.amount, Math.max(0f, amount));
        aura.amount -= used;
        if (aura.amount <= 1.0e-4f) {
            auras.remove(type);
            if (auras.isEmpty()) AURAS.remove(entity);
        }
        markDirty(entity, flash ? type : null);
        return used;
    }

    public static void flash(LivingEntity entity, ElementType type) {
        if (entity.level().isClientSide() || !hasAura(entity, type)) return;
        markDirty(entity, type);
    }

    public static void clear(LivingEntity entity) {
        if (entity.level().isClientSide()) return;
        if (AURAS.remove(entity) != null) markDirty(entity, null);
    }

    public static boolean hasAura(LivingEntity entity, ElementType type) {
        LinkedHashMap<ElementType, Aura> auras = AURAS.get(entity);
        return auras != null && auras.containsKey(type);
    }

    public static Map<ElementType, Aura> getAuras(LivingEntity entity) {
        LinkedHashMap<ElementType, Aura> auras = AURAS.get(entity);
        return auras == null ? Map.of() : Collections.unmodifiableMap(auras);
    }

    static void tick() {
        Iterator<Map.Entry<LivingEntity, LinkedHashMap<ElementType, Aura>>> it = AURAS.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<LivingEntity, LinkedHashMap<ElementType, Aura>> entry = it.next();
            LivingEntity entity = entry.getKey();
            if (entity.isRemoved()) {
                it.remove();
                continue;
            }
            boolean expired = false;
            Iterator<Aura> auraIt = entry.getValue().values().iterator();
            while (auraIt.hasNext()) {
                Aura aura = auraIt.next();
                aura.amount -= aura.decayPerTick;
                if (aura.amount <= 0f) {
                    auraIt.remove();
                    expired = true;
                }
            }
            if (expired) markDirty(entity, null);
            if (entry.getValue().isEmpty()) it.remove();
        }
        flush();
    }

    static void sendTo(ServerPlayer player, LivingEntity entity) {
        if (!AURAS.containsKey(entity)) return;
        GenshinercNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                snapshot(entity, EnumSet.noneOf(ElementType.class)));
    }

    static void reset() {
        AURAS.clear();
        DIRTY.clear();
    }

    private static void markDirty(LivingEntity entity, @Nullable ElementType flashed) {
        EnumSet<ElementType> set = DIRTY.computeIfAbsent(entity, k -> EnumSet.noneOf(ElementType.class));
        if (flashed != null) set.add(flashed);
    }

    private static void flush() {
        if (DIRTY.isEmpty()) return;
        for (Map.Entry<LivingEntity, EnumSet<ElementType>> entry : DIRTY.entrySet()) {
            LivingEntity entity = entry.getKey();
            if (entity.isRemoved()) continue;
            GenshinercNetwork.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity),
                    snapshot(entity, entry.getValue()));
        }
        DIRTY.clear();
    }

    private static SyncAuraPacket snapshot(LivingEntity entity, EnumSet<ElementType> flashed) {
        List<SyncAuraPacket.Entry> entries = new ArrayList<>();
        LinkedHashMap<ElementType, Aura> auras = AURAS.get(entity);
        if (auras != null) {
            auras.forEach((type, a) -> entries.add(new SyncAuraPacket.Entry(type, a.amount, a.decayPerTick)));
        }
        return new SyncAuraPacket(entity.getId(), entries, EnumSet.copyOf(flashed));
    }
}