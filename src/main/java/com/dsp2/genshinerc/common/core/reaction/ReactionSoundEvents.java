package com.dsp2.genshinerc.common.core.reaction;

import com.dsp2.genshinerc.common.core.element.ElementType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;

public final class ReactionSoundEvents {
    private ReactionSoundEvents() {}

    private static ServerLevel level(LivingEntity entity) {
        return entity.level() instanceof ServerLevel server ? server : null;
    }

    public static void playOverload(LivingEntity entity) {
        ServerLevel level = level(entity);
        if (level == null) return;

        level.playSound(null, entity.blockPosition(),
                SoundEvents.GENERIC_EXPLODE,
                SoundSource.HOSTILE, 0.80f,
                0.78f + level.getRandom().nextFloat() * 0.22f);
        level.playSound(null, entity.blockPosition(),
                SoundEvents.LIGHTNING_BOLT_THUNDER,
                SoundSource.HOSTILE, 0.42f,
                1.08f + level.getRandom().nextFloat() * 0.18f);
    }

    public static void playSuperconduct(LivingEntity entity) {
        ServerLevel level = level(entity);
        if (level == null) return;

        level.playSound(null, entity.blockPosition(),
                SoundEvents.GLASS_BREAK,
                SoundSource.HOSTILE, 0.68f,
                0.72f + level.getRandom().nextFloat() * 0.18f);
        level.playSound(null, entity.blockPosition(),
                SoundEvents.AMETHYST_BLOCK_CHIME,
                SoundSource.HOSTILE, 0.28f,
                1.48f + level.getRandom().nextFloat() * 0.22f);
    }

    public static void playElectroChargedStart(LivingEntity entity) {
        ServerLevel level = level(entity);
        if (level == null) return;

        level.playSound(null, entity.blockPosition(),
                SoundEvents.AMETHYST_BLOCK_RESONATE,
                SoundSource.HOSTILE, 0.38f,
                1.14f + level.getRandom().nextFloat() * 0.20f);
        level.playSound(null, entity.blockPosition(),
                SoundEvents.NOTE_BLOCK_BELL.value(),
                SoundSource.HOSTILE, 0.20f,
                1.42f + level.getRandom().nextFloat() * 0.20f);
        level.playSound(null, entity.blockPosition(),
                SoundEvents.GENERIC_SPLASH,
                SoundSource.HOSTILE, 0.14f, 1.74f);
    }

    public static void playElectroCharged(LivingEntity entity) {
        ServerLevel level = level(entity);
        if (level == null) return;

        level.playSound(null, entity.blockPosition(),
                SoundEvents.AMETHYST_BLOCK_CHIME,
                SoundSource.HOSTILE, 0.24f,
                1.42f + level.getRandom().nextFloat() * 0.36f);
        level.playSound(null, entity.blockPosition(),
                SoundEvents.NOTE_BLOCK_HAT.value(),
                SoundSource.HOSTILE, 0.16f,
                1.62f + level.getRandom().nextFloat() * 0.30f);
        level.playSound(null, entity.blockPosition(),
                SoundEvents.GENERIC_SPLASH,
                SoundSource.HOSTILE, 0.10f, 1.88f);
    }

    public static void playFreeze(LivingEntity entity) {
        ServerLevel level = level(entity);
        if (level == null) return;

        level.playSound(null, entity.blockPosition(),
                SoundEvents.GLASS_PLACE,
                SoundSource.HOSTILE, 0.72f,
                0.76f + level.getRandom().nextFloat() * 0.18f);
        level.playSound(null, entity.blockPosition(),
                SoundEvents.AMETHYST_BLOCK_CHIME,
                SoundSource.HOSTILE, 0.24f, 1.45f);
    }

    public static void playShatter(LivingEntity entity) {
        ServerLevel level = level(entity);
        if (level == null) return;

        level.playSound(null, entity.blockPosition(),
                SoundEvents.GLASS_BREAK,
                SoundSource.HOSTILE, 0.94f,
                0.92f + level.getRandom().nextFloat() * 0.22f);
        level.playSound(null, entity.blockPosition(),
                SoundEvents.AMETHYST_BLOCK_RESONATE,
                SoundSource.HOSTILE, 0.36f, 1.25f);
    }

