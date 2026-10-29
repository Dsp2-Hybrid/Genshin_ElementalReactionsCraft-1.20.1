package com.dsp2.genshinerc.client.render.hud.gui.rpg.render;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.Mth;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@SuppressWarnings({"deprecation", "removal"})
public final class RpgTextures {
    private static final Logger LOG = LogUtils.getLogger();
    private static final int VISIBLE_ALPHA = 8;
    private static final int OUTLINE_MAX_CHANNEL = 40;

    private record Key(ResourceLocation tex, boolean fx, boolean fy) {}

    private static final Map<Key, Info> CACHE = new HashMap<>();
    private static final List<ResourceLocation> OWNED = new ArrayList<>();
    private static ResourceLocation pixel, hatch;

    private RpgTextures() {}

    public record Info(int w, int h, int ax0, int ay0, int ax1, int ay1, int ix0, int ix1,
                       ResourceLocation white, ResourceLocation tint, ResourceLocation core, ResourceLocation outline) {
        public float u(float f) { return Mth.lerp(Mth.clamp(f, 0f, 1f), ix0, ix1) / (float) w; }
        public float vTop() { return ay0 / (float) h; }
        public float vBottom() { return ay1 / (float) h; }
        public float aspect() { return h / (float) w; }
    }

    public static Info info(ResourceLocation tex) { return info(tex, false, false); }

    public static Info info(ResourceLocation tex, boolean flipX, boolean flipY) {
        return CACHE.computeIfAbsent(new Key(tex, flipX, flipY), RpgTextures::build);
    }

    public static ResourceLocation pixel() {
        if (pixel == null) {
            NativeImage img = new NativeImage(1, 1, true);
            img.setPixelRGBA(0, 0, 0xFFFFFFFF);
            pixel = register("pixel", img);
        }
        return pixel;
    }

    public static ResourceLocation hatch() {
        if (hatch == null) {
            NativeImage img = new NativeImage(16, 16, true);
            for (int y = 0; y < 16; y++)
                for (int x = 0; x < 16; x++)
                    img.setPixelRGBA(x, y, ((x + y) & 7) < 3 ? 0xFFFFFFFF : 0);
            hatch = register("hatch", img);
        }
        return hatch;
    }

    public static void clear() {
        var tm = Minecraft.getInstance().getTextureManager();
        for (ResourceLocation id : OWNED) tm.release(id);
        OWNED.clear();
        CACHE.clear();
        TexScan.clear();
        pixel = hatch = null;
    }

    private static Info missing(ResourceLocation tex) { return new Info(1, 1, 0, 0, 1, 1, 0, 1, tex, tex, tex, tex); }

    private static NativeImage mirrored(NativeImage src, boolean fx, boolean fy) {
        int w = src.getWidth(), h = src.getHeight();
        NativeImage out = new NativeImage(w, h, true);
        for (int y = 0; y < h; y++)
            for (int x = 0; x < w; x++)
                out.setPixelRGBA(x, y, src.getPixelRGBA(fx ? w - 1 - x : x, fy ? h - 1 - y : y));
        return out;
    }

    private static Info build(Key k) {
        ResourceLocation tex = k.tex();
        Optional<Resource> res = Minecraft.getInstance().getResourceManager().getResource(tex);
        if (res.isEmpty()) {
            LOG.warn("[genshinerc] Missing HUD texture {}", tex);
            return missing(tex);
        }
        try (InputStream in = res.get().open(); NativeImage src = NativeImage.read(in)) {
            NativeImage img = (k.fx() || k.fy()) ? mirrored(src, k.fx(), k.fy()) : src;
            try {
                return analyse(k, img);
            } finally {
                if (img != src) img.close();
            }
        } catch (IOException e) {
            LOG.warn("[genshinerc] Failed to read HUD texture {}", tex, e);
            return missing(tex);
        }
    }

