package com.dsp2.genshinerc.client.render.element;

import com.dsp2.genshinerc.client.render.bar.config.BarConfig;
import com.dsp2.genshinerc.common.element.ElementType;
import com.dsp2.genshinerc.common.network.element.SyncAuraPacket;

import java.util.*;

public final class EntityAuras {
    private final LinkedHashMap<ElementType, ClientAura> auras = new LinkedHashMap<>();
    private final List<ClientAura> visible = new ArrayList<>();
    private float layoutCount = -1f;
    private long lastLayoutMs;

    void applySnapshot(SyncAuraPacket msg, double nowTicks, long nowMs, BarConfig cfg) {
        EnumSet<ElementType> present = EnumSet.noneOf(ElementType.class);
        for (SyncAuraPacket.Entry e : msg.auras()) {
            present.add(e.type());
            ClientAura aura = auras.get(e.type());
            boolean isNew = aura == null;
            if (isNew) {
                aura = new ClientAura(e.type(), nowMs);
                auras.put(e.type(), aura);
            }
            aura.sync(e.amount(), e.decayPerTick(), msg.flashed().contains(e.type()), isNew, nowTicks, nowMs, cfg);
        }

        Iterator<ClientAura> it = auras.values().iterator();
        while (it.hasNext()) {
            ClientAura aura = it.next();
            if (present.contains(aura.type()) || aura.isGhost()) continue;
            if (msg.flashed().contains(aura.type())) aura.becomeGhost(nowTicks, nowMs, cfg);
            else it.remove();
        }
    }

    boolean isEmpty() {
        return auras.isEmpty();
    }

    List<ClientAura> layout(long nowMs, double nowTicks, BarConfig cfg) {
        auras.values().removeIf(a -> a.isGhostDone(nowMs, cfg));

        visible.clear();
        for (ClientAura a : auras.values()) {
            if (a.isVisible(nowTicks)) visible.add(a);
        }

        float dt = lastLayoutMs == 0 ? 0f : Math.min(0.1f, Math.max(0L, nowMs - lastLayoutMs) / 1000f);
        lastLayoutMs = nowMs;
        float k = 1f - (float) Math.exp(-cfg.elementSlotSlideSpeed * dt);

        for (int i = 0; i < visible.size(); i++) visible.get(i).approachSlot(i, k);
        int n = visible.size();
        layoutCount = layoutCount < 0f ? n : layoutCount + (n - layoutCount) * k;
        return visible;
    }

    float layoutCount() {
        return Math.max(0f, layoutCount);
    }
}