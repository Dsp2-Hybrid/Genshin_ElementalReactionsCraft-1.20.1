package com.dsp2.genshinerc.client.render.vfx.weaponshaders.test;

public enum WeaponShaderMode {
    NONE("none"),

    KALEIDOSCOPE("kaleidoscope",
        new ParamSpec("trail",       0f, 4f, 1.0f),
        new ParamSpec("glow",        0f, 4f, 0.6f),
        new ParamSpec("distort",     0f, 4f, 1.0f),
        new ParamSpec("distortFreq", 0.2f, 4f, 1.0f),
        new ParamSpec("segments",    2f, 16f, 8f),
        new ParamSpec("zoomAmp",     0f, 0.5f, 0.07f),
        new ParamSpec("rotSpeed",    0f, 4f, 1.0f),
        new ParamSpec("patternFreq", 1f, 40f, 12f)
    ),

    PIXELSORT("pixelsort",
        new ParamSpec("trail",        0f, 4f, 1.0f),
        new ParamSpec("glow",         0f, 4f, 0.6f),
        new ParamSpec("rowDensity",   4f, 64f, 24f),
        new ParamSpec("sortSpeed",    0f, 4f, 1.8f),
        new ParamSpec("streakBright", 0f, 4f, 1.5f)
    ),

    EDGEGLOW("edgeglow",
        new ParamSpec("trail",      0f, 4f, 1.0f),
        new ParamSpec("glow",       0f, 4f, 0.6f),
        new ParamSpec("edgeWidth",  0.5f, 8f, 2f),
        new ParamSpec("flowSpeed",  0f, 4f, 1.0f),
        new ParamSpec("glowGain",   0f, 4f, 1.8f)
    ),

    WAVE("wave",
        new ParamSpec("trail",       0f, 4f, 1.0f),
        new ParamSpec("glow",        0f, 4f, 0.6f),
        new ParamSpec("distort",     0f, 4f, 1.0f),
        new ParamSpec("distortFreq", 0.2f, 4f, 1.0f),
        new ParamSpec("waveFreq",    0.2f, 4f, 1.0f),
        new ParamSpec("waveAmp",     0f, 4f, 1.0f),
        new ParamSpec("waveSpeed",   0f, 4f, 1.0f)
    ),

    EMBOSS("emboss",
        new ParamSpec("trail",          0f, 4f, 1.0f),
        new ParamSpec("glow",           0f, 4f, 0.6f),
        new ParamSpec("reliefStrength", 0f, 4f, 1.5f),
        new ParamSpec("noiseScale",     0.2f, 4f, 1.0f),
        new ParamSpec("specPower",      2f, 64f, 24f)
    ),

    CRT("crt",
        new ParamSpec("trail",      0f, 4f, 1.0f),
        new ParamSpec("glow",       0f, 4f, 0.6f),
        new ParamSpec("scanFreq",   8f, 128f, 48f),
        new ParamSpec("rollSpeed",  0f, 4f, 0.7f),
        new ParamSpec("flickerAmp", 0f, 0.5f, 0.06f)
    ),

    GLITCH_SLICE("glitch_slice",
        new ParamSpec("trail",       0f, 4f, 1.0f),
        new ParamSpec("glow",        0f, 4f, 0.6f),
        new ParamSpec("distort",     0f, 4f, 1.0f),
        new ParamSpec("distortFreq", 0.2f, 4f, 1.0f),
        new ParamSpec("blockSize",   4f, 64f, 16f),
        new ParamSpec("jumpAmp",     0f, 4f, 1.0f),
        new ParamSpec("tickRate",    2f, 30f, 14f)
    ),

    FISHEYE("fisheye",
        new ParamSpec("trail",       0f, 4f, 1.0f),
        new ParamSpec("glow",        0f, 4f, 0.6f),
        new ParamSpec("distort",     0f, 4f, 1.0f),
        new ParamSpec("distortFreq", 0.2f, 4f, 1.0f),
        new ParamSpec("lensRadius",  0.1f, 1f, 0.6f),
        new ParamSpec("lensSpeed",   0f, 4f, 0.5f),
        new ParamSpec("ringWidth",   0f, 0.3f, 0.08f)
    ),

