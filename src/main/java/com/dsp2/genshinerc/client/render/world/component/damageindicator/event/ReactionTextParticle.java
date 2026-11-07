package com.dsp2.genshinerc.client.render.world.component.damageindicator.event;

import com.dsp2.genshinerc.client.render.world.component.damageindicator.config.DamageIndicatorConfig;
import com.dsp2.genshinerc.common.core.damage.DamageKind;
import com.dsp2.genshinerc.common.core.element.ElementType;
import com.dsp2.genshinerc.common.core.reaction.Reaction;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

final class ReactionTextParticle {
    private static final float POP_TICKS = 3f;
    private static final float FADE_START = 0.6f;
    private static final float SLOT_SMOOTH = 0.35f;

    private final LivingEntity target;
    private final Reaction reaction;
    @Nullable private final ElementType first;
    @Nullable private final ElementType second;
    private final int maxAge;
    private final String name;
    private int age;
    private float slot;
    private float slotTarget;

    ReactionTextParticle(LivingEntity target, Reaction reaction, @Nullable ElementType first,
                         @Nullable ElementType second, int maxAge) {
        this.target = target;
        this.reaction = reaction;
        this.first = first != null ? first : second;
        this.second = first != null ? second : null;
        this.maxAge = Math.max(1, maxAge);
        this.name = Component.translatable("genshinerc.reaction." + reaction.name().toLowerCase(Locale.ROOT)).getString();
    }

    LivingEntity target() { return target; }

    boolean matches(LivingEntity t, Reaction r, @Nullable ElementType a, @Nullable ElementType b) {
        return target == t && reaction == r && first == (a != null ? a : b) && second == (a != null ? b : null);
    }

    void refresh() { age = 0; }

    void setSlotTarget(int s) { slotTarget = s; }

    boolean tick() {
        if (target.isRemoved()) return true;
        slot += (slotTarget - slot) * SLOT_SMOOTH;
        return ++age >= maxAge;
    }

    void render(PoseStack ps, MultiBufferSource buf, Camera cam, float partialTick) {
        renderNow(ps, buf, cam, partialTick);
    }

    private static int rgb(DamageIndicatorConfig cfg, @Nullable ElementType type, String fallbackKey) {
        try {
            return Long.decode(cfg.colorFor(type == null ? fallbackKey : type.colorKey())).intValue() & 0xFFFFFF;
        } catch (NumberFormatException ex) {
            return 0xFFFFFF;
        }
    }

    private static int mix(float t, int a, int b) { return Math.round(Mth.lerp(t, a, b)); }

    private void renderNow(PoseStack ps, MultiBufferSource buf, Camera cam, float partialTick) {
        DamageIndicatorConfig cfg = DamageIndicatorConfig.get();
        float renderAge = Math.min(maxAge, age + partialTick);
        float life = renderAge / maxAge;
        float alpha = life < FADE_START ? 1f : Math.max(0f, 1f - (life - FADE_START) / (1f - FADE_START));
        int a = (int) (alpha * 255f);
        if (a <= 3) return;

        String fallback = reaction.indicatorKey(DamageKind.PHYSICAL);
        int c1 = rgb(cfg, first, fallback);
        int c2 = second == null ? c1 : rgb(cfg, second, fallback);

        double x = Mth.lerp(partialTick, target.xo, target.getX()) - cam.getPosition().x;
        double y = Mth.lerp(partialTick, target.yo, target.getY()) + target.getBbHeight()
                + cfg.reactionTextOffsetY + cfg.reactionTextRise * life + slot * cfg.reactionTextSpacing
                - cam.getPosition().y;
        double z = Mth.lerp(partialTick, target.zo, target.getZ()) - cam.getPosition().z;

        float pop = Math.min(1f, renderAge / POP_TICKS);
        float s = 0.03f * (float) (cfg.normalSize * cfg.reactionTextScale) * pop;
        if (s <= 0f) return;

        ps.pushPose();
        ps.translate(x, y, z);
        ps.mulPose(cam.rotation());
        ps.translate(0f, 0f, (float) cfg.zOffset);
        ps.scale(-s, -s, s);

        Font font = Minecraft.getInstance().font;
        int[] cps = name.codePoints().toArray();
        Component[] glyphs = new Component[cps.length];
        float total = 0f;
        float[] widths = new float[cps.length];
        for (int i = 0; i < cps.length; i++) {
            glyphs[i] = Component.literal(new String(Character.toChars(cps[i])));
            widths[i] = font.width(glyphs[i]);
            total += widths[i];
        }
        float px = -total / 2f;
        for (int i = 0; i < cps.length; i++) {
            float t = cps.length <= 1 ? 0f : i / (float) (cps.length - 1);
            int color = (a << 24)
                    | (mix(t, c1 >> 16 & 255, c2 >> 16 & 255) << 16)
                    | (mix(t, c1 >> 8 & 255, c2 >> 8 & 255) << 8)
                    | mix(t, c1 & 255, c2 & 255);
            font.drawInBatch(glyphs[i], px, 0f, color, cfg.drawShadow,
                    ps.last().pose(), buf, Font.DisplayMode.NORMAL, 0, 15728880);
            px += widths[i];
        }
        ps.popPose();
    }
}