    public static void playMelt(LivingEntity entity) {
        ServerLevel level = level(entity);
        if (level == null) return;

        level.playSound(null, entity.blockPosition(),
                SoundEvents.LAVA_EXTINGUISH,
                SoundSource.HOSTILE, 0.58f,
                1.24f + level.getRandom().nextFloat() * 0.18f);
        level.playSound(null, entity.blockPosition(),
                SoundEvents.FIRE_EXTINGUISH,
                SoundSource.HOSTILE, 0.32f, 1.52f);
    }

    public static void playVaporize(LivingEntity entity) {
        ServerLevel level = level(entity);
        if (level == null) return;

        level.playSound(null, entity.blockPosition(),
                SoundEvents.FIRE_EXTINGUISH,
                SoundSource.HOSTILE, 0.72f,
                1.04f + level.getRandom().nextFloat() * 0.18f);
        level.playSound(null, entity.blockPosition(),
                SoundEvents.GENERIC_SPLASH,
                SoundSource.HOSTILE, 0.24f, 1.46f);
    }

    public static void playSwirl(LivingEntity entity) {
        playSwirl(entity, ElementType.ANEMO);
    }

    public static void playSwirl(
            LivingEntity entity,
            ElementType element) {
        ServerLevel level = level(entity);
        if (level == null) return;

        level.playSound(null, entity.blockPosition(),
                SoundEvents.PLAYER_ATTACK_SWEEP,
                SoundSource.HOSTILE, 0.55f,
                1.08f + level.getRandom().nextFloat() * 0.28f);

        var sound = switch (element) {
            case PYRO -> SoundEvents.FIRE_EXTINGUISH;
            case HYDRO -> SoundEvents.GENERIC_SPLASH;
            case CRYO, FROZEN -> SoundEvents.AMETHYST_BLOCK_CHIME;
            case ELECTRO -> SoundEvents.AMETHYST_BLOCK_RESONATE;
            case DENDRO, QUICKEN -> SoundEvents.GRASS_BREAK;
            case GEO -> SoundEvents.STONE_PLACE;
            default -> SoundEvents.PLAYER_ATTACK_SWEEP;
        };

        level.playSound(null, entity.blockPosition(),
                sound, SoundSource.HOSTILE, 0.32f,
                1.24f + level.getRandom().nextFloat() * 0.28f);
    }

    public static void playCrystallize(LivingEntity entity) {
        ServerLevel level = level(entity);
        if (level == null) return;

        float pitch = 1.04f + level.getRandom().nextFloat() * 0.28f;
        level.playSound(null, entity.blockPosition(),
                SoundEvents.AMETHYST_BLOCK_RESONATE,
                SoundSource.HOSTILE, 0.56f, pitch);
        level.playSound(null, entity.blockPosition(),
                SoundEvents.AMETHYST_BLOCK_CHIME,
                SoundSource.HOSTILE, 0.34f, pitch + 0.40f);
    }

    public static void playBloom(LivingEntity entity) {
        ServerLevel level = level(entity);
        if (level == null) return;

        level.playSound(null, entity.blockPosition(),
                SoundEvents.GRASS_BREAK,
                SoundSource.HOSTILE, 0.62f,
                0.78f + level.getRandom().nextFloat() * 0.18f);
        level.playSound(null, entity.blockPosition(),
                SoundEvents.BAMBOO_BREAK,
                SoundSource.HOSTILE, 0.40f,
                1.16f + level.getRandom().nextFloat() * 0.28f);
    }

    public static void playBurning(LivingEntity entity) {
        ServerLevel level = level(entity);
        if (level == null) return;

        level.playSound(null, entity.blockPosition(),
                SoundEvents.FIRE_AMBIENT,
                SoundSource.HOSTILE, 0.34f,
                0.82f + level.getRandom().nextFloat() * 0.18f);
        level.playSound(null, entity.blockPosition(),
                SoundEvents.FIRECHARGE_USE,
                SoundSource.HOSTILE, 0.18f,
                1.20f + level.getRandom().nextFloat() * 0.24f);
    }
}