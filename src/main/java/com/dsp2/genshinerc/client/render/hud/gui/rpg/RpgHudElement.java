package com.dsp2.genshinerc.client.render.hud.gui.rpg;

import com.dsp2.genshinerc.client.render.hud.bar.config.BarConfig;

import java.util.List;
import java.util.Locale;

public enum RpgHudElement {
    PORTRAIT(-8.0f, -87.0f, 81.0f, 77.0f, 16, 16, Kind.FREE, false, false, false),
    LEVEL_TEXT(1.0f, -11.0f, 58.0f, 15.0f, 16, 6, Kind.FREE, false, false, false),
    EXP(-6.0f, -81.0f, 35.0f, 69.0f, 6, 10, Kind.ASPECT, true, false, false),
    HEALTH(59.0f, -36.0f, 138.0f, 10.0f, 20, 3, Kind.FREE, true, false, false),
    HP_ICON(55.0f, -36.0f, 8.0f, 8.0f, 3, 3, Kind.ASPECT, false, false, false),
    STAMINA(57.0f, -27.0f, 141.0f, 4.0f, 20, 2, Kind.FREE, true, false, false),
    STAMINA_ICON(55.0f, -28.0f, 7.0f, 7.0f, 3, 3, Kind.ASPECT, false, false, false),
    SHIELD(68.0f, -38.0f, 122.0f, 2.0f, 10, 1, Kind.FREE, true, false, false),
    ARMOR(66.0f, -59.0f, 93.0f, 6.0f, 20, 1.5f, Kind.FREE, true, true, false),
    ARMOR_ICON(62.0f, -59.0f, 7.0f, 7.0f, 3, 3, Kind.ASPECT, false, false, false),
    TOUGHNESS(72.0f, -63.0f, 89.0f, 5.0f, 20, 1.5f, Kind.FREE, true, true, false),
    TOUGHNESS_ICON(68.0f, -64.0f, 7.0f, 7.0f, 3, 3, Kind.ASPECT, false, false, false),
    HUNGER(68.0f, -51.0f, 107.0f, 8.0f, 20, 1.5f, Kind.FREE, true, false, false),
    HUNGER_ICON(65.0f, -51.0f, 8.0f, 8.0f, 3, 3, Kind.ASPECT, false, false, false),
    EXHAUSTION(69.0f, -52.0f, 99.0f, 1.0f, 10, 0.5f, Kind.FREE, true, false, false),
    ZHEN_MIE_BORDER(45.0f, -19.0f, 167.0f, 9.0f, 8, 6, Kind.NINE_SLICE, true, false, false),
    ZHEN_MIE(42.0f, -19.0f, 176.0f, 9.0f, 20, 4, Kind.FREE, true, false, false),
    ZHEN_MIE_LOGO(36.0f, -21.0f, 11.0f, 11.0f, 4, 4, Kind.ASPECT, false, false, false),
    ZHEN_MIE_TRIANGLE(113.0f, -26.0f, 28.0f, 24.0f, 3, 3, Kind.ASPECT, false, false, false),
    MOUNT(61.0f, -76.0f, 81.0f, 5.0f, 20, 1.5f, Kind.FREE, true, false, false),
    MOUNT_ICON(57.0f, -76.8f, 7.0f, 7.0f, 3, 3, Kind.ASPECT, false, false, false),
    OXYGEN(61.0f, -72.0f, 83.0f, 7.0f, 20, 1.5f, Kind.FREE, true, false, false),
    OXYGEN_ICON(56.0f, -72.0f, 8.0f, 8.0f, 3, 3, Kind.ASPECT, false, false, false);

    public enum Kind { FREE, ASPECT, NINE_SLICE }

    public record Box(float x, float y, float w, float h, boolean flipX, boolean flipY) {
        public Box(float x, float y, float w, float h) { this(x, y, w, h, false, false); }
        public float aw() { return Math.abs(w); }
        public float ah() { return Math.abs(h); }
        public float x2() { return x + aw(); }
        public float y2() { return y + ah(); }
        public float cx() { return x + aw() / 2f; }
        public float cy() { return y + ah() / 2f; }
        public Box abs() { return new Box(x, y, aw(), ah(), flipX, flipY); }
        public Box withPos(float nx, float ny) { return new Box(nx, ny, w, h, flipX, flipY); }
        public Box withSize(float nw, float nh) { return new Box(x, y, Math.abs(nw), Math.abs(nh), flipX, flipY); }
        public Box withFlip(boolean fx, boolean fy) { return new Box(x, y, w, h, fx, fy); }
    }

