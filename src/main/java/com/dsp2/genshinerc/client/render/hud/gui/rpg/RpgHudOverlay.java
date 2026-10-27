package com.dsp2.genshinerc.client.render.hud.gui.rpg;

import com.dsp2.genshinerc.client.render.hud.HudDraw;
import com.dsp2.genshinerc.client.render.hud.bar.ClientCombatBars;
import com.dsp2.genshinerc.client.render.hud.bar.config.BarConfig;
import com.dsp2.genshinerc.client.render.hud.bar.shield.ClientShieldState;
import com.dsp2.genshinerc.client.render.hud.bar.zhenmie.ClientZhenMieState;
import com.dsp2.genshinerc.client.render.hud.gui.rpg.anim.BarTrack;
import com.dsp2.genshinerc.client.render.hud.gui.rpg.anim.OxygenAnim;
import com.dsp2.genshinerc.client.render.hud.gui.rpg.anim.TextAnim;
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

@SuppressWarnings({"deprecation", "removal"})
public final class RpgHudOverlay {
    private static ResourceLocation tex(String name) { return new ResourceLocation("genshinerc", "textures/hud/rpg/" + name + ".png"); }

    static final ResourceLocation BAR_TRACK = tex("rpg_bar"), BAR_ORANGE = tex("rpg_bar_1"),
            BAR_BLUE = tex("rpg_bar_2"), BAR_RED = tex("rpg_bar_3");
    static final ResourceLocation EXP_TRACK = tex("rpg_exp"), EXP_FILL = tex("rpg_exps");
    static final ResourceLocation ZM_BAR = tex("zm_bar"), ZM_BORDER = tex("zm_bar_border"),
            ZM_LOGO = tex("zm_logo"), ZM_TRIANGLE = tex("zm_triangle"), STAMINA_LOGO = tex("stamina_logo");
    private static final ResourceLocation ICONS = new ResourceLocation("minecraft", "textures/gui/icons.png");

    public static final IGuiOverlay OVERLAY = RpgHudOverlay::render;

    private static final class Geo {
        static final float PORTRAIT = 56f;
        static final float BAR_X = 48f, HP_W = 124f, HP_BOTTOM = -14f;
        static final float STA_X = 6f, STA_W = 104f, STA_COVER = 0.45f;
        static final float STA_LOGO = 10f;
        static final float SHIELD_H = 2.5f, SHIELD_GAP = 1f;
        static final float ZM_LOGO = 12f, ZM_W = 74f, ZM_GAP = 2f, TRIANGLE = 7f;
        static final float ROW_H = 10f, ROW_GAP = 3f, COL_W = 32f, MINI_BAR_W = 48f;
    }

    private static final int ABS_RGB = 0xFFB040, MOUNT_RGB = 0xFFD54A, PREVIEW_RGB = 0x7CFF6A, HATCH_RGB = 0x1A1A1A;
    private static final float TOUGHNESS_DARKEN = 0.6f, DEATH_SHAKE_BOOST = 1.6f, ROW_FADE_S = 0.2f;
    private static final float CLASH_ALPHA = 0.85f, CRUSH_FLICKER_HZ = 10f;
    private static final long CRUSH_HUE_MS = 600L, KNOCKDOWN_SWAP_MS = 90L;

    private static final BarTrack HEALTH = new BarTrack(), STAMINA = new BarTrack(), SHIELD = new BarTrack(), MOUNT = new BarTrack();
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

