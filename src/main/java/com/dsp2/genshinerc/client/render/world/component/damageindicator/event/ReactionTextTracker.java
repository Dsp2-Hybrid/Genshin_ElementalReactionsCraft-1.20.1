package com.dsp2.genshinerc.client.render.world.component.damageindicator.event;

import com.dsp2.genshinerc.client.render.world.component.damageindicator.config.DamageIndicatorConfig;
import com.dsp2.genshinerc.common.core.element.ElementType;
import com.dsp2.genshinerc.common.core.reaction.Reaction;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class ReactionTextTracker {
    private static final int MAX_PER_ENTITY = 3;
    private static final List<ReactionTextParticle> ACTIVE = new ArrayList<>();

    private ReactionTextTracker() {}

    public static void add(int entityId, Reaction reaction, @Nullable ElementType first, @Nullable ElementType second) {
        DamageIndicatorConfig cfg = DamageIndicatorConfig.get();
        if (!cfg.serverEnabled || !cfg.reactionTextEnabled) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;
        if (!(mc.level.getEntity(entityId) instanceof LivingEntity target)) return;
        if (mc.player.distanceTo(target) > cfg.serverMaxDistance) return;

        for (ReactionTextParticle p : ACTIVE) {
            if (p.matches(target, reaction, first, second)) {
                p.refresh();
                return;
            }
        }
        List<ReactionTextParticle> own = new ArrayList<>();
        for (ReactionTextParticle p : ACTIVE) if (p.target() == target) own.add(p);
        if (own.size() >= MAX_PER_ENTITY) ACTIVE.remove(own.get(0));
        ACTIVE.add(new ReactionTextParticle(target, reaction, first, second, cfg.reactionTextLifeTime));
    }

    public static void tick() {
        if (Minecraft.getInstance().level == null) {
            ACTIVE.clear();
            return;
        }
        ACTIVE.removeIf(ReactionTextParticle::tick);
        for (int i = 0; i < ACTIVE.size(); i++) {
            ReactionTextParticle p = ACTIVE.get(i);
            int newer = 0;
            for (int j = i + 1; j < ACTIVE.size(); j++) if (ACTIVE.get(j).target() == p.target()) newer++;
            p.setSlotTarget(newer);
        }
    }

    public static void render(PoseStack ps, MultiBufferSource buf, Camera cam, float partialTick) {
        for (ReactionTextParticle p : ACTIVE) p.render(ps, buf, cam, partialTick);
    }
}
