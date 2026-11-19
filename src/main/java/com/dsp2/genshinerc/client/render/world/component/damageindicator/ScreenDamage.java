package com.dsp2.genshinerc.client.render.world.component.damageindicator;

import com.dsp2.genshinerc.client.render.world.component.damageindicator.event.DamageTracker;
import com.dsp2.genshinerc.client.render.world.component.damageindicator.api.CustomColor;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.*;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.*;
import java.util.ArrayList;

@Mod.EventBusSubscriber(modid = "genshinerc", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ScreenDamage {
    private record Popup(LivingEntity target, Component text, int color, Integer second, float scale, int born, int life, int slot) {}
    private record Frame(Matrix4f matrix, Vec3 eye, float partial) {}
    private static final ArrayList<Popup> POPUPS = new ArrayList<>();
    private static ClientLevel level;
    private static Frame frame;
    private static boolean enabled;
    private static int tick;
    public static boolean enabled() { return enabled; }
    private static void syncLevel() {
        var current = Minecraft.getInstance().level;
        if (level != current) { level = current; POPUPS.clear(); frame = null; tick = 0; }
    }
    @SubscribeEvent public static void commands(RegisterClientCommandsEvent e) {
        e.getDispatcher().register(Commands.literal("di").then(Commands.literal("on").executes(c -> mode(true))).then(Commands.literal("off").executes(c -> mode(false))));
    }
    private static int mode(boolean value) {
        if (enabled != value) { enabled = value; POPUPS.clear(); frame = null; DamageTracker.clearParticles(); }
        var player = Minecraft.getInstance().player;
        if (player != null) player.displayClientMessage(Component.literal(value ? "Damage indicator: GUI" : "Damage indicator: world particles"), false);
        return 1;
    }
    public static void add(LivingEntity target, String text, CustomColor color, CustomColor second, float scale, int life) {
        syncLevel();
        if (level == null || target.level() != level || !Float.isFinite(scale) || scale <= 0f) return;
        int used = 0, oldest = -1;
        for (int i = 0; i < POPUPS.size(); i++) { Popup p = POPUPS.get(i); if (p.target() == target) { used |= 1 << p.slot(); if (oldest < 0) oldest = i; } }
        int slot = Integer.numberOfTrailingZeros(~used);
        if (slot >= 6) slot = POPUPS.remove(oldest).slot();
        if (POPUPS.size() >= 128) POPUPS.remove(0);
        POPUPS.add(new Popup(target, Component.literal(text), color.argb(), second == null ? null : second.argb() & 0xFFFFFF, scale, tick, java.lang.Math.max(1, life), slot));
    }
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;
        syncLevel();
        if (!Minecraft.getInstance().isPaused()) { tick++; POPUPS.removeIf(p -> tick - p.born() >= p.life()); }
    }
    @SubscribeEvent public static void capture(RenderLevelStageEvent e) {
        if (!enabled || e.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;
        syncLevel();
        frame = POPUPS.isEmpty() ? null : new Frame(new Matrix4f(e.getProjectionMatrix()).mul(e.getPoseStack().last().pose()), Minecraft.getInstance().gameRenderer.getMainCamera().getPosition(), e.getPartialTick());
    }
    @SubscribeEvent public static void draw(RenderGuiEvent.Post e) {
        Frame f = frame; frame = null;
        var mc = Minecraft.getInstance();
        if (!enabled || f == null || mc.level != level || mc.options.hideGui) return;
        var gui = e.getGuiGraphics(); var pose = gui.pose(); Font font = mc.font;
        gui.flush();
        var buffers = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
        for (Popup p : POPUPS) {
            Vec3 at = p.target().isRemoved() ? p.target().position() : p.target().getPosition(f.partial());
            Vec3 delta = at.add(0, p.target().getBbHeight() * 1.12 + 0.12, 0).subtract(f.eye());
            Vector4f clip = f.matrix().transform(new Vector4f((float) delta.x, (float) delta.y, (float) delta.z, 1f));
            if (clip.w <= 0.001f || java.lang.Math.abs(clip.z) > clip.w || java.lang.Math.abs(clip.x) > clip.w || java.lang.Math.abs(clip.y) > clip.w) continue;
            float t = Mth.clamp((tick - p.born() + f.partial()) / p.life(), 0f, 1f);
            float fade = 1f - Mth.clamp((t - 0.60f) / 0.40f, 0f, 1f);
            int alpha = java.lang.Math.round((p.color() >>> 24) * fade * fade * Mth.lerp(Mth.clamp(t / 0.08f, 0f, 1f), 0.55f, 1f));
            if (alpha < 4) continue;
            int slot = p.slot(), side = slot == 0 ? 0 : (slot % 2 == 0 ? 1 : -1);
            float x = (0.5f + 0.5f * clip.x / clip.w) * gui.guiWidth() + side * (10f + 3f * slot);
            float y = (0.5f - 0.5f * clip.y / clip.w) * gui.guiHeight() - slot * 18f - 26f * t * (2f - t);
            float scale = Mth.clamp(p.scale(), 0.5f, 3.5f) * (1f + 0.24f * (float) java.lang.Math.sin(java.lang.Math.PI * Mth.clamp(t / 0.28f, 0f, 1f)));
            float width = font.width(p.text()), left = -width / 2f, top = -font.lineHeight / 2f;
            pose.pushPose(); pose.translate(x, y, 0f); pose.scale(scale, scale, 1f);
            Matrix4f matrix = pose.last().pose();
            for (int i = 0; i < 4; i++) font.drawInBatch(p.text(), left + (i < 2 ? i * 2 - 1 : 0), top + (i >= 2 ? (i - 2) * 2 - 1 : 0), (alpha << 24) | 0x191516, false, matrix, buffers, Font.DisplayMode.SEE_THROUGH, 0, 15728880);
            MultiBufferSource fill = p.second() == null ? buffers : new GradientTextBuffer(buffers, matrix, left, left + width, top, top + font.lineHeight, p.color() & 0xFFFFFF, p.second(), alpha, 0);
            font.drawInBatch(p.text(), left, top, p.second() == null ? (alpha << 24) | (p.color() & 0xFFFFFF) : 0xFFFFFFFF, false, matrix, fill, Font.DisplayMode.SEE_THROUGH, 0, 15728880);
            pose.popPose();
        }
        buffers.endBatch();
    }
}
