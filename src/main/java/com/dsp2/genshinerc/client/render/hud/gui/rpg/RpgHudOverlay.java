package com.dsp2.genshinerc.client.render.hud.gui.rpg;

import com.dsp2.genshinerc.client.render.hud.HudDraw;
import com.dsp2.genshinerc.client.render.hud.bar.ClientCombatBars;
import com.dsp2.genshinerc.client.render.hud.bar.config.BarConfig;
import com.dsp2.genshinerc.client.render.hud.bar.shield.ClientShieldState;
import com.dsp2.genshinerc.client.render.hud.bar.zhenmie.ClientZhenMieState;
import com.dsp2.genshinerc.client.render.hud.gui.rpg.RpgHudElement.Box;
import com.dsp2.genshinerc.client.render.hud.gui.rpg.anim.BarTrack;
import com.dsp2.genshinerc.client.render.hud.gui.rpg.anim.OxygenAnim;
import com.dsp2.genshinerc.client.render.hud.gui.rpg.anim.TextAnim;
import com.dsp2.genshinerc.client.render.hud.gui.rpg.editor.RpgHudEditorScreen;
import com.dsp2.genshinerc.client.render.hud.gui.rpg.render.DiamondModelView;
import com.dsp2.genshinerc.client.render.hud.gui.rpg.render.GuiQuad;
import com.dsp2.genshinerc.client.render.hud.gui.rpg.render.GuiStencil;
import com.dsp2.genshinerc.client.render.hud.gui.rpg.render.RpgTextures;
import com.dsp2.genshinerc.common.core.combat.zhenmie.ClashResult;
import com.dsp2.genshinerc.common.level.EntityLevelManager;
import com.dsp2.genshinerc.common.level.PlayerHudSync;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import org.jetbrains.annotations.Nullable;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

import java.util.Locale;

@SuppressWarnings({"deprecation", "removal"})
public final class RpgHudOverlay {
    private static ResourceLocation tex(String name) { return new ResourceLocation("genshinerc", "textures/hud/rpg/" + name + ".png"); }

    static final ResourceLocation BAR_HEALTH = tex("rpg_bar_3"), BAR_STAMINA = tex("rpg_bar_2"),
            BAR_ABSORB = tex("rpg_bar_1"), BAR_MOUNT = tex("rpg_bar_1"), BAR_OXYGEN = tex("rpg_bar_2");
    private static final int BAR_BG_RGB = 0x1E1E1E;
    private static final float BAR_BG_ALPHA = 0.75f;

    static final ResourceLocation EXP_TRACK = tex("rpg_exp"), EXP_FILL = tex("rpg_exps");
    static final ResourceLocation ZM_BAR = tex("zm_bar"), ZM_BORDER = tex("zm_bar_border"),
            ZM_LOGO = tex("zm_logo"), ZM_TRIANGLE = tex("zm_triangle"), STAMINA_LOGO = tex("stamina_logo");
    private static final ResourceLocation ICONS = new ResourceLocation("minecraft", "textures/gui/icons.png");

    public static final IGuiOverlay OVERLAY = RpgHudOverlay::render;

    private static final int WHITE = 0xFFFFFF, ABS_RGB = 0xFFB040, PREVIEW_RGB = 0x7CFF6A, HATCH_RGB = 0x1A1A1A;
    private static final int STACK_ROWS = 5;
    private static final float TOUGHNESS_DARKEN = 0.6f, DEATH_SHAKE_BOOST = 1.6f, ROW_FADE_S = 0.2f;
    private static final float CLASH_ALPHA = 0.85f, CRUSH_FLICKER_HZ = 10f;
    private static final long CRUSH_HUE_MS = 600L, KNOCKDOWN_SWAP_MS = 90L;

