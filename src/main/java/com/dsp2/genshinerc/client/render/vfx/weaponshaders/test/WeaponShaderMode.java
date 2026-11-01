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

    DISTORT_WAVE("none"),
    DISTORT_RADIAL("none"),
    RGB_SPLIT("none"),
    PIXELATE("none"),
    SCANLINE("none"),
    KALEIDO("none"),
    GLITCH("none"),
    FISHEYE_SCREEN("none"),
    INVERT("none"),
    DISTORT_TWIST("none"),
    DISTORT_MIRROR("none"),
    TEST_RED("test_red");

    public final String shaderName;

    WeaponShaderMode(String shaderName) {
        this.shaderName = shaderName;
    }

    public static volatile WeaponShaderMode currentMode = WeaponShaderMode.NONE;
}