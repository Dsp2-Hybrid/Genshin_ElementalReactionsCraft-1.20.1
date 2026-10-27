package com.dsp2.genshinerc.client.render.hud.gui.rpg.anim;

import net.minecraft.util.Mth;

public final class TextAnim {
    private boolean init;
    private float shown, target;
    private long popStart = -1L, last;

    public void reset() { init = false; popStart = -1L; }

    public void set(float v, long now) {
        if (!init) { init = true; shown = target = v; last = now; return; }
        if (Math.abs(v - target) > 1.0e-3f) { target = v; popStart = now; }
    }

    public void snap(float v) { init = true; shown = target = v; popStart = -1L; }

    public void update(long now, float speed) {
        float dt = Math.min(0.1f, Math.max(0L, now - last) / 1000f);
        last = now;
        shown += (target - shown) * (1f - (float) Math.exp(-speed * dt));
        if (Math.abs(target - shown) < 0.01f) shown = target;
    }

    public float shown() { return shown; }

    public float scale(long now, float popScale, float popSeconds) {
        if (popStart < 0L || popScale <= 0f || popSeconds <= 0f) return 1f;
        float t = (now - popStart) / (popSeconds * 1000f);
        if (t >= 1f) { popStart = -1L; return 1f; }
        return 1f + popScale * Mth.sin(t * Mth.PI);
    }
}