    private static final BarTrack B_HEALTH = new BarTrack(), B_STAMINA = new BarTrack(),
            B_SHIELD = new BarTrack(), B_MOUNT = new BarTrack();
    private static final OxygenAnim OXYGEN = new OxygenAnim();
    private static final TextAnim T_HP = new TextAnim(), T_ABS = new TextAnim(), T_FOOD = new TextAnim(), T_SAT = new TextAnim(),
            T_ARMOR = new TextAnim(), T_TOUGH = new TextAnim(), T_MOUNT = new TextAnim(), T_AIR = new TextAnim(),
            T_XP = new TextAnim(), T_EXP_FILL = new TextAnim();
    private static final TextAnim[] TEXTS = {T_HP, T_ABS, T_FOOD, T_SAT, T_ARMOR, T_TOUGH, T_MOUNT, T_AIR, T_XP, T_EXP_FILL};
    private static final Fade F_MOUNT = new Fade(), F_AIR = new Fade();

    @Nullable private static LocalPlayer lastPlayer;
    private static long deathStart = -1L;
    private static int lastXpLevel = -1, mountId = -1;
    private static float mountMax = 1f;

    private RpgHudOverlay() {}

    public static float originX(BarConfig cfg) { return cfg.rpgHudOffsetX; }
    public static float originY(BarConfig cfg, int screenHeight) { return screenHeight - cfg.rpgHudOffsetY; }

    public static void render(ForgeGui gui, GuiGraphics g, float partialTick, int sw, int sh) {
        if (Minecraft.getInstance().screen instanceof RpgHudEditorScreen) return;
        draw(g, sh, false);
    }

    public static void draw(GuiGraphics g, int sh, boolean preview) {
        Minecraft mc = Minecraft.getInstance();
        BarConfig cfg = BarConfig.get();
        LocalPlayer p = mc.player;
        if (p == null) return;
        if (!preview && (!cfg.rpgHudEnabled || mc.options.hideGui || p.isSpectator() || p.isCreative())) return;
        long now = Util.getMillis();
        if (p != lastPlayer) { resetAll(); lastPlayer = p; }
        float alpha = preview ? 1f : deathAlpha(p, now, cfg);
        if (alpha <= 0.01f) return;

        Snap s = Snap.read(p);
        tick(s, now, cfg);

        PoseStack pose = g.pose();
        pose.pushPose();
        pose.translate(originX(cfg), originY(cfg, sh), 0f);
        pose.scale(cfg.rpgHudScale, cfg.rpgHudScale, 1f);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        try {
            drawPortrait(g, p, RpgHudElement.PORTRAIT.get(cfg), cfg, alpha);
            drawLevelText(g, s, RpgHudElement.LEVEL_TEXT.get(cfg), now, cfg, alpha);

            float shake = B_HEALTH.shakeOffset(now, cfg) * cfg.rpgShakePixels * (p.isDeadOrDying() ? DEATH_SHAKE_BOOST : 1f);
            pose.pushPose();
            pose.translate(shake, 0f, 0f);
            drawVitals(g, s, RpgHudElement.HEALTH.get(cfg), RpgHudElement.STAMINA.get(cfg), now, cfg, alpha);
            drawShield(g, RpgHudElement.SHIELD.get(cfg), cfg, now, alpha, preview);
            pose.popPose();

            drawZhenMie(g, p, RpgHudElement.ZHEN_MIE.get(cfg), RpgHudElement.ZHEN_MIE_BORDER.get(cfg), now, cfg, alpha);
            drawStack(g, s, RpgHudElement.STACK.get(cfg), now, cfg, alpha, preview);
        } finally {
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
            pose.popPose();
        }
    }

    private static void resetAll() {
        B_HEALTH.reset(); B_STAMINA.reset(); B_SHIELD.reset(); B_MOUNT.reset();
        for (TextAnim t : TEXTS) t.reset();
        F_MOUNT.v = 0f; F_AIR.v = 0f;
        deathStart = -1L; lastXpLevel = -1; mountId = -1;
    }

