package com.dsp2.genshinerc.common.core.reaction;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public final class ReactionResult {
    private final EnumSet<Reaction> reactions = EnumSet.noneOf(Reaction.class);
    private float amplify = 1f;
    private float catalyzeFlat = 0f;
    private float remaining;
    private com.dsp2.genshinerc.common.core.element.ElementType crystallized;

    void noteCrystallized(com.dsp2.genshinerc.common.core.element.ElementType element) {
        if (crystallized == null) crystallized = element;
    }

    public String damagePalette(com.dsp2.genshinerc.common.core.damage.DamageKind kind) {
        if (has(Reaction.MELT)) return Reaction.MELT.damagePalette(kind);
        if (has(Reaction.VAPORIZE)) return Reaction.VAPORIZE.damagePalette(kind);
        if (has(Reaction.AGGRAVATE) || has(Reaction.SPREAD)) return Reaction.palette("electro", "dendro");
        return crystallized == null ? null : Reaction.palette("geo", crystallized.colorKey());
    }

    public static ReactionResult none() { return new ReactionResult(); }

    boolean add(Reaction r) { return reactions.add(r); }
    void setAmplify(float v) { amplify = v; }
    void raiseFlat(float v) { catalyzeFlat = Math.max(catalyzeFlat, v); }
    void setRemaining(float v) { remaining = v; }

    public Set<Reaction> reactions() { return Collections.unmodifiableSet(reactions); }
    public boolean has(Reaction r) { return reactions.contains(r); }
    public float amplify() { return amplify; }
    public float catalyzeFlat() { return catalyzeFlat; }
    public float remaining() { return remaining; }

    public String indicatorKey(String base) {
        if (has(Reaction.SPREAD)) return "spread";
        if (has(Reaction.AGGRAVATE)) return "aggravate";
        if (has(Reaction.CATALYZE)) return "quicken";
        return base;
    }
}
