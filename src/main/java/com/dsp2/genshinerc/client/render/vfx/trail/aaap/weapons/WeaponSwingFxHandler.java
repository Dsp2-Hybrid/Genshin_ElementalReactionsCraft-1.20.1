package com.dsp2.genshinerc.client.render.vfx.trail.aaap.weapons;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import mod.chloeprime.aaaparticles.api.client.EffectDefinition;
import mod.chloeprime.aaaparticles.api.client.EffectRegistry;
import mod.chloeprime.aaaparticles.api.client.effekseer.ParticleEmitter;
import mod.chloeprime.aaaparticles.api.common.AAALevel;
import mod.chloeprime.aaaparticles.api.common.ParticleEmitterInfo;
import mod.chloeprime.aaaparticles.client.installer.NativePlatform;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import yesman.epicfight.api.animation.Joint;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

@Mod.EventBusSubscriber(modid = "genshinerc", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class WeaponSwingFxHandler {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final double MAX_DISTANCE_SQR = 64.0 * 64.0;
    private static final boolean SKIP_LOCAL_FIRST_PERSON = true;

    private static final Map<Integer, SwingTracker> TRACKERS = new HashMap<>();
    private static final Set<ResourceLocation> WARNED = ConcurrentHashMap.newKeySet();
    private static long frame;

    private WeaponSwingFxHandler() {}

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null || mc.isPaused() || NativePlatform.isRunningOnUnsupportedPlatform()) return;

        float partialTick = event.getPartialTick();
        Vec3 cameraPos = event.getCamera().getPosition();
        frame++;

        for (Entity entity : level.entitiesForRendering()) {
            if (!(entity instanceof LivingEntity living) || !living.isAlive()) continue;
            if (entity.distanceToSqr(cameraPos) > MAX_DISTANCE_SQR) continue;
            if (SKIP_LOCAL_FIRST_PERSON && entity == mc.player && mc.options.getCameraType().isFirstPerson()) continue;

            LivingEntityPatch<?> patch = EpicFightCapabilities.getEntityPatch(entity, LivingEntityPatch.class);
            if (patch == null || patch.getArmature() == null) continue;

            if (!patch.getEntityState().attacking()) {
                continue;
            }

            InteractionHand hand = patch.getAttackingHand();
            if (hand == null) {
                hand = InteractionHand.MAIN_HAND;
            }

            Joint joint = patch.getParentJointOfHand(hand);
            if (joint == null || joint == patch.getArmature().rootJoint) continue;

            SwingFxProfile profile = SwingFxProfiles.resolve(patch, patch.getValidItemInHand(hand));
            if (profile == null) continue;

            SwingTracker tracker = TRACKERS.computeIfAbsent(entity.getId(), id -> new SwingTracker());
            tracker.lastSeenFrame = frame;
            update(level, patch, joint, profile, tracker, entity.tickCount, partialTick);
        }

        TRACKERS.values().removeIf(tracker -> {
            if (tracker.lastSeenFrame == frame) return false;
            tracker.endSwing();
            return true;
        });
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel().isClientSide()) {
            TRACKERS.values().forEach(SwingTracker::endSwing);
            TRACKERS.clear();
        }
    }

    private static void update(ClientLevel level, LivingEntityPatch<?> patch, Joint joint, SwingFxProfile profile,
                               SwingTracker tracker, int tickCount, float partialTick) {
        tracker.spawnedThisFrame = 0;

        float start;
        if (tracker.lastSegment == null) {
            start = partialTick;
        } else if (tickCount == tracker.lastTickCount) {
            start = tracker.lastPartial;
        } else if (tickCount == tracker.lastTickCount + 1) {
            start = 0.0F;
        } else {
            tracker.lastSegment = null;
            start = partialTick;
        }

        if (tracker.lastSegment == null) {
            BladeSegment first = BladeSampler.sample(patch, joint, profile.baseOffset(), profile.tipOffset(), partialTick);
            if (first == null) return;
            tracker.lastSegment = first;
            tracker.lastTickCount = tickCount;
            tracker.lastPartial = partialTick;
            startFollow(patch, joint, profile, tracker);
            return;
        }

        if (tickCount == tracker.lastTickCount && partialTick - start < 1.0E-4F) return;

        int samples = Math.max(1, profile.subSamples());
        for (int i = 1; i <= samples; i++) {
            float t = Mth.lerp(i / (float) samples, start, partialTick);
            BladeSegment current = BladeSampler.sample(patch, joint, profile.baseOffset(), profile.tipOffset(), t);
            if (current == null) continue;
            spawnInSweptQuad(level, profile, tracker, tracker.lastSegment, current);
            tracker.lastSegment = current;
        }

        tracker.lastTickCount = tickCount;
        tracker.lastPartial = partialTick;
    }

    private static void spawnInSweptQuad(ClientLevel level, SwingFxProfile profile, SwingTracker tracker,
                                         BladeSegment prev, BladeSegment cur) {
        if (profile.sweptEffect() == null) return;

        Vec3 tipStep = cur.tip().subtract(prev.tip());
        if (tipStep.lengthSqr() < (double) profile.minTipStep() * profile.minTipStep()) return;

        tracker.spawnBudget += (float) (SweptArea.area(prev, cur) * profile.sweptDensity());
        int wanted = (int) tracker.spawnBudget;
        tracker.spawnBudget -= wanted;
        int count = Math.min(wanted, profile.maxSweptPerFrame() - tracker.spawnedThisFrame);
        if (count <= 0) return;

        RandomSource random = level.random;
        for (int i = 0; i < count; i++) {
            double bladeFraction = Math.sqrt(random.nextDouble());
            double timeFraction = random.nextDouble();
            Vec3 pos = SweptArea.point(prev, cur, bladeFraction, timeFraction);

            ParticleEmitterInfo info = ParticleEmitterInfo.create(level, profile.sweptEffect())
                    .position(pos)
                    .rotationFromForward(tipStep, random.nextFloat() * (float) (Math.PI * 2.0))
                    .scale(profile.sweptScale());
            AAALevel.addParticle(level, false, info);
        }
        tracker.spawnedThisFrame += count;
    }

    private static void startFollow(LivingEntityPatch<?> patch, Joint joint, SwingFxProfile profile, SwingTracker tracker) {
        ResourceLocation id = profile.followEffect();
        if (id == null || tracker.followRequested) return;
        tracker.followRequested = true;
        int swingId = tracker.swingId;

        EffectRegistry.load(id)
                .thenAcceptAsync(effek -> attachFollow(effek, patch, joint, profile, tracker, swingId), Minecraft.getInstance())
                .exceptionally(err -> {
                    if (WARNED.add(id)) {
                        LOGGER.warn("[genshinerc] Failed to load follow effek {}; check assets/{}/effeks/{}.efkefc",
                                id, id.getNamespace(), id.getPath(), err);
                    }
                    return null;
                });
    }

    private static void attachFollow(EffectDefinition effek, LivingEntityPatch<?> patch, Joint joint, SwingFxProfile profile,
                                     SwingTracker tracker, int swingId) {
        if (tracker.swingId != swingId) return;
        ParticleEmitter emitter = effek.play();
        if (emitter == ParticleEmitter.dummy(emitter.type)) return;

        float scale = profile.followScale();
        emitter.setScale(scale, scale, scale);
        followBlade(emitter, patch, joint, profile, tracker, swingId, Minecraft.getInstance().getFrameTime());
        emitter.addPreDrawCallback((em, partial) -> followBlade(em, patch, joint, profile, tracker, swingId, partial));
        tracker.follow = emitter;
    }

    private static void followBlade(ParticleEmitter emitter, LivingEntityPatch<?> patch, Joint joint, SwingFxProfile profile,
                                    SwingTracker tracker, int swingId, float partial) {
        if (!emitter.exists()) {
            if (tracker.follow == emitter) tracker.follow = null;
            return;
        }
        if (tracker.swingId != swingId || !patch.getOriginal().isAlive()) {
            emitter.stop();
            return;
        }
        BladeSegment segment = BladeSampler.sample(patch, joint, profile.baseOffset(), profile.tipOffset(), partial);
        if (segment == null) return;

        Vec3 anchor = segment.at(profile.followAnchor());
        float[] rotation = AaaRotation.fromForward(segment.direction());
        emitter.setPosition((float) anchor.x, (float) anchor.y, (float) anchor.z);
        emitter.setRotation(rotation[0], rotation[1], 0.0F);
    }
}