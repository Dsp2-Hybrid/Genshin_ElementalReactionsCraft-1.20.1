package com.dsp2.genshinerc.client.render.hud.gui.rpg;

import com.dsp2.genshinerc.client.render.hud.bar.config.BarConfig;

import java.util.Locale;

public enum RpgHudElement {
    PORTRAIT(0, -52, 52, 52, 16, 16, Kind.FREE, false),
    LEVEL_TEXT(0, -66, 52, 12, 16, 6, Kind.FREE, false),
    EXP(1, -48, 23.5f, 46, 6, 10, Kind.ASPECT, true),
    HEALTH(48, -26, 110, 9, 20, 3, Kind.FREE, true),
    HP_ICON(38, -26, 8, 8, 3, 3, Kind.ASPECT, true),
    STAMINA(54, -19, 92, 5, 20, 2, Kind.FREE, true),
    SHIELD(50, -29, 106, 2, 10, 1, Kind.FREE, true),
    ARMOR(54, -47, 92, 3.5f, 20, 1.5f, Kind.FREE, true),
    ARMOR_ICON(44, -47, 8, 8, 3, 3, Kind.ASPECT, true),
    TOUGHNESS(54, -43, 92, 3.5f, 20, 1.5f, Kind.FREE, true),
    TOUGHNESS_ICON(44, -43, 8, 8, 3, 3, Kind.ASPECT, true),
    HUNGER(54, -39, 92, 3.5f, 20, 1.5f, Kind.FREE, true),
    HUNGER_ICON(44, -39, 8, 8, 3, 3, Kind.ASPECT, true),
    EXHAUSTION(54, -35.5f, 92, 1, 10, 0.5f, Kind.FREE, true),
    ZHEN_MIE_BORDER(65, -63, 55, 14, 8, 6, Kind.NINE_SLICE, true),
    ZHEN_MIE(56, -61, 62, 10, 20, 4, Kind.FREE, true),
    ZHEN_MIE_TRIANGLE(89, -66, 7, 6, 3, 3, Kind.ASPECT, true),
    MOUNT(58, -10, 60, 3.5f, 20, 1.5f, Kind.FREE, true),
    MOUNT_ICON(51, -10.75f, 4.5f, 4.5f, 3, 3, Kind.ASPECT, true),
    OXYGEN(58, -4, 60, 3.5f, 20, 1.5f, Kind.FREE, true);

    public enum Kind { FREE, ASPECT, NINE_SLICE }

    public record Box(float x, float y, float w, float h) {
        public float aw() { return Math.abs(w); }
        public float ah() { return Math.abs(h); }
        public float x2() { return x + aw(); }
        public float y2() { return y + ah(); }
        public float cx() { return x + aw() / 2f; }
        public float cy() { return y + ah() / 2f; }
        public boolean flipX() { return w < 0f; }
        public boolean flipY() { return h < 0f; }
        public Box abs() { return new Box(x, y, aw(), ah()); }
        public Box withPos(float nx, float ny) { return new Box(nx, ny, w, h); }
        public Box withSize(float nw, float nh) {
            return new Box(x, y, flipX() ? -Math.abs(nw) : Math.abs(nw), flipY() ? -Math.abs(nh) : Math.abs(nh));
        }
        public Box withFlip(boolean fx, boolean fy) { return new Box(x, y, fx ? -aw() : aw(), fy ? -ah() : ah()); }
    }

    private final Box def;
    private final float minW, minH;
    public final Kind kind;
    public final boolean mirrorable;

    RpgHudElement(float x, float y, float w, float h, float minW, float minH, Kind kind, boolean mirrorable) {
        this.def = new Box(x, y, w, h);
        this.minW = minW;
        this.minH = minH;
        this.kind = kind;
        this.mirrorable = mirrorable;
    }

    public Box defaults() { return def; }
    public String key() { return name().toLowerCase(Locale.ROOT); }
    public void reset(BarConfig c) { set(c, def); }

    public float minW(BarConfig c) {
        return kind == Kind.NINE_SLICE ? Math.max(minW, RpgHudOverlay.frameCorner(c).w() * 2f + 1f) : minW;
    }

    public float minH(BarConfig c) {
        return kind == Kind.NINE_SLICE ? Math.max(minH, RpgHudOverlay.frameCorner(c).h() * 2f + 1f) : minH;
    }

    public Box get(BarConfig c) {
        return switch (this) {
            case PORTRAIT -> new Box(c.rpgPortraitX, c.rpgPortraitY, c.rpgPortraitW, c.rpgPortraitH);
            case LEVEL_TEXT -> new Box(c.rpgLevelTextX, c.rpgLevelTextY, c.rpgLevelTextW, c.rpgLevelTextH);
            case HEALTH -> new Box(c.rpgHpX, c.rpgHpY, c.rpgHpW, c.rpgHpH);
            case STAMINA -> new Box(c.rpgStaminaX, c.rpgStaminaY, c.rpgStaminaW, c.rpgStaminaH);
            case SHIELD -> new Box(c.rpgShieldX, c.rpgShieldY, c.rpgShieldW, c.rpgShieldH);
            default -> {
                float[] v = c.layout().get(key());
                yield v != null && v.length == 4 ? new Box(v[0], v[1], v[2], v[3]) : def;
            }
        };
    }

    public void set(BarConfig c, Box b) {
        float w = Math.max(minW(c), b.aw()), h = Math.max(minH(c), b.ah());
        Box n = new Box(b.x(), b.y(), mirrorable && b.flipX() ? -w : w, mirrorable && b.flipY() ? -h : h);
        switch (this) {
            case PORTRAIT -> { c.rpgPortraitX = n.x(); c.rpgPortraitY = n.y(); c.rpgPortraitW = n.w(); c.rpgPortraitH = n.h(); }
            case LEVEL_TEXT -> { c.rpgLevelTextX = n.x(); c.rpgLevelTextY = n.y(); c.rpgLevelTextW = n.w(); c.rpgLevelTextH = n.h(); }
            case HEALTH -> { c.rpgHpX = n.x(); c.rpgHpY = n.y(); c.rpgHpW = n.w(); c.rpgHpH = n.h(); }
            case STAMINA -> { c.rpgStaminaX = n.x(); c.rpgStaminaY = n.y(); c.rpgStaminaW = n.w(); c.rpgStaminaH = n.h(); }
            case SHIELD -> { c.rpgShieldX = n.x(); c.rpgShieldY = n.y(); c.rpgShieldW = n.w(); c.rpgShieldH = n.h(); }
            default -> c.layout().put(key(), new float[]{n.x(), n.y(), n.w(), n.h()});
        }
    }
}