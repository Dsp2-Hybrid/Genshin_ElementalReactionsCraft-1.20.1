package com.dsp2.genshinerc.common.core.reaction;

import com.dsp2.genshinerc.client.render.world.component.damageindicator.config.DamageIndicatorConfig;
import com.dsp2.genshinerc.common.core.element.ElementType;
import org.jetbrains.annotations.Nullable;

public final class ReactionColors {
    private ReactionColors() {}
    public record Ends(String a, @Nullable String b) {}

    public static Ends ends(Reaction r, @Nullable ElementType first, @Nullable ElementType second) {
        String extra = extraKey(r);
        
        if (r == Reaction.SWIRL) return new Ends(key("anemo"), null);
        if (r == Reaction.CRYSTALLIZE) return new Ends(key("geo"), null);

        if (extra != null) {
            ElementType pure = pureOwner(r, first, second);
            ElementType oth = other(pure, first, second);
            String otherKey = oth != null ? oth.colorKey() : "physical";
            return new Ends(key(extra), key(otherKey));
        }
        
        if (first != null && second != null) return new Ends(key(first.colorKey()), key(second.colorKey()));
        
        ElementType pure = pureOwner(r, first, second);
        String pk = pure != null ? pure.colorKey() : "physical";
        return new Ends(key(pk), null);
    }

    @Nullable
    private static ElementType pureOwner(Reaction r, @Nullable ElementType first, @Nullable ElementType second) {
        return switch (r) {
            case OVERLOAD, BURNING, VAPORIZE -> ElementType.PYRO;
            case SUPERCONDUCT, FREEZE, MELT  -> ElementType.CRYO;
            case ELECTRO_CHARGED, AGGRAVATE  -> ElementType.ELECTRO;
            case BLOOM, CATALYZE             -> ElementType.DENDRO;
            case SPREAD                      -> ElementType.DENDRO;
            case SHATTER                     -> ElementType.CRYO;
            default                          -> first != null ? first : second;
        };
    }

    @Nullable
    private static ElementType other(@Nullable ElementType pure, @Nullable ElementType a, @Nullable ElementType b) {
        if (a != null && a != pure) return a;
        if (b != null && b != pure) return b;
        return switch (pure) {
            case PYRO         -> ElementType.ELECTRO;
            case CRYO         -> ElementType.ELECTRO;
            case ELECTRO      -> ElementType.HYDRO;
            case DENDRO       -> ElementType.ELECTRO;
            default           -> null;
        };
    }

    @Nullable
    private static String extraKey(Reaction r) {
        return switch (r) {
            case OVERLOAD        -> "overload";
            case ELECTRO_CHARGED -> "electro_charged";
            case SHATTER         -> "shatter";
            case BLOOM           -> "bloom";
            case BURNING         -> "burning";
            case CATALYZE        -> "quicken";
            case AGGRAVATE       -> "aggravate";
            case SPREAD          -> "spread";
            case HYPERBLOOM      -> "hyperbloom";
            case BURGEON         -> "burgeon";
            default              -> null;
        };
    }

    private static String key(String key) {
        try { return DamageIndicatorConfig.get().colorFor(key); }
        catch (Exception e) { return "#FFFFFF"; }
    }
}