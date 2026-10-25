package com.dsp2.genshinerc.client.render.bar.layout;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public final class BarTextureInfo {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int ALPHA_CUTOFF = 8;
    private static final int PROFILE_TOLERANCE = 12;
    private static final Map<ResourceLocation, BarTextureInfo> CACHE = new HashMap<>();

    public final int width, height;
    public final int left, right;
    public final int top, bottom;
    public final int capLeft, capRight;

    private BarTextureInfo(int width, int height, int left, int right, int top, int bottom, int capLeft, int capRight) {
        this.width = width;
        this.height = height;
        this.left = left;
        this.right = right;
        this.top = top;
        this.bottom = bottom;
        this.capLeft = capLeft;
        this.capRight = capRight;
    }

    public static BarTextureInfo get(ResourceLocation texture) {
        return CACHE.computeIfAbsent(texture, BarTextureInfo::load);
    }

    public static void clear() {
        CACHE.clear();
    }

    private static BarTextureInfo load(ResourceLocation texture) {
        try (InputStream in = Minecraft.getInstance().getResourceManager().open(texture);
             NativeImage img = NativeImage.read(NativeImage.Format.RGBA, in)) {
            BarTextureInfo info = analyze(img);
            LOGGER.info("Bar texture {}: {}x{} px, visible x[{},{}) y[{},{}), caps L{} R{}",
                    texture, info.width, info.height, info.left, info.right, info.top, info.bottom, info.capLeft, info.capRight);
            return info;
        } catch (IOException | RuntimeException e) {
            LOGGER.warn("Could not analyze bar texture {}, using full-quad layout", texture, e);
            return new BarTextureInfo(64, 8, 0, 64, 0, 8, 4, 4);
        }
    }

    private static BarTextureInfo analyze(NativeImage img) {
        int w = img.getWidth(), h = img.getHeight();
        int left = w, right = -1, top = h, bottom = -1;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if (alpha(img, x, y) > ALPHA_CUTOFF) {
                    left = Math.min(left, x);
                    right = Math.max(right, x);
                    top = Math.min(top, y);
                    bottom = Math.max(bottom, y);
                }
            }
        }
        if (right < 0) return new BarTextureInfo(w, h, 0, w, 0, h, 0, 0);
        right++;
        bottom++;

        int mid = (left + right) / 2;
        int fallbackCap = Math.min(bottom - top, mid - left);

        int capL = fallbackCap;
        for (int x = left; x < mid; x++) {
            if (sameColumn(img, x, mid, top, bottom)) { capL = x - left; break; }
        }
        int capR = fallbackCap;
        for (int x = right - 1; x > mid; x--) {
            if (sameColumn(img, x, mid, top, bottom)) { capR = right - 1 - x; break; }
        }
        return new BarTextureInfo(w, h, left, right, top, bottom, capL, capR);
    }

    private static boolean sameColumn(NativeImage img, int x, int ref, int top, int bottom) {
        for (int y = top; y < bottom; y++) {
            if (Math.abs(alpha(img, x, y) - alpha(img, ref, y)) > PROFILE_TOLERANCE) return false;
        }
        return true;
    }

    private static int alpha(NativeImage img, int x, int y) {
        return img.getPixelRGBA(x, y) >>> 24;
    }

    public float[] quadForVisible(float vx1, float vy1, float vx2, float vy2) {
        float pxW = (vx2 - vx1) / (right - left);
        float pxH = (vy2 - vy1) / (bottom - top);
        float qx1 = vx1 - left * pxW;
        float qx2 = qx1 + width * pxW;
        float qy2 = vy2 + top * pxH;
        float qy1 = qy2 - height * pxH;
        return new float[]{qx1, qy1, qx2, qy2};
    }

    public float visibleLeft(float[] q)   { return q[0] + (q[2] - q[0]) * left / width; }
    public float visibleRight(float[] q)  { return q[0] + (q[2] - q[0]) * right / width; }
    public float visibleTop(float[] q)    { return q[3] - (q[3] - q[1]) * top / height; }
    public float visibleBottom(float[] q) { return q[3] - (q[3] - q[1]) * bottom / height; }
}