package com.dsp2.genshinerc.common.config;

import java.util.ArrayList;
import java.util.List;

public class ConfigRegistry {
    private static final List<IConfigModule> MODULES = new ArrayList<>();

    public static void register(IConfigModule module) {
        MODULES.add(module);
    }

    public static List<IConfigModule> getModules() {
        return MODULES;
    }

    public static void loadAll() {
        for (IConfigModule module : MODULES) {
            module.load();
        }
    }

    public static void saveAll() {
        for (IConfigModule module : MODULES) {
            module.save();
        }
    }
}