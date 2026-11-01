package com.dsp2.genshinerc.client.render.vfx.weaponshaders.test;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = "genshinerc", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class WeaponShaderManager {
    private static final Map<WeaponShaderMode, ShaderInstance> CACHE = new HashMap<>();

    @SubscribeEvent
    public static void onRegisterShaders(RegisterShadersEvent event) {
        for (WeaponShaderMode mode : WeaponShaderMode.values()) {
            if (mode == WeaponShaderMode.NONE) continue;
            try {
                // 使用 getResourceProvider() 获取资源提供器
                event.registerShader(
                        new ShaderInstance(
                                event.getResourceProvider(),
                                mode.shaderName,
                                DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP // 粒子对应的标准顶点格式
                        ),
                        shaderInstance -> CACHE.put(mode, shaderInstance)
                );
            } catch (IOException e) {
                System.err.println("[GenshinERC] 无法注册 Shader: " + mode.shaderName);
                e.printStackTrace();
            }
        }
    }

    public static ShaderInstance getShader(WeaponShaderMode mode) {
        return CACHE.get(mode);
    }
}