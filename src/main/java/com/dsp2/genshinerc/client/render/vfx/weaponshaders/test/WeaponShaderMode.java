package com.dsp2.genshinerc.client.render.vfx.weaponshaders.test;

public enum WeaponShaderMode {
    NONE("none"),
    KALEIDOSCOPE("weaponvfxs/kaleidoscope"),
    PIXELSORT("weaponvfxs/pixelsort"),
    EDGEGLOW("weaponvfxs/edgeglow"),
    WAVE("weaponvfxs/wave"),
    EMBOSS("weaponvfxs/emboss"),
    CRT("weaponvfxs/crt"),
    GLITCH_SLICE("weaponvfxs/glitch_slice"),
    FISHEYE("weaponvfxs/fisheye"),
    RADIAL_BLUR("weaponvfxs/radial_blur"),
    HSV_FILTER("weaponvfxs/hsv_filter"),
    TEST_RED("weaponvfxs/test_red");

    public final String shaderName;

    WeaponShaderMode(String shaderName) {
        this.shaderName = shaderName;
    }

    public static volatile WeaponShaderMode currentMode = WeaponShaderMode.NONE;
}