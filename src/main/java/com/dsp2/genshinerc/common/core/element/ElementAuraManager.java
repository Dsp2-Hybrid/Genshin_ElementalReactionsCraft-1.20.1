package com.dsp2.genshinerc.common.core.element;

import com.dsp2.genshinerc.common.config.ServerConfig;
import com.dsp2.genshinerc.common.network.GenshinercNetwork;
import com.dsp2.genshinerc.common.network.base.element.SyncAuraPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public final class ElementAuraManager {
    private static final float QUICKEN_DECAY = 0.08f / 20f;

    public static final class Aura {
        private float amount;
        private float decayPerTick;
        private int age;
        private float holdCap;
        private float releaseDecay;
        private long holdUntil;

        private Aura(float amount, float decayPerTick) {
            this.amount = amount;
            this.decayPerTick = decayPerTick;
        }

        public float amount() { return amount; }
        public float decayPerTick() { return decayPerTick; }
    }

    private record FrozenMemory(int age, long tick) {}

    private static final Map<LivingEntity, LinkedHashMap<ElementType, Aura>> AURAS = new WeakHashMap<>();
    private static final Map<LivingEntity, EnumSet<ElementType>> DIRTY = new LinkedHashMap<>();
    private static final Map<LivingEntity, FrozenMemory> FROZEN_MEMORY = new WeakHashMap<>();

    private ElementAuraManager() {}

    public static void applyUnits(LivingEntity entity, ElementType type, float units) {
        apply(entity, type, ElementGauge.gaugeFromUnits(units), ElementGauge.durationTicksFromUnits(units));
    }

    public static void apply(LivingEntity entity, ElementType type, float gauge, int durationTicks) {
        if (entity.level().isClientSide() || gauge <= 0f || durationTicks <= 0) return;
        float clamped = Math.min(ElementGauge.MAX_GAUGE, gauge);
        raise(entity, type, clamped, clamped / durationTicks, 0);
    }

    public static void freeze(LivingEntity entity, float amount) {
        if (entity.level().isClientSide() || amount <= 0f) return;
        int age = 0;
        FrozenMemory m = FROZEN_MEMORY.get(entity);
        if (m != null && entity.level().getGameTime() - m.tick() <= ServerConfig.FREEZE_MEMORY_TICKS.get()) age = m.age();
        raise(entity, ElementType.FROZEN, Math.min(ElementGauge.MAX_GAUGE, amount), frozenRate(age), age);
    }

    public static void quicken(LivingEntity entity, float amount) {
        if (entity.level().isClientSide() || amount <= 0f) return;
        raise(entity, ElementType.QUICKEN, Math.min(ElementGauge.MAX_GAUGE, amount), QUICKEN_DECAY, 0);
    }

    public static void startBurning(LivingEntity entity) {
        if (entity.level().isClientSide() || hasAura(entity, ElementType.BURNING)) return;
        raise(entity, ElementType.BURNING, 2f, 0f, 0);
    }

    public static void hold(LivingEntity entity, ElementType type, float add, float cap, int step) {
        if (entity.level().isClientSide() || cap <= 0f) return;
        long now = entity.level().getGameTime();
        LinkedHashMap<ElementType, Aura> auras = AURAS.computeIfAbsent(entity, k -> new LinkedHashMap<>());
        Aura a = auras.get(type);
        float release = cap / ElementGauge.durationTicksFromUnits(cap / ElementGauge.AURA_TAX);
        if (a == null) {
            if (add <= 0f) {
                if (auras.isEmpty()) AURAS.remove(entity);
                return;
            }
            a = new Aura(Math.min(add, cap), 0f);
            auras.put(type, a);
            markDirty(entity, null);
        } else if (a.amount < cap - 1.0e-4f && add > 0f) {
            a.amount = Math.min(cap, a.amount + add);
            a.decayPerTick = 0f;
            markDirty(entity, null);
        } else if (Math.abs(a.amount - cap) <= 1.0e-4f && a.decayPerTick != 0f) {
            a.decayPerTick = 0f;
            markDirty(entity, null);
        }
        a.releaseDecay = release;
        a.holdCap = cap;
        a.holdUntil = now + 2L * step + 1L;
    }

    public static float consume(LivingEntity entity, ElementType type, float amount, boolean flash) {
        if (entity.level().isClientSide()) return 0f;
        LinkedHashMap<ElementType, Aura> auras = AURAS.get(entity);
        Aura aura = auras == null ? null : auras.get(type);
        if (aura == null) return 0f;

        float used = Math.min(aura.amount, Math.max(0f, amount));
        aura.amount -= used;
        if (aura.amount <= 1.0e-4f) {
            drop(entity, type, flash);
        } else {
            markDirty(entity, flash ? type : null);
        }
        return used;
    }

    public static void remove(LivingEntity entity, ElementType type, boolean flash) {
        if (entity.level().isClientSide()) return;
        drop(entity, type, flash);
    }

    public static void flash(LivingEntity entity, ElementType type) {
        if (entity.level().isClientSide() || !hasAura(entity, type)) return;
        markDirty(entity, type);
    }

    public static void clear(LivingEntity entity) {
        if (entity.level().isClientSide()) return;
        LinkedHashMap<ElementType, Aura> auras = AURAS.remove(entity);
        if (auras == null) return;
        Aura frozen = auras.get(ElementType.FROZEN);
        if (frozen != null) rememberFrozen(entity, frozen);
        markDirty(entity, null);
    }

    public static boolean hasAura(LivingEntity entity, ElementType type) {
        LinkedHashMap<ElementType, Aura> auras = AURAS.get(entity);
        return auras != null && auras.containsKey(type);
    }

    public static float amount(LivingEntity entity, ElementType type) {
        LinkedHashMap<ElementType, Aura> auras = AURAS.get(entity);
        Aura aura = auras == null ? null : auras.get(type);
        return aura == null ? 0f : aura.amount;
    }

    public static Map<ElementType, Aura> getAuras(LivingEntity entity) {
        LinkedHashMap<ElementType, Aura> auras = AURAS.get(entity);
        return auras == null ? Map.of() : Collections.unmodifiableMap(new LinkedHashMap<>(auras));
    }

    static void tick() {
        Iterator<Map.Entry<LivingEntity, LinkedHashMap<ElementType, Aura>>> it = AURAS.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<LivingEntity, LinkedHashMap<ElementType, Aura>> entry = it.next();
            LivingEntity entity = entry.getKey();
            LinkedHashMap<ElementType, Aura> auras = entry.getValue();
            if (entity.isRemoved() || !entity.isAlive()) {
                Aura frozen = auras.get(ElementType.FROZEN);
                if (frozen != null) rememberFrozen(entity, frozen);
                it.remove();
                if (!entity.isRemoved()) markDirty(entity, null);
                continue;
            }
            long now = entity.level().getGameTime();
            boolean changed = false;
            Iterator<Map.Entry<ElementType, Aura>> auraIt = auras.entrySet().iterator();
            while (auraIt.hasNext()) {
                Map.Entry<ElementType, Aura> ae = auraIt.next();
                Aura aura = ae.getValue();
                if (aura.holdCap > 0f) {
                    if (now > aura.holdUntil) {
                        aura.holdCap = 0f;
                        if (aura.decayPerTick == 0f) {
                            aura.decayPerTick = aura.releaseDecay;
                            changed = true;
                        }
                    } else if (aura.decayPerTick > 0f && aura.amount > aura.holdCap
                            && aura.amount - aura.decayPerTick <= aura.holdCap) {
                        aura.amount = aura.holdCap;
                        aura.decayPerTick = 0f;
                        changed = true;
                    }
                }
                aura.age++;
                if (ae.getKey() == ElementType.FROZEN && aura.age % 20 == 0) {
                    aura.decayPerTick = frozenRate(aura.age);
                    changed = true;
                }
                aura.amount -= aura.decayPerTick;
                if (aura.amount <= 0f) {
                    if (ae.getKey() == ElementType.FROZEN) rememberFrozen(entity, aura);
                    auraIt.remove();
                    changed = true;
                }
            }
            if (changed) markDirty(entity, null);
            if (auras.containsKey(ElementType.FROZEN)) holdFrozen(entity);
            if (auras.isEmpty()) it.remove();
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
        FROZEN_MEMORY.clear();
    }

    private static void raise(LivingEntity entity, ElementType type, float amount, float decay, int startAge) {
        LinkedHashMap<ElementType, Aura> auras = AURAS.computeIfAbsent(entity, k -> new LinkedHashMap<>());
        Aura existing = auras.get(type);
        if (existing == null) {
            Aura created = new Aura(amount, decay);
            created.age = startAge;
            auras.put(type, created);
        } else if (amount > existing.amount) {
            existing.amount = amount;
            if (type != ElementType.FROZEN) existing.decayPerTick = decay;
        } else {
            return;
        }
        markDirty(entity, null);
    }

    private static void drop(LivingEntity entity, ElementType type, boolean flash) {
        LinkedHashMap<ElementType, Aura> auras = AURAS.get(entity);
        if (auras == null) return;
        Aura removed = auras.remove(type);
        if (removed == null) return;
        if (type == ElementType.FROZEN) rememberFrozen(entity, removed);
        if (auras.isEmpty()) AURAS.remove(entity);
        markDirty(entity, flash ? type : null);
    }

    private static float frozenRate(int age) {
        return (0.4f + 0.1f * (age / 20)) / 20f;
    }

    private static void rememberFrozen(LivingEntity entity, Aura aura) {
        FROZEN_MEMORY.put(entity, new FrozenMemory(aura.age, entity.level().getGameTime()));
    }

    private static void holdFrozen(LivingEntity entity) {
        if (entity instanceof Mob && entity.tickCount % 5 == 0) {
            entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 10,
                    ServerConfig.FREEZE_SLOW_AMPLIFIER.get(), false, false, false));
        }
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
