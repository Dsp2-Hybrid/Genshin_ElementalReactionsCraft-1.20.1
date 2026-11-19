package com.dsp2.genshinerc.client.render.world.component.damageindicator;

import com.dsp2.genshinerc.client.render.world.component.damageindicator.event.DamageTracker;

import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.*;
import net.minecraft.client.multiplayer.ClientLevel;

import net.minecraft.client.renderer.MultiBufferSource;
import com.dsp2.genshinerc.client.render.world.component.damageindicator.config.DamageIndicatorConfig;



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

    private record Frame(Matrix4f matrix, Vec3 eye, float partial) {}
    private static final ArrayList<TextParticle> POPUPS = new ArrayList<>();
    private static ClientLevel level;
    private static Frame frame;
    private static boolean mode;
    public static boolean enabled() {
        syncLevel();
        boolean next = DamageIndicatorConfig.get().screenDamageEnabled;
        if (mode != next) {
            mode = next;
            POPUPS.clear();
            frame = null;
            DamageTracker.clearParticles();
        }
        return mode;
    }
    private static void syncLevel() {
        var current = Minecraft.getInstance().level;
        if (level != current) { level = current; POPUPS.clear(); frame = null; DamageTracker.clearParticles(); }
    }
    public static void add(TextParticle particle) {
        if (!enabled() || level == null) return;
        if (POPUPS.size() >= 128) POPUPS.remove(0);
        POPUPS.add(particle);
    }
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;
        syncLevel();
        if (enabled() && !Minecraft.getInstance().isPaused()) POPUPS.removeIf(TextParticle::tick);
    }
    @SubscribeEvent public static void capture(RenderLevelStageEvent e) {
        if (e.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || !enabled()) return;
        syncLevel();
        frame = POPUPS.isEmpty() ? null : new Frame(new Matrix4f(e.getProjectionMatrix()).mul(e.getPoseStack().last().pose()), Minecraft.getInstance().gameRenderer.getMainCamera().getPosition(), e.getPartialTick());
    }
    @SubscribeEvent public static void draw(RenderGuiEvent.Post e) {
        Frame f = frame; frame = null;
        var mc = Minecraft.getInstance();
        if (!enabled() || f == null || mc.level != level || mc.options.hideGui) return;
        var gui = e.getGuiGraphics();
        gui.flush();
        var buffers = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
        for (TextParticle p : POPUPS) {
            Vec3 delta = p.position(f.partial()).subtract(f.eye());
            Vector4f clip = f.matrix().transform(new Vector4f((float) delta.x, (float) delta.y, (float) delta.z, 1f));
            if (clip.w <= 0.001f || java.lang.Math.abs(clip.z) > clip.w
                    || java.lang.Math.abs(clip.x) > clip.w || java.lang.Math.abs(clip.y) > clip.w) continue;
            float x = (0.5f + 0.5f * clip.x / clip.w) * gui.guiWidth();
            float y = (0.5f - 0.5f * clip.y / clip.w) * gui.guiHeight();
            p.renderScreen(gui.pose(), buffers, f.partial(), x, y);
        }
        buffers.endBatch();
    }
}
