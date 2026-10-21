package com.dsp2.genshinerc.client.render.bar.api;

public enum ElementType {
    PYRO("hud/pyro.png", null),
    BURNING("hud/pyro.png", "pyro_ani"),
    FROZEN("hud/cry_0.png", "cry_ani"),
    HYDRO("hud/hyd.png", null),
    ELECTRO("hud/elec.png", null),
    DENDRO("hud/den.png", "den_ani"),
    ANEMO("hud/ane.png", null),
    GEO("hud/geo.png", null),
    CRYO("hud/cry_0.png", null);

    private final String staticPath;
    private final String animFolder;

    ElementType(String staticPath, String animFolder) {
        this.staticPath = staticPath;
        this.animFolder = animFolder;
    }

    public String getStaticPath() { return staticPath; }
    public String getAnimFolder() { return animFolder; }
    public boolean isAnimated() { return animFolder != null; }
}