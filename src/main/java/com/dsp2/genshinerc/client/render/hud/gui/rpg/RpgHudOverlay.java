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
import com.dsp2.genshinerc.client.render.hud.gui.rpg.editor.RpgHudEditorScreen;
import com.dsp2.genshinerc.client.render.hud.gui.rpg.render.DiamondModelView;
import com.dsp2.genshinerc.client.render.hud.gui.rpg.render.GuiQuad;
import com.dsp2.genshinerc.client.render.hud.gui.rpg.render.HudGeom;
import com.dsp2.genshinerc.client.render.hud.gui.rpg.render.HudMirror;
import com.dsp2.genshinerc.client.render.hud.gui.rpg.render.NineSlice;
import com.dsp2.genshinerc.client.render.hud.gui.rpg.render.Strip;
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
    static final ResourceLocation EXP_TRACK = tex("rpg_exp"), EXP_FILL = tex("rpg_exps");
    static final ResourceLocation ZM_BAR = tex("zm_bar"), ZM_BORDER = tex("zm_bar_border"),
            ZM_LOGO = tex("zm_logo"), ZM_TRIANGLE = tex("zm_triangle"), STAMINA_LOGO = tex("stamina_logo");

    public static final IGuiOverlay OVERLAY = RpgHudOverlay::render;

    static final int WHITE = 0xFFFFFF;
    static final float BAR_BG_ALPHA = 0.75f, OUTLINE_ALPHA = 1f;
    static final int NATURAL = -1;

    private static final int ABS_RGB = 0xFFB040;
    private static final float DEATH_SHAKE_BOOST = 1.6f, CLASH_ALPHA = 0.85f, CRUSH_FLICKER_HZ = 10f, HP_COLOR_SPEED = 10f;
    private static final float SWEEP_HZ = 0.6f, SWEEP_WIDTH = 0.18f, SWEEP_ALPHA = 0.45f, LOW_HP_BLINK = 0.15f;
    private static final long CRUSH_HUE_MS = 600L, KNOCKDOWN_SWAP_MS = 90L;

    private static final BarTrack B_HEALTH = new BarTrack(), B_STAMINA = new BarTrack(), B_SHIELD = new BarTrack();
    private static final NumberFx N_HP = new NumberFx(400L, 100L, 400L);
    private static final TextAnim T_ABS = new TextAnim(), T_XP = new TextAnim();
    private static final XpGauge XP = new XpGauge();
    private static final RpgPalette.Tween C_HP = new RpgPalette.Tween();

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
            int expRgb = cfg.rpgExpColor.getRGB() & 0xFFFFFF;
            HudMirror.draw(g, RpgHudElement.EXP.get(cfg), b -> XP.draw(g, b, expRgb, alpha, now, cfg));
            drawLevelText(g, s, RpgHudElement.LEVEL_TEXT.get(cfg).abs(), now, cfg, alpha);

            float shake = B_HEALTH.shakeOffset(now, cfg) * cfg.rpgShakePixels * (p.isDeadOrDying() ? DEATH_SHAKE_BOOST : 1f);
            pose.pushPose();
            pose.translate(shake, 0f, 0f);
            drawVitals(g, s, cfg, now, alpha);
            drawShield(g, cfg, now, alpha, preview);
            pose.popPose();

            drawZhenMie(g, p, now, cfg, alpha);
            RpgStatBars.draw(g, s, now, cfg, alpha, preview);
        } finally {
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
            pose.popPose();
        }
    }

    private static void resetAll() {
        B_HEALTH.reset(); B_STAMINA.reset(); B_SHIELD.reset();
        N_HP.reset(); T_ABS.reset(); T_XP.reset(); XP.reset(); C_HP.reset();
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
        B_SHIELD.update(now, s.shield(), 0f, Math.max(s.maxHp(), s.shieldInit()), cfg, false, false);
        N_HP.set(s.hp(), now);
        N_HP.update(now);
        T_ABS.set(s.abs(), now);
        T_XP.set(s.xpPoints(), now);
        T_ABS.update(now, cfg.rpgCountSpeed);
        T_XP.update(now, cfg.rpgCountSpeed);
        XP.update(now, s.xpLevel(), s.xpProgress());
        C_HP.step(s.hpColor(), now, HP_COLOR_SPEED);
        RpgStatBars.tick(s, now, cfg);
    }

    private static void drawPortrait(GuiGraphics g, LocalPlayer p, Box b, BarConfig cfg, float a) {
        float size = Math.min(b.w(), b.h()), x = b.cx() - size / 2f, y = b.cy() - size / 2f;
        if (a < 1f) RenderSystem.setShaderColor(1f, 1f, 1f, a);
        DiamondModelView.render(g, p, x, y, size, cfg);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }

    private static void drawLevelText(GuiGraphics g, Snap s, Box b, long now, BarConfig cfg, float a) {
        float scale = b.h() / 18f;
        int exColor = HudDraw.lerpRgb(0xFFFFFF, 0x5BE35B, s.xpProgress());

        MutableComponent line1 = Component.empty()
                .append(Component.literal("Ex." + s.xpLevel()).withStyle(st -> st.withColor(exColor)))
                .append(Component.literal(" / ").withStyle(st -> st.withColor(exColor)))
                .append(Component.literal("Lv." + s.level()).withStyle(st -> st.withColor(0xFFFFFF)));

        text(g, line1, b.cx(), b.y() + b.h() * 0.27f, scale, 0xFFFFFF, a, true);
        text(g, Component.literal("EXP:" + Math.round(T_XP.shown()) + " / " + s.xpNeed()), b.cx(), b.y() + b.h() * 0.77f,
                scale * 0.9f * pop(T_XP, now, cfg), exColor, a, true);
    }

    private static void drawVitals(GuiGraphics g, Snap s, BarConfig cfg, long now, float a) {
        Box hpS = RpgHudElement.HEALTH.get(cfg), staS = RpgHudElement.STAMINA.get(cfg);
        int hpRgb = C_HP.rgb();
        HudMirror.draw(g, hpS, b -> drawBar(g, BAR_HEALTH, b, B_HEALTH, hpRgb, true, now, cfg, a));

        boolean hasSta = s.maxSta() > 0f;
        if (hasSta) {
            HudMirror.draw(g, staS, b -> {
                drawBar(g, BAR_STAMINA, b, B_STAMINA, NATURAL, false, now, cfg, a);
                float ls = Math.max(6f, b.h() * 2f);
                GuiQuad.blit(g, STAMINA_LOGO, b.x() - ls * 0.7f, b.cy() - ls / 2f, b.x() + ls * 0.3f, b.cy() + ls / 2f,
                        0f, 0f, 1f, 1f, argb(a, WHITE));
            });
        }

        HudMirror.draw(g, RpgHudElement.HP_ICON.get(cfg), b -> {
            HudGeom.icon(g, HudGeom.ICONS, 16, s.heartV(), b, argb(a, WHITE));
            HudGeom.icon(g, HudGeom.ICONS, s.heartU(), s.heartV(), b, argb(a, WHITE));
        });

        Box hp = hpS.abs(), sta = staS.abs();
        float top = hp.y(), bottom = hp.y2();
        if (hasSta && sta.y() > top && sta.y() < bottom && sta.x() < hp.x2() && sta.x2() > hp.x()) bottom = sta.y();
        float scale = Mth.clamp((bottom - top) / 8f * 0.8f, 0.35f, 1.5f);
        hpText(g, s, hp.cx(), (top + bottom) / 2f, scale, now, a);
    }

    private static void hpText(GuiGraphics g, Snap s, float cx, float cy, float scale, long now, float a) {
        float frac = s.maxHp() <= 0f ? 0f : N_HP.shown() / s.maxHp();
        int curRgb = N_HP.blink(now, frac, LOW_HP_BLINK) ? WHITE : RpgPalette.hpText(frac);
        float ta = a * N_HP.alpha();
        String cur = oneDecimal(N_HP.shown());
        MutableComponent tail = Component.literal(" / " + oneDecimal(s.maxHp()));
        if (T_ABS.shown() >= 0.05f) tail.append(colored(" +" + oneDecimal(T_ABS.shown()), ABS_RGB));

        Font font = Minecraft.getInstance().font;
        float wc = font.width(cur) * scale, wt = font.width(tail) * scale, x0 = cx - (wc + wt) / 2f;
        text(g, Component.literal(cur), x0 + wc / 2f, cy, scale * N_HP.scale(now), curRgb, ta, true);
        text(g, tail, x0 + wc, cy, scale, WHITE, ta, false);
    }

    static void drawBar(GuiGraphics g, ResourceLocation tex, Box b, BarTrack t, int rgb, boolean absorption,
                        long now, BarConfig cfg, float a) {
        Strip st = Strip.of(tex);
        boolean tinted = rgb != NATURAL;
        if (tinted) st.background(g, b, HudDraw.scaleRgb(rgb, RpgPalette.BG_DARKEN), a * BAR_BG_ALPHA);
        else st.full(g, st.i.core(), b, argb(a * BAR_BG_ALPHA, HudDraw.scaleRgb(WHITE, RpgPalette.BG_DARKEN)));

        float main = t.main(), extra = t.extra();
        st.seg(g, tinted ? st.i.tint() : st.i.core(), b, 0f, main, argb(a, tinted ? rgb : WHITE));
        if (absorption && extra > main + 0.001f) {
            Strip abs = Strip.of(BAR_ABSORB);
            abs.seg(g, abs.i.core(), b, main, extra, argb(a, WHITE));
        }
        t.forEachWhite((s0, s1, wa) -> st.seg(g, st.i.white(), b, s0, s1, argb(a * wa * 0.4f, WHITE)));
        float sa = t.smallAlpha(now, cfg);
        if (sa > 0.01f) st.seg(g, st.i.white(), b, 0f, Math.max(main, extra), argb(a * sa, WHITE));
        st.outline(g, b, a * OUTLINE_ALPHA);
    }

    private static void drawShield(GuiGraphics g, BarConfig cfg, long now, float a, boolean preview) {
        if (!preview && B_SHIELD.main() <= 0.001f && !B_SHIELD.busy()) return;
        int c = cfg.shieldSpecialColor.getRGB() & 0xFFFFFF;
        HudMirror.draw(g, RpgHudElement.SHIELD.get(cfg), b -> {
            HudGeom.rect(g, b.x(), b.y(), b.x2(), b.y2(), argb(a * cfg.shieldBgAlpha, HudDraw.scaleRgb(c, cfg.shieldBgBrightness)));
            float x0 = b.x(), span = b.w();
            HudGeom.rect(g, x0, b.y(), x0 + span * B_SHIELD.main(), b.y2(), argb(a, c));
            B_SHIELD.forEachWhite((s0, s1, wa) -> HudGeom.rect(g, x0 + span * s0, b.y(), x0 + span * s1, b.y2(), argb(a * wa * 0.4f, WHITE)));
            float sa = B_SHIELD.smallAlpha(now, cfg);
            if (sa > 0.01f) HudGeom.rect(g, x0, b.y(), x0 + span * B_SHIELD.main(), b.y2(), argb(a * sa, WHITE));
        });
    }

    private static void drawZhenMie(GuiGraphics g, LocalPlayer p, long now, BarConfig cfg, float a) {
        ClientZhenMieState z = ClientCombatBars.zhenMie(p.getId());
        if (z != null) z.tick(now, cfg);
        Box zmS = RpgHudElement.ZHEN_MIE.get(cfg);
        HudMirror.draw(g, zmS, b -> zmBar(g, z, b, now, cfg, a));
        HudMirror.draw(g, RpgHudElement.ZHEN_MIE_BORDER.get(cfg),
                b -> NineSlice.draw(g, ZM_BORDER, b, frameCorner(cfg), argb(a, WHITE)));
        HudMirror.draw(g, zmS, b -> {
            float ls = b.h();
            GuiQuad.blit(g, ZM_LOGO, b.x(), b.y(), b.x() + ls, b.y() + ls, 0f, 0f, 1f, 1f, argb(a, WHITE));
        });
        HudMirror.draw(g, RpgHudElement.ZHEN_MIE_TRIANGLE.get(cfg),
                b -> GuiQuad.blit(g, ZM_TRIANGLE, b.x(), b.y(), b.x2(), b.y2(), 0f, 0f, 1f, 1f, argb(a, WHITE)));
    }

    private static void zmBar(GuiGraphics g, @Nullable ClientZhenMieState z, Box b, long now, BarConfig cfg, float a) {
        float ls = b.h();
        Box bar = new Box(b.x() + ls + 1f, b.y(), Math.max(1f, b.w() - ls - 1f), b.h());
        Strip st = Strip.of(ZM_BAR);
        st.full(g, ZM_BAR, bar, argb(a, WHITE));
        if (z == null) return;

        int zhenBase = cfg.zhenColor.getRGB() & 0xFFFFFF, mieBase = cfg.mieColor.getRGB() & 0xFFFFFF;
        int zc = z.zhenBuff() ? cycle(zhenBase, cfg.zhenBuffColor.getRGB() & 0xFFFFFF, cfg.zhenMieBuffCycleHz, now) : zhenBase;
        int mc = z.mieBuff() ? cycle(mieBase, cfg.mieBuffColor.getRGB() & 0xFFFFFF, cfg.zhenMieBuffCycleHz, now) : mieBase;
        float zm = Mth.clamp(z.zhen(), 0f, 1f), zt = Mth.clamp(z.zhenTrail(), 0f, 1f);
        float mm = Mth.clamp(z.mie(), 0f, 1f), mt = Mth.clamp(z.mieTrail(), 0f, 1f);

        if (zt > zm) st.seg(g, st.i.white(), bar, zm, zt, argb(a, WHITE));
        st.seg(g, st.i.tint(), bar, 0f, zm, argb(a, HudDraw.lerpRgb(zc, WHITE, z.zhenWhite(now, cfg))));

        int sweepColor = HudDraw.scaleRgb(mc, 0.75f);
        sweep(g, st, bar, 0f, zm, now, 0f, a, sweepColor);

        if (mt > mm) st.seg(g, st.i.white(), bar, 1f - mt, 1f - mm, argb(a, WHITE));
        st.seg(g, st.i.tint(), bar, 1f - mm, 1f, argb(a, HudDraw.lerpRgb(mc, WHITE, z.mieWhite(now, cfg))));
        sweep(g, st, bar, 1f - mm, 1f, now, 0.5f, a, sweepColor);

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
        st.seg(g, st.i.white(), bar, 0f, 1f, argb(a * fade * ca * CLASH_ALPHA, color));
        float mx = st.xAt(bar, st.i.u(z.clashPoint())), ts = bar.h() * 0.8f, ty = bar.y() - ts * 0.6f;
        GuiQuad.blit(g, ZM_TRIANGLE, mx - ts / 2f, ty, mx + ts / 2f, ty + ts, 0f, 0f, 1f, 1f, argb(a * fade, WHITE));
    }

    private static void sweep(GuiGraphics g, Strip st, Box bar, float lo, float hi, long now, float phase, float a, int color) {
        if (hi - lo <= 0.02f) return;
        float t = (now / 1000f * SWEEP_HZ + phase) % 1f;
        float c = Mth.lerp(t, lo - SWEEP_WIDTH, hi + SWEEP_WIDTH);
        int steps = 6;
        for (int i = 0; i < steps; i++) {
            float s0 = c - SWEEP_WIDTH + 2f * SWEEP_WIDTH * i / steps, s1 = c - SWEEP_WIDTH + 2f * SWEEP_WIDTH * (i + 1) / steps;
            float w = 1f - Math.abs((s0 + s1) / 2f - c) / SWEEP_WIDTH;
            st.seg(g, st.i.white(), bar, Math.max(lo, s0), Math.min(hi, s1), argb(a * SWEEP_ALPHA * w, color));
        }
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