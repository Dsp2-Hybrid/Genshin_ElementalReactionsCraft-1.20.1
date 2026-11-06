package com.dsp2.genshinerc.common.world.entity.asset;
import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.AnimationManager.AnimationBuilder;
import yesman.epicfight.api.animation.types.MovementAnimation;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.model.Armature;
public class Motions {
    public static AnimationAccessor<StaticAnimation> TRAINING_IDLE;
    public static AnimationAccessor<MovementAnimation> TRAINING_WALK;
    public static AnimationAccessor<StaticAnimation> TRAINING_STRIKE;
    public static AnimationAccessor<StaticAnimation> TRAINING_THRUST;
    public static void build(AnimationBuilder builder) {
        AssetAccessor<? extends Armature> rig = Rigs.UNIT;
        TRAINING_IDLE = builder.nextAccessor("training/living/idle", a -> new StaticAnimation(0.1F, true, a, rig));
        TRAINING_WALK = builder.nextAccessor("training/living/walk", a -> new MovementAnimation(true, a, rig));
        TRAINING_STRIKE = builder.nextAccessor("training/combat/strike", a -> new StaticAnimation(0.05F, false, a, rig));
        TRAINING_THRUST = builder.nextAccessor("training/combat/thrust", a -> new StaticAnimation(0.05F, false, a, rig));
    }
}