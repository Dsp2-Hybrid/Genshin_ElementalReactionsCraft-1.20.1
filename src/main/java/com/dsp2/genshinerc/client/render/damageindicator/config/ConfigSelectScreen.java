package com.dsp2.genshinerc.client.render.damageindicator.config;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ConfigSelectScreen extends Screen {
    private final Screen parent;

    public ConfigSelectScreen(Screen parent) {
        super(Component.translatable("genshinerc.config.select"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int cy = this.height / 2;

        this.addRenderableWidget(Button.builder(
                Component.translatable("genshinerc.config.client"),
                b -> this.minecraft.setScreen(
                        DamageIndicatorConfig.createScreen(DamageIndicatorConfig.get(), false, this.parent))
        ).bounds(cx - 100, cy - 30, 200, 20).build());

        this.addRenderableWidget(Button.builder(
                Component.translatable("genshinerc.config.server"),
                b -> this.minecraft.setScreen(
                        DamageIndicatorConfig.createScreen(DamageIndicatorConfig.get(), true, this.parent))
        ).bounds(cx - 100, cy + 10, 200, 20).build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, this.height / 2 - 70, 0xFFFFFF);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parent);
    }
}