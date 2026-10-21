package com.dsp2.genshinerc.client.render.bar.api;

public enum ElementType {
    PYRO("pyro.png", null),
    BURNING("pyro.png", "pyro_ani"),
    FROZEN("cry_0.png", "cry_ani"),
    HYDRO("hyd.png", null),
    ELECTRO("elec.png", null),
    DENDRO("den.png", "den_ani"),
    ANEMO("ane.png", null),
    GEO("geo.png", null),
    CRYO("cry_0.png", null);

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