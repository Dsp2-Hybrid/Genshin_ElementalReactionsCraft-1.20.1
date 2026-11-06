package com.dsp2.genshinerc.common.world.entity.combat;
import java.util.Map;
import java.util.Random;
public final class TacticGraph {
    private final Map<String, TacticNode> points;
    private final Map<String, TacticStep> steps;
    private final Random random = new Random();
    TacticGraph(Map<String, TacticNode> points, Map<String, TacticStep> steps) { this.points = points; this.steps = steps; }
    public TacticStep lookup(String id) { return steps.get(id); }
    public String selectNext(String current, FightContext ctx) {
        TacticNode node = points.get(current);
        if (node == null) return current;
        float total = 0F;
        for (TacticLink l : node.links()) if (l.predicate().test(ctx)) total += l.mass();
        if (total <= 0F) return current;
        float roll = random.nextFloat() * total;
        for (TacticLink l : node.links()) {
            if (!l.predicate().test(ctx)) continue;
            roll -= l.mass();
            if (roll <= 0F) return l.destId();
        }
        return current;
    }
}