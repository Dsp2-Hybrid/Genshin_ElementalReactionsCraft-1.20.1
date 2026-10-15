package com.dsp2.genshinerc.util.framesequence.meta;

public class FrameMetadata {

    /** 总帧数 */
    public int frames;

    /** 每秒帧率 */
    public int fps;

    /** 是否循环 */
    public boolean loop = true;

    /** 每帧宽度 */
    public int width;

    /** 每帧高度 */
    public int height;

    /** 文件名格式，例如 frame_0000.png 对应 "frame_%04d" */
    public String pattern = "frame_%04d";

    /** 纹理所在目录，相对于当前 mod 的 textures/ */
    public String texturePath;
}