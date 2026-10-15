package com.dsp2.genshinerc.client.render.damageindicator;

import com.dsp2.genshinerc.client.render.damageindicator.api.CustomColor;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public class TextParticle {
    private double x, y, z, prevX, prevY, prevZ, vx, vy, vz;
    private int age;
    private final int maxAge;
    private final Component text;
    private final CustomColor color;
    private final float scale;
    private final boolean drawShadow;
    private final float gravity;
    private final float velocityMult;

    public TextParticle(double x, double y, double z, double vx, double vy, double vz,
                        Component text, CustomColor color, float velocityMult, float gravity,
                        int maxAge, float scale, boolean drawShadow) {
        this.x = x; this.y = y; this.z = z;
        this.prevX = x; this.prevY = y; this.prevZ = z;
        this.vx = vx; this.vy = vy; this.vz = vz;
        this.text = text;
        this.color = color;
        this.velocityMult = velocityMult;
        this.gravity = gravity;
        this.maxAge = maxAge;
        this.scale = scale;
        this.drawShadow = drawShadow;
    }

    public boolean tick() {
        prevX = x; prevY = y; prevZ = z;
        vy += gravity;
        vx *= velocityMult; vy *= velocityMult; vz *= velocityMult;
        x += vx; y += vy; z += vz;
        return ++age >= maxAge;
    }

    public void render(PoseStack poseStack, MultiBufferSource buffer, Camera camera, float partialTick) {
        double px = Mth.lerp(partialTick, prevX, x) - camera.getPosition().x;
        double py = Mth.lerp(partialTick, prevY, y) - camera.getPosition().y;
        double pz = Mth.lerp(partialTick, prevZ, z) - camera.getPosition().z;

        poseStack.pushPose();
        poseStack.translate(px, py, pz);
        poseStack.mulPose(camera.rotation());
        float s = 0.03f * scale;
        poseStack.scale(-s, -s, s);

        Font font = Minecraft.getInstance().font;
        font.drawInBatch(text, font.width(text) / -2.0f, 0f, color.argb(), drawShadow,
                poseStack.last().pose(), buffer, Font.DisplayMode.NORMAL, 0, 15728880);
        poseStack.popPose();
    }
}