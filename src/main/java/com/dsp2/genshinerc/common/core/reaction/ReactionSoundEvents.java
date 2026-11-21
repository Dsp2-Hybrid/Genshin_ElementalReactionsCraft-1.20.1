package com.dsp2.genshinerc.common.core.reaction;

import com.dsp2.genshinerc.common.core.element.ElementType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;

public final class ReactionSoundEvents {
    private ReactionSoundEvents() {}
    private static ServerLevel L(LivingEntity e) { return e.level() instanceof ServerLevel sl ? sl : null; }

    public static void playOverload(LivingEntity e) { ServerLevel sl=L(e); if(sl==null)return; sl.playSound(null,e.blockPosition(),SoundEvents.GENERIC_EXPLODE,SoundSource.HOSTILE,1.0f,0.8f+sl.getRandom().nextFloat()*0.4f);
        sl.playSound(null,e.blockPosition(),SoundEvents.LIGHTNING_BOLT_THUNDER,SoundSource.HOSTILE,0.6f,1.2f); }
    public static void playSuperconduct(LivingEntity e) { ServerLevel sl=L(e); if(sl==null)return; sl.playSound(null,e.blockPosition(),SoundEvents.GLASS_BREAK,SoundSource.HOSTILE,0.8f,0.7f);
        sl.playSound(null,e.blockPosition(),SoundEvents.LIGHTNING_BOLT_THUNDER,SoundSource.HOSTILE,0.4f,1.5f); }
    public static void playElectroChargedStart(LivingEntity e) {
        ServerLevel level = L(e);
        if (level == null) return;
        level.playSound(null, e.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME,
                SoundSource.HOSTILE, 0.42f, 1.55f + level.getRandom().nextFloat() * 0.18f);
        level.playSound(null, e.blockPosition(), SoundEvents.GENERIC_SPLASH,
                SoundSource.HOSTILE, 0.20f, 1.75f);
    }

    public static void playElectroCharged(LivingEntity e) {
        ServerLevel level = L(e);
        if (level == null) return;
        float pitch = 1.65f + level.getRandom().nextFloat() * 0.30f;
        level.playSound(null, e.blockPosition(), SoundEvents.LIGHTNING_BOLT_IMPACT,
                SoundSource.HOSTILE, 0.20f, pitch);
        level.playSound(null, e.blockPosition(), SoundEvents.GENERIC_SPLASH,
                SoundSource.HOSTILE, 0.13f, 1.85f);
    }
    public static void playFreeze(LivingEntity e) { ServerLevel sl=L(e); if(sl==null)return; sl.playSound(null,e.blockPosition(),SoundEvents.GLASS_PLACE,SoundSource.HOSTILE,0.7f,0.8f); }
    public static void playShatter(LivingEntity e) {
        ServerLevel sl = L(e);
        if (sl == null) return;
        sl.playSound(null, e.blockPosition(), SoundEvents.GLASS_BREAK, SoundSource.HOSTILE,
                0.95f, 0.95f + sl.getRandom().nextFloat() * 0.20f);
        sl.playSound(null, e.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.HOSTILE, 0.40f, 1.25f);
    }
    public static void playMelt(LivingEntity e) { ServerLevel sl=L(e); if(sl==null)return; sl.playSound(null,e.blockPosition(),SoundEvents.LAVA_EXTINGUISH,SoundSource.HOSTILE,0.6f,1.3f); }
    public static void playVaporize(LivingEntity e) { ServerLevel sl=L(e); if(sl==null)return; sl.playSound(null,e.blockPosition(),SoundEvents.FIRE_EXTINGUISH,SoundSource.HOSTILE,0.8f,1.1f); }
    public static void playSwirl(LivingEntity e) {
        playSwirl(e, ElementType.ANEMO);
    }

    public static void playSwirl(LivingEntity e, ElementType element) {
        ServerLevel level = L(e);
        if (level == null) return;
        level.playSound(null, e.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.HOSTILE,
                0.60f, 1.1f + level.getRandom().nextFloat() * 0.24f);
        SoundEventChoice.play(level, e, element);
    }

    private static final class SoundEventChoice {
        private static void play(ServerLevel level, LivingEntity e, ElementType element) {
            var sound = switch (element) {
                case PYRO -> SoundEvents.FIRE_EXTINGUISH;
                case HYDRO -> SoundEvents.GENERIC_SPLASH;
                case CRYO, FROZEN -> SoundEvents.AMETHYST_BLOCK_CHIME;
                case ELECTRO -> SoundEvents.LIGHTNING_BOLT_IMPACT;
                default -> SoundEvents.PLAYER_ATTACK_SWEEP;
            };
            level.playSound(null, e.blockPosition(), sound, SoundSource.HOSTILE, 0.30f, 1.55f);
        }
    }
    public static void playCrystallize(LivingEntity e) { ServerLevel sl=L(e); if(sl==null)return; sl.playSound(null,e.blockPosition(),SoundEvents.AMETHYST_BLOCK_CHIME,SoundSource.HOSTILE,0.5f,1.2f); }
    public static void playBloom(LivingEntity e) { ServerLevel sl=L(e); if(sl==null)return; sl.playSound(null,e.blockPosition(),SoundEvents.GRASS_BREAK,SoundSource.HOSTILE,0.8f,0.9f); }
    public static void playBurning(LivingEntity e) { ServerLevel sl=L(e); if(sl==null)return; sl.playSound(null,e.blockPosition(),SoundEvents.FIRE_AMBIENT,SoundSource.HOSTILE,0.4f,1.0f); }
}