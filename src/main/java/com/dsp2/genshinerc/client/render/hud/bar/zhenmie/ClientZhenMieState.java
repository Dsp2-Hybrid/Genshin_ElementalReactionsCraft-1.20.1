package com.dsp2.genshinerc.client.render.hud.bar.zhenmie;

import com.dsp2.genshinerc.client.render.hud.bar.anim.AnimatedBarValue;
import com.dsp2.genshinerc.client.render.hud.bar.config.BarConfig;
import com.dsp2.genshinerc.common.core.combat.zhenmie.ClashResult;
import com.dsp2.genshinerc.common.network.base.combat.SyncZhenMiePacket;
import org.jetbrains.annotations.Nullable;

public final class ClientZhenMieState {
    final AnimatedBarValue zhen = new AnimatedBarValue();
    final AnimatedBarValue mie = new AnimatedBarValue();
    boolean zhenBuff, mieBuff;
    @Nullable ClashResult clash;
    long clashStartMs;
    float clashPoint;

    float zhenTarget, mieTarget;
    boolean decaying;

    public void apply(SyncZhenMiePacket msg, long nowMs, BarConfig cfg) {
        boolean trail = !msg.decaying();
        zhenTarget = (float) msg.zhen();
        mieTarget = (float) msg.mie();
        decaying = msg.decaying();

        zhen.set(msg.zhen(), nowMs, cfg.subBarWhiteHoldTime, trail);
        mie.set(msg.mie(), nowMs, cfg.subBarWhiteHoldTime, trail);
        zhenBuff = msg.zhenBuff();
        mieBuff = msg.mieBuff();
        if (msg.clash() != null) {
            clash = msg.clash();
            clashStartMs = nowMs;
            clashPoint = msg.clashPoint();
            zhen.flash(nowMs);
            mie.flash(nowMs);
        }
    }

    public float zhen() { return zhen.main(); }
    public float mie()  { return mie.main(); }

    void update(long nowMs, BarConfig cfg) {
        zhen.update(nowMs, cfg.subBarFillSpeed, cfg.subBarWhiteDrainSpeed);
        mie.update(nowMs, cfg.subBarFillSpeed, cfg.subBarWhiteDrainSpeed);
        if (clash != null && nowMs - clashStartMs > clash.visualSeconds() * 1000f) clash = null;
    }

    boolean isIdle(long nowMs, BarConfig cfg) {
        return clash == null && zhen.isIdle(nowMs, cfg.subBarFlashTime) && mie.isIdle(nowMs, cfg.subBarFlashTime);
    }

    public float zhenTrail() { return zhen.trail(); }
    public float mieTrail()  { return mie.trail(); }
    public float zhenWhite(long nowMs, BarConfig cfg) { return zhen.whiteness(nowMs, cfg.subBarFlashTime); }
    public float mieWhite(long nowMs, BarConfig cfg)  { return mie.whiteness(nowMs, cfg.subBarFlashTime); }
    public boolean zhenBuff() { return zhenBuff; }
    public boolean mieBuff()  { return mieBuff; }

    public float zhenTarget() { return zhenTarget; }
    public float mieTarget()  { return mieTarget; }
    public boolean decaying() { return decaying; }

    @Nullable public ClashResult clash() { return clash; }
    public long clashStartMs() { return clashStartMs; }
    public float clashPoint()  { return clashPoint; }
    public void tick(long nowMs, BarConfig cfg) { update(nowMs, cfg); }
    public boolean idle(long nowMs, BarConfig cfg) { return isIdle(nowMs, cfg); }
}