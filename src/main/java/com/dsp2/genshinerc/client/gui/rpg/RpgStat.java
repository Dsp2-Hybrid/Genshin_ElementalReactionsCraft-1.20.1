package com.dsp2.genshinerc.client.gui.rpg;

import com.dsp2.genshinerc.client.render.bar.anim.AnimatedBarValue;
import com.dsp2.genshinerc.client.render.bar.config.BarConfig;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public final class RpgStat {
    public interface Source {
        boolean present(Player player);
        float value(Player player);
        float max(Player player);
    }

    public interface FloatGetter { float get(Player player); }
    public interface Presence { boolean test(Player player); }
    public interface ColorFn { int rgb(Player player, float fraction); }

    public static final Source NONE = source(p -> false, p -> 0f, p -> 1f);
    private static final float VISIBILITY_SPEED = 14f;

    final String iconName;
    private Source source;
    final ColorFn color;
    final boolean trailOnDrop;
    final boolean tall;
    @Nullable final int[] vanillaIcon;

    final AnimatedBarValue bar = new AnimatedBarValue();
    float visibility;
    float value, max = 1f;

    RpgStat(String iconName, Source source, ColorFn color, boolean trailOnDrop, boolean tall, @Nullable int[] vanillaIcon) {
        this.iconName = iconName;
        this.source = source;
        this.color = color;
        this.trailOnDrop = trailOnDrop;
        this.tall = tall;
        this.vanillaIcon = vanillaIcon;
    }

    public static Source source(Presence present, FloatGetter value, FloatGetter max) {
        return new Source() {
            @Override public boolean present(Player p) { return present.test(p); }
            @Override public float value(Player p) { return value.get(p); }
            @Override public float max(Player p) { return max.get(p); }
        };
    }

    public void setSource(Source source) { this.source = source == null ? NONE : source; }
    public float value() { return value; }
    public float max() { return max; }

    void update(Player player, long nowMs, float dt, BarConfig cfg) {
        boolean present = source.present(player);
        float target = present ? 1f : 0f;
        visibility += (target - visibility) * (1f - (float) Math.exp(-VISIBILITY_SPEED * dt));
        if (Math.abs(target - visibility) < 0.01f) visibility = target;

        if (present) {
            value = Math.max(0f, source.value(player));
            max = Math.max(1.0e-3f, source.max(player));
            bar.set(value, nowMs, cfg.subBarWhiteHoldTime, trailOnDrop);
        }
        bar.update(nowMs, cfg.subBarFillSpeed, cfg.subBarWhiteDrainSpeed);
    }

    int rowHeight() { return tall ? RpgHudLayout.TALL_BAR_H : RpgHudLayout.ICON; }
}