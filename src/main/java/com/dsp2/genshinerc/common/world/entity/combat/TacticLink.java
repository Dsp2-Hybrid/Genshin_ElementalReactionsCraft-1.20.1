package com.dsp2.genshinerc.common.world.entity.combat;
import java.util.function.Predicate;
public record TacticLink(String destId, float mass, Predicate<FightContext> predicate) {
    public static TacticLink of(String destId, float mass) { return new TacticLink(destId, mass, c -> true); }
}