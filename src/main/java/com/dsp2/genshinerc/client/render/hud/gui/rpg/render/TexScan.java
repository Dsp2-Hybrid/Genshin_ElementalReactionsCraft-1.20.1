package com.dsp2.genshinerc.client.render.hud.gui.rpg.render;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class TexScan {
    private static final Logger LOG = LogUtils.getLogger();
    private static final int VISIBLE_ALPHA = 8;
    private static final Map<ResourceLocation, Frame> FRAMES = new HashMap<>();
    private static final Map<ResourceLocation, Rows> ROWS = new HashMap<>();

    private TexScan() {}

    public record Frame(int w, int h, int ax0, int ay0, int ax1, int ay1, int sliceX, int sliceY) {}

    public record Rows(int w, int h, int[] min, int[] max) {
        public float centerX(int row) {
            for (int d = 0; d < h; d++) {
                int up = row - d, dn = row + d;
                if (up >= 0 && up < h && min[up] >= 0) return (min[up] + max[up]) / 2f;
                if (dn >= 0 && dn < h && min[dn] >= 0) return (min[dn] + max[dn]) / 2f;
            }
            return -1f;
        }
    }

    public static Frame frame(ResourceLocation tex) { return FRAMES.computeIfAbsent(tex, TexScan::scanFrame); }
    public static Rows rows(ResourceLocation tex) { return ROWS.computeIfAbsent(tex, TexScan::scanRows); }

    public static void clear() { FRAMES.clear(); ROWS.clear(); }

    private static Frame scanFrame(ResourceLocation tex) {
        NativeImage img = read(tex);
        if (img == null) return new Frame(1, 1, 0, 0, 1, 1, 0, 0);
        try (img) {
            int w = img.getWidth(), h = img.getHeight(), ax0 = w, ay0 = h, ax1 = 0, ay1 = 0;
            for (int y = 0; y < h; y++)
                for (int x = 0; x < w; x++)
                    if (opaque(img, x, y)) {
                        ax0 = Math.min(ax0, x); ax1 = Math.max(ax1, x + 1);
                        ay0 = Math.min(ay0, y); ay1 = Math.max(ay1, y + 1);
                    }
            if (ax1 <= ax0) return new Frame(w, h, 0, 0, w, h, 0, 0);
            int bw = ax1 - ax0, bh = ay1 - ay0, mx = (ax0 + ax1) / 2, my = (ay0 + ay1) / 2;
            int top = run(img, mx, ay0, 0, 1, bh, true), bottom = run(img, mx, ay1 - 1, 0, -1, bh, true);
            int left = run(img, ax0, my, 1, 0, bw, true), right = run(img, ax1 - 1, my, -1, 0, bw, true);
            int bevelX = run(img, ax0, ay0, 1, 0, bw, false), bevelY = run(img, ax0, ay0, 0, 1, bh, false);
            int sx = clampSlice(Math.max(Math.max(left, right), bevelX + 1) + 1, bw);
            int sy = clampSlice(Math.max(Math.max(top, bottom), bevelY + 1) + 1, bh);
            return new Frame(w, h, ax0, ay0, ax1, ay1, sx, sy);
        }
    }

    private static Rows scanRows(ResourceLocation tex) {
        NativeImage img = read(tex);
        if (img == null) return new Rows(0, 0, new int[0], new int[0]);
        try (img) {
            int w = img.getWidth(), h = img.getHeight();
            int[] min = new int[h], max = new int[h];
            for (int y = 0; y < h; y++) {
                min[y] = -1; max[y] = -1;
                for (int x = 0; x < w; x++) {
                    if (!opaque(img, x, y)) continue;
                    if (min[y] < 0) min[y] = x;
                    max[y] = x;
                }
            }
            return new Rows(w, h, min, max);
        }
    }

    private static int clampSlice(int v, int extent) { return Math.max(1, Math.min(v, Math.max(1, extent / 2 - 1))); }

    private static int run(NativeImage img, int x, int y, int dx, int dy, int max, boolean wantOpaque) {
        int n = 0;
        while (n < max && x >= 0 && y >= 0 && x < img.getWidth() && y < img.getHeight() && opaque(img, x, y) == wantOpaque) {
            n++; x += dx; y += dy;
        }
        return n;
    }

    private static boolean opaque(NativeImage img, int x, int y) { return (img.getPixelRGBA(x, y) >>> 24) > VISIBLE_ALPHA; }

    @Nullable
    private static NativeImage read(ResourceLocation tex) {
        Optional<Resource> res = Minecraft.getInstance().getResourceManager().getResource(tex);
        if (res.isEmpty()) { LOG.warn("[genshinerc] Missing HUD texture {}", tex); return null; }
        try (InputStream in = res.get().open()) {
            return NativeImage.read(in);
        } catch (IOException e) {
            LOG.warn("[genshinerc] Failed to read HUD texture {}", tex, e);
            return null;
        }
    }
}