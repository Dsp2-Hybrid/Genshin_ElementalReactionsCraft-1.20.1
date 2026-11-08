package com.dsp2.genshinerc.util.framesequence.meta;

public class FrameMetadata {

    /** Total number of frames */
    public int frames;

    /** Frames per second */
    public int fps;

    /** Whether the sequence loops */
    public boolean loop = true;

    /** Width of each frame */
    public int width;

    /** Height of each frame */
    public int height;

    /** Filename format, e.g., frame_0000.png corresponds to "frame_%04d" */
    public String pattern = "frame_%04d";

    /** Texture directory path, relative to the current mod's textures/ */
    public String texturePath;
}