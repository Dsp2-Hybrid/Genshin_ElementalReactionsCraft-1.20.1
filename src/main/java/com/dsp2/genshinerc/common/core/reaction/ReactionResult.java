package com.dsp2.genshinerc.common.core.reaction;

// Assembled from fragments Claude supplied across replies.

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public final class ReactionResult {
    private final EnumSet<Reaction> reactions = EnumSet.noneOf(Reaction.class);
    private float amplify = 1f;
    private float catalyzeFlat = 0f;
    private float remaining;

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
