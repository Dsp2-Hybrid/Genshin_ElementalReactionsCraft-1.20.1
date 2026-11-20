package com.dsp2.genshinerc.mixin.client.render;

import net.minecraft.client.Camera;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Camera.class)
public interface FrozenCameraAccessor {
    @Invoker("setPosition")
    void genshinerc$setPosition(Vec3 position);
}
