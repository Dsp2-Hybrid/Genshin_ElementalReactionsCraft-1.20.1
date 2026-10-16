package com.dsp2.genshinerc.client.render.damageindicator.config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;

public class ConfigSelectScreen extends Screen {

    private final Screen modListParent;
    public static volatile Screen lastModListParent = null;

    public ConfigSelectScreen(Screen parent) {
        super(Component.translatable("genshinerc.config.select"));
        this.modListParent = parent;
        lastModListParent = parent;
    }

    private Screen gameUiParent() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) {
            return new PauseScreen(true);
        }
        return new TitleScreen();
    }

    private void returnToGame() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) {
            mc.setScreen(null);
        } else {
            mc.setScreen(new TitleScreen());
        }
    }

    @Override
    protected void init() {
        this.addRenderableWidget(Button.builder(
                        Component.translatable("genshinerc.config.back_to_game_ui"),
                        b -> this.minecraft.setScreen(gameUiParent()))
                .bounds(10, 10, 140, 20).build());

        this.addRenderableWidget(Button.builder(
                        Component.translatable("genshinerc.config.back_to_game"),
                        b -> returnToGame())
                .bounds(10, 32, 140, 20).build());

        int cx = this.width / 2;
        int cy = this.height / 2;

        this.addRenderableWidget(Button.builder(
                Component.translatable("genshinerc.config.client"),
                b -> this.minecraft.setScreen(
                        DamageIndicatorConfig.createScreen(DamageIndicatorConfig.get(), false, this))
        ).bounds(cx - 100, cy - 30, 200, 20).build());

        this.addRenderableWidget(Button.builder(
                Component.translatable("genshinerc.config.server"),
                b -> this.minecraft.setScreen(
                        DamageIndicatorConfig.createScreen(DamageIndicatorConfig.get(), true, this))
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
        this.minecraft.setScreen(modListParent);
    }
}