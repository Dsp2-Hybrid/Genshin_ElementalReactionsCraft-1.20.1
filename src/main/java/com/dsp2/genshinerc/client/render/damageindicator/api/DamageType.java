package com.dsp2.genshinerc.client.render.damageindicator.api;

public class DamageType {
    private final String key;
    private final CustomColor defaultColor;
    private final String nameKey;
    public DamageType(String key, CustomColor defaultColor, String nameKey) {
        this.key = key; this.defaultColor = defaultColor; this.nameKey = nameKey;
    }
    public String getKey() { return key; }
    public CustomColor getDefaultColor() { return defaultColor; }
    public String getNameKey() { return nameKey; }
}