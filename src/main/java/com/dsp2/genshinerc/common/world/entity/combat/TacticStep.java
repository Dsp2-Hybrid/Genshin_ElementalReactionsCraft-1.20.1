package com.dsp2.genshinerc.common.world.entity.combat;
public interface TacticStep {
    String id();
    void launch(CombatAvatar<?> avatar);
    float duration();
    default boolean cancellable() { return true; }
}