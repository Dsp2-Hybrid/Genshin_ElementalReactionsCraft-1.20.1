package com.dsp2.genshinerc.client.render.hud.bar.shield;

import com.dsp2.genshinerc.client.render.hud.bar.anim.AnimatedBarValue;
import com.dsp2.genshinerc.client.render.hud.bar.config.BarConfig;
import com.dsp2.genshinerc.client.render.hud.bar.element.ElementHudRenderer;
import com.dsp2.genshinerc.common.core.combat.shield.ShieldKind;
import com.dsp2.genshinerc.common.core.element.ElementType;
import com.dsp2.genshinerc.common.network.base.combat.SyncShieldPacket;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class ClientShieldState {
    public final AnimatedBarValue bar = new AnimatedBarValue();
    float max;
    ShieldKind kind = ShieldKind.SPECIAL;
    @Nullable ElementType element;
    List<SyncShieldPacket.Entry> entries = List.of();
    private long warningEndMs = Long.MAX_VALUE;

    public void apply(SyncShieldPacket msg, long nowMs, BarConfig cfg) {
        entries = msg.shields();
        SyncShieldPacket.Entry top = null;

        for (SyncShieldPacket.Entry entry : entries) {
            if (top == null || entry.amount() > top.amount()) {
                top = entry;
            }
        }

        if (top != null) {
            max = top.max();
            kind = top.kind();
            element = top.element();
            warningEndMs = top.ticksLeft() < 0
                    ? Long.MAX_VALUE
                    : nowMs + top.ticksLeft() * 50L;
        } else {
            max = 0f;
            kind = ShieldKind.SPECIAL;
            element = null;
            warningEndMs = Long.MAX_VALUE;
        }

        bar.set(
                top == null ? 0f : top.amount(),
                nowMs,
                cfg.subBarWhiteHoldTime,
                true);

        if (msg.hit()) {
            bar.flash(nowMs);
        }
    }

    public List<SyncShieldPacket.Entry> entries() {
        return entries;
    }

    public float largestAmount() {
        float value = 0f;

        for (SyncShieldPacket.Entry entry : entries) {
            value = Math.max(value, entry.amount());
        }

        return value;
    }

    public float maxAmount() {
        return max;
    }

    public ShieldKind kind() {
        return kind;
    }

    public int color(BarConfig cfg, float hpMax, int layer) {
        if (kind == ShieldKind.ELEMENTAL && element != null) {
            int baseColor = ElementHudRenderer.elementColor(element);

            if (layer <= 0) {
                return baseColor;
            }

            float mix = layer == 1
                    ? cfg.shieldLayer2WhiteMix
                    : cfg.shieldLayer3WhiteMix;

            int red = (int) Mth.lerp(
                    mix,
                    (baseColor >> 16) & 0xFF,
                    255);

            int green = (int) Mth.lerp(
                    mix,
                    (baseColor >> 8) & 0xFF,
                    255);

            int blue = (int) Mth.lerp(
                    mix,
                    baseColor & 0xFF,
                    255);

            return (red << 16) | (green << 8) | blue;
        }

        return switch (layer) {
            case 0 -> 150 << 16 | 150 << 8 | 150;
            case 1 -> 180 << 16 | 180 << 8 | 180;
            default -> 210 << 16 | 210 << 8 | 210;
        };
    }

    public float warningAlpha(long nowMs) {
        if (warningEndMs == Long.MAX_VALUE) {
            return 1f;
        }

        long remainingMs = warningEndMs - nowMs;

        if (remainingMs >= 4000L) {
            return 1f;
        }

        float progress = Mth.clamp(
                1f - remainingMs / 4000f,
                0f,
                1f);

        double frequency = 0.7 + progress * 6.8;
        double wave = (
                Math.sin(nowMs / 1000.0 * frequency * Math.PI * 2.0)
                        + 1.0) * 0.5;

        double shaped = wave * wave * (3.0 - 2.0 * wave);

        return (float) Mth.clamp(
                0.04 + shaped * 0.96,
                0.04,
                1.0);
    }

    public float warningProgress(long nowMs) {
        if (warningEndMs == Long.MAX_VALUE) {
            return 0f;
        }

        return Mth.clamp(
                1f - (warningEndMs - nowMs) / 4000f,
                0f,
                1f);
    }

    public float amount() {
        return bar.main();
    }

    public float initial() {
        return max;
    }
}