    RADIAL_BLUR("radial_blur",
        new ParamSpec("trail",         0f, 4f, 1.0f),
        new ParamSpec("glow",          0f, 4f, 0.6f),
        new ParamSpec("streakDensity", 0.1f, 4f, 1.0f),
        new ParamSpec("streakSpeed",   0f, 4f, 1.0f),
        new ParamSpec("falloff",       1f, 8f, 4f)
    ),

    HSV_FILTER("hsv_filter",
        new ParamSpec("trail",     0f, 4f, 1.0f),
        new ParamSpec("glow",      0f, 4f, 0.6f),
        new ParamSpec("hueSpeed",  0f, 2f, 0.2f),
        new ParamSpec("satBoost",  1f, 3f, 1.3f),
        new ParamSpec("hueOffset", 0f, 2f, 0.8f),
        new ParamSpec("minSat",    0f, 1f, 0.6f),
        new ParamSpec("minVal",    0f, 1f, 0.5f)
    ),

    TWIST("twist",
        new ParamSpec("trail",       0f, 4f, 1.0f),
        new ParamSpec("glow",        0f, 4f, 0.6f),
        new ParamSpec("distort",     0f, 4f, 1.0f),
        new ParamSpec("distortFreq", 0.2f, 4f, 1.0f),
        new ParamSpec("twistFreq",   0.2f, 4f, 1.0f),
        new ParamSpec("twistPhase",  0f, 1f, 0.35f),
        new ParamSpec("twistSpeed",  0f, 4f, 1.0f),
        new ParamSpec("lineWidth",   0.01f, 0.2f, 0.04f),
        new ParamSpec("colorSpeed",  0f, 4f, 1.0f),
        new ParamSpec("glowGain",    0f, 4f, 1.3f)
    ),

    PULSE("pulse",
        new ParamSpec("trail",      0f, 4f, 1.0f),
        new ParamSpec("glow",       0f, 4f, 0.6f),
        new ParamSpec("distort",    0f, 4f, 1.0f),
        new ParamSpec("distortFreq",0.2f, 4f, 1.0f),
        new ParamSpec("pulseSpeed", 0f, 4f, 1.6f),
        new ParamSpec("pulseSharp", 4f, 32f, 12f),
        new ParamSpec("coreGain",   0f, 4f, 0.6f)
    ),

    RGBSPLIT("rgbsplit",
        new ParamSpec("trail",       0f, 4f, 1.0f),
        new ParamSpec("glow",        0f, 4f, 0.6f),
        new ParamSpec("distort",     0f, 4f, 1.0f),
        new ParamSpec("distortFreq", 0.2f, 4f, 1.0f),
        new ParamSpec("split",       0f, 0.3f, 0.05f),
        new ParamSpec("splitSpeed",  0f, 4f, 1.0f),
        new ParamSpec("splitWobble", 0f, 0.1f, 0.03f)
    ),

    PIXELATE("pixelate",
        new ParamSpec("trail",       0f, 4f, 1.0f),
        new ParamSpec("glow",        0f, 4f, 0.6f),
        new ParamSpec("cellSize",    4f, 64f, 24f),
        new ParamSpec("cellWobble",  0f, 1f, 0.5f),
        new ParamSpec("flickChance", 0f, 1f, 0.8f)
    ),

    SQUASH("squash",
        new ParamSpec("trail",       0f, 4f, 1.0f),
        new ParamSpec("glow",        0f, 4f, 0.6f),
        new ParamSpec("distort",     0f, 4f, 1.0f),
        new ParamSpec("distortFreq", 0.2f, 4f, 1.0f),
        new ParamSpec("squashAmp",   0f, 1f, 0.4f),
        new ParamSpec("squashFreq",  1f, 32f, 9f),
        new ParamSpec("squashSpeed", 0f, 4f, 1.0f)
    ),

