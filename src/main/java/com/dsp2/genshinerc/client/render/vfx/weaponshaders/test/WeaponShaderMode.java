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
    HSV_FILTER("hsv_filter");

    public final String shaderName;

    WeaponShaderMode(String shaderName) {
        this.shaderName = shaderName;
    }

    public static WeaponShaderMode currentMode = WeaponShaderMode.NONE;
}