    private static float deathAlpha(Player p, long now, BarConfig cfg) {
        if (!p.isDeadOrDying()) { deathStart = -1L; return 1f; }
        if (deathStart < 0L) deathStart = now;
        float el = (now - deathStart) / 1000f - cfg.shakeDuration;
        if (el <= 0f) return 1f;
        return cfg.deathFadeOutTime <= 0f ? 0f : Mth.clamp(1f - el / cfg.deathFadeOutTime, 0f, 1f);
    }

    private static void tick(Snap s, long now, BarConfig cfg) {
        B_HEALTH.update(now, s.hp, s.abs, s.maxHp, cfg, true, true);
        B_STAMINA.update(now, s.sta, 0f, Math.max(1f, s.maxSta), cfg, false, false);
        B_SHIELD.update(now, s.shield, 0f, Math.max(s.maxHp, s.shieldInit), cfg, false, false);

        if (s.mount != null) {
            if (s.mount.getId() != mountId) { B_MOUNT.reset(); T_MOUNT.reset(); mountId = s.mount.getId(); }
            mountMax = s.mount.getMaxHealth();
            B_MOUNT.update(now, s.mount.getHealth(), 0f, mountMax, cfg, false, false);
            T_MOUNT.set(s.mount.getHealth(), now);
        }
        F_MOUNT.update(now, s.mount != null, ROW_FADE_S);

        float air = s.maxAir > 0 ? Math.max(0, s.air) / (float) s.maxAir : 1f;
        OXYGEN.update(now, air, cfg);
        F_AIR.update(now, OXYGEN.visible(s.underwater), ROW_FADE_S);

        T_HP.set(s.hp, now); T_ABS.set(s.abs, now);
        T_FOOD.set(s.food, now); T_SAT.set(s.sat, now);
        T_ARMOR.set(s.armor, now); T_TOUGH.set(s.toughness, now);
        T_AIR.set(Math.round(air * 100f), now);
        T_XP.set(s.xpPoints, now);
        if (s.xpLevel != lastXpLevel) { T_EXP_FILL.snap(s.xpProgress); lastXpLevel = s.xpLevel; }
        else T_EXP_FILL.set(s.xpProgress, now);
        for (TextAnim t : TEXTS) t.update(now, cfg.rpgCountSpeed);
    }

    private static void drawPortrait(GuiGraphics g, LocalPlayer p, Box b, BarConfig cfg, float a) {
        float size = Math.min(b.w(), b.h()), x = b.cx() - size / 2f, y = b.cy() - size / 2f;
        if (a < 1f) RenderSystem.setShaderColor(1f, 1f, 1f, a);
        DiamondModelView.render(g, p, x, y, size, cfg);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        GuiQuad.blit(g, EXP_TRACK, x, y, x + size, y + size, 0f, 0f, 1f, 1f, argb(a, WHITE));
        RpgTextures.Info fill = RpgTextures.info(EXP_FILL);
        float f = Mth.clamp(T_EXP_FILL.shown(), 0f, 1f);
        if (f > 0.001f) {
            float vCut = Mth.lerp(f, fill.vBottom(), fill.vTop());
            GuiQuad.blit(g, fill.tint(), x, y + size * vCut, x + size, y + size, 0f, vCut, 1f, 1f,
                    argb(a, cfg.rpgExpColor.getRGB() & 0xFFFFFF));
        }
    }

    private static void drawLevelText(GuiGraphics g, Snap s, Box b, long now, BarConfig cfg, float a) {
        float scale = b.h() / 18f;
        text(g, Component.literal("Ex." + s.xpLevel + " / Lv." + s.level), b.cx(), b.y() + b.h() * 0.27f, scale, WHITE, a, true);
        text(g, Component.literal("EXP:" + Math.round(T_XP.shown()) + " / " + s.xpNeed), b.cx(), b.y() + b.h() * 0.77f,
                scale * 0.9f * pop(T_XP, now, cfg), 0xD8FFD8, a, true);
    }

