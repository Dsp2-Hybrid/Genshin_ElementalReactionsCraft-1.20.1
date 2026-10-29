package com.dsp2.genshinerc.client.render.hud.gui.rpg;

import com.dsp2.genshinerc.client.render.hud.bar.config.BarConfig;

import java.util.List;
import java.util.Locale;

public enum RpgHudElement {
    PORTRAIT(0, -52, 52, 52, 16, 16, Kind.FREE, false),
    LEVEL_TEXT(0, -66, 52, 12, 16, 6, Kind.FREE, false),
    EXP(1, -48, 23.5f, 46, 6, 10, Kind.ASPECT, true),
    HEALTH(48, -26, 110, 9, 20, 3, Kind.FREE, true),
    HP_ICON(38, -26, 8, 8, 3, 3, Kind.ASPECT, false),
    STAMINA(54, -19, 92, 5, 20, 2, Kind.FREE, true),
    STAMINA_ICON(44, -19, 5, 5, 3, 3, Kind.ASPECT, false),
    SHIELD(50, -29, 106, 2, 10, 1, Kind.FREE, true),
    ARMOR(54, -47, 92, 3.5f, 20, 1.5f, Kind.FREE, true),
    ARMOR_ICON(44, -47, 8, 8, 3, 3, Kind.ASPECT, false),
    TOUGHNESS(54, -43, 92, 3.5f, 20, 1.5f, Kind.FREE, true),
    TOUGHNESS_ICON(44, -43, 8, 8, 3, 3, Kind.ASPECT, false),
    HUNGER(54, -39, 92, 3.5f, 20, 1.5f, Kind.FREE, true),
    HUNGER_ICON(44, -39, 8, 8, 3, 3, Kind.ASPECT, false),
    EXHAUSTION(54, -35.5f, 92, 1, 10, 0.5f, Kind.FREE, true),
    ZHEN_MIE_BORDER(65, -63, 55, 14, 8, 6, Kind.NINE_SLICE, true),
    ZHEN_MIE(56, -61, 62, 10, 20, 4, Kind.FREE, true),
    ZHEN_MIE_LOGO(56, -61, 10, 10, 4, 4, Kind.ASPECT, false),
    ZHEN_MIE_TRIANGLE(89, -66, 7, 6, 3, 3, Kind.ASPECT, false),
    MOUNT(58, -10, 60, 3.5f, 20, 1.5f, Kind.FREE, true),
    MOUNT_ICON(51, -10.75f, 4.5f, 4.5f, 3, 3, Kind.ASPECT, false),
    OXYGEN(58, -4, 60, 3.5f, 20, 1.5f, Kind.FREE, true),
    OXYGEN_ICON(51, -4.5f, 4.5f, 4.5f, 3, 3, Kind.ASPECT, false);

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
        this.def = new Box(x, y, w, h);
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