package com.dsp2.genshinerc.client.render.vfx.aura.elevfx;

import com.dsp2.genshinerc.client.render.hud.bar.element.ClientAuraCache;
import com.dsp2.genshinerc.client.render.hud.bar.element.EntityAuras;
import com.dsp2.genshinerc.client.render.vfx.config.VFXConfig;
import com.dsp2.genshinerc.client.render.vfx.manager.EffekVisibilityManager;
import com.dsp2.genshinerc.client.render.vfx.aura.elevfx.fadeout.ElementEmitterAlpha;
import com.dsp2.genshinerc.client.render.vfx.aura.elevfx.fadeout.ElementOpacityProcessor;
import com.dsp2.genshinerc.common.core.element.ElementType;
import com.dsp2.genshinerc.client.render.vfx.feature.light.AaapLightRegistry;
import com.dsp2.genshinerc.client.render.vfx.feature.light.AaapLightRenderer;
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
    private static final float[] HYDRO_LIGHT = {0.25f, 0.6f, 1.0f};

    private static final Map<Integer, Shell> SHELLS = new HashMap<>();
    private static final Set<ResourceLocation> WARNED = ConcurrentHashMap.newKeySet();
    private static long frame;
    private static final float SHELL_SIZE_FACTOR = 0.8f;
    private static long lastNanos;

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
        
        long now = System.nanoTime();
        float dt = lastNanos == 0 ? 0.05f : Mth.clamp((now - lastNanos) / 1.0e9f, 0f, 0.25f);
        lastNanos = now;

        float maxAlphaThisFrame = 0f;

        for (Entity entity : level.entitiesForRendering()) {
            if (!(entity instanceof LivingEntity living) || !living.isAlive()) continue;
            if (entity.distanceToSqr(cam) > MAX_DISTANCE_SQR) continue;
            if (entity == mc.player && mc.options.getCameraType().isFirstPerson()) continue;
            


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
            shell.setVisible(EffekVisibilityManager.isVisible(entity));
            shell.lastSeenFrame = frame;
            maxAlphaThisFrame = Math.max(maxAlphaThisFrame, shell.tick(living, hydro, cfg, partialTick, dt));
        }

        SHELLS.values().removeIf(s -> {
            if (s.lastSeenFrame == frame) return false;
            s.stop();
            return true;
        });
        com.dsp2.genshinerc.client.render.vfx.aura.elevfx.fadeout.ElementEmitterAlpha.prune();

        ElementOpacityProcessor.currentAlpha = maxAlphaThisFrame;
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (!event.getLevel().isClientSide()) return;
        SHELLS.values().forEach(Shell::stop);
        ElementEmitterAlpha.clear();
        SHELLS.clear();
        ElementReactionDim.clear();
        EffekVisibilityManager.clear();
        AaapLightRegistry.clear();
    }

    private static boolean selfOccluded(LivingEntity player, Vec3 camera, float partial) {
        if (Minecraft.getInstance().getCameraEntity() != player || player.isSpectator()) return false;
        Vec3 now = player.getPosition(partial);
        var ownBox = player.getBoundingBox().move(now.subtract(player.position()));
        var bounds = new net.minecraft.world.phys.AABB(camera, ownBox.getCenter()).minmax(ownBox).inflate(0.5);
        var blockers = player.level().getEntitiesOfClass(LivingEntity.class, bounds,
                e -> e != player && e.isAlive() && !e.isSpectator() && !e.isInvisibleTo((net.minecraft.world.entity.player.Player) player)
                        && !player.isPassengerOfSameVehicle(e));
        double offset = player.getBbWidth() * 0.31;
        Vec3 horizontal = ownBox.getCenter().subtract(camera);
        Vec3 side = new Vec3(-horizontal.z, 0.0, horizontal.x).normalize().scale(offset);
        for (double height : new double[]{0.27, 0.52, 0.76}) {
            for (int column = -1; column <= 1; column++) {
                Vec3 point = now.add(0.0, player.getBbHeight() * height, 0.0).add(side.scale(column));
                boolean blocked = player.level().clip(new net.minecraft.world.level.ClipContext(camera, point,
                        net.minecraft.world.level.ClipContext.Block.VISUAL,
                        net.minecraft.world.level.ClipContext.Fluid.NONE, player)).getType()
                        == net.minecraft.world.phys.HitResult.Type.BLOCK;
                if (!blocked) {
                    for (LivingEntity blocker : blockers) {
                        var box = blocker.getBoundingBox().move(blocker.getPosition(partial).subtract(blocker.position()));
                        if (box.contains(camera) || box.clip(camera, point).isPresent()) {
                            blocked = true;
                            break;
                        }
                    }
                }
                if (!blocked) return false;
            }
        }
        return true;
    }

    private static final class Shell {
        private boolean visible = true;
        private boolean stopped;
        private final ResourceLocation effectId;
        private ParticleEmitter emitter;
        private boolean requested;
        private float alpha = 1f;
        private boolean alphaInit;
        private float smoothedSize = -1f;
        private long lastSeenFrame;

        private static float targetSize(LivingEntity entity, VFXConfig cfg) {
            return Math.max(entity.getBbWidth(), entity.getBbHeight() * 0.5f) * cfg.shellScale * SHELL_SIZE_FACTOR;
        }

        Shell(ResourceLocation effectId) { this.effectId = effectId; }

        void setVisible(boolean visible) {
            this.visible = visible;
            if (emitter != null && emitter.isVisible() != visible) {

                emitter.setVisibility(visible);
            }
        }

        float tick(LivingEntity entity, float gauge, VFXConfig cfg, float partialTick, float dt) {
            if (!requested) {
                requested = true;
                EffectRegistry.load(effectId)
                        .thenAcceptAsync(effek -> attach(effek, entity, cfg), Minecraft.getInstance())
                        .exceptionally(err -> {
                            if (WARNED.add(effectId)) LOGGER.warn("[genshinerc] Failed to load aura shell {}; check assets/{}/effeks/{}.efkefc", effectId, effectId.getNamespace(), effectId.getPath(), err);
                            return null;
                        });
                return 0f;
            }
            if (emitter == null || !emitter.exists()) return 0f;

            float gaugeAlpha = gauge >= 3f ? 1f : Mth.clamp(gauge / 3f, 0f, 1f);
            float reactionAlpha = ElementReactionDim.factor(entity.getId(), cfg);
            float target = gaugeAlpha * reactionAlpha;

            float k = 1f - (float) Math.exp(-cfg.shellAlphaSmoothSpeed * dt);
            if (!alphaInit) {
                alpha = target;
                alphaInit = true;
            } else {
                alpha += (target - alpha) * k;
            }

            float want = targetSize(entity, cfg);
            if (smoothedSize < 0f) smoothedSize = want;
            smoothedSize += (want - smoothedSize) * k;
            emitter.setScale(smoothedSize, smoothedSize, smoothedSize);

            double x = Mth.lerp(partialTick, entity.xo, entity.getX());
            double y = Mth.lerp(partialTick, entity.yo, entity.getY()) + entity.getBbHeight() * 0.5;
            double z = Mth.lerp(partialTick, entity.zo, entity.getZ());
            emitter.setPosition((float) x, (float) y, (float) z);

            float lightI = cfg.aaaLightIntensity * alpha;
            if (cfg.aaaLightEnabled && AaapLightRenderer.isActive() && lightI > 0.01f) {
                AaapLightRegistry.put(emitter, new AaapLightRegistry.Light(x, y, z,
                        smoothedSize * cfg.aaaLightRadiusScale, smoothedSize * 0.5f,
                        HYDRO_LIGHT[0], HYDRO_LIGHT[1], HYDRO_LIGHT[2], lightI));
            } else {
                AaapLightRegistry.remove(emitter);
            }

            ElementEmitterAlpha.put(emitter, alpha);
            return alpha;
        }

        private void attach(EffectDefinition effek, LivingEntity entity, VFXConfig cfg) {
            if (stopped || entity.isRemoved()) return;
            ParticleEmitter em = effek.play();
            if (em == ParticleEmitter.dummy(em.type)) return;
            float s = targetSize(entity, cfg);
            em.setScale(s, s, s);
            ElementEmitterAlpha.put(em, 0f);
            em.setVisibility(visible);
            this.emitter = em;
        }

        void stop() {
            stopped = true;
            if (emitter != null) {
                AaapLightRegistry.remove(emitter);
                if (emitter.exists()) emitter.stop();
            }
            emitter = null;
        }
    }
}