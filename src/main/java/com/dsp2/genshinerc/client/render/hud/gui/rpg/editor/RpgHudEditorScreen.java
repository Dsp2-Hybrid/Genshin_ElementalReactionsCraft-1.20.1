package com.dsp2.genshinerc.client.render.hud.gui.rpg.editor;

import com.dsp2.genshinerc.client.render.hud.bar.config.BarConfig;
import com.dsp2.genshinerc.client.render.hud.gui.rpg.RpgHudElement;
import com.dsp2.genshinerc.client.render.hud.gui.rpg.RpgHudElement.Box;
import com.dsp2.genshinerc.client.render.hud.gui.rpg.RpgHudOverlay;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.Locale;

public final class RpgHudEditorScreen extends Screen {
    private static final int DIM = 0xA0000000, BOX = 0x80FFFFFF, BOX_HOVER = 0xFFFFFFFF,
            BOX_SEL = 0xFF4FD8FF, FILL_SEL = 0x204FD8FF, HELP = 0xFFCCCCCC;
    private static final int HANDLE = 5, MIN_HIT = 6;
    private static final float SCROLL_STEP = 1.05f;

    private enum Drag { NONE, MOVE, RESIZE }

    @Nullable private final Screen parent;
    @Nullable private RpgHudElement selected;
    private Drag drag = Drag.NONE;
    private float grabX, grabY;
    private Box start;
    private boolean dirty;
    private boolean hideBoxes = false;

