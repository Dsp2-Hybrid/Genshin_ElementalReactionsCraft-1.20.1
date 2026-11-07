package com.dsp2.genshinerc.common.core.element;

import com.dsp2.genshinerc.common.core.reaction.ElementReactions;
import com.dsp2.genshinerc.common.core.reaction.ReactionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

public final class ElementAttachment {
    private static final UUID NO_ATTACKER = new UUID(0L, 0L);

    private record Key(UUID attacker, String attack) {}

    private static final class Window {
        final long start;
        final int timerTicks;
        int hits;

        Window(long start, int timerTicks) {
            this.start = start;
            this.timerTicks = timerTicks;
        }

        boolean expired(long now) {
            return now < start || now - start >= timerTicks;
        }
    }

    private static final Map<LivingEntity, Map<Key, Window>> WINDOWS = new WeakHashMap<>();

    private ElementAttachment() {}

    public static ReactionResult attach(LivingEntity target, @Nullable Entity attacker, ElementType element,
                                        GaugeTier tier, String attackKey, AttachCooldown cooldown) {
        if (target.level().isClientSide() || !target.isAlive() || !isTrigger(element)) return ReactionResult.none();
        if (!passes(target, attacker, attackKey, cooldown)) return ReactionResult.none();
        return ElementReactions.trigger(target, attacker, element, tier.units());
    }

    public static boolean isTrigger(ElementType element) {
        return switch (element) {
            case PYRO, HYDRO, ELECTRO, CRYO, DENDRO, ANEMO, GEO -> true;
            default -> false;
        };
    }

    private static boolean passes(LivingEntity target, @Nullable Entity attacker, String attackKey,
                                  AttachCooldown cooldown) {
        long now = target.level().getGameTime();
        Map<Key, Window> windows = WINDOWS.computeIfAbsent(target, k -> new HashMap<>());
        windows.values().removeIf(w -> w.expired(now));

        Key key = new Key(attacker == null ? NO_ATTACKER : attacker.getUUID(), attackKey);
        Window window = windows.get(key);
        if (window == null) {
            window = new Window(now, cooldown.timerTicks());
            window.hits = 1;
            windows.put(key, window);
            return true;
        }
        boolean attach = window.hits % cooldown.interval() == 0;
        window.hits++;
        return attach;
    }

    static void reset() {
        WINDOWS.clear();
    }
}
