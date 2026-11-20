package com.dsp2.genshinerc.common.core.reaction;

import com.dsp2.genshinerc.common.config.ServerConfig;
import com.dsp2.genshinerc.common.core.element.ElementAuraManager;
import com.dsp2.genshinerc.common.core.element.ElementType;
import com.dsp2.genshinerc.common.network.GenshinercNetwork;
import com.dsp2.genshinerc.common.network.base.element.ReactionTextPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

final class ReactionText {
    private record TextKey(Reaction reaction, @Nullable ElementType partner) {}
    private static final Map<LivingEntity, Map<TextKey, Long>> ACTIVE = new WeakHashMap<>();
    private static final long RESET_TICKS = 20;

    private ReactionText() {}

    static void send(LivingEntity target, Reaction reaction, @Nullable ElementType a, @Nullable ElementType b) {
        send(target, reaction, a, b, false);
    }

    static void send(LivingEntity target, Reaction reaction, @Nullable ElementType a, @Nullable ElementType b, boolean force) {
        if (!ServerConfig.ENABLED.get() || !target.isAlive()) return;
        if (!(target.level() instanceof ServerLevel level)) return;

        ElementType first = a != null ? a : reaction.first();
        ElementType second = b != null ? b : reaction.second();

        ElementType partner = reaction == Reaction.SWIRL || reaction == Reaction.CRYSTALLIZE
                ? (second == ElementType.FROZEN ? ElementType.CRYO : second) : null;
        TextKey key = new TextKey(reaction, partner);
        Map<TextKey, Long> active = ACTIVE.computeIfAbsent(target, ignored -> new HashMap<>());

        if (force || !active.containsKey(key)) {
            active.put(key, level.getGameTime());
            GenshinercNetwork.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> target),
                    new ReactionTextPacket(target.getId(), reaction, first, second));
        }
    }

    static void tick() {
        ACTIVE.entrySet().removeIf(entry -> {
            LivingEntity entity = entry.getKey();
            if (entity.isRemoved() || !entity.isAlive()) return true;
            if (!(entity.level() instanceof ServerLevel level)) return true;

            long now = level.getGameTime();
            Map<TextKey, Long> active = entry.getValue();

            active.entrySet().removeIf(e -> {
                Reaction r = e.getKey().reaction();
                ElementType first = r.first();
                ElementType second = r.second();

                boolean hasFirst = first == null || ElementAuraManager.amount(entity, first) > 0.001f;
                boolean hasSecond = second == null || ElementAuraManager.amount(entity, second) > 0.001f;

                boolean gone = !hasFirst || !hasSecond;

                if (gone) {
                    Long time = e.getValue();
                    if (now - time >= RESET_TICKS) {
                        return true; 
                    }
                } else {
                    e.setValue(now); 
                }
                return false;
            });

            return active.isEmpty();
        });
    }

    static void reset() { ACTIVE.clear(); }
}