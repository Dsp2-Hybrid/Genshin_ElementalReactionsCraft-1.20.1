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

import java.util.Locale;

public final class ReactionTextTracker {
    private ReactionTextTracker() {}

    public static void add(int entityId, Reaction reaction, @Nullable ElementType first, @Nullable ElementType second) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;
        if (!(mc.level.getEntity(entityId) instanceof LivingEntity target)) return;

        ElementReactionDim.mark(entityId);

        String name = Component.translatable("genshinerc.reaction." + reaction.name().toLowerCase(Locale.ROOT)).getString();
        
        ReactionColors.Ends e = ReactionColors.ends(reaction, first, second);
        
        DamageIndicatorConfig cfg = DamageIndicatorConfig.get();
        float scale = (float) (cfg.normalSize * cfg.reactionTextScale);
        
        DamageTracker.addCustomText(target, name, e.a(), e.b(), scale);
    }

    public static void tick() {}
    public static void render(PoseStack ps, MultiBufferSource buf, Camera cam, float partialTick) {}
}