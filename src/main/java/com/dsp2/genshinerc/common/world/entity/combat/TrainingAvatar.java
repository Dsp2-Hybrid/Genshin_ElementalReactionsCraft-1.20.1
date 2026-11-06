package com.dsp2.genshinerc.common.world.entity.combat;
import com.dsp2.genshinerc.common.world.entity.asset.Motions;
import com.dsp2.genshinerc.common.world.entity.unit.TrainingUnit;
import net.minecraft.world.entity.PathfinderMob;
import yesman.epicfight.api.animation.Animator;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.world.capabilities.entitypatch.Factions;
import yesman.epicfight.world.entity.ai.goal.TargetChasingGoal;
public class TrainingAvatar extends CombatAvatar<TrainingUnit> {
    public TrainingAvatar() { super(Factions.UNDEAD); }
    @Override protected void initAI() {
        super.initAI();
        this.getOriginal().goalSelector.addGoal(2, new TargetChasingGoal(this, (PathfinderMob) this.getOriginal(), 1.2, true));
    }
    @Override public void initAnimator(Animator animator) {
        super.initAnimator(animator);
        animator.addLivingAnimation(LivingMotions.IDLE, Motions.TRAINING_IDLE);
        animator.addLivingAnimation(LivingMotions.WALK, Motions.TRAINING_WALK);
        animator.addLivingAnimation(LivingMotions.CHASE, Motions.TRAINING_WALK);
        animator.addLivingAnimation(LivingMotions.RUN, Motions.TRAINING_WALK);
    }
    @Override public void updateMotion(boolean considerInaction) { super.commonAggressiveRangedMobUpdateMotion(considerInaction); }
    @Override protected TacticGraph defineTactics() {
        return new TacticBuilder()
                .step(motion("idle", 0.2F, Motions.TRAINING_IDLE))
                .step(motion("strike", 0.8F, Motions.TRAINING_STRIKE))
                .step(motion("thrust", 0.8F, Motions.TRAINING_THRUST))
                .point("idle").link("idle", "strike", 1.0F)
                .point("strike").link("strike", "thrust", 0.5F).link("strike", "idle", 0.5F)
                .point("thrust").link("thrust", "idle", 1.0F)
                .seal();
    }
    @Override protected String entryTactic() { return "idle"; }
    private TacticStep motion(String id, float sec, AssetAccessor<? extends StaticAnimation> anim) {
        return new TacticStep() {
            public String id() { return id; }
            public void launch(CombatAvatar<?> avatar) { avatar.playAnimationSynchronized(anim, 0.0F); }
            public float duration() { return sec; }
        };
    }
}