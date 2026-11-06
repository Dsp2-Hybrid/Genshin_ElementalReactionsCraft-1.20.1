package com.dsp2.genshinerc.client.render.vfx.trail.aaap.weapons;

import javax.annotation.Nullable;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.api.animation.Joint;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.api.utils.math.Vec3f;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

@OnlyIn(Dist.CLIENT)
public final class BladeSampler {
    private static final Vec3f Y_AXIS = new Vec3f(0.0F, 1.0F, 0.0F);

    private BladeSampler() {}

    @Nullable
    public static BladeSegment sample(LivingEntityPatch<?> patch, Joint joint, Vec3f baseOffset, Vec3f tipOffset, float partialTick) {
        Armature armature = patch.getArmature();
        if (armature == null) return null;

        OpenMatrix4f jointMatrix;
        try {
            jointMatrix = armature.getBoundTransformFor(patch.getAnimator().getPose(partialTick), joint);
        } catch (IllegalArgumentException missingJoint) {
            return null;
        }

        float yRot = Mth.rotLerp(partialTick, patch.getYRotO(), patch.getYRot());
        OpenMatrix4f yaw = new OpenMatrix4f().rotate(-(float) Math.toRadians(yRot + 180.0F), Y_AXIS);
        OpenMatrix4f world = OpenMatrix4f.mul(yaw, jointMatrix, null);

        LivingEntity entity = patch.getOriginal();
        Vec3 origin = new Vec3(
                Mth.lerp(partialTick, entity.xo, entity.getX()),
                Mth.lerp(partialTick, entity.yo, entity.getY()),
                Mth.lerp(partialTick, entity.zo, entity.getZ()));

        Vec3 base = OpenMatrix4f.transform(world, new Vec3(baseOffset.x, baseOffset.y, baseOffset.z)).add(origin);
        Vec3 tip = OpenMatrix4f.transform(world, new Vec3(tipOffset.x, tipOffset.y, tipOffset.z)).add(origin);
        return new BladeSegment(base, tip);
    }
}