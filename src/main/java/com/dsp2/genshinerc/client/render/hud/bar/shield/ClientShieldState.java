package com.dsp2.genshinerc.client.render.hud.bar.shield;

import com.dsp2.genshinerc.client.render.hud.bar.anim.AnimatedBarValue;
import com.dsp2.genshinerc.client.render.hud.bar.config.BarConfig;
import com.dsp2.genshinerc.client.render.hud.bar.element.ElementHudRenderer;
import com.dsp2.genshinerc.common.core.combat.shield.ShieldKind;
import com.dsp2.genshinerc.common.core.element.ElementType;
import com.dsp2.genshinerc.common.network.base.combat.SyncShieldPacket;
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

    // --- 以下是新加的方法 ---

    public float largestAmount() {
        float m = 0f;
        for (SyncShieldPacket.Entry e : entries) m = Math.max(m, e.amount());
        return m;
    }

    public float maxAmount() {
        return max;
    }

    public int color(BarConfig cfg) {
        if (kind == ShieldKind.ELEMENTAL && element != null) return ElementHudRenderer.elementColor(element);
        return cfg.shieldSpecialColor.getRGB() & 0xFFFFFF;
    }
}