package com.dsp2.genshinerc.client.render.world.component.damageindicator;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import org.joml.Vector4f;

final class GradientTextBuffer implements MultiBufferSource {
    private final MultiBufferSource delegate;
    private final Matrix4f inversePose;
    private final float minX, maxX, minY, maxY;
    private final int rgbFrom, rgbTo, alpha;
    private final int direction;

    GradientTextBuffer(MultiBufferSource delegate, Matrix4f pose, float minX, float maxX, float minY, float maxY,
                       int rgbFrom, int rgbTo, int alpha, int direction) {
        this.delegate = delegate;
        this.inversePose = new Matrix4f(pose).invert();
        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;
        this.rgbFrom = rgbFrom;
        this.rgbTo = rgbTo;
        this.alpha = alpha;
        this.direction = direction;
    }

    @Override
    public VertexConsumer getBuffer(RenderType type) {
        return new GradientConsumer(delegate.getBuffer(type));
    }

    private static int mix(int from, int to, float t) {
        return Math.round(from + (to - from) * t);
    }

    private final class GradientConsumer implements VertexConsumer {
        private final VertexConsumer out;
        private float localX, localY;

        private GradientConsumer(VertexConsumer out) {
            this.out = out;
        }

        @Override
        public VertexConsumer vertex(Matrix4f m, float x, float y, float z) {
            localX = x;
            localY = y;
            out.vertex(m, x, y, z);
            return this;
        }

        @Override
        public VertexConsumer vertex(double x, double y, double z) {
            Vector4f v = inversePose.transform(new Vector4f((float) x, (float) y, (float) z, 1f));
            localX = v.x();
            localY = v.y();
            out.vertex(x, y, z);
            return this;
        }

        @Override
        public VertexConsumer color(int r, int g, int b, int a) {
            float t = 0f;
            if (direction == 0) {
                float width = maxX - minX;
                t = width <= 0f ? 0f : Mth.clamp((localX - minX) / width, 0f, 1f);
            } else if (direction == 1) {
                float height = maxY - minY;
                t = height <= 0f ? 0f : Mth.clamp((localY - minY) / height, 0f, 1f);
            } else {
                float totalDist = (maxX - minX) + (maxY - minY);
                if (totalDist > 0f) {
                    float currentDist = (localX - minX) + (localY - minY);
                    t = Mth.clamp(currentDist / totalDist, 0f, 1f);
                }
            }

            out.color(
                    mix((rgbFrom >> 16) & 0xFF, (rgbTo >> 16) & 0xFF, t),
                    mix((rgbFrom >> 8) & 0xFF, (rgbTo >> 8) & 0xFF, t),
                    mix(rgbFrom & 0xFF, rgbTo & 0xFF, t),
                    alpha * a / 255);
            return this;
        }

        @Override
        public VertexConsumer uv(float u, float v) { out.uv(u, v); return this; }
        @Override
        public VertexConsumer overlayCoords(int u, int v) { out.overlayCoords(u, v); return this; }
        @Override
        public VertexConsumer uv2(int u, int v) { out.uv2(u, v); return this; }
        @Override
        public VertexConsumer normal(float x, float y, float z) { out.normal(x, y, z); return this; }
        @Override
        public void endVertex() { out.endVertex(); }
        @Override
        public void defaultColor(int r, int g, int b, int a) { out.defaultColor(r, g, b, a); }
        @Override
        public void unsetDefaultColor() { out.unsetDefaultColor(); }
    }
}