    public static void render(ForgeGui gui, GuiGraphics g, float partialTick, int sw, int sh) {
        Minecraft mc = Minecraft.getInstance();
        BarConfig cfg = BarConfig.get();
        LocalPlayer p = mc.player;
        if (!cfg.rpgHudEnabled || p == null || mc.options.hideGui || p.isSpectator()) return;

        long now = Util.getMillis();
        if (p != lastPlayer) { resetAll(); lastPlayer = p; }
        float alpha = deathAlpha(p, now, cfg);
        if (alpha <= 0.01f) return;

        Snap s = Snap.read(p);
        tick(s, now, cfg);

        RpgTextures.Info track = RpgTextures.info(BAR_TRACK);
        Rect hp = new Rect(Geo.BAR_X, Geo.HP_BOTTOM - Geo.HP_W * track.aspect(), Geo.HP_W, Geo.HP_W * track.aspect());
        float hpVisibleTop = hp.y + hp.h * track.vTop();
        float shieldTop = hpVisibleTop - Geo.SHIELD_GAP - Geo.SHIELD_H;

        PoseStack pose = g.pose();
        pose.pushPose();
        pose.translate(cfg.rpgHudOffsetX, sh - cfg.rpgHudOffsetY, 0f);
        pose.scale(cfg.rpgHudScale, cfg.rpgHudScale, 1f);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        try {
            drawPortrait(g, p, s, now, cfg, alpha);

            float shake = HEALTH.shakeOffset(now, cfg) * cfg.rpgShakePixels * (p.isDeadOrDying() ? DEATH_SHAKE_BOOST : 1f);
            pose.pushPose();
            pose.translate(shake, 0f, 0f);
            drawVitals(g, s, hp, track, now, cfg, alpha);
            drawShield(g, hp, track, shieldTop, cfg, now, alpha);
            pose.popPose();

            drawZhenMie(g, p, shieldTop - Geo.ZM_GAP, now, cfg, alpha);
            drawStack(g, s, hp.x2(), shieldTop - Geo.ROW_GAP, now, cfg, alpha);
        } finally {
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
            pose.popPose();
        }
    }

