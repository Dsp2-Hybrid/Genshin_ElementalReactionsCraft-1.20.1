package com.dsp2.genshinerc.client.render.hud.bar.element;

import com.dsp2.genshinerc.client.render.hud.bar.config.BarConfig;
import com.dsp2.genshinerc.common.network.base.element.SyncAuraPacket;
import net.minecraft.Util;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public final class ClientAuraCache {
    private static final Map<Integer, EntityAuras> CACHE = new HashMap<>();

    private ClientAuraCache() {}

    public static void apply(SyncAuraPacket msg) {
        EntityAuras auras = CACHE.computeIfAbsent(msg.entityId(), k -> new EntityAuras());
        auras.applySnapshot(msg, ClientAura.nowTicks(), Util.getMillis(), BarConfig.get());
        if (auras.isEmpty()) CACHE.remove(msg.entityId());
    }

    @Nullable
    public static EntityAuras get(int entityId) {
        return CACHE.get(entityId);
    }

    public static void remove(int entityId) {
        CACHE.remove(entityId);
    }

    public static void clear() {
        CACHE.clear();
    }
}