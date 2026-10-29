package com.dsp2.genshinerc.client.render.hud.bar.anim;

public final class AnimatedBarValue {
    private static final float EPS = 1.0e-4f;

    private float target, main, trail;
    private boolean init;
    private boolean snapDown = true;
    private long holdUntilMs;
    private long flashStartMs = -1L;
    private long lastMs;

    public void set(float value, long nowMs, float holdSeconds, boolean trailOnDrop) {
        value = Math.max(0f, value);
        if (!init) {
            init = true;
            main = trail = 0f;
            lastMs = nowMs;
        } else if (value < target && trailOnDrop) {
            trail = Math.max(trail, main);
            holdUntilMs = nowMs + (long) (Math.max(0f, holdSeconds) * 1000f);
        } else if (value > target) {
            main = value;
            trail = value;
        }
        snapDown = trailOnDrop;
        target = value;
    }

    public void flash(long nowMs) {
        flashStartMs = nowMs;
    }

    public void update(long nowMs, float fillSpeed, float drainSpeed) {
        float dt = Math.min(0.1f, Math.max(0L, nowMs - lastMs) / 1000f);
        lastMs = nowMs;

        if (target < main && snapDown) {
            main = target;
        } else {
            main += (target - main) * (1f - (float) Math.exp(-fillSpeed * dt));
            if (Math.abs(target - main) < EPS) main = target;
        }

        if (trail < main || (!snapDown && nowMs >= holdUntilMs)) {
            trail = main;
        } else if (nowMs >= holdUntilMs) {
            trail += (main - trail) * (1f - (float) Math.exp(-drainSpeed * dt));
            if (trail - main < EPS) trail = main;
        }
    }

    public float main()  { return main; }
    public float trail() { return trail; }
    public float target() { return target; }

    public float whiteness(long nowMs, float flashSeconds) {
        if (flashStartMs < 0 || flashSeconds <= 0f) return 0f;
        float t = (nowMs - flashStartMs) / (flashSeconds * 1000f);
        if (t >= 1f) return 0f;
        float r = 1f - t;
        return r * r;
    }

    public boolean isIdle(long nowMs, float flashSeconds) {
        return init && target <= EPS && main <= EPS && trail <= EPS && whiteness(nowMs, flashSeconds) <= 0f;
    }
}