    MIRROR("mirror",
        new ParamSpec("trail",       0f, 4f, 1.0f),
        new ParamSpec("glow",        0f, 4f, 0.6f),
        new ParamSpec("distort",     0f, 4f, 1.0f),
        new ParamSpec("distortFreq", 0.2f, 4f, 1.0f),
        new ParamSpec("seamWobble",  0f, 3f, 1.0f),
        new ParamSpec("mirrorSpeed", 0f, 4f, 1.0f),
        new ParamSpec("seamWidth",   0.005f, 0.1f, 0.03f)
    ),

    VORTEX("vortex",
        new ParamSpec("trail",       0f, 4f, 1.0f),
        new ParamSpec("glow",        0f, 4f, 0.6f),
        new ParamSpec("distort",     0f, 4f, 1.0f),
        new ParamSpec("distortFreq", 0.2f, 4f, 1.0f),
        new ParamSpec("vortexFreq",  0.2f, 4f, 1.5f),
        new ParamSpec("swirlSpeed",  0f, 4f, 1.0f),
        new ParamSpec("swirlSharp",  2f, 16f, 6f)
    ),

    SOBEL_EDGE("sobel_edge",
        new ParamSpec("trail",       0f, 4f, 1.0f),
        new ParamSpec("glow",        0f, 4f, 0.6f),
        new ParamSpec("crackWidth",  0.01f, 0.5f, 0.08f),
        new ParamSpec("scrollSpeed", 0f, 4f, 1.0f),
        new ParamSpec("crackGain",   0f, 4f, 1.6f)
    ),

    HALFTONE("halftone",
        new ParamSpec("trail",      0f, 4f, 1.0f),
        new ParamSpec("glow",       0f, 4f, 0.6f),
        new ParamSpec("cellSize",   4f, 64f, 24f),
        new ParamSpec("dotMin",     0f, 0.5f, 0.1f),
        new ParamSpec("pulseAmp",   0f, 1f, 0.2f)
    ),

    OIL_PAINT("oil_paint",
        new ParamSpec("trail",       0f, 4f, 1.0f),
        new ParamSpec("glow",        0f, 4f, 0.6f),
        new ParamSpec("brushScale",  0.2f, 4f, 1.0f),
        new ParamSpec("brushAspect", 0.2f, 4f, 1.0f),
        new ParamSpec("colorMix",    0f, 1f, 0.5f)
    ),

    NEON_GLOW("neon_glow",
        new ParamSpec("trail",         0f, 4f, 1.0f),
        new ParamSpec("glow",          0f, 4f, 0.6f),
        new ParamSpec("tubeWidth",     0.01f, 0.15f, 0.035f),
        new ParamSpec("tubeRadius",    0.1f, 0.5f, 0.38f),
        new ParamSpec("flickerChance", 0f, 1f, 0.1f)
    ),

    THERMAL("thermal",
        new ParamSpec("trail",       0f, 4f, 1.0f),
        new ParamSpec("glow",        0f, 4f, 0.6f),
        new ParamSpec("rampOffset",  0f, 1f, 0f),
        new ParamSpec("shimmerAmp",  0f, 1f, 0.3f),
        new ParamSpec("coreGain",    0f, 1f, 0.3f)
    ),

    POSTERIZE("posterize",
        new ParamSpec("trail",      0f, 4f, 1.0f),
        new ParamSpec("glow",       0f, 4f, 0.6f),
        new ParamSpec("levels",     2f, 12f, 4f),
        new ParamSpec("bandFreq",   1f, 16f, 6f),
        new ParamSpec("edgeGain",   0f, 1f, 0.5f)
    ),

