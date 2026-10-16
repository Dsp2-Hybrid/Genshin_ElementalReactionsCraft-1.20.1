package com.dsp2.genshinerc.client.render.damageindicator.api;

public class CustomColor {
    private final float a, r, g, b;

    public CustomColor(float a, float r, float g, float b) { this.a = a; this.r = r; this.g = g; this.b = b; }

    public static CustomColor rgb(int r, int g, int b) { return new CustomColor(1f, r/255f, g/255f, b/255f); }
    public static CustomColor rgba(int r, int g, int b, int a) { return new CustomColor(a/255f, r/255f, g/255f, b/255f); }

    public static CustomColor hex(int hex) {
        return new CustomColor(1f, ((hex>>16)&0xFF)/255f, ((hex>>8)&0xFF)/255f, (hex&0xFF)/255f);
    }

    public static CustomColor hsl(float h, float s, float l) { return hsl(h, s, l, 1f); }

    public static CustomColor hsl(float h, float s, float l, float a) {
        float c = (1f - Math.abs(2f*l-1f)) * s;
        float x = c * (1f - Math.abs((h/60f) % 2f - 1f));
        float m = l - c/2f;
        float r1, g1, b1;
        if (h < 60f) { r1=c; g1=x; b1=0; }
        else if (h < 120f) { r1=x; g1=c; b1=0; }
        else if (h < 180f) { r1=0; g1=c; b1=x; }
        else if (h < 240f) { r1=0; g1=x; b1=c; }
        else if (h < 300f) { r1=x; g1=0; b1=c; }
        else { r1=c; g1=0; b1=x; }
        return new CustomColor(a, r1+m, g1+m, b1+m);
    }

    public static CustomColor fromHexString(String hex) {
        try { return hex(Integer.parseInt(hex.replace("#","").trim(), 16)); }
        catch (Exception e) { return CustomColor.hex(0xFFFFFF); }
    }

    public int argb() {
        int ai = (int) (Math.min(1f, Math.max(0f, a)) * 255f);
        int ri = (int) (Math.min(1f, Math.max(0f, r)) * 255f);
        int gi = (int) (Math.min(1f, Math.max(0f, g)) * 255f);
        int bi = (int) (Math.min(1f, Math.max(0f, b)) * 255f);
        return (ai<<24) | (ri<<16) | (gi<<8) | bi;
    }

    public String toHexString() { return String.format("#%02X%02X%02X", (int)(r*255), (int)(g*255), (int)(b*255)); }

    public float getA() { return a; } public float getR() { return r; } public float getG() { return g; } public float getB() { return b; }
}