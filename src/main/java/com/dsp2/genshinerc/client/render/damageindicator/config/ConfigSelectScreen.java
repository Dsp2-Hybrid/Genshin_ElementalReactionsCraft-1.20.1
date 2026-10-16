package com.dsp2.genshinerc.client.render.damageindicator.config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

public class ConfigSelectScreen extends Screen {

    private final Screen modListParent;

    public static volatile boolean pendingReturnToModList = false;
    public static volatile Screen lastModListParent = null;

    public ConfigSelectScreen(Screen parent) {
        super(Component.translatable("genshinerc.config.select"));
        this.modListParent = parent;
        lastModListParent = parent;
    }

    @Override
    protected void init() {
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

    @Mod.EventBusSubscriber(modid = "genshinerc", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class TickHandler {
        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent e) {
            if (e.phase != TickEvent.Phase.END) return;
            if (!pendingReturnToModList) return;
            pendingReturnToModList = false;
            Minecraft mc = Minecraft.getInstance();
            if (lastModListParent != null) {
                mc.setScreen(lastModListParent);
            }
        }
    }
}