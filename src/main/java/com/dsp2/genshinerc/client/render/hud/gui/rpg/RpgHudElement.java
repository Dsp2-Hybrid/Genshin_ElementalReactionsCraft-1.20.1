package com.dsp2.genshinerc.client.render.hud.gui.rpg;

import com.dsp2.genshinerc.client.render.hud.bar.config.BarConfig;
import java.util.Locale;

public enum RpgHudElement {
    PORTRAIT(0, -52, 52, 52, 16, 16),
    LEVEL_TEXT(0, -66, 52, 12, 16, 6),
    HEALTH(48, -26, 110, 9, 20, 3),
    STAMINA(54, -19, 92, 5, 20, 2),
    SHIELD(50, -29, 106, 2, 10, 1),
    ZHEN_MIE(48, -43, 62, 10, 20, 4),
    STACK(112, -80, 46, 48, 20, 15);

    public record Box(float x, float y, float w, float h) {
        public float x2() { return x + w; }
        public float y2() { return y + h; }
        public float cx() { return x + w / 2f; }
        public float cy() { return y + h / 2f; }
        public Box withPos(float nx, float ny) { return new Box(nx, ny, w, h); }
    }

    private final Box def;
    public final float minW, minH;

    RpgHudElement(float x, float y, float w, float h, float minW, float minH) {
        this.def = new Box(x, y, w, h);
        this.minW = minW;
        this.minH = minH;
    }

    public Box defaults() { return def; }
    public String key() { return name().toLowerCase(Locale.ROOT); }
    public void reset(BarConfig c) { set(c, def); }

    public Box get(BarConfig c) {
        return switch (this) {
            case PORTRAIT -> new Box(c.rpgPortraitX, c.rpgPortraitY, c.rpgPortraitW, c.rpgPortraitH);
            case LEVEL_TEXT -> new Box(c.rpgLevelTextX, c.rpgLevelTextY, c.rpgLevelTextW, c.rpgLevelTextH);
            case HEALTH -> new Box(c.rpgHpX, c.rpgHpY, c.rpgHpW, c.rpgHpH);
            case STAMINA -> new Box(c.rpgStaminaX, c.rpgStaminaY, c.rpgStaminaW, c.rpgStaminaH);
            case SHIELD -> new Box(c.rpgShieldX, c.rpgShieldY, c.rpgShieldW, c.rpgShieldH);
            case ZHEN_MIE -> new Box(c.rpgZhenMieX, c.rpgZhenMieY, c.rpgZhenMieW, c.rpgZhenMieH);
            case STACK -> new Box(c.rpgStackX, c.rpgStackY, c.rpgStackW, c.rpgStackH);
        };
    }

    public void set(BarConfig c, Box b) {
        float w = Math.max(minW, b.w()), h = Math.max(minH, b.h());
        switch (this) {
            case PORTRAIT -> { c.rpgPortraitX = b.x(); c.rpgPortraitY = b.y(); c.rpgPortraitW = w; c.rpgPortraitH = h; }
            case LEVEL_TEXT -> { c.rpgLevelTextX = b.x(); c.rpgLevelTextY = b.y(); c.rpgLevelTextW = w; c.rpgLevelTextH = h; }
            case HEALTH -> { c.rpgHpX = b.x(); c.rpgHpY = b.y(); c.rpgHpW = w; c.rpgHpH = h; }
            case STAMINA -> { c.rpgStaminaX = b.x(); c.rpgStaminaY = b.y(); c.rpgStaminaW = w; c.rpgStaminaH = h; }
            case SHIELD -> { c.rpgShieldX = b.x(); c.rpgShieldY = b.y(); c.rpgShieldW = w; c.rpgShieldH = h; }
            case ZHEN_MIE -> { c.rpgZhenMieX = b.x(); c.rpgZhenMieY = b.y(); c.rpgZhenMieW = w; c.rpgZhenMieH = h; }
            case STACK -> { c.rpgStackX = b.x(); c.rpgStackY = b.y(); c.rpgStackW = w; c.rpgStackH = h; }
        }
    }
}