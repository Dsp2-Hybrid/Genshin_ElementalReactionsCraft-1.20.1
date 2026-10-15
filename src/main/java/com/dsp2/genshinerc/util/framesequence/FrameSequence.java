package com.dsp2.genshinerc.util.framesequence;

import com.dsp2.genshinerc.util.framesequence.meta.FrameMetadata;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;

public final class FrameSequence {

    private final ResourceLocation id;
    private final FrameMetadata    meta;

    private boolean loop;
    private float   speed;
    private float   alpha;

    private long    startTime;
    private boolean playing;
    private boolean finished;

    FrameSequence(FrameSequenceBuilder builder) {
        this.id    = builder.getId();
        this.meta  = builder.getMetadata();
        this.loop  = builder.isLoop();
        this.speed = builder.getSpeed();
        this.alpha = builder.getAlpha();
    }

    public static FrameSequenceBuilder startBuilder(ResourceLocation id) {
        return new FrameSequenceBuilder(id);
    }

    // ---------- 控制 ----------

    public FrameSequence play() {
        this.playing   = true;
        this.finished  = false;
        this.startTime = Util.getMillis();
        return this;
    }

    public FrameSequence pause() {
        this.playing = false;
        return this;
    }

    public FrameSequence reset() {
        this.startTime = Util.getMillis();
        this.finished  = false;
        return this;
    }

    public FrameSequence withLoop(boolean loop) {
        this.loop = loop;
        return this;
    }

    public FrameSequence withSpeed(float speed) {
        this.speed = speed;
        return this;
    }

    public FrameSequence withAlpha(float alpha) {
        this.alpha = alpha;
        return this;
    }

    // ---------- 状态 ----------

    public ResourceLocation getId()         { return id; }
    public FrameMetadata    getMetadata()   { return meta; }
    public boolean          isLoop()        { return loop; }
    public boolean          isPlaying()     { return playing; }
    public boolean          isFinished()    { return finished; }
    public float            getSpeed()      { return speed; }
    public float            getAlpha()      { return alpha; }
    public int              getWidth()      { return meta.width; }
    public int              getHeight()     { return meta.height; }

    // ---------- 当前帧 ----------

    public ResourceLocation getCurrentFrame() {
        int totalFrames = meta.frames;
        int frameIndex;

        if (!playing || finished) {
            frameIndex = finished ? totalFrames - 1 : 0;
        } else {
            long elapsed = Util.getMillis() - startTime;
            frameIndex = (int) (elapsed / 1000.0 * meta.fps * speed);

            if (loop) {
                frameIndex = ((frameIndex % totalFrames) + totalFrames) % totalFrames;
            } else if (frameIndex >= totalFrames) {
                frameIndex = totalFrames - 1;
                playing    = false;
                finished   = true;
            }
            if (frameIndex < 0) frameIndex = 0;
        }

        String fileName = String.format(meta.pattern, frameIndex);
        return new ResourceLocation(id.getNamespace(), meta.texturePath + "/" + fileName + ".png");
    }
}