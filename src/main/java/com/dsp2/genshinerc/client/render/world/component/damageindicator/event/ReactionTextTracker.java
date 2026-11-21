package com.dsp2.genshinerc.client.render.world.component.damageindicator.event;

import com.dsp2.genshinerc.client.render.vfx.aura.elevfx.ElementReactionDim;
import com.dsp2.genshinerc.client.render.world.component.damageindicator.config.DamageIndicatorConfig;
import com.dsp2.genshinerc.common.core.element.ElementType;
import com.dsp2.genshinerc.common.core.reaction.Reaction;
import com.dsp2.genshinerc.common.core.reaction.ReactionColors;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class ReactionTextTracker {
    private static final Map<Integer, Long> LAST_FREEZE_TEXT = new HashMap<>();
    private static final Map<String, Long> LAST_REACTION_TEXT = new HashMap<>();
    private ReactionTextTracker() {}

    public static void add(int entityId, Reaction reaction, @Nullable ElementType first, @Nullable ElementType second) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;
        if (!(mc.level.getEntity(entityId) instanceof LivingEntity target)) return;

        if (reaction != Reaction.FREEZE) {
            long now = System.nanoTime();
            LAST_REACTION_TEXT.entrySet().removeIf(entry -> now - entry.getValue() > 2_000_000_000L);
            String key = entityId + ":" + reaction.name();
            Long previous = LAST_REACTION_TEXT.get(key);
            if (previous != null && now - previous < 250_000_000L) return;
            LAST_REACTION_TEXT.put(key, now);
        }

        ElementReactionDim.mark(entityId);

        if (reaction == Reaction.FREEZE) {
            long now = System.nanoTime();
            LAST_FREEZE_TEXT.entrySet().removeIf(entry -> now - entry.getValue() > 2_000_000_000L);
            Long previous = LAST_FREEZE_TEXT.get(entityId);
            if (previous != null && now - previous < 750_000_000L) return;
            LAST_FREEZE_TEXT.put(entityId, now);
        }

        String name = Component.translatable("genshinerc.reaction." + reaction.name().toLowerCase(Locale.ROOT)).getString();
        
        ReactionColors.Ends e = reaction == Reaction.FREEZE
                ? new ReactionColors.Ends(
                        com.dsp2.genshinerc.client.render.world.component.damageindicator.config.DamageIndicatorConfig.get().colorHydro,
                        com.dsp2.genshinerc.client.render.world.component.damageindicator.config.DamageIndicatorConfig.get().colorCryo)
                : ReactionColors.ends(reaction, first, second);
        
        DamageIndicatorConfig cfg = DamageIndicatorConfig.get();
        if (!cfg.reactionTextEnabled) return;
        float scale = (float) (cfg.normalSize * cfg.reactionTextScale);
        
        DamageTracker.addCustomText(target, name, e.a(), e.b(), scale);
    }

    public static void tick() {}
    public static void render(PoseStack ps, MultiBufferSource buf, Camera cam, float partialTick) {}
}