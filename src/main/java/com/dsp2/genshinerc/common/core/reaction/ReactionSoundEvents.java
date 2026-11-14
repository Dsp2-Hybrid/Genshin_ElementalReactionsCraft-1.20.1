package com.dsp2.genshinerc.common.core.reaction;

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
    public static void playElectroCharged(LivingEntity e) { ServerLevel sl=L(e); if(sl==null)return; sl.playSound(null,e.blockPosition(),SoundEvents.LIGHTNING_BOLT_THUNDER,SoundSource.HOSTILE,0.5f,1.8f); }
    public static void playFreeze(LivingEntity e) { ServerLevel sl=L(e); if(sl==null)return; sl.playSound(null,e.blockPosition(),SoundEvents.GLASS_PLACE,SoundSource.HOSTILE,0.7f,0.8f); }
    public static void playShatter(LivingEntity e) { ServerLevel sl=L(e); if(sl==null)return; sl.playSound(null,e.blockPosition(),SoundEvents.GLASS_BREAK,SoundSource.HOSTILE,1.2f,0.9f); }
    public static void playMelt(LivingEntity e) { ServerLevel sl=L(e); if(sl==null)return; sl.playSound(null,e.blockPosition(),SoundEvents.LAVA_EXTINGUISH,SoundSource.HOSTILE,0.6f,1.3f); }
    public static void playVaporize(LivingEntity e) { ServerLevel sl=L(e); if(sl==null)return; sl.playSound(null,e.blockPosition(),SoundEvents.FIRE_EXTINGUISH,SoundSource.HOSTILE,0.8f,1.1f); }
    public static void playSwirl(LivingEntity e) { ServerLevel sl=L(e); if(sl==null)return; sl.playSound(null,e.blockPosition(),SoundEvents.PLAYER_ATTACK_SWEEP,SoundSource.HOSTILE,0.7f,1.4f); }
    public static void playCrystallize(LivingEntity e) { ServerLevel sl=L(e); if(sl==null)return; sl.playSound(null,e.blockPosition(),SoundEvents.AMETHYST_BLOCK_CHIME,SoundSource.HOSTILE,0.5f,1.2f); }
    public static void playBloom(LivingEntity e) { ServerLevel sl=L(e); if(sl==null)return; sl.playSound(null,e.blockPosition(),SoundEvents.GRASS_BREAK,SoundSource.HOSTILE,0.8f,0.9f); }
    public static void playBurning(LivingEntity e) { ServerLevel sl=L(e); if(sl==null)return; sl.playSound(null,e.blockPosition(),SoundEvents.FIRE_AMBIENT,SoundSource.HOSTILE,0.4f,1.0f); }
}