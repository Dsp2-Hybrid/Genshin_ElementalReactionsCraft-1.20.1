package com.dsp2.genshinerc.common.core.element.apply.probe;

import com.dsp2.genshinerc.common.core.element.ElementType;
import com.dsp2.genshinerc.common.core.reaction.HitboxReactionDetector;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.WeakHashMap;

public final class ElementImbue {
    public static final float DEFAULT_UNITS = 2f;
    public record State(ElementType element, float units) {}
    public interface Resolver { State resolve(LivingEntity entity); }

    private static final WeakHashMap<LivingEntity, State> MANUAL = new WeakHashMap<>();
    private static final List<Resolver> RESOLVERS = new CopyOnWriteArrayList<>();

    private ElementImbue() {}

    public static void register(Resolver resolver) { if (resolver != null) RESOLVERS.add(resolver); }
    public static void set(LivingEntity entity, ElementType element, float units) {
        if (entity == null || element == null || units <= 0f) return;
        MANUAL.put(entity, new State(element, units));
    }
    public static void clear(LivingEntity entity) { if (entity != null) MANUAL.remove(entity); }

    public static State get(LivingEntity entity) {
        if (entity == null) return null;
        State manual = MANUAL.get(entity);
        if (manual != null) return manual;
        for (Resolver resolver : RESOLVERS) {
            State state = resolver.resolve(entity);
            if (state != null && state.element() != null && state.units() > 0f) return state;
        }
        ElementType tagged = HitboxReactionDetector.current(entity);
        return tagged == null ? null : new State(tagged, DEFAULT_UNITS);
    }
}
