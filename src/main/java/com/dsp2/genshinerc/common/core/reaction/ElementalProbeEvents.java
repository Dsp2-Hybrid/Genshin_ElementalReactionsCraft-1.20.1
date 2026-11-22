package com.dsp2.genshinerc.common.core.reaction;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "genshinerc")
public final class ElementalProbeEvents {
    private ElementalProbeEvents() {}

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onExplosion(ExplosionEvent.Detonate event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;

        Vec3 position = event.getExplosion().getPosition();
        double radius = Math.max(1.0, event.getExplosion().getRadius());
        float units = (float) Math.max(1.0, Math.min(4.0, radius * 0.65));

        ElementReactions.probeExplosion(level, null, position, units, radius);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLightning(LivingAttackEvent event) {
        if (!(event.getSource().getDirectEntity() instanceof LightningBolt lightning)) return;
        if (!(event.getEntity().level() instanceof ServerLevel level)) return;

        LivingEntity target = event.getEntity();
        Vec3 position = lightning.position();
        ElementReactions.probeLightning(level, target, position, 1.0f, 2.5);
    }
}