    RIPPLE("ripple",
        new ParamSpec("trail",       0f, 4f, 1.0f),
        new ParamSpec("glow",        0f, 4f, 0.6f),
        new ParamSpec("distort",     0f, 4f, 1.0f),
        new ParamSpec("distortFreq", 0.2f, 4f, 1.0f),
        new ParamSpec("rippleFreq",  0.2f, 4f, 1.0f),
        new ParamSpec("rippleSpeed", 0f, 4f, 1.0f),
        new ParamSpec("falloff",     0.5f, 4f, 1.5f)
    ),

    NOISE_GRAIN("noise_grain",
        new ParamSpec("trail",           0f, 4f, 1.0f),
        new ParamSpec("glow",            0f, 4f, 0.6f),
        new ParamSpec("sparkleDensity",  2f, 32f, 12f),
        new ParamSpec("sparkleChance",   0f, 1f, 0.88f),
        new ParamSpec("grainAmp",        0f, 1f, 0.2f)
    ),

    ZOOM_BLUR("zoom_blur",
        new ParamSpec("trail",        0f, 4f, 1.0f),
        new ParamSpec("glow",         0f, 4f, 0.6f),
        new ParamSpec("blurStrength", 0f, 0.5f, 0.04f),
        new ParamSpec("streakSpeed",  0f, 4f, 1.0f),
        new ParamSpec("streakFreq",   1f, 128f, 60f)
    ),

    DIAMOND_GRID("diamond_grid",
        new ParamSpec("trail",       0f, 4f, 1.0f),
        new ParamSpec("glow",        0f, 4f, 0.6f),
        new ParamSpec("distort",     0f, 4f, 1.0f),
        new ParamSpec("distortFreq", 0.2f, 4f, 1.0f),
        new ParamSpec("gridScale",   0.2f, 8f, 3f),
        new ParamSpec("lineWidth",   0.005f, 0.2f, 0.03f),
        new ParamSpec("sweepSpeed",  0f, 4f, 1.0f)
    ),

    HEXAGON("hexagon",
        new ParamSpec("trail",       0f, 4f, 1.0f),
        new ParamSpec("glow",        0f, 4f, 0.6f),
        new ParamSpec("distort",     0f, 4f, 1.0f),
        new ParamSpec("distortFreq", 0.2f, 4f, 1.0f),
        new ParamSpec("hexScale",    0.2f, 8f, 3f),
        new ParamSpec("scrollSpeed", 0f, 4f, 1.0f),
        new ParamSpec("edgeWidth",   0.01f, 0.3f, 0.08f)
    ),

    CHROMATIC("chromatic",
        new ParamSpec("trail",       0f, 4f, 1.0f),
        new ParamSpec("glow",        0f, 4f, 0.6f),
        new ParamSpec("distort",     0f, 4f, 1.0f),
        new ParamSpec("distortFreq", 0.2f, 4f, 1.0f),
        new ParamSpec("split",       0f, 0.3f, 0.05f),
        new ParamSpec("splitSpeed",  0f, 4f, 1.0f),
        new ParamSpec("noiseScale",  0.5f, 8f, 3f)
    ),

    BLOOM("bloom",
        new ParamSpec("trail",        0f, 4f, 1.0f),
        new ParamSpec("glow",         0f, 4f, 0.6f),
        new ParamSpec("bloomRadius",  1f, 6f, 3f),
        new ParamSpec("bloomGain",    0f, 4f, 0.8f),
        new ParamSpec("coreGain",     0f, 4f, 0.7f)
    ),

    TEST_RED("test_red",
        new ParamSpec("trail", 0f, 4f, 1.0f),
        new ParamSpec("glow",  0f, 4f, 0.6f)
    );

    public final String shaderName;
    public final ParamSpec[] params;

    WeaponShaderMode(String shaderName, ParamSpec... params) {
        this.shaderName = shaderName;
        this.params = params;
    }

    public static volatile WeaponShaderMode currentMode = WeaponShaderMode.NONE;
}