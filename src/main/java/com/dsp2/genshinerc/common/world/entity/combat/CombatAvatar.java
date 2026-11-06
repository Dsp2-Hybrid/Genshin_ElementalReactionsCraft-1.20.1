package com.dsp2.genshinerc.common.world.entity.combat;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import yesman.epicfight.api.animation.Animator;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.utils.math.MathUtils;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.world.capabilities.entitypatch.Faction;
import yesman.epicfight.world.capabilities.entitypatch.MobPatch;
import yesman.epicfight.world.damagesource.StunType;
public abstract class CombatAvatar<T extends Mob> extends MobPatch<T> {
    protected AssetAccessor<? extends StaticAnimation> burstMotion;
    protected AssetAccessor<? extends StaticAnimation> staggerMotion;
    private boolean burstReady, burstLocked, orbitLocked;
    private final FightContext context = new FightContext();
    private TacticMachine machine;
    public CombatAvatar(Faction faction) { super(faction); }
    protected abstract TacticGraph defineTactics();
    protected abstract String entryTactic();
    public TacticMachine tactics() {
        if (machine == null) { machine = new TacticMachine(defineTactics()); machine.enter(entryTactic()); }
        return machine;
    }
    protected FightContext context() { return context; }
    protected void advanceTactic(LivingEntity target) {
        if (this.isLogicalClient()) return;
        TacticMachine m = this.tactics();
        T original = this.getOriginal();
        if (!m.active()) return;
        if (target != null) {
            context.gap = original.distanceTo(target);
            context.targetLiving = target.isAlive();
            context.targetGrounded = target.onGround();
        }
        context.vitality = original.getHealth() / original.getMaxHealth();
        context.diceRoll = original.getRandom().nextInt(100);
        m.update(this, context);
    }
    @Override public void onJoinWorld(T original, EntityJoinLevelEvent event) { super.onJoinWorld(original, event); }
    @Override public void initAnimator(Animator animator) { super.initAnimator(animator); }
    @Override public void updateMotion(boolean considerInaction) { }
    @Override public AssetAccessor<? extends StaticAnimation> getHitAnimation(StunType stunType) { return null; }
    @Override public OpenMatrix4f getModelMatrix(float partialTicks) {
        T o = this.getOriginal();
        float yawO = this.isLogicalClient() ? o.yBodyRotO : o.yBodyRot;
        float yaw  = this.isLogicalClient() ? o.yBodyRot  : o.yBodyRot;
        return MathUtils.getModelMatrixIntegral(0F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, yawO, yaw, partialTicks, 1F, 1F, 1F);
    }
    public AssetAccessor<? extends StaticAnimation> staggerMotion() { return staggerMotion; }
    public AssetAccessor<? extends StaticAnimation> burstMotion() { return burstMotion; }
    public boolean burstReady() { return burstReady; }
    public void setBurstReady(boolean v) { this.burstReady = v; }
    public boolean burstLocked() { return burstLocked; }
    public void lockBurst() { this.burstLocked = true; }
    public boolean orbitLocked() { return orbitLocked; }
    public void lockOrbit() { this.orbitLocked = true; }
    public AssetAccessor<? extends StaticAnimation> activeMotion() {
        return this.isLogicalClient() ? this.getClientAnimator().baseLayer.animationPlayer.getRealAnimation()
                                      : this.getServerAnimator().animationPlayer.getRealAnimation();
    }
    public float motionClock() {
        return this.isLogicalClient() ? this.getClientAnimator().baseLayer.animationPlayer.getPrevElapsedTime()
                                      : this.getServerAnimator().animationPlayer.getElapsedTime();
    }
}