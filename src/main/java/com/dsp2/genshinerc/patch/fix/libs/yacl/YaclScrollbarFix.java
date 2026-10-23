package com.dsp2.genshinerc.patch.fix.libs.yacl;

import dev.isxander.yacl3.gui.tab.ListHolderWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Workaround for YACL 3.6.6 (1.20.1): OptionListWidget routes every drag to its
 * focused entry while the list's dragging flag is set, and AbstractSelectionList
 * never clears that flag on release. After any entry click, scrollbar dragging
 * becomes unreachable. Clearing the flag on release restores vanilla behavior.
 */
@Mod.EventBusSubscriber(modid = "genshinerc", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class YaclScrollbarFix {

    private YaclScrollbarFix() {}

    // Post runs after the screen has handled the release, so sliders and other
    // widgets still receive their normal onRelease first.
    @SubscribeEvent
    public static void onMouseReleased(ScreenEvent.MouseButtonReleased.Post event) {
        for (GuiEventListener child : event.getScreen().children()) {
            if (child instanceof ListHolderWidget<?> holder) {
                holder.setDragging(false);
            }
        }
    }
}