package com.dsp2.genshinerc.client.render.vfx.aura.elevfx;

import com.dsp2.genshinerc.client.render.entity.elereaction.overlay.FrozenSurface;
import com.dsp2.genshinerc.client.render.vfx.config.VFXConfig;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "genshinerc", value = Dist.CLIENT)
public final class ElementCryoVisuals {
    private static final ResourceLocation FROST = new ResourceLocation("minecraft", "textures/misc/powder_snow_outline.png");
    private static float alpha;
    private static long lastFrame;

    private ElementCryoVisuals() {}

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (event.phase != TickEvent.Phase.END || mc.level == null || mc.isPaused()
                || mc.level.getGameTime() % 3L != 0L) return;
        var random = mc.level.random;
        for (Entity entity : mc.level.entitiesForRendering()) {
            if (!(entity instanceof LivingEntity living) || !FrozenSurface.frozen(living) || living.isInvisible()
                    || (living == mc.getCameraEntity() && mc.options.getCameraType().isFirstPerson())) continue;
            if (mc.player == null || mc.player.distanceToSqr(living) > 1024.0) continue;
            double spread = living.getBbWidth() * 1.1;
            mc.level.addParticle(ParticleTypes.SNOWFLAKE,
                    living.getX() + (random.nextDouble() - 0.5) * spread,
                    living.getY() + random.nextDouble() * living.getBbHeight(),
                    living.getZ() + (random.nextDouble() - 0.5) * spread,
                    0.0, 0.018, 0.0);
        }
    }

    @SubscribeEvent
    public static void onGui(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        VFXConfig config = VFXConfig.get();
        if (mc.player == null || mc.getCameraEntity() != mc.player
                || !config.frostOverlayEnabled || mc.options.hideGui) {
            alpha = 0f;
            lastFrame = 0L;
            return;
        }
        long now = System.nanoTime();
        float dt = lastFrame == 0L ? 0.05f : Math.min(0.1f, (now - lastFrame) * 1.0e-9f);
        lastFrame = now;
        boolean frozen = FrozenSurface.frozen(mc.player);
        float gauge = FrozenSurface.gaugeRatio(mc.player);
        float target = frozen ? (float) (Math.max(0.0, Math.min(0.65, config.frostMaxAlpha)) * Math.sqrt(gauge)) : 0f;
        float vanilla = mc.options.getCameraType().isFirstPerson() ? mc.player.getPercentFrozen() : 0f;
        target *= 1f - vanilla;
        double speed = frozen ? config.frostEaseInSpeed : config.frostEaseOutSpeed;
        alpha += (target - alpha) * (float) (1.0 - Math.exp(-dt * Math.max(0.1, speed)));
        if (alpha < 0.003f) return;
        var graphics = event.getGuiGraphics();
        int width = graphics.guiWidth();
        int height = graphics.guiHeight();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1f, 1f, 1f, alpha);
        try {
            graphics.blit(FROST, 0, 0, width, height, 0f, 0f, 256, 256, 256, 256);
        } finally {
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
            RenderSystem.disableBlend();
        }
    }
}
