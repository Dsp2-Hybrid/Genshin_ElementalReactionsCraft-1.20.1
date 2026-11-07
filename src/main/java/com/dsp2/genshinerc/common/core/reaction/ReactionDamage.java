package com.dsp2.genshinerc.common.core.reaction;

// Assembled from Claude's full ReactionDamage plus the later patches:
//   isNatural() guard, capReaction(), discount(), null cause for BURNING/ELECTRO_CHARGED.

import com.dsp2.genshinerc.common.config.ServerConfig;
import com.dsp2.genshinerc.common.core.damage.DamageKind;
import com.dsp2.genshinerc.common.core.damage.ElementalDamage;
import com.dsp2.genshinerc.common.core.damage.ElementalMath;
import com.dsp2.genshinerc.common.core.damage.ElementalStats;
import com.dsp2.genshinerc.common.network.base.damageindicator.DamageIndicatorNetworkHandler;
import com.dsp2.genshinerc.server.render.damageindicator.DamageServerEventHandler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.EnumMap;
import java.util.Map;
import java.util.WeakHashMap;

public final class ReactionDamage {
    private static final int WINDOW_TICKS = 10;
    private static boolean dealing;
    private static final Map<LivingEntity, Map<Reaction, ArrayDeque<Long>>> HITS = new WeakHashMap<>();

    private ReactionDamage() {}

    public static boolean isDealing() { return dealing; }

    public static void deal(LivingEntity target, @Nullable Entity attacker, DamageKind kind, Reaction reaction,
                            ElementalStats stats) {
        if (ElementReactions.isNatural()) return;
        if (!(target.level() instanceof ServerLevel level) || !target.isAlive()) return;
        if (!allow(target, reaction, level.getGameTime())) return;
        double amount = ElementalMath.transformative(reaction.coefficient(), stats);
        amount = ElementalMath.capReaction(amount, target);
        if (amount <= 0.0) return;

        Entity cause = (reaction == Reaction.BURNING || reaction == Reaction.ELECTRO_CHARGED) ? null : attacker;
        DamageSource source = ElementalDamage.source(level, kind, attacker, cause);

        float hp = target.getHealth();
        float absorb = target.getAbsorptionAmount();
        int invulnerable = target.invulnerableTime;
        boolean previous = dealing;
        dealing = true;
        try {
            target.invulnerableTime = 0;
            target.hurt(source, (float) amount);
        } finally {
            target.invulnerableTime = invulnerable;
            dealing = previous;
        }

        float lostHp = Math.max(0f, hp - target.getHealth());
        float lostAbsorb = Math.max(0f, absorb - target.getAbsorptionAmount());
        if (lostHp < 0.001f && lostAbsorb < 0.001f) return;
        DamageServerEventHandler.discount(target, lostHp, lostAbsorb);
        if (ServerConfig.ENABLED.get()) {
            DamageIndicatorNetworkHandler.sendDamage(target, reaction.indicatorKey(kind), lostHp, false,
                    target.getHealth() <= 0.001f, false, lostAbsorb, null, null, false, false);
        }
    }

    private static boolean allow(LivingEntity target, Reaction reaction, long now) {
        ArrayDeque<Long> hits = HITS.computeIfAbsent(target, k -> new EnumMap<>(Reaction.class))
                .computeIfAbsent(reaction, k -> new ArrayDeque<>());
        while (!hits.isEmpty() && now - hits.peekFirst() >= WINDOW_TICKS) hits.pollFirst();
        int max = reaction == Reaction.OVERLOAD ? 1 : 2;
        if (hits.size() >= max) return false;
        hits.addLast(now);
        return true;
    }

    static void reset() { HITS.clear(); }
}
