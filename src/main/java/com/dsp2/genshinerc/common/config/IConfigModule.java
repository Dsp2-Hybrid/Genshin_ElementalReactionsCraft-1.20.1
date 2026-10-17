package com.dsp2.genshinerc.common.config;

import dev.isxander.yacl3.api.OptionGroup;

public interface IConfigModule {
    OptionGroup buildGroup();
    void load();
    void save();
}