    private static void drawVitals(GuiGraphics g, Snap s, Box hp, Box sta, long now, BarConfig cfg, float a) {
        drawBar(g, BAR_HEALTH, hp, B_HEALTH, true, now, cfg, a);

        boolean hasSta = s.maxSta > 0f;
        if (hasSta) {
            drawBar(g, BAR_STAMINA, sta, B_STAMINA, false, now, cfg, a);
            float ls = Math.max(6f, sta.h() * 2f);
            GuiQuad.blit(g, STAMINA_LOGO, sta.x() - ls * 0.7f, sta.cy() - ls / 2f, sta.x() + ls * 0.3f, sta.cy() + ls / 2f,
                    0f, 0f, 1f, 1f, argb(a, WHITE));
        }

        float top = hp.y(), bottom = hp.y2();
        if (hasSta && sta.y() > top && sta.y() < bottom && sta.x() < hp.x2() && sta.x2() > hp.x()) bottom = sta.y();

        MutableComponent t = Component.literal(oneDecimal(T_HP.shown()) + " / " + oneDecimal(s.maxHp));
        if (T_ABS.shown() >= 0.05f)
            t.append(Component.literal(" +" + oneDecimal(T_ABS.shown())).withStyle(st -> st.withColor(ABS_RGB)));

        float scale = Mth.clamp((bottom - top) / 8f * 0.8f, 0.35f, 1.5f);
        text(g, t, hp.cx(), (top + bottom) / 2f, scale * pop(T_HP, now, cfg), WHITE, a, true);
    }

    private static void drawBar(GuiGraphics g, ResourceLocation fillTex, Box b, BarTrack t, boolean absorption,
                                long now, BarConfig cfg, float a) {
        Crop c = new Crop(fillTex);
        c.full(g, c.i.tint(), b, argb(a * BAR_BG_ALPHA, BAR_BG_RGB));
        float main = t.main(), extra = t.extra();
        c.seg(g, fillTex, b, 0f, main, argb(a, WHITE));
        if (absorption && extra > main + 0.001f) {
            Crop abs = new Crop(BAR_ABSORB);
            abs.seg(g, BAR_ABSORB, b, main, extra, argb(a, WHITE));
        }
        t.forEachWhite((s0, s1, wa) -> c.seg(g, c.i.white(), b, s0, s1, argb(a * wa, WHITE)));
        float sa = t.smallAlpha(now, cfg);
        if (sa > 0.01f) c.seg(g, c.i.white(), b, 0f, Math.max(main, extra), argb(a * sa, WHITE));
    }

    private static void drawShield(GuiGraphics g, Box b, BarConfig cfg, long now, float a, boolean preview) {
        if (!preview && B_SHIELD.main() <= 0.001f && !B_SHIELD.busy()) return;
        int c = cfg.shieldSpecialColor.getRGB() & 0xFFFFFF;
        rect(g, b.x(), b.y(), b.x2(), b.y2(), argb(a * cfg.shieldBgAlpha, HudDraw.scaleRgb(c, cfg.shieldBgBrightness)));
        float x0 = b.x(), span = b.w();
        rect(g, x0, b.y(), x0 + span * B_SHIELD.main(), b.y2(), argb(a, c));
        B_SHIELD.forEachWhite((s0, s1, wa) -> rect(g, x0 + span * s0, b.y(), x0 + span * s1, b.y2(), argb(a * wa, WHITE)));
        float sa = B_SHIELD.smallAlpha(now, cfg);
        if (sa > 0.01f) rect(g, x0, b.y(), x0 + span * B_SHIELD.main(), b.y2(), argb(a * sa, WHITE));
    }