    public RpgHudEditorScreen(@Nullable Screen parent) {
        super(Component.translatable("genshinerc.hud_editor.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int w = 60, gap = 4, x = width / 2 - (w * 4 + gap * 3) / 2, y = 6;
        addRenderableWidget(Button.builder(Component.translatable("genshinerc.hud_editor.reset_selected"), b -> {
            if (selected != null) { selected.reset(cfg()); changed(); }
        }).bounds(x, y, w, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("genshinerc.hud_editor.reset_all"), b -> {
            for (RpgHudElement e : RpgHudElement.values()) e.reset(cfg());
            changed();
        }).bounds(x + w + gap, y, w, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("genshinerc.hud_editor.hide_boxes"), b -> {
            hideBoxes = !hideBoxes;
        }).bounds(x + (w + gap) * 2, y, w, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("genshinerc.hud_editor.done"), b -> onClose())
                .bounds(x + (w + gap) * 3, y, w, 20).build());
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        g.fill(0, 0, width, height, hideBoxes ? 0x40000000 : DIM);
        if (minecraft != null && minecraft.player != null) RpgHudOverlay.draw(g, height, true);
        else g.drawCenteredString(font, Component.translatable("genshinerc.hud_editor.no_world"), width / 2, height / 2, HELP);

        if (!hideBoxes) {
            RpgHudElement hover = drag == Drag.NONE ? hit(mouseX, mouseY) : null;
            for (RpgHudElement e : RpgHudElement.values()) drawGizmo(g, e, e == selected, e == hover);
            g.drawCenteredString(font, Component.translatable("genshinerc.hud_editor.help"), width / 2, 30, HELP);
        }
        super.render(g, mouseX, mouseY, partialTick);
    }

    private void drawGizmo(GuiGraphics g, RpgHudElement e, boolean sel, boolean hover) {
        int[] r = screenRect(e);
        int x = r[0], y = r[1], w = r[2], h = r[3];
        if (sel) g.fill(x, y, x + w, y + h, FILL_SEL);
        g.renderOutline(x, y, w, h, sel ? BOX_SEL : hover ? BOX_HOVER : BOX);
        if (!sel && !hover) return;

        String label = Component.translatable("genshinerc.hud_editor.element." + e.key()).getString();
        if (sel) {
            g.fill(x + w - HANDLE, y + h - HANDLE, x + w, y + h, BOX_SEL);
            Box b = e.get(cfg());
            label += String.format(Locale.ROOT, "  %.1f, %.1f  %.1f x %.1f", b.x(), b.y(), b.aw(), b.ah());
            if (b.flipX()) label += " [H]";
            if (b.flipY()) label += " [V]";
        }
        int ly = y - 10 >= 0 ? y - 10 : y + h + 2;
        g.drawString(font, label, x, ly, sel ? BOX_SEL : BOX_HOVER, true);
    }

    private void moveChild(BarConfig c, RpgHudElement e, float dx, float dy) {
        for (RpgHudElement child : e.children()) {
            Box cb = child.get(c);
            child.set(c, cb.withPos(cb.x() + dx, cb.y() + dy));
            moveChild(c, child, dx, dy);
        }
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (super.mouseClicked(mx, my, button)) return true;
        if (hideBoxes || button != GLFW.GLFW_MOUSE_BUTTON_LEFT) return false;

        if (selected != null && onHandle(selected, mx, my)) {
            start = selected.get(cfg());
            drag = Drag.RESIZE;
            return true;
        }
        selected = hit(mx, my);
        if (selected == null) return false;
        start = selected.get(cfg());
        grabX = localX(mx) - start.x();
        grabY = localY(my) - start.y();
        drag = Drag.MOVE;
        return true;
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (hideBoxes || drag == Drag.NONE || selected == null || button != GLFW.GLFW_MOUSE_BUTTON_LEFT)
            return super.mouseDragged(mx, my, button, dx, dy);
        BarConfig c = cfg();
        if (drag == Drag.MOVE) {
            float nx = snap(localX(mx) - grabX);
            float ny = snap(localY(my) - grabY);
            float dX = nx - start.x();
            float dY = ny - start.y();
            selected.set(c, start.withPos(nx, ny));
            moveChild(c, selected, dX, dY);
            start = selected.get(c);
        } else {
            resize(c, mx, my);
        }
        dirty = true;
        return true;
    }

    private void resize(BarConfig c, double mx, double my) {
        RpgHudElement e = selected;
        float minW = e.minW(c), minH = e.minH(c);
        float w = Math.max(minW, snap(localX(mx) - start.x()));
        float h = Math.max(minH, snap(localY(my) - start.y()));
        boolean lock = e.kind == RpgHudElement.Kind.ASPECT;
        if (lock || hasShiftDown()) {
            float k = Math.max(w / Math.max(1e-3f, start.aw()), h / Math.max(1e-3f, start.ah()));
            w = start.aw() * k;
            h = lock ? w * e.defaults().ah() / Math.max(1e-3f, e.defaults().aw()) : start.ah() * k;
            if (w < minW || h < minH) {
                float up = Math.max(minW / Math.max(1e-3f, w), minH / Math.max(1e-3f, h));
                w *= up; h *= up;
            }
            w = snap(w); h = snap(h);
        }
        e.set(c, start.withSize(w, h));
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        if (drag != Drag.NONE && button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            drag = Drag.NONE;
            saveIfDirty();
            return true;
        }
        return super.mouseReleased(mx, my, button);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double delta) {
        if (hideBoxes) return super.mouseScrolled(mx, my, delta);
        RpgHudElement e = selected != null && contains(screenRect(selected), mx, my) ? selected : hit(mx, my);
        if (e == null || delta == 0) return super.mouseScrolled(mx, my, delta);
        BarConfig c = cfg();
        float k = delta > 0 ? SCROLL_STEP : 1f / SCROLL_STEP;
        Box b = e.get(c);
        float w = Math.max(e.minW(c), b.aw() * k), h = Math.max(e.minH(c), b.ah() * k);
        e.set(c, new Box(b.cx() - w / 2f, b.cy() - h / 2f, b.w(), b.h()).withSize(w, h));
        selected = e;
        changed();
        return true;
    }

    @Override
    public boolean keyPressed(int key, int scan, int mods) {
        if (key == GLFW.GLFW_KEY_B) {
            hideBoxes = !hideBoxes;
            return true;
        }
        if (hideBoxes) return super.keyPressed(key, scan, mods);
        if (selected != null) {
            BarConfig c = cfg();
            Box b = selected.get(c);
            if (key == GLFW.GLFW_KEY_R) { selected.reset(c); changed(); return true; }
            if (selected.mirrorable && (key == GLFW.GLFW_KEY_H || key == GLFW.GLFW_KEY_V)) {
                boolean h = key == GLFW.GLFW_KEY_H;
                selected.set(c, b.withFlip(h != b.flipX(), !h != b.flipY()));
                changed();
                return true;
            }
            int step = hasShiftDown() ? 5 : 1, dx = 0, dy = 0;
            switch (key) {
                case GLFW.GLFW_KEY_LEFT -> dx = -step;
                case GLFW.GLFW_KEY_RIGHT -> dx = step;
                case GLFW.GLFW_KEY_UP -> dy = -step;
                case GLFW.GLFW_KEY_DOWN -> dy = step;
                default -> { }
            }
            if (dx != 0 || dy != 0) {
                selected.set(c, b.withPos(b.x() + dx, b.y() + dy));
                moveChild(c, selected, dx, dy);
                changed();
                return true;
            }
        }
        return super.keyPressed(key, scan, mods);
    }

    @Override
    public void onClose() {
        saveIfDirty();
        if (minecraft != null) minecraft.setScreen(parent);
    }

    @Override
    public void removed() { saveIfDirty(); }

    private static BarConfig cfg() { return BarConfig.get(); }
    private float localX(double sx) { BarConfig c = cfg(); return (float) ((sx - RpgHudOverlay.originX(c)) / c.rpgHudScale); }
    private float localY(double sy) { BarConfig c = cfg(); return (float) ((sy - RpgHudOverlay.originY(c, height)) / c.rpgHudScale); }

    private int[] screenRect(RpgHudElement e) {
        BarConfig c = cfg();
        Box b = e.get(c).abs();
        float s = c.rpgHudScale, ox = RpgHudOverlay.originX(c), oy = RpgHudOverlay.originY(c, height);
        float x = ox + b.x() * s, y = oy + b.y() * s, w = b.w() * s, h = b.h() * s;
        if (w < MIN_HIT) { x -= (MIN_HIT - w) / 2f; w = MIN_HIT; }
        if (h < MIN_HIT) { y -= (MIN_HIT - h) / 2f; h = MIN_HIT; }
        return new int[]{Math.round(x), Math.round(y), Math.round(w), Math.round(h)};
    }

    private static boolean contains(int[] r, double mx, double my) {
        return mx >= r[0] && mx < r[0] + r[2] && my >= r[1] && my < r[1] + r[3];
    }

    private boolean onHandle(RpgHudElement e, double mx, double my) {
        int[] r = screenRect(e);
        int hx = r[0] + r[2], hy = r[1] + r[3];
        return mx >= hx - HANDLE - 1 && mx <= hx + 1 && my >= hy - HANDLE - 1 && my <= hy + 1;
    }

    @Nullable
    private RpgHudElement hit(double mx, double my) {
        if (selected != null && contains(screenRect(selected), mx, my)) return selected;
        RpgHudElement[] all = RpgHudElement.values();
        for (int i = all.length - 1; i >= 0; i--) if (contains(screenRect(all[i]), mx, my)) return all[i];
        return null;
    }

    private static float snap(float v) { return hasAltDown() ? Math.round(v * 4f) / 4f : Math.round(v); }
    private void changed() { dirty = true; saveIfDirty(); }
    private void saveIfDirty() {
        if (!dirty) return;
        BarConfig.save();
        dirty = false;
    }
}