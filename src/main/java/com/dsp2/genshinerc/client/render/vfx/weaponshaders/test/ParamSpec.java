package com.dsp2.genshinerc.client.render.vfx.weaponshaders.test;

public record ParamSpec(String name, float min, float max, float def) {
    public String uniformName() {
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }
}