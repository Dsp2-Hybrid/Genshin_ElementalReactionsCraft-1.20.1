package com.dsp2.genshinerc.client.events;

import com.dsp2.genshinerc.client.render.entity.elereaction.overlay.FrozenSurface;
import com.dsp2.genshinerc.common.core.element.FrozenControl;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "genshinerc", value = Dist.CLIENT)
public final class ClientActionLock {
    private ClientActionLock() {}

    private static boolean locked() {
        var player = Minecraft.getInstance().player;
        return player != null && (FrozenControl.locked(player) || FrozenSurface.frozen(player));
    }

    @SubscribeEvent
    public static void onInput(MovementInputUpdateEvent event) {
        if (!locked()) return;
        var input = event.getInput();
        input.forwardImpulse = 0f;
        input.leftImpulse = 0f;
        input.up = input.down = input.left = input.right = input.jumping = input.shiftKeyDown = false;
    }

    @SubscribeEvent
    public static void onInteraction(InputEvent.InteractionKeyMappingTriggered event) {
        if (!locked()) return;
        event.setSwingHand(false);
        event.setCanceled(true);
    }
}
