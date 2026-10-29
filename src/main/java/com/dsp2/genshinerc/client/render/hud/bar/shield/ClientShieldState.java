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
    final AnimatedBarValue bar = new AnimatedBarValue();
    float max;
    ShieldKind kind = ShieldKind.SPECIAL;
    @Nullable ElementType element;
    List<SyncShieldPacket.Entry> entries = List.of();

    public void apply(SyncShieldPacket msg, long nowMs, BarConfig cfg) {
        entries = msg.shields();
        SyncShieldPacket.Entry top = null;
        for (SyncShieldPacket.Entry e : entries) {
            if (top == null || e.amount() > top.amount()) top = e;
        }
        if (top != null) {
            max = top.max();
            kind = top.kind();
            element = top.element();
        }
        bar.set(top == null ? 0f : top.amount(), nowMs, cfg.subBarWhiteHoldTime, true);
        if (msg.hit()) bar.flash(nowMs);
    }

    public List<SyncShieldPacket.Entry> entries() { return entries; }

    public float largestAmount() {
        float m = 0f;
        for (SyncShieldPacket.Entry e : entries) m = Math.max(m, e.amount());
        return m;
    }

    public float maxAmount() {
        return max;
    }

    public int color(BarConfig cfg, float hpMax, int layer) {
        int baseColor = (kind == ShieldKind.ELEMENTAL && element != null)
                ? ElementHudRenderer.elementColor(element)
                : 0xC3E1FF;

        if (layer <= 0) return baseColor;

        float mix = layer == 1 ? 0.35f : 0.70f;
        int r = (int) Mth.lerp(mix, (baseColor >> 16) & 0xFF, 255);
        int g = (int) Mth.lerp(mix, (baseColor >> 8) & 0xFF, 255);
        int b = (int) Mth.lerp(mix, baseColor & 0xFF, 255);
        return (r << 16) | (g << 8) | b;
    }

    public float amount() { return bar.main(); }
    public float initial() { return max; }
}