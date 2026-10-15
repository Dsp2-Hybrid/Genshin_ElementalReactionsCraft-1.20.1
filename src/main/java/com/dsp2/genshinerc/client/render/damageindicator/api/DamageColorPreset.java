package com.dsp2.genshinerc.client.render.damageindicator.api;

public enum DamageColorPreset {
    WHITE       (0xFFFFFF, "White"),
    ORANGE      (0xFF8C00, "Orange"),
    EARTH_YELLOW(0xD4A017, "Earth Yellow"),
    PURPLE      (0x9B30FF, "Purple"),
    GREEN       (0x32C832, "Green"),
    LIGHT_BLUE  (0x87CEEB, "Light Blue"),
    DARK_BLUE   (0x1E3A8A, "Dark Blue"),
    CYAN_GREEN  (0x20B2AA, "Cyan Green"),
    RED         (0xFF2D2D, "Red (Overload)");

    private final int hex;
    private final String label;

    DamageColorPreset(int hex, String label) {
        this.hex = hex;
        this.label = label;
    }

    public int getHex() { return hex; }
    public String getLabel() { return label; }
    public CustomColor toColor() { return CustomColor.hex(hex); }

    public static DamageColorPreset fromName(String name) {
        if (name == null) return WHITE;
        for (DamageColorPreset p : values()) {
            if (p.name().equalsIgnoreCase(name) || p.label.equalsIgnoreCase(name)) return p;
        }
        return WHITE;
    }
}