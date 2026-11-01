package com.dsp2.genshinerc.client.render.vfx.weaponshaders.test;

public enum WeaponShaderMode {
    NONE("none"),
    KALEIDOSCOPE("kaleidoscope"),
    PIXELSORT("pixelsort"),
    EDGEGLOW("edgeglow"),
    WAVE("wave"),
    EMBOSS("emboss"),
    CRT("crt"),
    GLITCH_SLICE("glitch_slice"),
    FISHEYE("fisheye"),
    RADIAL_BLUR("radial_blur"),
    HSV_FILTER("hsv_filter"),
    TWIST("twist"),
    PULSE("pulse"),
    RGBSPLIT("rgbsplit"),
    PIXELATE("pixelate"),
    SQUASH("squash"),
    MIRROR("mirror"),
    VORTEX("vortex"),
    SOBEL_EDGE("sobel_edge"),
    HALFTONE("halftone"),
    OIL_PAINT("oil_paint"),
    NEON_GLOW("neon_glow"),
    THERMAL("thermal"),
    POSTERIZE("posterize"),
    RIPPLE("ripple"),
    NOISE_GRAIN("noise_grain"),
    ZOOM_BLUR("zoom_blur"),
    DIAMOND_GRID("diamond_grid"),
    HEXAGON("hexagon"),
    CHROMATIC("chromatic"),
    BLOOM("bloom"),
    TEST_RED("test_red");

    public final String shaderName;

    WeaponShaderMode(String shaderName) {
        this.shaderName = shaderName;
    }

    public static volatile WeaponShaderMode currentMode = WeaponShaderMode.NONE;
}