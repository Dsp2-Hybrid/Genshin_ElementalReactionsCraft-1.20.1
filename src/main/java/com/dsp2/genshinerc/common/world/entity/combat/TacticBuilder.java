package com.dsp2.genshinerc.common.world.entity.combat;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
public final class TacticBuilder {
    private final Map<String, TacticNode> points = new HashMap<>();
    private final Map<String, TacticStep> steps = new HashMap<>();
    public TacticBuilder step(TacticStep s) { steps.put(s.id(), s); return this; }
    public TacticBuilder point(String id) { points.put(id, new TacticNode(id)); return this; }
    public TacticBuilder link(String from, String to, float mass) { return link(from, to, mass, c -> true); }
    public TacticBuilder link(String from, String to, float mass, Predicate<FightContext> p) {
        TacticNode n = points.get(from);
        if (n == null) throw new IllegalStateException("unknown point " + from);
        n.link(new TacticLink(to, mass, p));
        return this;
    }
    public TacticGraph seal() { return new TacticGraph(points, steps); }
}