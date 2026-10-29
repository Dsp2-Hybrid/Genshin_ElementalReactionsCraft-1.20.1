package com.dsp2.genshinerc.client.render.hud.gui.rpg;

import com.dsp2.genshinerc.client.render.hud.HudDraw;
import com.dsp2.genshinerc.client.render.hud.bar.ClientCombatBars;
import com.dsp2.genshinerc.client.render.hud.bar.config.BarConfig;
import com.dsp2.genshinerc.client.render.hud.bar.shield.ClientShieldState;
import com.dsp2.genshinerc.client.render.hud.bar.zhenmie.ClientZhenMieState;
import com.dsp2.genshinerc.client.render.hud.gui.rpg.RpgHudElement.Box;
import com.dsp2.genshinerc.client.render.hud.gui.rpg.anim.BarTrack;
import com.dsp2.genshinerc.client.render.hud.gui.rpg.anim.NumberFx;
import com.dsp2.genshinerc.client.render.hud.gui.rpg.anim.TextAnim;
import com.dsp2.genshinerc.client.render.hud.gui.rpg.anim.WhiteOverlay;
import com.dsp2.genshinerc.client.render.hud.gui.rpg.editor.RpgHudEditorScreen;
import com.dsp2.genshinerc.client.render.hud.gui.rpg.render.DiamondModelView;
import com.dsp2.genshinerc.client.render.hud.gui.rpg.render.GuiQuad;
import com.dsp2.genshinerc.client.render.hud.gui.rpg.render.HudGeom;
import com.dsp2.genshinerc.client.render.hud.gui.rpg.render.NineSlice;
import com.dsp2.genshinerc.client.render.hud.gui.rpg.render.Strip;
import com.dsp2.genshinerc.common.core.combat.shield.ShieldKind;
import com.dsp2.genshinerc.common.core.combat.zhenmie.ClashResult;
import com.dsp2.genshinerc.common.level.EntityLevelManager;
import com.dsp2.genshinerc.common.network.sync.PlayerHudSync;
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
import net.minecraft.world.effect.MobEffects;
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

    static final ResourceLocation BAR_HEALTH = tex("rpg_bar_3"), BAR_STAMINA = tex("rpg_bar_2"), BAR_ABSORB = tex("rpg_bar_1"),
            BAR_MOUNT = tex("rpg_bar_3"), BAR_OXYGEN = tex("rpg_bar_2"), BAR_STAT = tex("rpg_bar_2");
    static final ResourceLocation EXP_SHADOW = tex("rpg_exps"), EXP_FILL = tex("rpg_exp");
    static final ResourceLocation ZM_BAR = tex("zm_bar"), ZM_BORDER = tex("zm_bar_border"),
            ZM_LOGO = tex("zm_logo"), ZM_TRIANGLE = tex("zm_triangle"), STAMINA_LOGO = tex("stamina_logo");

    public static final IGuiOverlay OVERLAY = RpgHudOverlay::render;

    static final int WHITE = 0xFFFFFF;
    static final float BAR_BG_ALPHA = 0.75f, OUTLINE_ALPHA = 1f;
    static final int NATURAL = -1;

    private static final int ABS_RGB = 0xFFB040;
    private static final float DEATH_SHAKE_BOOST = 1.6f, CLASH_ALPHA = 0.85f, CRUSH_FLICKER_HZ = 10f, HP_COLOR_SPEED = 10f;
    private static final float LOW_HP_BLINK = 0.15f, CLASH_MARKER_WIDTH = 0.6f;
    private static final long CRUSH_HUE_MS = 600L, KNOCKDOWN_SWAP_MS = 90L;

    private static final float BREATH_HZ = 0.5f, BREATH_MIN_A = 0.25f, BREATH_MAX_A = 0.70f;
    private static final float MIE_DARKEN = 0.35f;
    private static final int MIE_MIN_PEAK = 0x30;
    private static final float BASE_DARKEN = 0.30f;

    private static final BarTrack B_HEALTH = new BarTrack(), B_STAMINA = new BarTrack();
    private static final NumberFx N_HP = new NumberFx(400L, 100L, 400L);
    private static final NumberFx N_SHIELD = new NumberFx(400L, 100L, 400L);
    private static final TextAnim T_ABS = new TextAnim(), T_XP = new TextAnim();
    private static final XpGauge XP = new XpGauge();
    private static final RpgPalette.Tween C_HP = new RpgPalette.Tween();

    private static final WhiteOverlay ZH_W = new WhiteOverlay(), MI_W = new WhiteOverlay();
    private static final WhiteOverlay SHIELD_O1 = new WhiteOverlay(), SHIELD_O2 = new WhiteOverlay(), SHIELD_O3 = new WhiteOverlay();
    private static float zhLast = -1f, miLast = -1f;
    private static float lastT1 = -1f, lastT2 = -1f, lastT3 = -1f;

    @Nullable private static LocalPlayer lastPlayer;
    private static long deathStart = -1L;

    private RpgHudOverlay() {}

    public static float originX(BarConfig cfg) { return cfg.rpgHudOffsetX; }
    public static float originY(BarConfig cfg, int screenHeight) { return screenHeight - cfg.rpgHudOffsetY; }

    public static NineSlice.Corner frameCorner(BarConfig cfg) {
        return NineSlice.corner(ZM_BORDER, cfg.rpgFrameSlice, cfg.rpgFrameCornerPx, RpgHudElement.ZHEN_MIE_BORDER.defaults().ah());
    }

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
            drawPortrait(g, p, RpgHudElement.PORTRAIT.get(cfg).abs(), cfg, alpha);
            XP.draw(g, RpgHudElement.EXP.get(cfg), alpha, now, cfg);
            drawLevelText(g, s, RpgHudElement.LEVEL_TEXT.get(cfg).abs(), now, cfg, alpha);

            float shake = B_HEALTH.shakeOffset(now, cfg) * cfg.rpgShakePixels * (p.isDeadOrDying() ? DEATH_SHAKE_BOOST : 1f);
            pose.pushPose();
            pose.translate(shake, 0f, 0f);
            drawVitals(g, s, cfg, now, alpha);
            drawShield(g, cfg, now, alpha, preview);
            pose.popPose();

            drawZhenMie(g, p, now, cfg, alpha, sh);
            RpgStatBars.draw(g, s, now, cfg, alpha, preview);
        } finally {
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
            pose.popPose();
        }
    }

    private static void resetAll() {
        B_HEALTH.reset(); B_STAMINA.reset();
        N_HP.reset(); N_SHIELD.reset(); T_ABS.reset(); T_XP.reset(); XP.reset(); C_HP.reset();
        ZH_W.reset(); MI_W.reset(); zhLast = miLast = -1f;
        SHIELD_O1.reset(); SHIELD_O2.reset(); SHIELD_O3.reset();
        lastT1 = lastT2 = lastT3 = -1f;
        RpgStatBars.reset();
        deathStart = -1L;
    }

    private static float deathAlpha(Player p, long now, BarConfig cfg) {
        if (!p.isDeadOrDying()) { deathStart = -1L; return 1f; }
        if (deathStart < 0L) deathStart = now;
        float el = (now - deathStart) / 1000f - cfg.shakeDuration;
        if (el <= 0f) return 1f;
        return cfg.deathFadeOutTime <= 0f ? 0f : Mth.clamp(1f - el / cfg.deathFadeOutTime, 0f, 1f);
    }

    private static void tick(Snap s, long now, BarConfig cfg) {
        B_HEALTH.update(now, s.hp(), s.abs(), s.maxHp(), cfg, true, true);
        B_STAMINA.update(now, s.sta(), 0f, Math.max(1f, s.maxSta()), cfg, false, false);
        N_HP.set(s.hp(), now);
        N_HP.update(now);
        N_SHIELD.set(s.shield(), now);
        N_SHIELD.update(now);
        T_ABS.set(s.abs(), now);
        T_XP.set(s.xpPoints(), now);
        T_ABS.update(now, cfg.rpgCountSpeed);
        T_XP.update(now, cfg.rpgCountSpeed);
        XP.update(now, s.xpLevel(), s.xpProgress());
        C_HP.step(s.hpColor(), now, HP_COLOR_SPEED);
        RpgStatBars.tick(s, now, cfg);
    }

    static void pushMirror(PoseStack pose, Box b) {
        pose.pushPose();
        if (b.flipX()) {
            pose.translate(b.cx(), 0f, 0f);
            pose.scale(-1f, 1f, 1f);
            pose.translate(-b.cx(), 0f, 0f);
        }
        if (b.flipY()) {
            pose.translate(0f, b.cy(), 0f);
            pose.scale(1f, -1f, 1f);
            pose.translate(0f, -b.cy(), 0f);
        }
    }

    private static void drawPortrait(GuiGraphics g, LocalPlayer p, Box b, BarConfig cfg, float a) {
        float size = Math.min(b.w(), b.h()), x = b.cx() - size / 2f, y = b.cy() - size / 2f;
        if (a < 1f) RenderSystem.setShaderColor(1f, 1f, 1f, a);
        DiamondModelView.render(g, p, x, y, size, cfg);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }

    private static void drawLevelText(GuiGraphics g, Snap s, Box b, long now, BarConfig cfg, float a) {
        float scale = b.h() / 18f;

        MutableComponent line1 = Component.empty()
                .append(Component.literal("Lv." + s.level()).withStyle(st -> st.withColor(0xFFFFFF)))
                .append(Component.literal(" / ").withStyle(st -> st.withColor(0x00FF00)))
                .append(Component.literal("Ex." + s.xpLevel()).withStyle(st -> st.withColor(0x00FF00)));

        int expColor = HudDraw.lerpRgb(0xFFFFFF, 0x00FF00, s.xpProgress());
        MutableComponent line2 = Component.empty()
                .append(Component.literal("EXP:").withStyle(st -> st.withColor(0xFFFFFF)))
                .append(Component.literal(String.valueOf(Math.round(T_XP.shown()))).withStyle(st -> st.withColor(expColor)))
                .append(Component.literal(" / " + s.xpNeed()).withStyle(st -> st.withColor(0xFFFFFF)));

        text(g, line1, b.cx(), b.y() + b.h() * 0.27f, scale, 0xFFFFFF, a, true);
        text(g, line2, b.cx(), b.y() + b.h() * 0.77f, scale * 0.9f * pop(T_XP, now, cfg), 0xFFFFFF, a, true);
    }

    private static void drawVitals(GuiGraphics g, Snap s, BarConfig cfg, long now, float a) {
        Box hpB = RpgHudElement.HEALTH.get(cfg);
        Box staB = RpgHudElement.STAMINA.get(cfg);
        int hpRgb = C_HP.rgb();

        drawBar(g, BAR_HEALTH, hpB, B_HEALTH, hpRgb, true, now, cfg, a);

        boolean hasSta = s.maxSta() > 0f;
        if (hasSta) {
            drawBar(g, BAR_STAMINA, staB, B_STAMINA, NATURAL, false, now, cfg, a);
            Box staIconB = RpgHudElement.STAMINA_ICON.get(cfg).abs().withFlip(false, false);
            GuiQuad.blit(g, STAMINA_LOGO, staIconB.x(), staIconB.y(), staIconB.x2(), staIconB.y2(), 0f, 0f, 1f, 1f, argb(a, WHITE));
        }

        Box iconB = RpgHudElement.HP_ICON.get(cfg).abs().withFlip(false, false);
        HudGeom.icon(g, HudGeom.ICONS, 16, s.heartV(), iconB, argb(a, WHITE));
        HudGeom.icon(g, HudGeom.ICONS, s.heartU(), s.heartV(), iconB, argb(a, WHITE));

        Box hp = hpB.abs(), sta = staB.abs();
        float top = hp.y(), bottom = hp.y2();
        if (hasSta && sta.y() > top && sta.y() < bottom && sta.x() < hp.x2() && sta.x2() > hp.x()) bottom = sta.y();
        float scale = Mth.clamp((bottom - top) / 8f * 0.8f, 0.35f, 1.5f) * cfg.rpgHpTextScale;
        hpText(g, s, hp.cx(), (top + bottom) / 2f, scale, now, a);
    }

    private static String formatNum(float v) {
        return v >= 1000f ? String.format(Locale.ROOT, "%.0f", v) : oneDecimal(v);
    }

    private static void hpText(GuiGraphics g, Snap s, float cx, float cy, float scale, long now, float a) {
        float frac = s.maxHp() <= 0f ? 0f : N_HP.shown() / s.maxHp();
        int curRgb = N_HP.blink(now, frac, LOW_HP_BLINK) ? WHITE : RpgPalette.hpText(frac);
        float ta = a * N_HP.alpha();
        String cur = formatNum(N_HP.shown());
        MutableComponent tail = Component.literal(" / " + formatNum(s.maxHp()));
        if (T_ABS.shown() >= 0.05f) tail.append(colored(" +" + formatNum(T_ABS.shown()), ABS_RGB));

        Font font = Minecraft.getInstance().font;
        float wc = font.width(cur) * scale, wt = font.width(tail) * scale, x0 = cx - (wc + wt) / 2f;
        text(g, Component.literal(cur), x0 + wc / 2f, cy, scale * N_HP.scale(now), curRgb, ta, true);
        text(g, tail, x0 + wc, cy, scale, WHITE, ta, false);
    }

    static void drawBar(GuiGraphics g, ResourceLocation tex, Box signedBox, BarTrack t, int rgb, boolean absorption,
                        long now, BarConfig cfg, float a) {
        Box b = signedBox.abs();
        Strip st = Strip.of(tex, signedBox.flipX(), signedBox.flipY());
        boolean tinted = rgb != NATURAL;

        if (tinted) st.background(g, b, HudDraw.scaleRgb(rgb, RpgPalette.BG_DARKEN), a * BAR_BG_ALPHA);
        else st.full(g, st.i.core(), b, argb(a * BAR_BG_ALPHA, HudDraw.scaleRgb(WHITE, RpgPalette.BG_DARKEN)));

        st.outline(g, b, a * OUTLINE_ALPHA);

        float main = t.main, extra = t.extra;

        st.seg(g, tinted ? st.i.tint() : st.i.core(), b, 0f, main, argb(a, tinted ? rgb : WHITE));

        if (absorption && extra > main + 0.001f) {
            Strip abs = Strip.of(BAR_ABSORB, signedBox.flipX(), signedBox.flipY());
            abs.seg(g, abs.i.core(), b, main, extra, argb(a, WHITE));
        }

        t.forEachWhite((ws0, ws1, wa) -> st.seg(g, st.i.white(), b, ws0, ws1, argb(a * wa, WHITE)));

        float sa = t.smallAlpha(now, cfg);
        if (sa > 0.01f) st.seg(g, st.i.white(), b, 0f, Math.max(main, extra), argb(a * sa, WHITE));
    }

    private static void drawShield(GuiGraphics g, BarConfig cfg, long now, float a, boolean preview) {
        LocalPlayer lp = Minecraft.getInstance().player;
        if (lp == null) return;
        ClientShieldState ss = ClientCombatBars.shield(lp.getId());
        float hpMax = (float) lp.getMaxHealth();

        if (!preview && (ss == null || (ss.amount() <= 0.001f && !SHIELD_O1.busy() && !SHIELD_O2.busy() && !SHIELD_O3.busy()))) return;

        Box absBox = RpgHudElement.SHIELD.get(cfg).abs();

        float currentShield = ss != null ? ss.amount() : 0f;
        float targetShield = ss != null ? ss.bar.target() : 0f;
        float maxShield = hpMax * 3f;

        float main3 = (currentShield / maxShield) * 3f;
        float target3 = (targetShield / maxShield) * 3f;

        float l1 = Mth.clamp(main3, 0f, 1f);
        float l2 = Mth.clamp(main3 - 1f, 0f, 1f);
        float l3 = Mth.clamp(main3 - 2f, 0f, 1f);

        float t1 = Mth.clamp(target3, 0f, 1f);
        float t2 = Mth.clamp(target3 - 1f, 0f, 1f);
        float t3 = Mth.clamp(target3 - 2f, 0f, 1f);

        if (lastT1 < 0) { lastT1 = 0f; lastT2 = 0f; lastT3 = 0f; }

        if (Math.abs(t1 - lastT1) > 0.001f) SHIELD_O1.trigger(lastT1, t1, now);
        if (Math.abs(t2 - lastT2) > 0.001f) SHIELD_O2.trigger(lastT2, t2, now);
        if (Math.abs(t3 - lastT3) > 0.001f) SHIELD_O3.trigger(lastT3, t3, now);

        lastT1 = t1; lastT2 = t2; lastT3 = t3;

        SHIELD_O1.update(now, l1, cfg);
        SHIELD_O2.update(now, l2, cfg);
        SHIELD_O3.update(now, l3, cfg);

        int bgColor = (ss != null && ss.kind() == ShieldKind.ELEMENTAL)
                ? HudDraw.scaleRgb(ss.color(cfg, hpMax, 0), 0.3f)
                : 0xFF404040;
        HudGeom.rect(g, absBox.x(), absBox.y(), absBox.x2(), absBox.y2(), argb(a, bgColor));

        if (ss != null && main3 > 0.001f) {
            if (l1 > 0.001f) {
                int c = ss.color(cfg, hpMax, 0);
                HudGeom.rect(g, absBox.x(), absBox.y(), absBox.x() + absBox.w() * l1, absBox.y2(), argb(a, c));
            }
            if (l2 > 0.001f) {
                int c = ss.color(cfg, hpMax, 1);
                HudGeom.rect(g, absBox.x(), absBox.y(), absBox.x() + absBox.w() * l2, absBox.y2(), argb(a, c));
            }
            if (l3 > 0.001f) {
                int c = ss.color(cfg, hpMax, 2);
                HudGeom.rect(g, absBox.x(), absBox.y(), absBox.x() + absBox.w() * l3, absBox.y2(), argb(a, c));
            }

            float sa = ss.bar.whiteness(now, cfg.subBarFlashTime);
            if (sa > 0.01f) {
                HudGeom.rect(g, absBox.x(), absBox.y(), absBox.x() + absBox.w() * l1, absBox.y2(), argb(a * sa, WHITE));
            }

            SHIELD_O1.emit((lo, hi, wa) -> HudGeom.rect(g, absBox.x() + absBox.w() * lo, absBox.y(), absBox.x() + absBox.w() * hi, absBox.y2(), argb(a * wa, WHITE)));
            SHIELD_O2.emit((lo, hi, wa) -> HudGeom.rect(g, absBox.x() + absBox.w() * lo, absBox.y(), absBox.x() + absBox.w() * hi, absBox.y2(), argb(a * wa, WHITE)));
            SHIELD_O3.emit((lo, hi, wa) -> HudGeom.rect(g, absBox.x() + absBox.w() * lo, absBox.y(), absBox.x() + absBox.w() * hi, absBox.y2(), argb(a * wa, WHITE)));

            float shownVal = N_SHIELD.shown();
            float pop = N_SHIELD.scale(now);
            float ta = a * N_SHIELD.alpha();

            int topLayer = main3 > 2f ? 2 : (main3 > 1f ? 1 : 0);
            int numColor = HudDraw.lerpRgb(ss.color(cfg, hpMax, topLayer), 0xFFFFFF, 0.20f);
            String numStr = String.valueOf(Math.round(shownVal));
            text(g, Component.literal(numStr), absBox.cx(), absBox.cy(), 0.6f * pop, numColor, ta, true);
        }
    }

    private static void drawZhenMie(GuiGraphics g, LocalPlayer p, long now, BarConfig cfg, float a, int sh) {
        ClientZhenMieState z = ClientCombatBars.zhenMie(p.getId());
        if (z != null) z.tick(now, cfg);

        zmBar(g, z, RpgHudElement.ZHEN_MIE.get(cfg), now, cfg, a, sh);

        Box borderB = RpgHudElement.ZHEN_MIE_BORDER.get(cfg).abs();
        pushMirror(g.pose(), borderB);
        NineSlice.draw(g, ZM_BORDER, borderB.withFlip(false, false), frameCorner(cfg), argb(a, WHITE));
        g.pose().popPose();

        Box logoB = RpgHudElement.ZHEN_MIE_LOGO.get(cfg).abs();
        GuiQuad.blit(g, ZM_LOGO, logoB.x(), logoB.y(), logoB.x2(), logoB.y2(), 0f, 0f, 1f, 1f, argb(a, WHITE));

        Box triB = RpgHudElement.ZHEN_MIE_TRIANGLE.get(cfg).abs();
        GuiQuad.blit(g, ZM_TRIANGLE, triB.x(), triB.y(), triB.x2(), triB.y2(), 0f, 0f, 1f, 1f, argb(a, WHITE));
    }

    private static int darkMie(int rgb) {
        int r = (int) (((rgb >> 16) & 0xFF) * MIE_DARKEN), gr = (int) (((rgb >> 8) & 0xFF) * MIE_DARKEN), b = (int) ((rgb & 0xFF) * MIE_DARKEN);
        int peak = Math.max(r, Math.max(gr, b));
        if (peak <= 0) return (MIE_MIN_PEAK << 16) | (MIE_MIN_PEAK << 8) | MIE_MIN_PEAK;
        if (peak < MIE_MIN_PEAK) {
            float k = MIE_MIN_PEAK / (float) peak;
            r = Math.min(255, Math.round(r * k)); gr = Math.min(255, Math.round(gr * k)); b = Math.min(255, Math.round(b * k));
        }
        return (r << 16) | (gr << 8) | b;
    }

    private static int dim(int rgb, float k) {
        return (Math.round(((rgb >> 16) & 0xFF) * k) << 16) | (Math.round(((rgb >> 8) & 0xFF) * k) << 8) | Math.round((rgb & 0xFF) * k);
    }

    private static float watch(WhiteOverlay w, float last, float target, float visual, ClientZhenMieState z, BarConfig cfg, long now) {
        float delta = Math.abs(target - last);
        boolean small = cfg.smallChangeEnabled && delta < cfg.smallChangeThreshold;
        if (last >= 0f && delta > 0.001f && !z.decaying() && !small) w.trigger(last, target, now);
        w.update(now, visual, cfg);
        return target;
    }

    private static void zmBar(GuiGraphics g, @Nullable ClientZhenMieState z, Box b, long now, BarConfig cfg, float a, int sh) {
        Box absBox = b.abs();

        int zhenBase = cfg.zhenColor.getRGB() & 0xFFFFFF;
        int mieBase = cfg.mieColor.getRGB() & 0xFFFFFF;
        int zc = z != null && z.zhenBuff() ? cycle(zhenBase, cfg.zhenBuffColor.getRGB() & 0xFFFFFF, cfg.zhenMieBuffCycleHz, now) : zhenBase;
        int mc = z != null && z.mieBuff() ? cycle(mieBase, cfg.mieBuffColor.getRGB() & 0xFFFFFF, cfg.zhenMieBuffCycleHz, now) : mieBase;

        GuiQuad.blit(g, ZM_BAR, absBox.x(), absBox.y(), absBox.x2(), absBox.y2(), 0f, 0f, 1f, 1f, argb(a * 0.5f, dim(0xFFFFFF, BASE_DARKEN)));

        if (z == null) { ZH_W.reset(); MI_W.reset(); zhLast = miLast = -1f; return; }

        float zm = Mth.clamp(z.zhen(), 0f, 1f);
        float mm = Mth.clamp(z.mie(), 0f, 1f);

        zhLast = watch(ZH_W, zhLast, Mth.clamp(z.zhenTarget(), 0f, 1f), zm, z, cfg, now);
        miLast = watch(MI_W, miLast, Mth.clamp(z.mieTarget(), 0f, 1f), mm, z, cfg, now);

        int ox = (int) originX(cfg);
        int oy = (int) originY(cfg, sh);

        if (zm > 0.001f) {
            int sx1 = (int)(ox + absBox.x() * cfg.rpgHudScale);
            int sy1 = (int)(oy + absBox.y() * cfg.rpgHudScale);
            int sx2 = (int)(ox + (absBox.x() + absBox.w() * zm) * cfg.rpgHudScale);
            int sy2 = (int)(oy + absBox.y2() * cfg.rpgHudScale);
            g.enableScissor(sx1, sy1, sx2, sy2);
            GuiQuad.blit(g, ZM_BAR, absBox.x(), absBox.y(), absBox.x2(), absBox.y2(), 0f, 0f, 1f, 1f, argb(a, HudDraw.lerpRgb(zc, WHITE, z.zhenWhite(now, cfg))));
            g.disableScissor();
        }

        if (mm > 0.001f) {
            int sx1 = (int)(ox + (absBox.x() + absBox.w() * (1f - mm)) * cfg.rpgHudScale);
            int sy1 = (int)(oy + absBox.y() * cfg.rpgHudScale);
            int sx2 = (int)(ox + absBox.x2() * cfg.rpgHudScale);
            int sy2 = (int)(oy + absBox.y2() * cfg.rpgHudScale);
            g.enableScissor(sx1, sy1, sx2, sy2);
            GuiQuad.blit(g, ZM_BAR, absBox.x(), absBox.y(), absBox.x2(), absBox.y2(), 0f, 0f, 1f, 1f, argb(a, HudDraw.lerpRgb(mc, WHITE, z.mieWhite(now, cfg))));
            g.disableScissor();

            int dark = darkMie(mc);
            float breath = 0.5f - 0.5f * Mth.cos(now / 1000f * BREATH_HZ * Mth.TWO_PI);
            g.enableScissor(sx1, sy1, sx2, sy2);
            GuiQuad.blit(g, ZM_BAR, absBox.x(), absBox.y(), absBox.x2(), absBox.y2(), 0f, 0f, 1f, 1f, argb(a * Mth.lerp(breath, BREATH_MIN_A, BREATH_MAX_A), dark));
            g.disableScissor();
        }

        ZH_W.emit((lo, hi, wa) -> {
            int sx1 = (int)(ox + (absBox.x() + absBox.w() * lo) * cfg.rpgHudScale);
            int sy1 = (int)(oy + absBox.y() * cfg.rpgHudScale);
            int sx2 = (int)(ox + (absBox.x() + absBox.w() * hi) * cfg.rpgHudScale);
            int sy2 = (int)(oy + absBox.y2() * cfg.rpgHudScale);
            g.enableScissor(sx1, sy1, sx2, sy2);
            GuiQuad.blit(g, ZM_BAR, absBox.x(), absBox.y(), absBox.x2(), absBox.y2(), 0f, 0f, 1f, 1f, argb(a * wa, WHITE));
            g.disableScissor();
        });

        MI_W.emit((lo, hi, wa) -> {
            int sx1 = (int)(ox + (absBox.x() + absBox.w() * (1f - hi)) * cfg.rpgHudScale);
            int sy1 = (int)(oy + absBox.y() * cfg.rpgHudScale);
            int sx2 = (int)(ox + (absBox.x() + absBox.w() * (1f - lo)) * cfg.rpgHudScale);
            int sy2 = (int)(oy + absBox.y2() * cfg.rpgHudScale);
            g.enableScissor(sx1, sy1, sx2, sy2);
            GuiQuad.blit(g, ZM_BAR, absBox.x(), absBox.y(), absBox.x2(), absBox.y2(), 0f, 0f, 1f, 1f, argb(a * wa, WHITE));
            g.disableScissor();
        });

        ClashResult clash = z.clash();
        if (clash == null) return;
        float t = (now - z.clashStartMs()) / (clash.visualSeconds() * 1000f), fade = 1f - Mth.clamp(t, 0f, 1f), ca = 1f;
        int color = switch (clash) {
            case CRUSH -> {
                ca = 0.5f + 0.5f * Mth.cos(now / 1000f * CRUSH_FLICKER_HZ * Mth.TWO_PI);
                yield Mth.hsvToRgb((now % CRUSH_HUE_MS) / (float) CRUSH_HUE_MS, 0.85f, 1f);
            }
            case KNOCKDOWN -> (now / KNOCKDOWN_SWAP_MS) % 2 == 0 ? zhenBase : mieBase;
            default -> WHITE;
        };

        float rightEdge = (clash == ClashResult.KNOCKDOWN) ? (absBox.x() + absBox.w() * (1f - mm)) : absBox.x2();
        int sx1 = (int)(ox + absBox.x() * cfg.rpgHudScale);
        int sy1 = (int)(oy + absBox.y() * cfg.rpgHudScale);
        int sx2 = (int)(ox + rightEdge * cfg.rpgHudScale);
        int sy2 = (int)(oy + absBox.y2() * cfg.rpgHudScale);

        g.enableScissor(sx1, sy1, sx2, sy2);
        GuiQuad.blit(g, ZM_BAR, absBox.x(), absBox.y(), absBox.x2(), absBox.y2(), 0f, 0f, 1f, 1f, argb(a * fade * ca * CLASH_ALPHA, color));
        g.disableScissor();

        float mx = absBox.x() + absBox.w() * z.clashPoint();
        float half = absBox.h() * CLASH_MARKER_WIDTH / 2f;
        HudGeom.rect(g, mx - half, absBox.y(), mx + half, absBox.y2(), argb(a * fade, WHITE));
        float ts = absBox.h() * 0.8f, ty = absBox.y() - ts * 0.6f;
        GuiQuad.blit(g, ZM_TRIANGLE, mx - ts / 2f, ty, mx + ts / 2f, ty + ts, 0f, 0f, 1f, 1f, argb(a * fade, WHITE));
    }

    static void text(GuiGraphics g, Component c, float x, float cy, float scale, int rgb, float a, boolean center) {
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

    static void label(GuiGraphics g, Box signed, Component c, float pop, float a) {
        Box b = signed.abs();
        float scale = Mth.clamp(b.h() * 0.14f, 0.3f, 0.5f) * pop;
        text(g, c, b.cx(), b.cy(), scale, WHITE, a, true);
    }

    static MutableComponent colored(String s, int rgb) { return Component.literal(s).withStyle(st -> st.withColor(rgb)); }

    private static float pop(TextAnim t, long now, BarConfig cfg) { return t.scale(now, cfg.rpgTextPopScale, cfg.rpgTextPopTime); }

    private static int cycle(int a, int b, float hz, long now) {
        return HudDraw.lerpRgb(a, b, 0.5f - 0.5f * Mth.cos(now / 1000f * hz * Mth.TWO_PI));
    }

    static int argb(float a, int rgb) { return HudGeom.argb(a, rgb); }
    static int ceil(float v) { return (int) Math.ceil(v - 0.001f); }
    static String oneDecimal(float v) { return String.format(Locale.ROOT, "%.1f", v); }

    static final class Fade {
        float v;
        long last;

        void update(long now, boolean on, float seconds) {
            float dt = last == 0L ? 0f : Math.min(0.1f, (now - last) / 1000f);
            last = now;
            float step = seconds <= 0f ? 1f : dt / seconds;
            v = Mth.clamp(v + (on ? step : -step), 0f, 1f);
        }
    }

    record Snap(float hp, float maxHp, float abs, float sta, float maxSta, float shield, float shieldInit,
                int armor, int toughness, int food, float sat, float exhaustion, boolean hungerEffect,
                @Nullable FoodProperties held, int air, int maxAir, boolean underwater, @Nullable LivingEntity mount,
                int xpLevel, float xpProgress, int xpNeed, int xpPoints, int level,
                int hpColor, int heartU, int heartV) {
        static Snap read(LocalPlayer p) {
            PlayerPatch<?> patch = EpicFightCapabilities.getEntityPatch(p, PlayerPatch.class);
            ClientShieldState st = ClientCombatBars.shield(p.getId());
            int need = p.getXpNeededForNextLevel();
            float hp = p.getHealth(), maxHp = p.getMaxHealth();
            int heartU = p.hasEffect(MobEffects.WITHER) ? 124 : p.hasEffect(MobEffects.POISON) ? 88 : p.isFullyFrozen() ? 178 : 52;
            int heartV = p.level().getLevelData().isHardcore() ? 45 : 0;
            return new Snap(hp, maxHp, p.getAbsorptionAmount(),
                    patch == null ? 0f : patch.getStamina(), patch == null ? 0f : patch.getMaxStamina(),
                    st == null ? 0f : st.amount(), st == null ? 0f : st.initial(),
                    p.getArmorValue(), (int) Math.round(p.getAttributeValue(Attributes.ARMOR_TOUGHNESS)),
                    p.getFoodData().getFoodLevel(), p.getFoodData().getSaturationLevel(), PlayerHudSync.clientExhaustion(),
                    p.hasEffect(MobEffects.HUNGER), heldFood(p),
                    p.getAirSupply(), p.getMaxAirSupply(), p.isEyeInFluid(FluidTags.WATER),
                    p.getVehicle() instanceof LivingEntity m ? m : null,
                    p.experienceLevel, p.experienceProgress, need, Math.round(p.experienceProgress * need),
                    EntityLevelManager.getPlayerLevel(p),
                    RpgPalette.hp(p, maxHp > 0f ? hp / maxHp : 0f), heartU, heartV);
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