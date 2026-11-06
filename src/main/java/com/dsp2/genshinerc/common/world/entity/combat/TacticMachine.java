package com.dsp2.genshinerc.common.world.entity.combat;
public final class TacticMachine {
    private final TacticGraph graph;
    private String cursor;
    private float clock;
    private boolean running, connected;
    public TacticMachine(TacticGraph g) { this.graph = g; }
    public void enter(String id) { cursor = id; clock = 0F; running = true; connected = false; }
    public void snapTo(String id) { cursor = id; clock = 0F; connected = false; }
    public void update(CombatAvatar<?> avatar, FightContext ctx) {
        if (!running || cursor == null) return;
        TacticStep s = graph.lookup(cursor);
        if (s == null) { running = false; return; }
        ctx.stageTime = clock;
        ctx.connected = connected;
        if (clock == 0F) s.launch(avatar);
        clock += 0.05F;
        if (clock >= s.duration()) {
            String next = graph.selectNext(cursor, ctx);
            TacticStep ns = graph.lookup(next);
            if (ns == null) { running = false; return; }
            cursor = next;
            clock = 0F;
            connected = false;
        }
    }
    public void recordConnect() { connected = true; }
    public void halt() { running = false; }
    public boolean active() { return running; }
    public String position() { return cursor; }
}