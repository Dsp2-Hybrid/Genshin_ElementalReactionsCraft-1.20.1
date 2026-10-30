package com.dsp2.genshinerc.client.render.hud.bar.zhenmie;

import com.dsp2.genshinerc.client.render.hud.bar.config.BarConfig;
import com.dsp2.genshinerc.common.core.combat.zhenmie.ClashResult;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

public final class ClashFlash {
    private static final long KNOCKDOWN_SWAP_MS = 90L, NO_TARGET_TIMEOUT_MS = 2000L;
    private static final float SLIDE_SECONDS = 1.5f;
    private static final float EPS = 0.001f;

    private boolean active;
    @Nullable private ClashResult type;
    private long startMs, lastMs;
    private float left;

    public void reset() { active = false; type = null; left = 0f; }

    public void start(ClashResult t, long now) {
        type = t; active = true; left = 0f; startMs = now; lastMs = now;
    }

    public boolean active() { return active; }

    public float left() { return left; }

    public float fill(float mm) { return active ? Math.max(mm, 1f - left) : mm; }

    public void update(long now, float mieTarget) {
        if (!active) return;
        float dt = Mth.clamp((now - lastMs) / 1000f, 0f, 0.1f);
        lastMs = now;
        float to = Mth.clamp(1f - mieTarget, 0f, 1f);
        if (to > EPS) {
            left = Math.min(to, left + dt / SLIDE_SECONDS);
            if (left >= to - EPS) reset();
        } else if (now - startMs >= NO_TARGET_TIMEOUT_MS) {
            reset();
        }
    }

    public float pulse(long now, BarConfig cfg) {
        return type == ClashResult.CRUSH
                ? 0.5f + 0.5f * Mth.cos((now / 1000f) * cfg.zhenMieClashHueHz * Mth.TWO_PI)
                : 1f;
    }

    public int color(long now, int zhenBase, int mieBase, BarConfig cfg) {
        if (type == null) return 0xFFFFFF;
        return switch (type) {
            case CRUSH -> {
                float periodMs = 1000f / cfg.zhenMieClashHueHz;
                yield Mth.hsvToRgb((now % (long) periodMs) / periodMs, 0.85f, 1f);
            }
            case KNOCKDOWN -> (now / KNOCKDOWN_SWAP_MS) % 2 == 0 ? zhenBase : mieBase;
            default -> 0xFFFFFF;
        };
    }
}