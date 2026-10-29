package com.dsp2.genshinerc.common.core.combat.shield;

import com.dsp2.genshinerc.common.core.element.ElementType;
import org.jetbrains.annotations.Nullable;
import java.util.Objects;

public final class ShieldInstance {
    private final ShieldKind kind;
    @Nullable private final ElementType element;
    private float amount;
    private float max;
    private int ticksLeft;

    ShieldInstance(ShieldKind kind, @Nullable ElementType element, float amount, int durationTicks) {
        this(kind, element, amount, amount, durationTicks);
    }

    public ShieldInstance(ShieldKind kind, @Nullable ElementType element, float amount, float max, int ticksLeft) {
        this.kind = kind;
        this.element = element;
        this.amount = amount;
        this.max = max;
        this.ticksLeft = ticksLeft;
    }

    public ShieldKind kind() { return kind; }
    @Nullable public ElementType element() { return element; }
    public float amount() { return amount; }
    public float max() { return max; }
    public int ticksLeft() { return ticksLeft; }

    boolean sameAttribute(ShieldKind kind, @Nullable ElementType element) {
        return this.kind == kind && Objects.equals(this.element, element);
    }

    void stack(float add, int durationTicks) {
        amount += add;
        max = Math.max(max, amount);
        ticksLeft = (ticksLeft < 0 || durationTicks < 0) ? -1 : Math.max(ticksLeft, durationTicks);
    }

    void damage(float value) {
        amount -= value;
    }

    boolean tickExpired() {
        if (amount <= 0f) return true;
        return ticksLeft >= 0 && --ticksLeft <= 0;
    }
}