    private static Info analyse(Key k, NativeImage img) {
        ResourceLocation tex = k.tex();
        int w = img.getWidth(), h = img.getHeight();
        boolean[] inside = interior(img, w, h);
        int ax0 = w, ay0 = h, ax1 = 0, ay1 = 0, ix0 = w, ix1 = 0, maxLum = 1;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int p = img.getPixelRGBA(x, y);
                if ((p >>> 24) > VISIBLE_ALPHA) {
                    ax0 = Math.min(ax0, x); ax1 = Math.max(ax1, x + 1);
                    ay0 = Math.min(ay0, y); ay1 = Math.max(ay1, y + 1);
                }
                if (inside[y * w + x]) {
                    ix0 = Math.min(ix0, x); ix1 = Math.max(ix1, x + 1);
                    maxLum = Math.max(maxLum, lum(p));
                }
            }
        }
        if (ax1 <= ax0) { ax0 = 0; ay0 = 0; ax1 = w; ay1 = h; }
        if (ix1 <= ix0) { ix0 = ax0; ix1 = ax1; }

        NativeImage white = new NativeImage(w, h, true), tint = new NativeImage(w, h, true),
                core = new NativeImage(w, h, true), outline = new NativeImage(w, h, true);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int p = img.getPixelRGBA(x, y), a = p >>> 24;
                if (inside[y * w + x]) {
                    int l = Math.min(255, lum(p) * 255 / maxLum);
                    white.setPixelRGBA(x, y, (a << 24) | 0xFFFFFF);
                    tint.setPixelRGBA(x, y, (a << 24) | (l << 16) | (l << 8) | l);
                    core.setPixelRGBA(x, y, p);
                    outline.setPixelRGBA(x, y, 0);
                } else {
                    white.setPixelRGBA(x, y, 0);
                    tint.setPixelRGBA(x, y, 0);
                    core.setPixelRGBA(x, y, 0);
                    outline.setPixelRGBA(x, y, a > VISIBLE_ALPHA ? p : 0);
                }
            }
        }
        String key = tex.getNamespace() + "/" + tex.getPath() + (k.fx() ? "_fx" : "") + (k.fy() ? "_fy" : "");
        return new Info(w, h, ax0, ay0, ax1, ay1, ix0, ix1,
                register("white/" + key, white), register("tint/" + key, tint),
                register("core/" + key, core), register("outline/" + key, outline));
    }

    private static boolean[] interior(NativeImage img, int w, int h) {
        int n = w * h;
        boolean[] visible = new boolean[n], outline = new boolean[n], outside = new boolean[n];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int p = img.getPixelRGBA(x, y), i = y * w + x;
                visible[i] = (p >>> 24) > VISIBLE_ALPHA;
                int maxCh = Math.max(p & 0xFF, Math.max((p >> 8) & 0xFF, (p >> 16) & 0xFF));
                outline[i] = visible[i] && maxCh <= OUTLINE_MAX_CHANNEL;
            }
        }
        int[] queue = new int[n];
        int head = 0, tail = 0;
        for (int x = 0; x < w; x++) {
            tail = seed(x, outline, outside, queue, tail);
            tail = seed((h - 1) * w + x, outline, outside, queue, tail);
        }
        for (int y = 0; y < h; y++) {
            tail = seed(y * w, outline, outside, queue, tail);
            tail = seed(y * w + w - 1, outline, outside, queue, tail);
        }
        while (head < tail) {
            int i = queue[head++], x = i % w, y = i / w;
            if (x > 0) tail = seed(i - 1, outline, outside, queue, tail);
            if (x < w - 1) tail = seed(i + 1, outline, outside, queue, tail);
            if (y > 0) tail = seed(i - w, outline, outside, queue, tail);
            if (y < h - 1) tail = seed(i + w, outline, outside, queue, tail);
        }
        boolean[] inside = new boolean[n];
        boolean any = false;
        for (int i = 0; i < n; i++) { inside[i] = visible[i] && !outline[i] && !outside[i]; any |= inside[i]; }
        if (!any) for (int i = 0; i < n; i++) inside[i] = visible[i] && !outline[i];
        return inside;
    }

    private static int seed(int i, boolean[] outline, boolean[] outside, int[] queue, int tail) {
        if (outline[i] || outside[i]) return tail;
        outside[i] = true;
        queue[tail] = i;
        return tail + 1;
    }

    private static int lum(int p) { return ((p & 0xFF) * 299 + ((p >> 8) & 0xFF) * 587 + ((p >> 16) & 0xFF) * 114) / 1000; }

    private static ResourceLocation register(String path, NativeImage img) {
        ResourceLocation id = new ResourceLocation("genshinerc", "dynamic/rpg/" + path);
        Minecraft.getInstance().getTextureManager().register(id, new DynamicTexture(img));
        OWNED.add(id);
        return id;
    }
}