    private static void drawZhenMie(GuiGraphics g, LocalPlayer p, Box b, Box border, long now, BarConfig cfg, float a) {
        ClientZhenMieState z = ClientCombatBars.zhenMie(p.getId());
        if (z != null) z.tick(now, cfg);

        float ls = b.h();
        Box bar = new Box(b.x() + ls + 1f, b.y(), Math.max(1f, b.w() - ls - 1f), b.h());
        Crop c = new Crop(ZM_BAR);
        c.full(g, ZM_BAR, bar, argb(a, WHITE));

        if (z != null) {
            int zhenBase = cfg.zhenColor.getRGB() & 0xFFFFFF, mieBase = cfg.mieColor.getRGB() & 0xFFFFFF;
            int zc = z.zhenBuff() ? cycle(zhenBase, cfg.zhenBuffColor.getRGB() & 0xFFFFFF, cfg.zhenMieBuffCycleHz, now) : zhenBase;
            int mc = z.mieBuff() ? cycle(mieBase, cfg.mieBuffColor.getRGB() & 0xFFFFFF, cfg.zhenMieBuffCycleHz, now) : mieBase;
            float zm = Mth.clamp(z.zhen(), 0f, 1f), zt = Mth.clamp(z.zhenTrail(), 0f, 1f);
            float mm = Mth.clamp(z.mie(), 0f, 1f), mt = Mth.clamp(z.mieTrail(), 0f, 1f);

            if (zt > zm) c.seg(g, c.i.white(), bar, zm, zt, argb(a, WHITE));
            c.seg(g, c.i.tint(), bar, 0f, zm, argb(a, HudDraw.lerpRgb(zc, WHITE, z.zhenWhite(now, cfg))));
            if (mt > mm) c.seg(g, c.i.white(), bar, 1f - mt, 1f - mm, argb(a, WHITE));
            c.seg(g, c.i.tint(), bar, 1f - mm, 1f, argb(a, HudDraw.lerpRgb(mc, WHITE, z.mieWhite(now, cfg))));

            ClashResult clash = z.clash();
            if (clash != null) {
                float t = (now - z.clashStartMs()) / (clash.visualSeconds() * 1000f), fade = 1f - Mth.clamp(t, 0f, 1f), ca = 1f;
                int color = switch (clash) {
                    case CRUSH -> {
                        ca = 0.5f + 0.5f * Mth.cos(now / 1000f * CRUSH_FLICKER_HZ * Mth.TWO_PI);
                        yield Mth.hsvToRgb((now % CRUSH_HUE_MS) / (float) CRUSH_HUE_MS, 0.85f, 1f);
                    }
                    case KNOCKDOWN -> (now / KNOCKDOWN_SWAP_MS) % 2 == 0 ? zhenBase : mieBase;
                    default -> WHITE;
                };
                c.seg(g, c.i.white(), bar, 0f, 1f, argb(a * fade * ca * CLASH_ALPHA, color));
                float mx = c.x(bar, c.i.u(z.clashPoint())), ts = bar.h() * 0.8f, ty = bar.y() - ts * 0.6f;
                GuiQuad.blit(g, ZM_TRIANGLE, mx - ts / 2f, ty, mx + ts / 2f, ty + ts, 0f, 0f, 1f, 1f, argb(a * fade, WHITE));
            }
        }

        nineSlice(g, ZM_BORDER, border, cfg.rpgZmBorderSlice, cfg.rpgZmBorderCornerPx, argb(a, WHITE));
        GuiQuad.blit(g, ZM_LOGO, b.x(), b.y(), b.x() + ls, b.y() + ls, 0f, 0f, 1f, 1f, argb(a, WHITE));
    }

