package com.dsp2.genshinerc.common.world.entity.ai;
import com.dsp2.genshinerc.common.world.entity.combat.CombatAvatar;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import yesman.epicfight.api.animation.types.AttackAnimation;
public class HuntGoal extends MeleeAttackGoal {
    protected final CombatAvatar<? extends PathfinderMob> avatar;
    protected final double radiusSqr;
    public HuntGoal(CombatAvatar<? extends PathfinderMob> avatar, PathfinderMob mob, double speed, boolean memory, double radius) {
        super(mob, speed, memory);
        this.avatar = avatar;
        this.radiusSqr = radius * radius;
    }
    @Override public boolean canUse() {
        if (this.avatar.burstLocked()) return false;
        LivingEntity t = this.mob.getTarget();
        if (t != null && this.mob.distanceToSqr(t) > this.radiusSqr * 3.0) {
            this.avatar.setBurstReady(true);
            if (this.avatar.getEntityState().canBasicAttack() && !this.avatar.getEntityState().movementLocked()
                    && this.avatar.activeMotion().get() instanceof AttackAnimation) {
                this.avatar.stopPlaying(this.avatar.activeMotion());
                super.canUse();
                this.start();
                return true;
            }
        }
        return super.canUse() && this.avatar.getEntityState().canBasicAttack() && this.avatar.burstReady();
    }
    @Override public boolean canContinueToUse() {
        return super.canContinueToUse() && this.avatar.getEntityState().canBasicAttack() && this.avatar.burstReady();
    }
    @Override public void tick() {
        LivingEntity t = this.mob.getTarget();
        if (t != null && this.mob.distanceToSqr(t) < this.radiusSqr) {
            this.mob.getLookControl().setLookAt(t, 30F, 30F);
            if (this.avatar.burstReady() && this.avatar.getEntityState().canBasicAttack()) {
                this.avatar.setBurstReady(false);
                this.avatar.playAnimationSynchronized(this.avatar.burstMotion(), 0.0F);
            }
            this.mob.getNavigation().stop();
        } else { super.tick(); }
    }
}