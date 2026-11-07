package com.dsp2.genshinerc.util.framesequence.meta;

public class FrameMetadata {

    /** 鎬诲抚鏁?*/
    public int frames;

    /** 姣忕甯х巼 */
    public int fps;

    /** 鏄惁寰幆 */
    public boolean loop = true;

    /** 姣忓抚瀹藉害 */
    public int width;

    /** 姣忓抚楂樺害 */
    public int height;

    /** 鏂囦欢鍚嶆牸寮忥紝渚嬪 frame_0000.png 瀵瑰簲 "frame_%04d" */
    public String pattern = "frame_%04d";

    /** 绾圭悊鎵€鍦ㄧ洰褰曪紝鐩稿浜庡綋鍓?mod 鐨?textures/ */
    public String texturePath;
}