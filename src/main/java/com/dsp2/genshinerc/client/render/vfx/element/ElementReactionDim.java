package com.dsp2.genshinerc.client.render.vfx.element;

import com.dsp2.genshinerc.client.render.vfx.config.VFXConfig;
import java.util.HashMap;
import java.util.Map;

public final class ElementReactionDim {
    private static final Map<Integer, Long> UNTIL = new HashMap<>();

    private ElementReactionDim() {}

    public static void mark(int entityId) {
        UNTIL.put(entityId, System.currentTimeMillis() + (long) VFXConfig.get().shellReactionHoldMs);
    }

    public static float factor(int entityId, VFXConfig cfg) {
        Long until = UNTIL.get(entityId);
        if (until == null) return 1f;

        float dim = cfg.shellReactionDim;
        long hold = (long) cfg.shellReactionHoldMs;
        long fade = (long) cfg.shellReactionFadeMs;
        long elapsed = System.currentTimeMillis() - (until - hold);

        if (elapsed >= hold + fade) { UNTIL.remove(entityId); return 1f; }
        if (elapsed <= hold) return dim;
        float t = (elapsed - hold) / (float) fade;
        return dim + (1f - dim) * (t * t * (3f - 2f * t));
    }

    public static void remove(int entityId) { UNTIL.remove(entityId); }
    public static void clear() { UNTIL.clear(); }
}