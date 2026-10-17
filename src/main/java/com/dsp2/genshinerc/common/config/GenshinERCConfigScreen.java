package com.dsp2.genshinerc.common.config;

import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class GenshinERCConfigScreen {
    public static Screen createScreen(Screen parent) {
        var categoryBuilder = ConfigCategory.createBuilder()
                .name(Component.translatable("genshinerc.config.category.display"));

        for (IConfigModule module : ConfigRegistry.getModules()) {
            categoryBuilder.group(module.buildGroup());
        }

        return YetAnotherConfigLib.createBuilder()
                .title(Component.translatable("genshinerc.config.title"))
                .category(categoryBuilder.build())
                .save(ConfigRegistry::saveAll)
                .build()
                .generateScreen(parent);
    }
}