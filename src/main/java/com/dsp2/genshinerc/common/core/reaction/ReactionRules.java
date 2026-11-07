package com.dsp2.genshinerc.common.core.reaction;

import com.dsp2.genshinerc.common.core.element.ElementType;
import org.jetbrains.annotations.Nullable;

final class ReactionRules {
    private static final ElementType[] NONE = {};

    private ReactionRules() {}

    static ElementType[] order(ElementType trigger) {
        return switch (trigger) {
            case PYRO -> new ElementType[]{ElementType.ELECTRO, ElementType.CRYO, ElementType.FROZEN,
                    ElementType.HYDRO, ElementType.QUICKEN, ElementType.DENDRO};
            case HYDRO -> new ElementType[]{ElementType.PYRO, ElementType.CRYO, ElementType.QUICKEN,
                    ElementType.DENDRO, ElementType.ELECTRO};
            case ELECTRO -> new ElementType[]{ElementType.PYRO, ElementType.CRYO, ElementType.FROZEN,
                    ElementType.QUICKEN, ElementType.DENDRO, ElementType.HYDRO};
            case CRYO -> new ElementType[]{ElementType.PYRO, ElementType.ELECTRO, ElementType.HYDRO};
            case DENDRO -> new ElementType[]{ElementType.PYRO, ElementType.ELECTRO, ElementType.HYDRO,
                    ElementType.QUICKEN};
            case ANEMO -> new ElementType[]{ElementType.PYRO, ElementType.ELECTRO, ElementType.HYDRO,
                    ElementType.CRYO, ElementType.FROZEN};
            case GEO -> new ElementType[]{ElementType.FROZEN, ElementType.PYRO, ElementType.ELECTRO,
                    ElementType.HYDRO, ElementType.CRYO};
            default -> NONE;
        };
    }

    @Nullable
    static Reaction rule(ElementType trigger, ElementType aura) {
        return switch (trigger) {
            case PYRO -> switch (aura) {
                case ELECTRO -> Reaction.OVERLOAD;
                case CRYO, FROZEN -> Reaction.MELT;
                case HYDRO -> Reaction.VAPORIZE;
                case QUICKEN, DENDRO -> Reaction.BURNING;
                default -> null;
            };
            case HYDRO -> switch (aura) {
                case PYRO -> Reaction.VAPORIZE;
                case CRYO -> Reaction.FREEZE;
                case QUICKEN, DENDRO -> Reaction.BLOOM;
                case ELECTRO -> Reaction.ELECTRO_CHARGED;
                default -> null;
            };
            case ELECTRO -> switch (aura) {
                case PYRO -> Reaction.OVERLOAD;
                case CRYO, FROZEN -> Reaction.SUPERCONDUCT;
                case DENDRO -> Reaction.CATALYZE;
                case QUICKEN -> Reaction.AGGRAVATE;
                case HYDRO -> Reaction.ELECTRO_CHARGED;
                default -> null;
            };
            case CRYO -> switch (aura) {
                case PYRO -> Reaction.MELT;
                case ELECTRO -> Reaction.SUPERCONDUCT;
                case HYDRO -> Reaction.FREEZE;
                default -> null;
            };
            case DENDRO -> switch (aura) {
                case PYRO -> Reaction.BURNING;
                case ELECTRO -> Reaction.CATALYZE;
                case HYDRO -> Reaction.BLOOM;
                case QUICKEN -> Reaction.SPREAD;
                default -> null;
            };
            case ANEMO -> switch (aura) {
                case PYRO, ELECTRO, HYDRO, CRYO, FROZEN -> Reaction.SWIRL;
                default -> null;
            };
            case GEO -> switch (aura) {
                case FROZEN -> Reaction.SHATTER;
                case PYRO, ELECTRO, HYDRO, CRYO -> Reaction.CRYSTALLIZE;
                default -> null;
            };
            default -> null;
        };
    }

    static float ratio(ElementType trigger, ElementType aura) {
        if (trigger == ElementType.ANEMO || trigger == ElementType.GEO) return 0.5f;
        if (trigger == ElementType.HYDRO && aura == ElementType.PYRO) return 2f;
        if (trigger == ElementType.PYRO && (aura == ElementType.CRYO || aura == ElementType.FROZEN)) return 2f;
        if (trigger == ElementType.DENDRO && aura == ElementType.HYDRO) return 2f;
        if (trigger == ElementType.PYRO && aura == ElementType.HYDRO) return 0.5f;
        if (trigger == ElementType.CRYO && aura == ElementType.PYRO) return 0.5f;
        if (trigger == ElementType.HYDRO && (aura == ElementType.DENDRO || aura == ElementType.QUICKEN)) return 0.5f;
        return 1f;
    }
}
