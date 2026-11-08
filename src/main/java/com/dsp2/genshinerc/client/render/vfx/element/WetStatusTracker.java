package com.dsp2.genshinerc.client.render.vfx.element;

import com.dsp2.genshinerc.client.render.hud.bar.element.ClientAuraCache;
import com.dsp2.genshinerc.client.render.hud.bar.element.EntityAuras;
import com.dsp2.genshinerc.client.render.vfx.config.VFXConfig;
import com.dsp2.genshinerc.client.render.world.component.damageindicator.config.DamageIndicatorConfig;
import com.dsp2.genshinerc.client.render.world.component.damageindicator.event.DamageTracker;
import com.dsp2.genshinerc.common.core.element.ElementType;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = "genshinerc", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class WetStatusTracker {
    private static final Map<Integer, Boolean> WAS_WET = new HashMap<>();
    private static final Map<Integer, Boolean> WAS_FROZEN = new HashMap<>();

    private WetStatusTracker() {}

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            WAS_WET.clear();
            WAS_FROZEN.clear();
            return;
        }
        
        VFXConfig cfg = VFXConfig.get();
        if (!cfg.statusTextEnabled) return;

        for (var entity : mc.level.entitiesForRendering()) {
            if (!(entity instanceof LivingEntity living) || !living.isAlive()) continue;
            
            EntityAuras auras = ClientAuraCache.get(living.getId());
            boolean isWet = false;
            boolean isFrozen = false;
            
            if (auras != null) {
                isFrozen = auras.has(ElementType.FROZEN);
                float hydro = auras.gauge(ElementType.HYDRO);
                isWet = hydro > 0.05f;
            }

            boolean wasWet = WAS_WET.getOrDefault(living.getId(), false);
            boolean wasFrozen = WAS_FROZEN.getOrDefault(living.getId(), false);

            if (isFrozen && !wasFrozen) {
                spawnText(living, "genshinerc.status.frozen", ElementType.FROZEN);
            } else if (isWet && !wasWet && !isFrozen) {
                spawnText(living, "genshinerc.status.wet", ElementType.HYDRO);
            }

            WAS_WET.put(living.getId(), isWet);
            WAS_FROZEN.put(living.getId(), isFrozen);
        }
    }

    private static void spawnText(LivingEntity entity, String key, ElementType type) {
        VFXConfig vfx = VFXConfig.get();
        String text = Component.translatable(key).getString();
        String color;
        try {
            color = DamageIndicatorConfig.get().colorFor(type.colorKey());
        } catch (Exception e) {
            color = "#FFFFFF";
        }
        float scale = (float) vfx.statusTextScale;
        DamageTracker.addCustomText(entity, text, color, null, scale);
    }
}