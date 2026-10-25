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

    public void apply(SyncZhenMiePacket msg, long nowMs, BarConfig cfg) {
        boolean trail = !msg.decaying();
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
}