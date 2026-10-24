package com.dsp2.genshinerc.client.render.hud;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class WhiteMaskTextures {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<ResourceLocation, Optional<ResourceLocation>> CACHE = new HashMap<>();

    private WhiteMaskTextures() {}

    @Nullable
    public static ResourceLocation get(ResourceLocation source) {
        return CACHE.computeIfAbsent(source, WhiteMaskTextures::build).orElse(null);
    }

    private static Optional<ResourceLocation> build(ResourceLocation source) {
        Minecraft mc = Minecraft.getInstance();
        try (InputStream in = mc.getResourceManager().open(source);
             NativeImage src = NativeImage.read(NativeImage.Format.RGBA, in)) {
            NativeImage mask = new NativeImage(src.getWidth(), src.getHeight(), false);
            for (int y = 0; y < src.getHeight(); y++) {
                for (int x = 0; x < src.getWidth(); x++) {
                    int alpha = src.getPixelRGBA(x, y) >>> 24;
                    mask.setPixelRGBA(x, y, (alpha << 24) | 0x00FFFFFF);
                }
            }
            String name = "genshinerc_white_" + source.getPath().replaceAll("[^a-z0-9_]", "_");
            return Optional.of(mc.getTextureManager().register(name, new DynamicTexture(mask)));
        } catch (IOException | RuntimeException e) {
            LOGGER.warn("Could not build white mask for {}", source, e);
            return Optional.empty();
        }
    }

    public static void clear() {
        TextureManager tm = Minecraft.getInstance().getTextureManager();
        CACHE.values().forEach(opt -> opt.ifPresent(rl -> {
            tm.release(rl);
            HudRenderTypes.forget(rl);
        }));
        CACHE.clear();
    }
}