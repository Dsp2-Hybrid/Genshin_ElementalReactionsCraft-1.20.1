package com.dsp2.genshinerc.client.render.damageindicator.config;

public class DamageIndicatorConfigYaclActionTracker {
    public static volatile String lastAction = null;
    public static void setAction(String a) { lastAction = a; }
    public static String consume() {
        String a = lastAction;
        lastAction = null;
        return a;
    }
}