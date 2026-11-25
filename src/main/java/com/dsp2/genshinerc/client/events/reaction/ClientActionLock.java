package com.dsp2.genshinerc.client.events.reaction;

import com.dsp2.genshinerc.client.render.entity.elereaction.overlay.FrozenSurface;
import com.dsp2.genshinerc.client.render.hud.bar.element.ClientAuraCache;
import com.dsp2.genshinerc.common.core.element.ElementType;
import com.dsp2.genshinerc.common.core.element.FrozenControl;
import com.dsp2.genshinerc.common.core.element.FrozenWaterControl;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
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

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onTick(LivingEvent.LivingTickEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || event.getEntity() != mc.player) return;
        LocalPlayer player = mc.player;
        if (!player.isAlive() || player.isSpectator() || player.getAbilities().flying || player.isInWater()
                || player.onGround() || player.isPassenger() || player.isNoGravity()) return;

        boolean frozen = FrozenSurface.frozen(player);
        var auras = ClientAuraCache.get(player.getId());
        boolean cryo = auras != null && auras.hasActive(ElementType.CRYO);
        if (!frozen && !cryo) return;

        Vec3 velocity = player.getDeltaMovement();
        if (velocity.y > -0.02) return;

        AABB box = player.getBoundingBox();
        double feet = box.minY;
        double surface = FrozenWaterControl.findSurface(player.level(), box, feet, feet + velocity.y - 0.12);
        if (surface == Double.NEGATIVE_INFINITY) return;

        double target = surface - FrozenWaterControl.SINK;
        float distance = player.fallDistance + (float) Math.max(0.0, feet - target);
        if (frozen && FrozenWaterControl.fallRatio(player, distance, 1.0f) >= FrozenWaterControl.SHATTER_RATIO) return;
        if (!player.level().noCollision(player, box.move(0.0, target - feet, 0.0))) return;

        player.setPos(player.getX(), target, player.getZ());
        player.setDeltaMovement(velocity.x * 0.25, 0.0, velocity.z * 0.25);
        player.fallDistance = 0f;
    }
}