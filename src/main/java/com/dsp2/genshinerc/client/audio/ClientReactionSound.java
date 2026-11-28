package com.dsp2.genshinerc.client.audio;

import com.dsp2.genshinerc.common.network.base.element.ReactionSoundPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class ClientReactionSound {
    private ClientReactionSound() {}

    public static void play(ReactionSoundPacket msg) {
        SoundEvent event = BuiltInRegistries.SOUND_EVENT.get(msg.soundId);
        if (event == null) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        double dx = msg.x - mc.player.getX();
        double dy = msg.y - mc.player.getY();
        double dz = msg.z - mc.player.getZ();
        if (dx * dx + dy * dy + dz * dz > 64.0 * 64.0) return;
        mc.getSoundManager().play(new ClippedFadeSound(event, msg.x, msg.y, msg.z, msg.volume, msg.pitch, msg.clipTicks, msg.fadeTicks));
    }

    private static final class ClippedFadeSound extends AbstractTickableSoundInstance {
        private final int clipTicks;
        private final int fadeTicks;
        private final float baseVolume;
        private int age = 0;

        protected ClippedFadeSound(SoundEvent event, double x, double y, double z, float volume, float pitch, int clipTicks, int fadeTicks) {
            super(event, SoundSource.HOSTILE, SoundInstance.createUnseededRandom());
            this.x = x;
            this.y = y;
            this.z = z;
            this.baseVolume = volume;
            this.volume = volume;
            this.pitch = pitch;
            this.clipTicks = Math.max(1, clipTicks);
            this.fadeTicks = Math.max(0, fadeTicks);
            this.looping = false;
            this.delay = 0;
            this.attenuation = Attenuation.LINEAR;
        }

        @Override
        public void tick() {
            age++;
            int fadeStart = clipTicks - fadeTicks;
            if (age >= clipTicks) {
                this.stop();
                return;
            }
            if (fadeTicks > 0 && age >= fadeStart) {
                float t = 1.0f - ((float) (age - fadeStart) / (float) fadeTicks);
                this.volume = baseVolume * Math.max(0f, t);
            }
        }
    }
}