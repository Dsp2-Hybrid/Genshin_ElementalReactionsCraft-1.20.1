package com.dsp2.genshinerc.client.render.vfx.aaapshader;

import mod.chloeprime.aaaparticles.api.client.EffectRegistry;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;

@Mod.EventBusSubscriber(modid = "genshinerc", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class AaaProbe {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static int ticks;

    @SubscribeEvent
    public static void onTick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.END || ++ticks % 200 != 0) return;
        try {
            Field routes = com.guhao.vix.client.aaaeffect.AAAEffectPostProcessRegistry.class.getDeclaredField("EFFECT_ROUTES");
            routes.setAccessible(true);
            ((Map<?, ?>) routes.get(null)).forEach((k, v) -> LOGGER.info("PROBE route {} -> {}", k, v.getClass().getName()));

            Field failed = com.guhao.vix.client.aaaeffect.AAAEffectPostProcessDispatcher.class.getDeclaredField("FAILED_PROCESSORS");
            failed.setAccessible(true);
            LOGGER.info("PROBE failed processors: {}", ((Set<?>) failed.get(null)).size());

            EffectRegistry.entries().forEach(en -> {
                if (en.getKey().toString().contains("hit_fire")) LOGGER.info("PROBE aaa id {}", en.getKey());
            });
        } catch (ReflectiveOperationException ex) {
            LOGGER.warn("PROBE reflection failed", ex);
        }
    }
}