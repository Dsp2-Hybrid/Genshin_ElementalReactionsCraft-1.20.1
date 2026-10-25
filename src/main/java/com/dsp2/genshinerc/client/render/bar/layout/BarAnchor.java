package com.dsp2.genshinerc.client.render.bar.layout;

public record BarAnchor(float left, float right, float top, float bottom, float z) {
    public static BarAnchor fromFrameQuad(float x1, float y1, float x2, float y2, float z, BarTextureInfo frame) {
        float[] q = {x1, y1, x2, y2};
        return new BarAnchor(frame.visibleLeft(q), frame.visibleRight(q), frame.visibleTop(q), frame.visibleBottom(q), z);
    }

    public float width()   { return right - left; }
    public float height()  { return top - bottom; }
    public float centerX() { return (left + right) / 2f; }
}