package com.dsp2.genshinerc.common.core.element.apply.probe;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
import yesman.epicfight.api.animation.AnimationPlayer;
import yesman.epicfight.api.animation.Joint;
import yesman.epicfight.api.animation.JointTransform;
import yesman.epicfight.api.animation.Pose;
import yesman.epicfight.api.animation.types.AttackAnimation;
import yesman.epicfight.api.animation.types.EntityState;
import yesman.epicfight.api.collider.Collider;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;

public final class SwingVolumeSampler {
    public record SwingSample(AttackAnimation animation, float phaseStart, float elapsed, List<SwingVolume> volumes) {
        public static final SwingSample EMPTY = new SwingSample(null, 0f, 0f, List.of());
    }
    private record Shape(double cx, double cy, double cz, double hx, double hy, double hz) {}

    private static final WeakHashMap<Collider, Shape> SHAPES = new WeakHashMap<>();
    private SwingVolumeSampler() {}

    public static SwingSample sample(LivingEntityPatch<?> patch) {
        if (patch == null || patch.getArmature() == null || patch.getAnimator() == null) return SwingSample.EMPTY;
        AnimationPlayer player = patch.getAnimator().getPlayerFor(null);
        if (player == null || player.isEmpty() || !(player.getAnimation().get() instanceof AttackAnimation attack)) return SwingSample.EMPTY;
        float prev = player.getPrevElapsedTime();
        float now = player.getElapsedTime();
        EntityState prevState = attack.getState(patch, prev);
        EntityState state = attack.getState(patch, now);
        if (!(prevState.attacking() || state.attacking() || (prevState.getLevel() <= 2 && state.getLevel() > 2))) return SwingSample.EMPTY;
        AttackAnimation.Phase phase = attack.getPhaseByTime(now);
        if (phase == null || phase.getColliders() == null) return SwingSample.EMPTY;
        float from = prevState.attacking() ? prev : phase.preDelay;
        float to = state.attacking() ? now : phase.contact;
        int steps = Math.min(8, Math.max(4, (int) (Math.abs(to - from) / 0.05f)));
        List<SwingVolume> volumes = new ArrayList<>();
        for (var pair : phase.getColliders()) {
            Joint joint = pair.getFirst();
            if (joint == null) continue;
            Collider collider = pair.getSecond() == null ? patch.getColliderMatching(phase.getHand()) : pair.getSecond();
            if (collider == null) continue;
            Shape shape = shape(collider);
            for (int i = 0; i <= steps; i++) {
                float partial = steps == 0 ? 1f : i / (float) steps;
                OpenMatrix4f world = jointMatrix(patch, attack, joint, from + (to - from) * partial, partial);
                if (world == null || Math.abs(world.determinant()) < 1.0e-6f) continue;
                volumes.add(new SwingVolume(world, shape.cx, shape.cy, shape.cz, shape.hx, shape.hy, shape.hz));
            }
        }
        return volumes.isEmpty() ? SwingSample.EMPTY : new SwingSample(attack, phase.start, now, volumes);
    }

    private static OpenMatrix4f jointMatrix(LivingEntityPatch<?> patch, AttackAnimation attack, Joint joint, float time, float partial) {
        Armature armature = patch.getArmature();
        OpenMatrix4f bound;
        if (armature.rootJoint.equals(joint)) {
            Pose pose = new Pose();
            pose.putJointData("Root", JointTransform.empty());
            attack.modifyPose(attack, pose, patch, time, partial);
            bound = pose.orElseEmpty("Root").getAnimationBoundMatrix(armature.rootJoint, new OpenMatrix4f()).removeTranslation();
        } else {
            bound = new OpenMatrix4f(armature.getBoundTransformFor(attack.getPoseByTime(patch, time, partial), joint));
        }
        Entity entity = patch.getOriginal();
        double x = patch.getXOld() + (entity.getX() - patch.getXOld()) * partial;
        double y = patch.getYOld() + (entity.getY() - patch.getYOld()) * partial;
        double z = patch.getZOld() + (entity.getZ() - patch.getZOld()) * partial;
        OpenMatrix4f move = OpenMatrix4f.createTranslation(-(float) x, (float) y, -(float) z);
        bound.mulFront(move.mulBack(patch.getModelMatrix(partial)));
        return bound;
    }

    private static Shape shape(Collider collider) {
        Shape cached = SHAPES.get(collider);
        if (cached != null) return cached;
        CompoundTag tag = collider.serialize(new CompoundTag());
        double hx = 0.3, hy = 0.3, hz = 0.3, cx = 0, cy = 0, cz = 0;
        if (tag.contains("size", Tag.TAG_LIST)) {
            ListTag size = tag.getList("size", Tag.TAG_DOUBLE);
            hx = size.getDouble(0);
            hy = size.getDouble(1);
            hz = size.getDouble(2);
        }
        if (tag.contains("center", Tag.TAG_LIST)) {
            ListTag center = tag.getList("center", Tag.TAG_DOUBLE);
            cx = center.getDouble(0);
            cy = center.getDouble(1);
            cz = center.getDouble(2);
        }
        Shape shape = new Shape(cx, cy, cz, hx, hy, hz);
        SHAPES.put(collider, shape);
        return shape;
    }
}
