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
    private static final Map<LivingEntity, Map<Reaction, Long>> ACTIVE = new WeakHashMap<>();
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

        Map<Reaction, Long> active = ACTIVE.computeIfAbsent(target, k -> new HashMap<>());
        
        if (force || !active.containsKey(reaction)) {
            active.put(reaction, level.getGameTime());
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
            Map<Reaction, Long> active = entry.getValue();

            active.entrySet().removeIf(e -> {
                Reaction r = e.getKey();
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