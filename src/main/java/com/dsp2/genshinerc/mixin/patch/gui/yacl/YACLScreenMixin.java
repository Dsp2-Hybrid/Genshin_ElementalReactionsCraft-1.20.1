package com.dsp2.genshinerc.mixin.patch.gui.yacl;

import dev.isxander.yacl3.gui.YACLScreen;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(YACLScreen.class)
public abstract class YACLScreenMixin extends Screen {

    protected YACLScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void genshinerc$clickDispatch(double mouseX, double mouseY, int button,
                                          CallbackInfoReturnable<Boolean> cir) {
        GuiEventListener focused = this.getFocused();

        if (focused instanceof AbstractWidget widget) {
            boolean inside =
                    mouseX >= widget.getX() && mouseX <= widget.getX() + widget.getWidth()
                            && mouseY >= widget.getY() && mouseY <= widget.getY() + widget.getHeight();
            if (inside) return;

            this.setFocused(null);
            this.setDragging(false);

            for (GuiEventListener listener : this.children()) {
                if (listener == focused) continue;
                if (listener.mouseClicked(mouseX, mouseY, button)) {
                    this.setFocused(listener);
                    if (button == 0) this.setDragging(true);
                    cir.setReturnValue(true);
                    return;
                }
            }
        }
    }
}