    private final Box def;
    private final float minW, minH;
    public final Kind kind;
    public final boolean mirrorable;

    RpgHudElement(float x, float y, float w, float h, float minW, float minH, Kind kind, boolean mirrorable) {
        this(x, y, w, h, minW, minH, kind, mirrorable, false, false);
    }

    RpgHudElement(float x, float y, float w, float h, float minW, float minH, Kind kind, boolean mirrorable, boolean fx, boolean fy) {
        this.def = new Box(x, y, w, h, fx, fy);
        this.minW = minW;
        this.minH = minH;
        this.kind = kind;
        this.mirrorable = mirrorable;
    }

    public List<RpgHudElement> children() {
        return switch (this) {
            case HEALTH -> List.of(SHIELD, STAMINA, HP_ICON);
            case STAMINA -> List.of(STAMINA_ICON);
            case ARMOR -> List.of(TOUGHNESS, ARMOR_ICON);
            case TOUGHNESS -> List.of(TOUGHNESS_ICON);
            case HUNGER -> List.of(EXHAUSTION, HUNGER_ICON);
            case MOUNT -> List.of(MOUNT_ICON);
            case OXYGEN -> List.of(OXYGEN_ICON);
            case ZHEN_MIE_BORDER -> List.of(ZHEN_MIE, ZHEN_MIE_TRIANGLE);
            case ZHEN_MIE -> List.of(ZHEN_MIE_LOGO);
            default -> List.of();
        };
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
        Box b = switch (this) {
            case PORTRAIT -> new Box(c.rpgPortraitX, c.rpgPortraitY, c.rpgPortraitW, c.rpgPortraitH, c.rpgPortraitFlipX, c.rpgPortraitFlipY);
            case LEVEL_TEXT -> new Box(c.rpgLevelTextX, c.rpgLevelTextY, c.rpgLevelTextW, c.rpgLevelTextH, c.rpgLevelTextFlipX, c.rpgLevelTextFlipY);
            case HEALTH -> new Box(c.rpgHpX, c.rpgHpY, c.rpgHpW, c.rpgHpH, c.rpgHpFlipX, c.rpgHpFlipY);
            case STAMINA -> new Box(c.rpgStaminaX, c.rpgStaminaY, c.rpgStaminaW, c.rpgStaminaH, c.rpgStaminaFlipX, c.rpgStaminaFlipY);
            case SHIELD -> new Box(c.rpgShieldX, c.rpgShieldY, c.rpgShieldW, c.rpgShieldH, c.rpgShieldFlipX, c.rpgShieldFlipY);
            default -> {
                float[] v = c.layout().get(key());
                yield v != null && v.length >= 6 ? new Box(v[0], v[1], v[2], v[3], v[4] != 0, v[5] != 0) : def;
            }
        };
        return mirrorable ? b : b.withFlip(false, false);
    }

    public void set(BarConfig c, Box b) {
        float w = Math.max(minW(c), b.aw()), h = Math.max(minH(c), b.ah());
        boolean fx = mirrorable && b.flipX(), fy = mirrorable && b.flipY();
        switch (this) {
            case PORTRAIT -> { c.rpgPortraitX = b.x(); c.rpgPortraitY = b.y(); c.rpgPortraitW = w; c.rpgPortraitH = h; c.rpgPortraitFlipX = fx; c.rpgPortraitFlipY = fy; }
            case LEVEL_TEXT -> { c.rpgLevelTextX = b.x(); c.rpgLevelTextY = b.y(); c.rpgLevelTextW = w; c.rpgLevelTextH = h; c.rpgLevelTextFlipX = fx; c.rpgLevelTextFlipY = fy; }
            case HEALTH -> { c.rpgHpX = b.x(); c.rpgHpY = b.y(); c.rpgHpW = w; c.rpgHpH = h; c.rpgHpFlipX = fx; c.rpgHpFlipY = fy; }
            case STAMINA -> { c.rpgStaminaX = b.x(); c.rpgStaminaY = b.y(); c.rpgStaminaW = w; c.rpgStaminaH = h; c.rpgStaminaFlipX = fx; c.rpgStaminaFlipY = fy; }
            case SHIELD -> { c.rpgShieldX = b.x(); c.rpgShieldY = b.y(); c.rpgShieldW = w; c.rpgShieldH = h; c.rpgShieldFlipX = fx; c.rpgShieldFlipY = fy; }
            default -> c.layout().put(key(), new float[]{b.x(), b.y(), w, h, fx ? 1f : 0f, fy ? 1f : 0f});
        }
    }
}