    private static void nineSlice(GuiGraphics g, ResourceLocation tex, Box b, int slicePx, float cornerPx, int argb) {
        RpgTextures.Info i = RpgTextures.info(tex);
        float tw = i.w(), th = i.h();
        float u0 = i.ax0() / tw, u3 = i.ax1() / tw, v0 = i.vTop(), v3 = i.vBottom();
        float su = Math.min(slicePx / tw, (u3 - u0) / 2f), sv = Math.min(slicePx / th, (v3 - v0) / 2f);
        float cs = Math.max(0f, Math.min(cornerPx, Math.min(b.w(), b.h()) / 2f));

        float[] xs = {b.x(), b.x() + cs, b.x2() - cs, b.x2()};
        float[] ys = {b.y(), b.y() + cs, b.y2() - cs, b.y2()};
        float[] us = {u0, u0 + su, u3 - su, u3};
        float[] vs = {v0, v0 + sv, v3 - sv, v3};
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                if (xs[col + 1] - xs[col] <= 0.001f || ys[row + 1] - ys[row] <= 0.001f) continue;
                GuiQuad.blit(g, tex, xs[col], ys[row], xs[col + 1], ys[row + 1],
                        us[col], vs[row], us[col + 1], vs[row + 1], argb);
            }
        }
    }

    private static void drawStack(GuiGraphics g, Snap s, Box b, long now, BarConfig cfg, float a, boolean preview) {
        float rowH = b.h() / STACK_ROWS, ic = rowH * 0.85f, ts = rowH / 12f, tx = b.x() + ic + 2f;

        float y = rowY(b, rowH, 0);
        drawHunger(g, s, b.x(), y, ic, rowH, ts, now, cfg, a);

        y = rowY(b, rowH, 1);
        float ab = layerBrightness(s.armor, cfg);
        icon(g, 34, 9, b.x(), y + (rowH - ic) / 2f, ic, ab, ab, ab, a);
        text(g, Component.literal(String.valueOf(Math.round(T_ARMOR.shown()))), tx, y + rowH / 2f, ts * pop(T_ARMOR, now, cfg), WHITE, a, false);

        y = rowY(b, rowH, 2);
        float tb = layerBrightness(s.toughness, cfg) * TOUGHNESS_DARKEN;
        icon(g, 34, 9, b.x(), y + (rowH - ic) / 2f, ic, tb, tb, tb, a);
        text(g, Component.literal(String.valueOf(Math.round(T_TOUGH.shown()))), tx, y + rowH / 2f, ts * pop(T_TOUGH, now, cfg), WHITE, a, false);

        y = rowY(b, rowH, 3);
        float ma = preview ? a : a * F_MOUNT.v;
        if (ma > 0.01f) {
            float iy = y + (rowH - ic) / 2f;
            icon(g, 16, 0, b.x(), iy, ic, 1f, 1f, 1f, ma);
            icon(g, 160, 0, b.x(), iy, ic, 1f, 1f, 1f, ma);
            Box bar = miniBar(b, tx, y, rowH);
            drawBar(g, BAR_MOUNT, bar, B_MOUNT, false, now, cfg, ma);
            text(g, Component.literal(ceil(T_MOUNT.shown()) + "/" + ceil(mountMax)), bar.cx(), bar.cy(),
                    ts * 0.8f * pop(T_MOUNT, now, cfg), WHITE, ma, true);
        }

        y = rowY(b, rowH, 4);
        float oa = preview ? a : a * F_AIR.v;
        if (oa > 0.01f) {
            icon(g, 16, 18, b.x(), y + (rowH - ic) / 2f, ic, 1f, 1f, 1f, oa);
            Box bar = miniBar(b, tx, y, rowH);
            Crop c = new Crop(BAR_OXYGEN);
            c.full(g, c.i.tint(), bar, argb(oa * BAR_BG_ALPHA, BAR_BG_RGB));
            c.seg(g, BAR_OXYGEN, bar, OXYGEN.bar(), OXYGEN.gainTop(), argb(oa * 0.35f, WHITE));
            c.seg(g, BAR_OXYGEN, bar, 0f, OXYGEN.bar(), argb(oa, WHITE));
            if (OXYGEN.ghostAlpha() > 0.01f)
                c.seg(g, c.i.white(), bar, OXYGEN.bar(), OXYGEN.ghostTop(), argb(oa * OXYGEN.ghostAlpha(), WHITE));
            text(g, Component.literal(Math.round(T_AIR.shown()) + "/100"), bar.cx(), bar.cy(), ts * 0.8f, WHITE, oa, true);
        }
    }

    private static void drawHunger(GuiGraphics g, Snap s, float x, float rowY, float ic, float rowH, float ts,
                                   long now, BarConfig cfg, float a) {
        float iy = rowY + (rowH - ic) / 2f;
        icon(g, 16, 27, x, iy, ic, 1f, 1f, 1f, a);
        icon(g, 52, 27, x, iy, ic, 1f, 1f, 1f, a);

        float ex = Mth.clamp(s.exhaustion / 4f, 0f, 1f);
        if (ex > 0.02f && cfg.rpgExhaustionAlpha > 0f && GuiStencil.ensureEnabled()) {
            int h = Math.max(1, Math.round(9 * ex));
            PoseStack ps = g.pose();
            ps.pushPose();
            ps.translate(x, iy, 0f);
            ps.scale(ic / 9f, ic / 9f, 1f);
            GuiStencil.beginMask(g);
            g.blit(ICONS, 0, 9 - h, 52, 27 + 9 - h, 9, h);
            GuiStencil.useMask(g);
            float scroll = (now % 2000L) / 2000f, span = 9f / 16f;
            GuiQuad.blit(g, RpgTextures.hatch(), 0f, 0f, 9f, 9f, scroll, 0f, scroll + span, span, argb(a * cfg.rpgExhaustionAlpha, HATCH_RGB));
            GuiStencil.end(g);
            ps.popPose();
        }

        float tx = x + ic + 2f, cy = rowY + rowH / 2f;
        float sc = ts * Math.max(pop(T_FOOD, now, cfg), pop(T_SAT, now, cfg));
        Component current = Component.literal(oneDecimal(T_FOOD.shown()) + "+" + oneDecimal(T_SAT.shown()));
        if (s.held == null) {
            text(g, current, tx, cy, sc, WHITE, a, false);
            return;
        }
        int pf = Math.min(20, s.food + s.held.getNutrition());
        float psat = Math.min(pf, s.sat + s.held.getNutrition() * s.held.getSaturationModifier() * 2f);
        float pulse = 0.5f - 0.5f * Mth.cos(now / 1000f * cfg.rpgFoodPreviewHz * Mth.TWO_PI);
        text(g, current, tx, cy, sc, WHITE, a * (1f - pulse), false);
        text(g, Component.literal(oneDecimal(pf) + "+" + oneDecimal(psat)), tx, cy, sc, PREVIEW_RGB, a * pulse, false);
    }

    private static float rowY(Box b, float rowH, int index) { return b.y2() - (index + 1) * rowH; }

    private static Box miniBar(Box stack, float x, float rowY, float rowH) {
        return new Box(x, rowY + rowH * 0.2f, Math.max(4f, stack.x2() - x), rowH * 0.6f);
    }

    private static final class Crop {
        final RpgTextures.Info i;
        final float u0, u1, v0, v1;

        Crop(ResourceLocation fill) {
            i = RpgTextures.info(fill);
            u0 = i.ax0() / (float) i.w();
            u1 = i.ax1() / (float) i.w();
            v0 = i.vTop();
            v1 = i.vBottom();
        }

        float x(Box b, float u) { return b.x() + b.w() * (u - u0) / Math.max(1.0e-6f, u1 - u0); }

        void full(GuiGraphics g, ResourceLocation tex, Box b, int argb) {
            GuiQuad.blit(g, tex, b.x(), b.y(), b.x2(), b.y2(), u0, v0, u1, v1, argb);
        }

        void seg(GuiGraphics g, ResourceLocation tex, Box b, float s0, float s1, int argb) {
            if (s1 - s0 <= 0.001f) return;
            float a0 = i.u(s0), a1 = i.u(s1);
            GuiQuad.blit(g, tex, x(b, a0), b.y(), x(b, a1), b.y2(), a0, v0, a1, v1, argb);
        }
    }

    private static void rect(GuiGraphics g, float x0, float y0, float x1, float y1, int argb) {
        if (x1 - x0 <= 0.001f) return;
        GuiQuad.blit(g, RpgTextures.pixel(), x0, y0, x1, y1, 0f, 0f, 1f, 1f, argb);
    }

    private static void icon(GuiGraphics g, int u, int v, float x, float y, float size, float r, float gr, float b, float a) {
        RenderSystem.setShaderColor(r, gr, b, a);
        g.pose().pushPose();
        g.pose().translate(x, y, 0f);
        g.pose().scale(size / 9f, size / 9f, 1f);
        g.blit(ICONS, 0, 0, u, v, 9, 9);
        g.pose().popPose();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }

    private static void text(GuiGraphics g, Component c, float x, float cy, float scale, int rgb, float a, boolean center) {
        if (a * 255f < 5f) return;
        Font font = Minecraft.getInstance().font;
        PoseStack ps = g.pose();
        ps.pushPose();
        ps.translate(x, cy, 0f);
        ps.scale(scale, scale, 1f);
        float w = font.width(c);
        g.drawString(font, c.getVisualOrderText(), center ? -w / 2f : 0f, -4f, argb(a, rgb), true);
        ps.popPose();
    }

    private static float pop(TextAnim t, long now, BarConfig cfg) { return t.scale(now, cfg.rpgTextPopScale, cfg.rpgTextPopTime); }

    private static float layerBrightness(float value, BarConfig cfg) {
        int layers = Math.max(1, (int) Math.ceil(value / 20f));
        return Math.max(cfg.rpgLayerMinBrightness, 1f - cfg.rpgLayerDarken * (layers - 1));
    }

    private static int cycle(int a, int b, float hz, long now) {
        return HudDraw.lerpRgb(a, b, 0.5f - 0.5f * Mth.cos(now / 1000f * hz * Mth.TWO_PI));
    }

    private static int argb(float a, int rgb) { return (Mth.clamp(Math.round(a * 255f), 0, 255) << 24) | (rgb & 0xFFFFFF); }
    private static int ceil(float v) { return (int) Math.ceil(v - 0.001f); }
    private static String oneDecimal(float v) { return String.format(Locale.ROOT, "%.1f", v); }

    private static final class Fade {
        float v;
        long last;

        void update(long now, boolean on, float seconds) {
            float dt = last == 0L ? 0f : Math.min(0.1f, (now - last) / 1000f);
            last = now;
            float step = seconds <= 0f ? 1f : dt / seconds;
            v = Mth.clamp(v + (on ? step : -step), 0f, 1f);
        }
    }

    private record Snap(float hp, float maxHp, float abs, float sta, float maxSta, float shield, float shieldInit,
                        int armor, int toughness, int food, float sat, float exhaustion, @Nullable FoodProperties held,
                        int air, int maxAir, boolean underwater, @Nullable LivingEntity mount,
                        int xpLevel, float xpProgress, int xpNeed, int xpPoints, int level) {
        static Snap read(LocalPlayer p) {
            PlayerPatch<?> patch = EpicFightCapabilities.getEntityPatch(p, PlayerPatch.class);
            ClientShieldState st = ClientCombatBars.shield(p.getId());
            int need = p.getXpNeededForNextLevel();
            return new Snap(p.getHealth(), p.getMaxHealth(), p.getAbsorptionAmount(),
                    patch == null ? 0f : patch.getStamina(), patch == null ? 0f : patch.getMaxStamina(),
                    st == null ? 0f : st.amount(), st == null ? 0f : st.initial(),
                    p.getArmorValue(), (int) Math.round(p.getAttributeValue(Attributes.ARMOR_TOUGHNESS)),
                    p.getFoodData().getFoodLevel(), p.getFoodData().getSaturationLevel(), PlayerHudSync.clientExhaustion(),
                    heldFood(p), p.getAirSupply(), p.getMaxAirSupply(), p.isEyeInFluid(FluidTags.WATER),
                    p.getVehicle() instanceof LivingEntity m ? m : null,
                    p.experienceLevel, p.experienceProgress, need, Math.round(p.experienceProgress * need),
                    EntityLevelManager.getPlayerLevel(p));
        }

        @Nullable
        private static FoodProperties heldFood(Player p) {
            for (InteractionHand hand : InteractionHand.values()) {
                FoodProperties f = p.getItemInHand(hand).getFoodProperties(p);
                if (f != null) return f;
            }
            return null;
        }
    }
}