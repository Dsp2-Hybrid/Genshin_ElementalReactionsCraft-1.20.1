package com.dsp2.genshinerc.common.core.element;

public record AttachCooldown(int timerTicks, int interval) {
    public static final AttachCooldown STANDARD = new AttachCooldown(50, 3);
    public static final AttachCooldown EVERY_HIT = new AttachCooldown(0, 1);

    public AttachCooldown {
        timerTicks = Math.max(0, timerTicks);
        interval = Math.max(1, interval);
    }

    public static AttachCooldown of(float timerSeconds, int interval) {
        return new AttachCooldown(Math.round(timerSeconds * 20f), interval);
    }
}
