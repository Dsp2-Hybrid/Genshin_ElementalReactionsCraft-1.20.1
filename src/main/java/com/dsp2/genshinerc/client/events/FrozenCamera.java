package com.dsp2.genshinerc.client.events;

import com.dsp2.genshinerc.client.render.entity.elereaction.overlay.FrozenSurface;
import com.dsp2.genshinerc.mixin.client.render.FrozenCameraAccessor;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ComputeFovModifierEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "genshinerc", value = Dist.CLIENT)
public final class FrozenCamera {
    private record View(LocalPlayer player, CameraType type, Vec3 arm, float yaw, float pitch, float roll) {}
    private static View last;
    private static LocalPlayer fovPlayer;
    private static CameraType fovType;
    private static float fov = 1f;

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onFov(ComputeFovModifierEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || event.getPlayer() != mc.player || mc.getCameraEntity() != mc.player) return;
        CameraType type = mc.options.getCameraType();
        if (!type.isFirstPerson() && FrozenSurface.frozen(mc.player)) {
            event.setNewFovModifier(fovPlayer == mc.player && type == fovType ? fov : 1f);
        } else {
            fovPlayer = mc.player;
            fovType = type;
            fov = event.getNewFovModifier();
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onCamera(ViewportEvent.ComputeCameraAngles event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        CameraType type = mc.options.getCameraType();
        if (player == null || event.getCamera().getEntity() != player || type.isFirstPerson()
                || player.isSpectator() || player.isSleeping()) {
            last = null;
            return;
        }
        Vec3 eye = player.getEyePosition((float) event.getPartialTick());
        if (!FrozenSurface.frozen(player) || last == null || last.player() != player || last.type() != type) {
            last = new View(player, type, event.getCamera().getPosition().subtract(eye),
                    event.getYaw(), event.getPitch(), event.getRoll());
            return;
        }
        Vec3 wanted = eye.add(last.arm());
        var hit = player.level().clip(new ClipContext(eye, wanted, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, player));
        if (hit.getType() == HitResult.Type.BLOCK) {
            double distance = Math.max(0.0, eye.distanceTo(hit.getLocation()) - 0.14);
            wanted = eye.add(last.arm().normalize().scale(distance));
        }
        ((FrozenCameraAccessor) event.getCamera()).genshinerc$setPosition(wanted);
        event.setYaw(last.yaw());
        event.setPitch(last.pitch());
        event.setRoll(last.roll());
    }
}
