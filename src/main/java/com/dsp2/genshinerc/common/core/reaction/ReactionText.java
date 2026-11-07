package com.dsp2.genshinerc.common.core.reaction;

import com.dsp2.genshinerc.common.config.ServerConfig;
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
    private static final Map<LivingEntity, Map<Integer, Long>> LAST = new WeakHashMap<>();

    private ReactionText() {}

    static void send(LivingEntity target, Reaction reaction, @Nullable ElementType a, @Nullable ElementType b) {
        if (ElementReactions.isNatural() || !ServerConfig.ENABLED.get() || !target.isAlive()) return;
        if (!(target.level() instanceof ServerLevel level)) return;

        ElementType first = a != null ? a : reaction.first();
        ElementType second = b != null ? b : reaction.second();

        long now = level.getGameTime();
        int key = reaction.ordinal() * 16 + (second == null ? 15 : second.ordinal());
        Map<Integer, Long> map = LAST.computeIfAbsent(target, k -> new HashMap<>());
        Long last = map.get(key);
        if (last != null && now - last < ServerConfig.REACTION_TEXT_COOLDOWN.get()) return;
        map.put(key, now);

        GenshinercNetwork.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> target),
                new ReactionTextPacket(target.getId(), reaction, first, second));
    }

    static void reset() { LAST.clear(); }
}
