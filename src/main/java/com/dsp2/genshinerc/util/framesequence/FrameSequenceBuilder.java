package com.dsp2.genshinerc.util.framesequence;

import com.dsp2.genshinerc.util.framesequence.internal.FrameSequenceManager;
import com.dsp2.genshinerc.util.framesequence.meta.FrameMetadata;
import net.minecraft.resources.ResourceLocation;

public class FrameSequenceBuilder {

    private final ResourceLocation id;
    private FrameMetadata meta;
    private boolean       loop  = true;
    private float         speed = 1.0F;
    private float         alpha = 1.0F;

    FrameSequenceBuilder(ResourceLocation id) {
        this.id = id;
    }

    public FrameSequenceBuilder withMetadata(FrameMetadata meta) {
        this.meta = meta;
        return this;
    }

    public FrameSequenceBuilder withMetadata(ResourceLocation metaLocation) {
        this.meta = FrameSequenceManager.loadMetadata(metaLocation);
        return this;
    }

    public FrameSequenceBuilder withLoop(boolean loop) {
        this.loop = loop;
        return this;
    }

    public FrameSequenceBuilder withSpeed(float speed) {
        this.speed = speed;
        return this;
    }

    public FrameSequenceBuilder withAlpha(float alpha) {
        this.alpha = alpha;
        return this;
    }

    public FrameSequence build() {
        if (meta == null) {
            throw new IllegalStateException("FrameSequence '" + id + "': metadata 未设置");
        }
        if (meta.frames <= 0) {
            throw new IllegalStateException("FrameSequence '" + id + "': frames 非法");
        }
        if (meta.fps <= 0) {
            throw new IllegalStateException("FrameSequence '" + id + "': fps 非法");
        }
        if (meta.texturePath == null || meta.texturePath.isEmpty()) {
            throw new IllegalStateException("FrameSequence '" + id + "': texturePath 未设置");
        }
        return new FrameSequence(this);
    }

    // ---------- 供 FrameSequence 读取 ----------

    ResourceLocation getId()          { return id; }
    FrameMetadata    getMetadata()    { return meta; }
    boolean          isLoop()         { return loop; }
    float            getSpeed()       { return speed; }
    float            getAlpha()       { return alpha; }
}