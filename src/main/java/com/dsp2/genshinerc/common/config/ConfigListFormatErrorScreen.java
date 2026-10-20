package com.dsp2.genshinerc.common.config;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ConfigListFormatErrorScreen extends Screen {
    private final Screen resumeScreen;
    private final Screen exitScreen;
    private final Component message;
    private final Component continueLabel;
    private final Component exitLabel;

    public ConfigListFormatErrorScreen(Screen resumeScreen, Screen exitScreen, Component message, Component continueLabel, Component exitLabel) {
        super(Component.empty());
        this.resumeScreen = resumeScreen;
        this.exitScreen = exitScreen;
        this.message = message;
        this.continueLabel = continueLabel;
        this.exitLabel = exitLabel;
    }

    @Override
    protected void init() {
        super.init();
        int cx = this.width / 2;
        int cy = this.height / 2;
        this.addRenderableWidget(Button.builder(this.continueLabel, b -> Minecraft.getInstance().setScreen(this.resumeScreen)).bounds(cx - 155, cy + 20, 150, 20).build());
        this.addRenderableWidget(Button.builder(this.exitLabel, b -> Minecraft.getInstance().setScreen(this.exitScreen)).bounds(cx + 5, cy + 20, 150, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (this.resumeScreen != null) {
            this.resumeScreen.render(graphics, -1, -1, partialTick);
        }

        RenderSystem.disableDepthTest();
        RenderSystem.disableScissor();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        graphics.fill(0, 0, this.width, this.height, 0xB3000000);

        int cx = this.width / 2;
        int cy = this.height / 2;
        graphics.drawCenteredString(this.font, this.message, cx, cy - 20, 0xFFFFFFFF);
        super.render(graphics, mouseX, mouseY, partialTick);
    }


    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(this.resumeScreen);
    }
}