package com.dsp2.genshinerc.common.config;

import com.dsp2.genshinerc.client.render.damageindicator.config.DamageIndicatorConfig;
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

    @Override
    protected void init() {
        int cornerW = 110;
        int cornerH = 18;
        int cornerX = 10;
        int cornerY = 10;

        this.addRenderableWidget(Button.builder(
                Component.translatable("genshinerc.config.back_to_game"),
                b -> this.minecraft.setScreen(null)
        ).bounds(cornerX, cornerY, cornerW, cornerH).build());

        this.addRenderableWidget(Button.builder(
                Component.translatable("genshinerc.config.back_to_game_ui"),
                b -> {
                    if (this.minecraft.level == null) {
                        this.minecraft.setScreen(new TitleScreen());
                    } else {
                        this.minecraft.setScreen(new PauseScreen(true));
                    }
                }
        ).bounds(cornerX, cornerY + cornerH + 3, cornerW, cornerH).build());

        this.addRenderableWidget(Button.builder(
                Component.translatable("genshinerc.config.back_to_list"),
                b -> this.minecraft.setScreen(modListParent)
        ).bounds(cornerX, cornerY + (cornerH + 3) * 2, cornerW, cornerH).build());

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