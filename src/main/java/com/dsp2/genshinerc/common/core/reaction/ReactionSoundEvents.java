package com.dsp2.genshinerc.common.core.reaction;

import com.dsp2.genshinerc.common.core.element.ElementType;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;

public final class ReactionSoundEvents {
    private ReactionSoundEvents() {}

    private static ServerLevel level(LivingEntity e) { return e.level() instanceof ServerLevel s ? s : null; }

    private static void play(ServerLevel l, BlockPos pos, SoundEvent sound, float volume, float pitch) {
        l.playSound(null, pos, sound, SoundSource.HOSTILE,
                volume * (0.86f + l.random.nextFloat() * 0.28f),
                pitch * (0.91f + l.random.nextFloat() * 0.18f));
    }
    private static void play(ServerLevel l, BlockPos pos, SoundEvent[] s, float v, float p) { play(l, pos, s[l.random.nextInt(s.length)], v, p); }
    private static void play(LivingEntity e, SoundEvent s, float v, float p) { ServerLevel l = level(e); if (l != null) play(l, e.blockPosition(), s, v, p); }
    private static void play(LivingEntity e, SoundEvent[] s, float v, float p) { ServerLevel l = level(e); if (l != null) play(l, e.blockPosition(), s, v, p); }

    private static final SoundEvent[] BLOOM_CREATE = { SoundEvents.CHERRY_LEAVES_BREAK, SoundEvents.AZALEA_LEAVES_BREAK, SoundEvents.MOSS_BREAK };
    private static final SoundEvent[] BLOOM_BURST = { SoundEvents.SLIME_BLOCK_FALL, SoundEvents.HONEY_BLOCK_SLIDE, SoundEvents.MUD_BREAK };
    private static final SoundEvent[] BURGEON_TRIGGER = { SoundEvents.FIRECHARGE_USE, SoundEvents.BLAZE_SHOOT, SoundEvents.CAMPFIRE_CRACKLE };
    private static final SoundEvent[] BURGEON_BURST = { SoundEvents.DRAGON_FIREBALL_EXPLODE, SoundEvents.WITHER_SHOOT, SoundEvents.GENERIC_BURN };
    private static final SoundEvent[] HYPERBLOOM_TRIGGER = { SoundEvents.CONDUIT_ACTIVATE, SoundEvents.BEACON_POWER_SELECT, SoundEvents.AMETHYST_BLOCK_RESONATE };
    private static final SoundEvent[] HYPERBLOOM_IMPACT = { SoundEvents.TRIDENT_THUNDER, SoundEvents.LIGHTNING_BOLT_IMPACT, SoundEvents.WARDEN_SONIC_BOOM };
    private static final SoundEvent[] CATALYZE = { SoundEvents.BONE_MEAL_USE, SoundEvents.COMPOSTER_READY, SoundEvents.MOSS_PLACE };
    private static final SoundEvent[] AGGRAVATE = { SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundEvents.END_PORTAL_FRAME_FILL, SoundEvents.BEACON_ACTIVATE };
    private static final SoundEvent[] SPREAD = { SoundEvents.SCULK_BLOCK_SPREAD, SoundEvents.SCULK_CATALYST_BLOOM, SoundEvents.CHORUS_FLOWER_GROW };
    private static final SoundEvent[] SWIRL = { SoundEvents.PHANTOM_FLAP, SoundEvents.PLAYER_ATTACK_SWEEP, SoundEvents.TRIDENT_HIT };
    private static final SoundEvent[] CRYSTALLIZE_CREATE = { SoundEvents.AMETHYST_BLOCK_PLACE, SoundEvents.AMETHYST_CLUSTER_PLACE, SoundEvents.CALCITE_PLACE };
    private static final SoundEvent[] SHIELD_HIT = { SoundEvents.AMETHYST_BLOCK_HIT, SoundEvents.GLASS_HIT, SoundEvents.SHIELD_BLOCK };

