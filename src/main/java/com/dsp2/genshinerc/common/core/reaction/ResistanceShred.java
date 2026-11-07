package com.dsp2.genshinerc.common.core.reaction;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

public final class ResistanceShred {
    private record Shred(RegistryObject<Attribute> attribute, UUID id, long expires) {}

    private static final Map<LivingEntity, List<Shred>> ACTIVE = new WeakHashMap<>();

    private ResistanceShred() {}

    public static void apply(LivingEntity target, RegistryObject<Attribute> attribute, UUID id, double amount, int ticks) {
        AttributeInstance instance = target.getAttribute(attribute.get());
        if (instance == null) return;
        instance.removeModifier(id);
        instance.addTransientModifier(new AttributeModifier(id, "genshinerc_shred", amount, AttributeModifier.Operation.ADDITION));
        List<Shred> list = ACTIVE.computeIfAbsent(target, k -> new ArrayList<>());
        list.removeIf(s -> s.id().equals(id));
        list.add(new Shred(attribute, id, target.level().getGameTime() + ticks));
    }

    static void tick() {
        Iterator<Map.Entry<LivingEntity, List<Shred>>> it = ACTIVE.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<LivingEntity, List<Shred>> entry = it.next();
            LivingEntity entity = entry.getKey();
            if (entity.isRemoved()) { it.remove(); continue; }
            long now = entity.level().getGameTime();
            entry.getValue().removeIf(s -> {
                if (now < s.expires()) return false;
                AttributeInstance instance = entity.getAttribute(s.attribute().get());
                if (instance != null) instance.removeModifier(s.id());
                return true;
            });
            if (entry.getValue().isEmpty()) it.remove();
        }
    }

    static void reset() { ACTIVE.clear(); }
}
