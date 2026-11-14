package com.dsp2.genshinerc.client.render.vfx.manager.handler;

import com.dsp2.genshinerc.client.render.vfx.trail.aaap.weapons.BladeSampler;
import com.dsp2.genshinerc.client.render.vfx.trail.aaap.weapons.BladeSegment;
import com.dsp2.genshinerc.client.render.vfx.trail.aaap.weapons.SwingFxProfile;
import com.dsp2.genshinerc.client.render.vfx.trail.aaap.weapons.SwingFxProfiles;
import mod.chloeprime.aaaparticles.api.common.AAALevel;
import mod.chloeprime.aaaparticles.api.common.ParticleEmitterInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import yesman.epicfight.api.animation.Joint;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

import java.util.HashMap;
import java.util.Map;

public final class WeaponSwingFxHandler {
    private WeaponSwingFxHandler() {}
    private static final Map<Integer, BladeSegment> LAST = new HashMap<>();

    public static void tick(LivingEntityPatch<?> patch, ItemStack stack) {
        Object obj = patch.getOriginal();
        if (!(obj instanceof LivingEntity e)) return;

        SwingFxProfile profile = SwingFxProfiles.resolve(patch, stack);
        if (profile == null) return;

        Joint joint = patch.getArmature().searchJointByName("Tool_R");
        if (joint == null) return;

        BladeSegment cur = BladeSampler.sample(patch, joint, profile.baseOffset(), profile.tipOffset(), Minecraft.getInstance().getPartialTick());
        if (cur == null) return;

        BladeSegment last = LAST.get(e.getId());
        if (last != null && cur.tip().subtract(last.tip()).length() >= profile.minTipStep()) {

            if (profile.sweptEffect() != null) {
                Vec3 mid = last.tip().add(cur.tip()).scale(0.5);
                ParticleEmitterInfo sweptInfo = ParticleEmitterInfo.create(e.level(), profile.sweptEffect())
                        .position(mid.x, mid.y, mid.z);
                AAALevel.addParticle(e.level(), true, sweptInfo);
            }

            if (profile.followEffect() != null) {
                Vec3 anchor = cur.at(profile.followAnchor());
                ParticleEmitterInfo followInfo = ParticleEmitterInfo.create(e.level(), profile.followEffect())
                        .position(anchor.x, anchor.y, anchor.z);
                AAALevel.addParticle(e.level(), true, followInfo);
            }
        }
        LAST.put(e.getId(), cur);
    }

    public static void clear() {
        LAST.clear();
    }
}