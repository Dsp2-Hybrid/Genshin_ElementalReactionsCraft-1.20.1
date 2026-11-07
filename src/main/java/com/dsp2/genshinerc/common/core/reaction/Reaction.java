package com.dsp2.genshinerc.common.core.reaction;

import com.dsp2.genshinerc.common.core.damage.DamageKind;
import com.dsp2.genshinerc.common.core.element.ElementType;
import org.jetbrains.annotations.Nullable;

public enum Reaction {
    MELT(0f, true, null, ElementType.PYRO, ElementType.CRYO),
    VAPORIZE(0f, true, null, ElementType.PYRO, ElementType.HYDRO),
    OVERLOAD(4.0f, true, "overload", ElementType.PYRO, ElementType.ELECTRO),
    SUPERCONDUCT(1.0f, true, "superconduct", ElementType.CRYO, ElementType.ELECTRO),
    ELECTRO_CHARGED(2.4f, false, "electro_charged", ElementType.ELECTRO, ElementType.HYDRO),
    FREEZE(0f, true, null, ElementType.HYDRO, ElementType.CRYO),
    SHATTER(3.0f, false, "shatter", ElementType.FROZEN, null),
    SWIRL(1.2f, true, null, ElementType.ANEMO, null),
    CRYSTALLIZE(0f, true, null, ElementType.GEO, null),
    BLOOM(4.0f, true, "bloom", ElementType.DENDRO, ElementType.HYDRO),
    HYPERBLOOM(6.0f, false, "hyperbloom", ElementType.DENDRO, ElementType.ELECTRO),
    BURGEON(6.0f, false, "burgeon", ElementType.DENDRO, ElementType.PYRO),
    BURNING(0.5f, false, "burning", ElementType.PYRO, ElementType.DENDRO),
    CATALYZE(0f, true, "quicken", ElementType.DENDRO, ElementType.ELECTRO),
    AGGRAVATE(0f, false, "aggravate", ElementType.ELECTRO, ElementType.DENDRO),
    SPREAD(0f, false, "spread", ElementType.DENDRO, ElementType.ELECTRO);

    private final float coefficient;
    private final boolean consumes;
    @Nullable private final String colorKey;
    @Nullable private final ElementType first;
    @Nullable private final ElementType second;

    Reaction(float coefficient, boolean consumes, @Nullable String colorKey,
             @Nullable ElementType first, @Nullable ElementType second) {
        this.coefficient = coefficient;
        this.consumes = consumes;
        this.colorKey = colorKey;
        this.first = first;
        this.second = second;
    }

    public float coefficient() { return coefficient; }
    public boolean consumes() { return consumes; }
    public boolean once() { return this != SWIRL && this != CRYSTALLIZE; }

    @Nullable public ElementType first() { return first; }
    @Nullable public ElementType second() { return second; }

    public String indicatorKey(DamageKind kind) {
        return colorKey != null ? colorKey : kind.indicatorKey();
    }
}
