package com.dsp2.genshinerc.client.render.gui.rpg;

import com.dsp2.genshinerc.client.render.bar.config.BarConfig;
import com.dsp2.genshinerc.client.render.hud.HudDraw;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static com.dsp2.genshinerc.client.render.gui.rpg.RpgHudLayout.*;

@SuppressWarnings({"deprecation", "removal"})
public final class RpgHudOverlay implements IGuiOverlay {
    public static final RpgHudOverlay INSTANCE = new RpgHudOverlay();
    private static final ResourceLocation VANILLA_ICONS = new ResourceLocation("textures/gui/icons.png");

    private long lastMs;

    private RpgHudOverlay() {}

    @Override
    public void render(ForgeGui gui, GuiGraphics g, float partialTick, int screenWidth, int screenHeight) {
        BarConfig cfg = BarConfig.get();
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (!cfg.rpgHudEnabled || player == null || mc.options.hideGui || !gui.shouldDrawSurvivalElements()) return;

        long now = Util.getMillis();
        float dt = Math.min(0.1f, Math.max(0L, now - lastMs) / 1000f);
        lastMs = now;

        float scale = cfg.rpgHudScale;
        float ox = cfg.rpgHudOffsetX;
        float oy = screenHeight - cfg.rpgHudOffsetY;

        PoseStack pose = g.pose();
        pose.pushPose();
        pose.translate(ox, oy, 0f);
        pose.scale(scale, scale, 1f);

        drawPanel(g, 0, -PANEL_H, PANEL_W, 0);
        PlayerModelPanel.render(g, player, 1, -PANEL_H + 1, PANEL_W - 1, -1, cfg, ox, oy, scale);

        Font font = mc.font;
        int x = PANEL_W + COLUMN_GAP;
        float bottom = 0f;
        List<RpgStat> stats = RpgStats.all();
        for (int i = stats.size() - 1; i >= 0; i--) {
            RpgStat stat = stats.get(i);
            stat.update(player, now, dt, cfg);
            if (stat.visibility <= 0.01f) continue;
            int rowH = stat.rowHeight();
            drawRow(g, font, stat, player, x, Math.round(bottom - rowH), now, cfg);
            bottom -= (rowH + ROW_GAP) * stat.visibility;
        }

        pose.popPose();
    }

    private static void drawPanel(GuiGraphics g, int x1, int y1, int x2, int y2) {
        Optional<ResourceLocation> frame = HudIconProvider.get("frame");
        if (frame.isPresent()) {
            RenderSystem.enableBlend();
            g.blit(frame.get(), x1, y1, x2 - x1, y2 - y1, 0f, 0f, 1, 1, 1, 1);
            RenderSystem.disableBlend();
            return;
        }
        g.fill(x1, y1, x2, y2, 0xCC000000);
        g.fill(x1 + 1, y1 + 1, x2 - 1, y2 - 1, 0x88202428);
    }

    private static void drawRow(GuiGraphics g, Font font, RpgStat st, Player player, int x, int top, long now, BarConfig cfg) {
        float a = st.visibility;
        int rowH = st.rowHeight();
        int barH = st.tall ? TALL_BAR_H : BAR_H;
        int rgb = st.color.rgb(player, st.value / st.max);

        drawIcon(g, st, x, top + (rowH - ICON) / 2, a, rgb);

        int bx = x + ICON + ICON_GAP;
        int by = top + (rowH - barH) / 2;
        drawBar(g, bx, by, BAR_W, barH, st, rgb, a, now, cfg);

        if (st.tall && a > 0.05f) {
            String text = String.format(Locale.ROOT, "%.1f / %.1f", st.value, st.max);
            g.drawString(font, text, bx + (BAR_W - font.width(text)) / 2, by + 1, argb(a, 0xFFFFFF), true);
        }
    }

    private static void drawBar(GuiGraphics g, int x, int y, int w, int h, RpgStat st, int rgb, float a, long now, BarConfig cfg) {
        g.fill(x - 1, y - 1, x + w + 1, y + h + 1, argb(a * 0.9f, 0x000000));
        g.fill(x, y, x + w, y + h, argb(a * 0.85f, HudDraw.scaleRgb(rgb, 0.25f)));

        int trail = Math.round(w * Mth.clamp(st.bar.trail() / st.max, 0f, 1f));
        int main = Math.round(w * Mth.clamp(st.bar.main() / st.max, 0f, 1f));
        if (trail > main) g.fill(x + main, y, x + trail, y + h, argb(a, 0xFFFFFF));
        if (main > 0) {
            int c = HudDraw.lerpRgb(rgb, 0xFFFFFF, st.bar.whiteness(now, cfg.subBarFlashTime));
            g.fillGradient(x, y, x + main, y + h, argb(a, HudDraw.lerpRgb(c, 0xFFFFFF, 0.3f)), argb(a, c));
        }
    }

    private static void drawIcon(GuiGraphics g, RpgStat st, int x, int y, float a, int rgb) {
        Optional<ResourceLocation> custom = HudIconProvider.get(st.iconName);
        if (custom.isPresent() || st.vanillaIcon != null) {
            RenderSystem.enableBlend();
            g.setColor(1f, 1f, 1f, a);
            if (custom.isPresent()) {
                g.blit(custom.get(), x, y, ICON, ICON, 0f, 0f, 1, 1, 1, 1);
            } else {
                for (int i = 0; i + 1 < st.vanillaIcon.length; i += 2) {
                    g.blit(VANILLA_ICONS, x, y, st.vanillaIcon[i], st.vanillaIcon[i + 1], ICON, ICON);
                }
            }
            g.setColor(1f, 1f, 1f, 1f);
            RenderSystem.disableBlend();
            return;
        }
        g.fill(x + 1, y + 1, x + ICON - 1, y + ICON - 1, argb(a, 0x000000));
        g.fill(x + 2, y + 2, x + ICON - 2, y + ICON - 2, argb(a, rgb));
    }

    private static int argb(float alpha, int rgb) {
        return (Mth.clamp(Math.round(alpha * 255f), 0, 255) << 24) | (rgb & 0xFFFFFF);
    }
}