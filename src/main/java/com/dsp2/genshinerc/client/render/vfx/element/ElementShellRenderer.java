package com.dsp2.genshinerc.client.render.vfx.element;

import com.dsp2.genshinerc.client.render.hud.bar.element.ClientAuraCache;
import com.dsp2.genshinerc.client.render.hud.bar.element.EntityAuras;
import com.dsp2.genshinerc.client.render.vfx.config.VFXConfig;
import com.dsp2.genshinerc.client.render.vfx.manager.EffekVisibilityManager;
import com.dsp2.genshinerc.common.core.element.ElementType;
import com.mojang.logging.LogUtils;
import mod.chloeprime.aaaparticles.api.client.EffectDefinition;
import mod.chloeprime.aaaparticles.api.client.EffectRegistry;
import mod.chloeprime.aaaparticles.api.client.effekseer.ParticleEmitter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings({"deprecation", "removal"})
@Mod.EventBusSubscriber(modid = "genshinerc", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class ElementShellRenderer {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final double MAX_DISTANCE_SQR = 48.0 * 48.0;
    private static final ResourceLocation HYDRO_EFFECT = new ResourceLocation("genshinerc", "hydro_eff");

    private static final Map<Integer, Shell> SHELLS = new HashMap<>();
    private static final Set<ResourceLocation> WARNED = ConcurrentHashMap.newKeySet();
    private static long frame;

    private ElementShellRenderer() {}

    public static void onReaction(int entityId) { ElementReactionDim.mark(entityId); }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;
        VFXConfig cfg = VFXConfig.get();
        if (!cfg.elementShellEnabled) return;

        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null || mc.isPaused()) return;

        float partialTick = event.getPartialTick();
        Vec3 cam = event.getCamera().getPosition();
        frame++;

        float maxAlphaThisFrame = 0f;

        for (Entity entity : level.entitiesForRendering()) {
            if (!(entity instanceof LivingEntity living) || !living.isAlive()) continue;
            if (entity.distanceToSqr(cam) > MAX_DISTANCE_SQR) continue;
            if (entity == mc.player && mc.options.getCameraType().isFirstPerson()) continue;
            
            if (!EffekVisibilityManager.isVisible(entity)) {
                Shell shell = SHELLS.get(entity.getId());
                if (shell != null) {
                    shell.setVisible(false);
                    shell.lastSeenFrame = frame;
                }
                continue;
            }

            EntityAuras auras = ClientAuraCache.get(entity.getId());
            float hydro = auras == null ? 0f : auras.gauge(ElementType.HYDRO);

            Shell shell = SHELLS.get(entity.getId());
            
            if (hydro <= 0.001f) {
                if (shell != null) { 
                    shell.stop(); 
                    SHELLS.remove(entity.getId()); 
                }
                continue;
            }
            
            if (shell == null) { 
                shell = new Shell(HYDRO_EFFECT); 
                SHELLS.put(entity.getId(), shell); 
            }
            shell.setVisible(true);
            shell.lastSeenFrame = frame;
            maxAlphaThisFrame = Math.max(maxAlphaThisFrame, shell.tick(living, hydro, cfg, partialTick));
        }

        SHELLS.values().removeIf(s -> {
            if (s.lastSeenFrame == frame) return false;
            s.stop();
            return true;
        });

        com.dsp2.genshinerc.client.render.vfx.element.elevfx.fadeout.ElementOpacityProcessor.currentAlpha = maxAlphaThisFrame;
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (!event.getLevel().isClientSide()) return;
        SHELLS.values().forEach(Shell::stop);
        SHELLS.clear();
        ElementReactionDim.clear();
        EffekVisibilityManager.clear();
    }

    private static final class Shell {
        private final ResourceLocation effectId;
        private ParticleEmitter emitter;
        private boolean requested;
        private float alpha = 1f;
        private float smoothedSize = -1f;
        private long lastSeenFrame;

        Shell(ResourceLocation effectId) { this.effectId = effectId; }

        void setVisible(boolean visible) {
            if (emitter != null && emitter.isVisible() != visible) {
                emitter.setVisibility(visible);
            }
        }

        float tick(LivingEntity entity, float gauge, VFXConfig cfg, float partialTick) {
            if (!requested) {
                requested = true;
                EffectRegistry.load(effectId)
                        .thenAcceptAsync(effek -> attach(effek, entity, cfg), Minecraft.getInstance())
                        .exceptionally(err -> {
                            if (WARNED.add(effectId)) LOGGER.warn("[genshinerc] Failed to load element shell {}; check assets/{}/effeks/{}.efkefc", effectId, effectId.getNamespace(), effectId.getPath(), err);
                            return null;
                        });
                return alpha;
            }
            if (emitter == null || !emitter.exists()) return alpha;

            float gaugeAlpha = gauge >= 3f ? 1f : Mth.clamp(gauge / 3f, 0f, 1f);
            float reactionAlpha = ElementReactionDim.factor(entity.getId(), cfg);
            float target = gaugeAlpha * reactionAlpha;

            float k = 1f - (float) Math.exp(-cfg.shellAlphaSmoothSpeed / 20f);
            alpha += (target - alpha) * k;

            float size = Math.max(entity.getBbWidth(), entity.getBbHeight() * 0.5f);
            float want = size * cfg.shellScale;
            if (smoothedSize < 0f) smoothedSize = want;
            smoothedSize += (want - smoothedSize) * k;

            emitter.setScale(smoothedSize, smoothedSize, smoothedSize);

            double x = Mth.lerp(partialTick, entity.xo, entity.getX());
            double y = Mth.lerp(partialTick, entity.yo, entity.getY()) + entity.getBbHeight() * 0.5;
            double z = Mth.lerp(partialTick, entity.zo, entity.getZ());
            emitter.setPosition((float) x, (float) y, (float) z);

            return alpha;
        }

        private void attach(EffectDefinition effek, LivingEntity entity, VFXConfig cfg) {
            ParticleEmitter em = effek.play();
            if (em == ParticleEmitter.dummy(em.type)) return;
            float s = Math.max(entity.getBbWidth(), entity.getBbHeight() * 0.5f) * cfg.shellScale;
            em.setScale(s, s, s);
            this.emitter = em;
        }

        void stop() {
            if (emitter != null && emitter.exists()) emitter.stop();
            emitter = null;
        }
    }
}