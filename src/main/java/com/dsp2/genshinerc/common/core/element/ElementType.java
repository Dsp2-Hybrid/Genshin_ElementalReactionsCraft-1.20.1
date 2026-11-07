package com.dsp2.genshinerc.common.core.element;

import org.jetbrains.annotations.Nullable;

public enum ElementType {
    PYRO("pyro.png", null, "pyro"),
    HYDRO("hyd.png", null, "hydro"),
    ELECTRO("elec.png", null, "electro"),
    CRYO("cry_0.png", null, "cryo"),
    DENDRO("den.png", null, "dendro"),
    ANEMO("ane.png", null, "anemo"),
    GEO("geo.png", null, "geo"),
    BURNING("pyro.png", "pyro_ani", "pyro"),
    FROZEN("cry_0.png", "cry_ani", "cryo"),
    QUICKEN("den.png", null, "dendro");

    private static final ElementType[] DEBUG_ORDER = {PYRO, HYDRO, ELECTRO, CRYO, DENDRO, ANEMO, GEO};

    private final String staticPath;
    private final String animFolder;
    private final String colorKey;

    ElementType(String staticPath, String animFolder, String colorKey) {
        this.staticPath = staticPath;
        this.animFolder = animFolder;
        this.colorKey = colorKey;
    }

    public String getStaticPath() { return staticPath; }
    public String getAnimFolder() { return animFolder; }
    public boolean isAnimated() { return animFolder != null; }
    public String colorKey() { return colorKey; }

    @Nullable
    public static ElementType fromDebugId(int id) {
        return id >= 1 && id <= DEBUG_ORDER.length ? DEBUG_ORDER[id - 1] : null;
    }
}
