package com.dsp2.genshinerc.common.world.entity.combat;
import java.util.ArrayList;
import java.util.List;
public final class TacticNode {
    private final String stepId;
    private final List<TacticLink> links = new ArrayList<>();
    public TacticNode(String stepId) { this.stepId = stepId; }
    public TacticNode link(TacticLink l) { links.add(l); return this; }
    public String stepId() { return stepId; }
    public List<TacticLink> links() { return links; }
}