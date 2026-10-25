package com.dsp2.genshinerc.client.render.hud.bar.config;

import dev.isxander.yacl3.gui.OptionListWidget;
import dev.isxander.yacl3.gui.YACLScreen;
import dev.isxander.yacl3.gui.tab.ListHolderWidget;
import net.minecraft.Util;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.components.tabs.Tab;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;

/**
 * Restores a YACL category's scroll position after returning from a sub-screen.
 * Save in the button action; restore runs automatically from Forge screen events.
 */
@Mod.EventBusSubscriber(modid = "genshinerc", value = Dist.CLIENT)
public final class YaclScrollMemory {
    private static final long STABLE_MS = 150L;
    private static final long TIMEOUT_MS = 1000L;
    private static final double EPS = 0.5;

    @Nullable private static String pendingKey;
    private static double pendingScroll;
    private static boolean armed;
    private static long armedAt;
    private static long stableSince = -1L;

    @Nullable private static Field smoothField;
    private static boolean smoothLookedUp;

    private YaclScrollMemory() {}

    /** Call right before opening the sub-screen. */
    public static void save(YACLScreen screen) {
        OptionListWidget list = findList(screen);
        if (list == null || list.getScrollAmount() <= EPS) {
            pendingKey = null;
            return;
        }
        pendingKey = key(screen);
        pendingScroll = list.getScrollAmount();
        armed = false;
    }

    @SubscribeEvent
    public static void onInit(ScreenEvent.Init.Post event) {
        if (pendingKey != null && event.getScreen() instanceof YACLScreen screen && pendingKey.equals(key(screen))) {
            armed = true;
            armedAt = Util.getMillis();
            stableSince = -1L;
        }
    }

    @SubscribeEvent
    public static void onRender(ScreenEvent.Render.Pre event) {
        if (!armed || pendingKey == null || !(event.getScreen() instanceof YACLScreen screen)) return;
        if (!pendingKey.equals(key(screen))) { // user switched tab
            finish();
            return;
        }
        OptionListWidget list = findList(screen);
        if (list == null) return;

        long now = Util.getMillis();
        if (Math.abs(list.getScrollAmount() - pendingScroll) < EPS) {
            if (stableSince < 0L) stableSince = now;
        } else {
            list.setScrollAmount(pendingScroll);
            snapSmooth(list, list.getScrollAmount());
            stableSince = Math.abs(list.getScrollAmount() - pendingScroll) < EPS ? now : -1L;
        }

        if ((stableSince >= 0L && now - stableSince >= STABLE_MS) || now - armedAt >= TIMEOUT_MS) finish();
    }

    private static void finish() {
        armed = false;
        pendingKey = null;
        stableSince = -1L;
    }

    @Nullable
    private static OptionListWidget findList(YACLScreen screen) {
        for (GuiEventListener child : screen.children()) {
            if (child instanceof ListHolderWidget<?> holder && holder.getList() instanceof OptionListWidget list) return list;
        }
        return null;
    }

    private static String key(YACLScreen screen) {
        Tab tab = screen.tabManager.getCurrentTab();
        return screen.getTitle().getString() + "|" + (tab == null ? "" : tab.getTabTitle().getString());
    }

    private static void snapSmooth(OptionListWidget list, double value) {
        if (!smoothLookedUp) {
            smoothLookedUp = true;
            for (Class<?> c = list.getClass(); c != null && c != AbstractSelectionList.class; c = c.getSuperclass()) {
                try {
                    Field f = c.getDeclaredField("smoothScrollAmount");
                    f.setAccessible(true);
                    smoothField = f;
                    break;
                } catch (NoSuchFieldException ignored) {}
            }
        }
        if (smoothField == null) return;
        try {
            smoothField.setDouble(list, value);
        } catch (IllegalAccessException ignored) {}
    }
}