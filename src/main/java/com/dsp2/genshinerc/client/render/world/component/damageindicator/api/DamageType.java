package com.dsp2.genshinerc.client.render.world.component.damageindicator.api;

public class DamageType {
    private final String key;
    private final String nameKey;
    private final String configColorKey;
    public DamageType(String key, String nameKey, String configColorKey) {
        this.key = key; this.nameKey = nameKey; this.configColorKey = configColorKey;
    }
    public String getKey() { return key; }
    public String getNameKey() { return nameKey; }
    public String getConfigColorKey() { return configColorKey; }
}