    private static void resetAll() {
        HEALTH.reset(); STAMINA.reset(); SHIELD.reset(); MOUNT.reset();
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
        HEALTH.update(now, s.hp, s.abs, s.maxHp, cfg, true, true);
        STAMINA.update(now, s.sta, 0f, Math.max(1f, s.maxSta), cfg, false, false);
        SHIELD.update(now, s.shield, 0f, Math.max(s.maxHp, s.shieldInit), cfg, false, false);

        if (s.mount != null) {
            if (s.mount.getId() != mountId) { MOUNT.reset(); T_MOUNT.reset(); mountId = s.mount.getId(); }
            mountMax = s.mount.getMaxHealth();
            MOUNT.update(now, s.mount.getHealth(), 0f, mountMax, cfg, false, false);
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

    private static void drawPortrait(GuiGraphics g, LocalPlayer p, Snap s, long now, BarConfig cfg, float a) {
        float size = Geo.PORTRAIT, x = 0f, y = -size;
        if (a < 1f) RenderSystem.setShaderColor(1f, 1f, 1f, a);
        DiamondModelView.render(g, p, x, y, size, cfg);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        GuiQuad.blit(g, EXP_TRACK, x, y, x + size, y + size, 0f, 0f, 1f, 1f, argb(a, 0xFFFFFF));
        RpgTextures.Info fill = RpgTextures.info(EXP_FILL);
        float f = Mth.clamp(T_EXP_FILL.shown(), 0f, 1f);
        if (f > 0.001f) {
            float vCut = Mth.lerp(f, fill.vBottom(), fill.vTop());
            GuiQuad.blit(g, fill.tint(), x, y + size * vCut, x + size, y + size, 0f, vCut, 1f, 1f,
                    argb(a, cfg.rpgExpColor.getRGB() & 0xFFFFFF));
        }

        text(g, Component.literal("Ex." + s.xpLevel + " / Lv." + s.level), x + size / 2f, y - 15f, 0.75f, 0xFFFFFF, a, true);
        text(g, Component.literal("EXP:" + Math.round(T_XP.shown()) + " / " + s.xpNeed), x + size / 2f, y - 7f,
                0.65f * pop(T_XP, now, cfg), 0xD8FFD8, a, true);
    }

    private static void drawVitals(GuiGraphics g, Snap s, Rect hp, RpgTextures.Info track, long now, BarConfig cfg, float a) {
        drawFillBar(g, BAR_RED, BAR_ORANGE, hp, HEALTH, now, cfg, a);

        Rect sta = null;
        if (s.maxSta > 0f) {
            float h = Geo.STA_W * track.aspect();
            sta = new Rect(hp.x + Geo.STA_X, hp.y + hp.h * (1f - Geo.STA_COVER), Geo.STA_W, h);
            drawFillBar(g, BAR_BLUE, null, sta, STAMINA, now, cfg, a);
            float ls = Geo.STA_LOGO;
            GuiQuad.blit(g, STAMINA_LOGO, sta.x - ls * 0.6f, sta.cy() - ls / 2f, sta.x + ls * 0.4f, sta.cy() + ls / 2f,
                    0f, 0f, 1f, 1f, argb(a, 0xFFFFFF));
        }

        MutableComponent t = Component.literal(ceil(T_HP.shown()) + " / " + ceil(s.maxHp));
        if (T_ABS.shown() >= 0.5f) t.append(Component.literal(" +" + ceil(T_ABS.shown())).withStyle(st -> st.withColor(ABS_RGB)));
        float top = hp.y + hp.h * track.vTop();
        float bottom = sta != null ? sta.y + sta.h * RpgTextures.info(BAR_TRACK).vTop() : hp.y + hp.h * track.vBottom();
        text(g, t, hp.x + hp.w / 2f, (top + bottom) / 2f, 0.7f * pop(T_HP, now, cfg), 0xFFFFFF, a, true);
    }

    private static void drawFillBar(GuiGraphics g, ResourceLocation fillTex, @Nullable ResourceLocation extraTex,
                                    Rect r, BarTrack t, long now, BarConfig cfg, float a) {
        GuiQuad.blit(g, BAR_TRACK, r.x, r.y, r.x2(), r.y2(), 0f, 0f, 1f, 1f, argb(a, 0xFFFFFF));
        RpgTextures.Info fi = RpgTextures.info(fillTex);
        float main = t.main(), extra = t.extra();
        seg(g, fillTex, fi, r, 0f, main, argb(a, 0xFFFFFF));
        if (extraTex != null && extra > main + 0.001f) seg(g, extraTex, RpgTextures.info(extraTex), r, main, extra, argb(a, 0xFFFFFF));
        t.forEachWhite((s0, s1, wa) -> seg(g, fi.white(), fi, r, s0, s1, argb(a * wa, 0xFFFFFF)));
        float sa = t.smallAlpha(now, cfg);
        if (sa > 0.01f) seg(g, fi.white(), fi, r, 0f, Math.max(main, extra), argb(a * sa, 0xFFFFFF));
    }

    private static void drawTintBar(GuiGraphics g, Rect r, BarTrack t, int rgb, long now, BarConfig cfg, float a) {
        GuiQuad.blit(g, BAR_TRACK, r.x, r.y, r.x2(), r.y2(), 0f, 0f, 1f, 1f, argb(a, 0xFFFFFF));
        RpgTextures.Info ti = RpgTextures.info(BAR_TRACK);
        seg(g, ti.tint(), ti, r, 0f, t.main(), argb(a, rgb));
        t.forEachWhite((s0, s1, wa) -> seg(g, ti.white(), ti, r, s0, s1, argb(a * wa, 0xFFFFFF)));
        float sa = t.smallAlpha(now, cfg);
        if (sa > 0.01f) seg(g, ti.white(), ti, r, 0f, t.main(), argb(a * sa, 0xFFFFFF));
    }

    private static void drawShield(GuiGraphics g, Rect hp, RpgTextures.Info track, float top, BarConfig cfg, long now, float a) {
        if (SHIELD.main() <= 0.001f && !SHIELD.busy()) return;
        float x0 = hp.x + hp.w * track.u(0f), x1 = hp.x + hp.w * track.u(1f), y1 = top + Geo.SHIELD_H;
        int c = cfg.shieldSpecialColor.getRGB() & 0xFFFFFF;
        rect(g, x0, top, x1, y1, argb(a * cfg.shieldBgAlpha, HudDraw.scaleRgb(c, cfg.shieldBgBrightness)));
        float span = x1 - x0;
        rect(g, x0, top, x0 + span * SHIELD.main(), y1, argb(a, c));
        SHIELD.forEachWhite((s0, s1, wa) -> rect(g, x0 + span * s0, top, x0 + span * s1, y1, argb(a * wa, 0xFFFFFF)));
        float sa = SHIELD.smallAlpha(now, cfg);
        if (sa > 0.01f) rect(g, x0, top, x0 + span * SHIELD.main(), y1, argb(a * sa, 0xFFFFFF));
    }

    private static void drawZhenMie(GuiGraphics g, LocalPlayer p, float bottom, long now, BarConfig cfg, float a) {
        ClientZhenMieState z = ClientCombatBars.zhenMie(p.getId());
        if (z == null) return;
        z.tick(now, cfg);
        if (z.idle(now, cfg)) return;

        RpgTextures.Info zi = RpgTextures.info(ZM_BAR);
        float h = Geo.ZM_W * zi.aspect();
        Rect r = new Rect(Geo.BAR_X + Geo.ZM_LOGO + 2f, bottom - h, Geo.ZM_W, h);
        GuiQuad.blit(g, ZM_BAR, r.x, r.y, r.x2(), r.y2(), 0f, 0f, 1f, 1f, argb(a, 0xFFFFFF));

        int zhenBase = cfg.zhenColor.getRGB() & 0xFFFFFF, mieBase = cfg.mieColor.getRGB() & 0xFFFFFF;
        int zc = z.zhenBuff() ? cycle(zhenBase, cfg.zhenBuffColor.getRGB() & 0xFFFFFF, cfg.zhenMieBuffCycleHz, now) : zhenBase;
        int mc = z.mieBuff() ? cycle(mieBase, cfg.mieBuffColor.getRGB() & 0xFFFFFF, cfg.zhenMieBuffCycleHz, now) : mieBase;
        float zm = Mth.clamp(z.zhen(), 0f, 1f), zt = Mth.clamp(z.zhenTrail(), 0f, 1f);
        float mm = Mth.clamp(z.mie(), 0f, 1f), mt = Mth.clamp(z.mieTrail(), 0f, 1f);

        if (zt > zm) seg(g, zi.white(), zi, r, zm, zt, argb(a, 0xFFFFFF));
        seg(g, zi.tint(), zi, r, 0f, zm, argb(a, HudDraw.lerpRgb(zc, 0xFFFFFF, z.zhenWhite(now, cfg))));
        if (mt > mm) seg(g, zi.white(), zi, r, 1f - mt, 1f - mm, argb(a, 0xFFFFFF));
        seg(g, zi.tint(), zi, r, 1f - mm, 1f, argb(a, HudDraw.lerpRgb(mc, 0xFFFFFF, z.mieWhite(now, cfg))));

        ClashResult clash = z.clash();
        if (clash != null) {
            float t = (now - z.clashStartMs()) / (clash.visualSeconds() * 1000f), fade = 1f - Mth.clamp(t, 0f, 1f), ca = 1f;
            int color = switch (clash) {
                case CRUSH -> {
                    ca = 0.5f + 0.5f * Mth.cos(now / 1000f * CRUSH_FLICKER_HZ * Mth.TWO_PI);
                    yield Mth.hsvToRgb((now % CRUSH_HUE_MS) / (float) CRUSH_HUE_MS, 0.85f, 1f);
                }
                case KNOCKDOWN -> (now / KNOCKDOWN_SWAP_MS) % 2 == 0 ? zhenBase : mieBase;
                default -> 0xFFFFFF;
            };
            seg(g, zi.white(), zi, r, 0f, 1f, argb(a * fade * ca * CLASH_ALPHA, color));
            float mx = r.x + r.w * zi.u(z.clashPoint()), ts = Geo.TRIANGLE, ty = r.y + r.h * zi.vTop() - ts;
            GuiQuad.blit(g, ZM_TRIANGLE, mx - ts / 2f, ty, mx + ts / 2f, ty + ts, 0f, 0f, 1f, 1f, argb(a * fade, 0xFFFFFF));
        }

        GuiQuad.blit(g, ZM_BORDER, r.x, r.y, r.x2(), r.y2(), 0f, 0f, 1f, 1f, argb(a, 0xFFFFFF));
        float ls = Geo.ZM_LOGO;
        GuiQuad.blit(g, ZM_LOGO, Geo.BAR_X, r.cy() - ls / 2f, Geo.BAR_X + ls, r.cy() + ls / 2f, 0f, 0f, 1f, 1f, argb(a, 0xFFFFFF));
    }

    private static void drawStack(GuiGraphics g, Snap s, float right, float bottom, long now, BarConfig cfg, float a) {
        float left = right - Geo.COL_W, y = bottom - Geo.ROW_H;

        drawHunger(g, s, left, y, now, cfg, a);
        y -= Geo.ROW_H;

        float ab = layerBrightness(s.armor, cfg);
        icon(g, 34, 9, left, y, ab, ab, ab, a);
        text(g, Component.literal(String.valueOf(Math.round(T_ARMOR.shown()))), left + 11f, y + 5f, 0.75f * pop(T_ARMOR, now, cfg), 0xFFFFFF, a, false);
        y -= Geo.ROW_H;

        float tb = layerBrightness(s.toughness, cfg) * TOUGHNESS_DARKEN;
        icon(g, 34, 9, left, y, tb, tb, tb, a);
        text(g, Component.literal(String.valueOf(Math.round(T_TOUGH.shown()))), left + 11f, y + 5f, 0.75f * pop(T_TOUGH, now, cfg), 0xFFFFFF, a, false);
        y -= Geo.ROW_H;

        if (F_MOUNT.v > 0.01f) {
            float ma = a * F_MOUNT.v;
            Rect r = miniBar(right, y);
            icon(g, 16, 0, r.x - 11f, y, 1f, 1f, 1f, ma);
            icon(g, 160, 0, r.x - 11f, y, 1f, 1f, 1f, ma);
            drawTintBar(g, r, MOUNT, MOUNT_RGB, now, cfg, ma);
            text(g, Component.literal(ceil(T_MOUNT.shown()) + "/" + ceil(mountMax)), r.x + r.w / 2f, r.cy(),
                    0.6f * pop(T_MOUNT, now, cfg), 0xFFFFFF, ma, true);
            y -= Geo.ROW_H;
        }

        if (F_AIR.v > 0.01f) {
            float oa = a * F_AIR.v;
            Rect r = miniBar(right, y);
            icon(g, 16, 18, r.x - 11f, y, 1f, 1f, 1f, oa);
            GuiQuad.blit(g, BAR_TRACK, r.x, r.y, r.x2(), r.y2(), 0f, 0f, 1f, 1f, argb(oa, 0xFFFFFF));
            RpgTextures.Info bi = RpgTextures.info(BAR_BLUE);
            seg(g, BAR_BLUE, bi, r, OXYGEN.bar(), OXYGEN.gainTop(), argb(oa * 0.35f, 0xFFFFFF));
            seg(g, BAR_BLUE, bi, r, 0f, OXYGEN.bar(), argb(oa, 0xFFFFFF));
            if (OXYGEN.ghostAlpha() > 0.01f) seg(g, bi.white(), bi, r, OXYGEN.bar(), OXYGEN.ghostTop(), argb(oa * OXYGEN.ghostAlpha(), 0xFFFFFF));
            text(g, Component.literal(Math.round(T_AIR.shown()) + "/100"), r.x + r.w / 2f, r.cy(), 0.6f, 0xFFFFFF, oa, true);
        }
    }

    private static void drawHunger(GuiGraphics g, Snap s, float x, float y, long now, BarConfig cfg, float a) {
        icon(g, 16, 27, x, y, 1f, 1f, 1f, a);
        icon(g, 52, 27, x, y, 1f, 1f, 1f, a);

        float ex = Mth.clamp(s.exhaustion / 4f, 0f, 1f);
        if (ex > 0.02f && cfg.rpgExhaustionAlpha > 0f && GuiStencil.ensureEnabled()) {
            int h = Math.max(1, Math.round(9 * ex));
            PoseStack ps = g.pose();
            ps.pushPose();
            ps.translate(x, y, 0f);
            GuiStencil.beginMask(g);
            g.blit(ICONS, 0, 9 - h, 52, 27 + 9 - h, 9, h);
            GuiStencil.useMask(g);
            float scroll = (now % 2000L) / 2000f, span = 9f / 16f;
            GuiQuad.blit(g, RpgTextures.hatch(), 0f, 0f, 9f, 9f, scroll, 0f, scroll + span, span, argb(a * cfg.rpgExhaustionAlpha, HATCH_RGB));
            GuiStencil.end(g);
            ps.popPose();
        }

        float sc = 0.75f * Math.max(pop(T_FOOD, now, cfg), pop(T_SAT, now, cfg));
        Component current = Component.literal(Math.round(T_FOOD.shown()) + "+" + oneDecimal(T_SAT.shown()));
        if (s.held == null) {
            text(g, current, x + 11f, y + 5f, sc, 0xFFFFFF, a, false);
            return;
        }
        int pf = Math.min(20, s.food + s.held.getNutrition());
        float psat = Math.min(pf, s.sat + s.held.getNutrition() * s.held.getSaturationModifier() * 2f);
        float pulse = 0.5f - 0.5f * Mth.cos(now / 1000f * cfg.rpgFoodPreviewHz * Mth.TWO_PI);
        text(g, current, x + 11f, y + 5f, sc, 0xFFFFFF, a * (1f - pulse), false);
        text(g, Component.literal(pf + "+" + oneDecimal(psat)), x + 11f, y + 5f, sc, PREVIEW_RGB, a * pulse, false);
    }

    private static Rect miniBar(float right, float rowY) {
        float h = Geo.MINI_BAR_W * RpgTextures.info(BAR_TRACK).aspect();
        return new Rect(right - Geo.MINI_BAR_W, rowY + (Geo.ROW_H - h) / 2f, Geo.MINI_BAR_W, h);
    }

    private static void seg(GuiGraphics g, ResourceLocation tex, RpgTextures.Info i, Rect r, float s0, float s1, int argb) {
        if (s1 - s0 <= 0.001f) return;
        float u0 = i.u(s0), u1 = i.u(s1);
        GuiQuad.blit(g, tex, r.x + r.w * u0, r.y, r.x + r.w * u1, r.y2(), u0, 0f, u1, 1f, argb);
    }

    private static void rect(GuiGraphics g, float x0, float y0, float x1, float y1, int argb) {
        if (x1 - x0 <= 0.001f) return;
        GuiQuad.blit(g, RpgTextures.pixel(), x0, y0, x1, y1, 0f, 0f, 1f, 1f, argb);
    }

    private static void icon(GuiGraphics g, int u, int v, float x, float y, float r, float gr, float b, float a) {
        RenderSystem.setShaderColor(r, gr, b, a);
        g.pose().pushPose();
        g.pose().translate(x, y, 0f);
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
    private static String oneDecimal(float v) { return String.format(java.util.Locale.ROOT, "%.1f", v); }

    private record Rect(float x, float y, float w, float h) {
        float x2() { return x + w; }
        float y2() { return y + h; }
        float cy() { return y + h / 2f; }
    }

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
            float[] sh = shieldValues(p);
            int need = p.getXpNeededForNextLevel();
            return new Snap(p.getHealth(), p.getMaxHealth(), p.getAbsorptionAmount(),
                    patch == null ? 0f : patch.getStamina(), patch == null ? 0f : patch.getMaxStamina(),
                    sh[0], sh[1], p.getArmorValue(), (int) Math.round(p.getAttributeValue(Attributes.ARMOR_TOUGHNESS)),
                    p.getFoodData().getFoodLevel(), p.getFoodData().getSaturationLevel(), PlayerHudSync.clientExhaustion(),
                    heldFood(p), p.getAirSupply(), p.getMaxAirSupply(), p.isEyeInFluid(FluidTags.WATER),
                    p.getVehicle() instanceof LivingEntity m ? m : null,
                    p.experienceLevel, p.experienceProgress, need, Math.round(p.experienceProgress * need),
                    EntityLevelManager.getPlayerLevel(p));
        }

        private static float[] shieldValues(LocalPlayer p) {
            ClientShieldState st = ClientCombatBars.shield(p.getId());
            return st == null ? new float[2] : new float[]{st.amount(), st.initial()};
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