    public static void playOverload(LivingEntity e) { play(e, SoundEvents.GENERIC_EXPLODE, 2.60f, 0.72f); play(e, SoundEvents.LIGHTNING_BOLT_THUNDER, 2.00f, 0.82f); }
    public static void playOverload(ServerLevel l, BlockPos pos) { play(l, pos, SoundEvents.GENERIC_EXPLODE, 2.60f, 0.72f); play(l, pos, SoundEvents.LIGHTNING_BOLT_THUNDER, 2.00f, 0.82f); }
    public static void playSuperconduct(LivingEntity e) { play(e, SoundEvents.POWDER_SNOW_BREAK, 1.35f, 0.72f); play(e, SoundEvents.GLASS_BREAK, 0.85f, 1.45f); }
    public static void playSuperconduct(ServerLevel l, BlockPos pos) { play(l, pos, SoundEvents.POWDER_SNOW_BREAK, 1.35f, 0.72f); play(l, pos, SoundEvents.GLASS_BREAK, 0.85f, 1.45f); }
    public static void playElectroChargedStart(LivingEntity e) { play(e, SoundEvents.BEEHIVE_WORK, 1.45f, 1.30f); play(e, SoundEvents.GENERIC_SPLASH, 0.38f, 1.74f); }
    public static void playElectroCharged(LivingEntity e) { play(e, SoundEvents.BEEHIVE_WORK, 1.30f, 1.48f); play(e, SoundEvents.NOTE_BLOCK_HAT.value(), 0.32f, 1.70f); }
    public static void playFreeze(LivingEntity e) { play(e, SoundEvents.GLASS_PLACE, 0.72f, 0.76f); play(e, SoundEvents.POWDER_SNOW_PLACE, 0.34f, 1.35f); }
    public static void playShatter(LivingEntity e) { play(e, SoundEvents.GLASS_BREAK, 0.94f, 0.92f); play(e, SoundEvents.AMETHYST_CLUSTER_BREAK, 0.36f, 1.25f); }
    public static void playMelt(LivingEntity e) { play(e, SoundEvents.LAVA_EXTINGUISH, 0.58f, 1.24f); play(e, SoundEvents.POINTED_DRIPSTONE_DRIP_WATER, 0.28f, 1.42f); }
    public static void playVaporize(LivingEntity e) { play(e, SoundEvents.FIRE_EXTINGUISH, 0.72f, 1.04f); play(e, SoundEvents.BUBBLE_COLUMN_UPWARDS_AMBIENT, 0.30f, 1.36f); }
    public static void playSwirl(LivingEntity e) { playSwirl(e, ElementType.ANEMO); }
    public static void playSwirl(LivingEntity e, ElementType element) {
        play(e, SWIRL, 0.52f, 1.30f);
        SoundEvent accent = switch (element) {
            case PYRO -> SoundEvents.CAMPFIRE_CRACKLE;
            case HYDRO -> SoundEvents.BUBBLE_COLUMN_BUBBLE_POP;
            case CRYO, FROZEN -> SoundEvents.POWDER_SNOW_STEP;
            case ELECTRO -> SoundEvents.RESPAWN_ANCHOR_CHARGE;
            case DENDRO, QUICKEN -> SoundEvents.AZALEA_LEAVES_BREAK;
            case GEO -> SoundEvents.CALCITE_HIT;
            default -> SoundEvents.PLAYER_ATTACK_SWEEP;
        };
        play(e, accent, 0.40f, 1.18f);
    }
    public static void playCrystallize(LivingEntity e) { play(e, CRYSTALLIZE_CREATE, 0.62f, 1.04f); play(e, SoundEvents.AMETHYST_BLOCK_CHIME, 0.30f, 1.46f); }
    public static void playBloom(LivingEntity e) { play(e, BLOOM_CREATE, 0.66f, 0.86f); }
    public static void playBloomBurst(LivingEntity e) { play(e, BLOOM_BURST, 0.80f, 0.76f); }
    public static void playBloomBurst(ServerLevel l, BlockPos pos) { play(l, pos, BLOOM_BURST, 0.80f, 0.76f); }
    public static void playBurgeonTrigger(LivingEntity e) { play(e, BURGEON_TRIGGER, 0.60f, 1.08f); }
    public static void playBurgeonTrigger(ServerLevel l, BlockPos pos) { play(l, pos, BURGEON_TRIGGER, 0.60f, 1.08f); }
    public static void playBurgeonBurst(LivingEntity e) { play(e, BURGEON_BURST, 0.78f, 1.02f); }
    public static void playBurgeonBurst(ServerLevel l, BlockPos pos) { play(l, pos, BURGEON_BURST, 0.78f, 1.02f); }
    public static void playHyperbloomTrigger(LivingEntity e) { play(e, HYPERBLOOM_TRIGGER, 0.58f, 1.32f); }
    public static void playHyperbloomTrigger(ServerLevel l, BlockPos pos) { play(l, pos, HYPERBLOOM_TRIGGER, 0.58f, 1.32f); }
    public static void playHyperbloomImpact(LivingEntity e) { play(e, HYPERBLOOM_IMPACT, 0.60f, 1.52f); }
    public static void playHyperbloomImpact(ServerLevel l, BlockPos pos) { play(l, pos, HYPERBLOOM_IMPACT, 0.60f, 1.52f); }
    public static void playCatalyze(LivingEntity e) { play(e, CATALYZE, 0.60f, 1.05f); }
    public static void playAggravate(LivingEntity e) { play(e, AGGRAVATE, 0.58f, 1.38f); }
    public static void playSpread(LivingEntity e) { play(e, SPREAD, 0.55f, 0.95f); }
    public static void playBurning(LivingEntity e) { play(e, SoundEvents.FIRE_AMBIENT, 0.38f, 0.82f); play(e, SoundEvents.CANDLE_AMBIENT, 0.22f, 1.15f); }
    public static void playShieldHit(LivingEntity e) { play(e, SHIELD_HIT, 0.78f, 1.42f); play(e, SoundEvents.AMETHYST_BLOCK_CHIME, 0.26f, 1.76f); }
}