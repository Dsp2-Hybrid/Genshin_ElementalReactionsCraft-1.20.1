package com.dsp2.genshinerc.common.core.reaction;

import com.dsp2.genshinerc.common.core.element.ElementType;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.concurrent.ThreadLocalRandom;

public final class ReactionSoundEvents {
    private ReactionSoundEvents() {}

    private static ThreadLocalRandom rnd() { return ThreadLocalRandom.current(); }

    private static float jitter(float base, float spread) {
        return base + (rnd().nextFloat() * 2f - 1f) * spread;
    }

    private static ServerLevel level(LivingEntity e) {
        return e.level() instanceof ServerLevel sl ? sl : null;
    }

    private static Vec3 center(LivingEntity e) {
        return e.position().add(0, e.getBbHeight() * 0.5, 0);
    }

    private static void send(ServerLevel level, double x, double y, double z, SoundEvent event, float volume, float pitch, int clipTicks, int fadeTicks) {
        if (level == null || event == null) return;
        float v = Math.max(0f, volume);
        int layers = Math.min(4, (int) Math.ceil(v));
        for (int i = 0; i < layers; i++) {
            float vol = Math.min(1f, v - i);
            if (vol <= 0.01f) break;
            level.playSound(null, x, y, z, event, SoundSource.PLAYERS, vol, pitch * (1f + i * 0.015f));
        }
    }

    private static void play(LivingEntity e, SoundEvent event, float volume, float pitch, int clipTicks, int fadeTicks) {
        ServerLevel l = level(e);
        if (l == null) return;
        Vec3 c = center(e);
        send(l, c.x, c.y, c.z, event, volume, pitch, clipTicks, fadeTicks);
    }

    private static void play(ServerLevel l, BlockPos pos, SoundEvent event, float volume, float pitch, int clipTicks, int fadeTicks) {
        send(l, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, event, volume, pitch, clipTicks, fadeTicks);
    }

    public static void playElectroChargedStart(LivingEntity e) {
        ServerLevel l = level(e);
        if (l == null) return;
        Vec3 c = center(e);
        send(l, c.x, c.y, c.z, SoundEvents.RESPAWN_ANCHOR_CHARGE, jitter(1.6f, 0.1f), jitter(1.5f, 0.1f), 14, 6);
        send(l, c.x, c.y, c.z, SoundEvents.BEEHIVE_WORK, jitter(2.0f, 0.2f), jitter(1.9f, 0.15f), 20, 8);
    }

    public static void playElectroCharged(LivingEntity e) {
        ServerLevel l = level(e);
        if (l == null) return;
        Vec3 c = center(e);
        SoundEvent[] crackles = { SoundEvents.BEEHIVE_WORK, SoundEvents.REDSTONE_TORCH_BURNOUT, SoundEvents.CONDUIT_AMBIENT_SHORT };
        for (int i = 0; i < 3; i++) {
            SoundEvent ev = crackles[rnd().nextInt(crackles.length)];
            send(l, c.x, c.y, c.z, ev, jitter(2.2f, 0.3f), jitter(1.8f, 0.3f), 10, 4);
        }
        send(l, c.x, c.y, c.z, SoundEvents.LIGHTNING_BOLT_IMPACT, jitter(0.5f, 0.1f), jitter(1.9f, 0.15f), 10, 4);
    }

    public static void playElectroAmbient(ServerLevel l, BlockPos pos) {
        if (l == null) return;
        SoundEvent[] zaps = { SoundEvents.BEEHIVE_WORK, SoundEvents.REDSTONE_TORCH_BURNOUT, SoundEvents.CONDUIT_AMBIENT_SHORT };
        play(l, pos, zaps[rnd().nextInt(zaps.length)], jitter(1.8f, 0.3f), jitter(1.8f, 0.3f), 8, 4);
    }

    public static void playOverload(LivingEntity e) {
        ServerLevel l = level(e);
        if (l == null) return;
        playOverload(l, e.blockPosition());
        Vec3 c = center(e);
        send(l, c.x, c.y, c.z, SoundEvents.RESPAWN_ANCHOR_CHARGE, jitter(1.4f, 0.15f), jitter(0.75f, 0.1f), 16, 8);
    }

    public static void playOverload(ServerLevel l, BlockPos pos) {
        if (l == null) return;
        send(l, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.GENERIC_EXPLODE, jitter(2.6f, 0.2f), jitter(0.75f, 0.08f), 40, 20);
        send(l, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.RESPAWN_ANCHOR_CHARGE, jitter(1.6f, 0.15f), jitter(0.7f, 0.1f), 18, 8);
    }

    public static void playSuperconduct(LivingEntity e) {
        ServerLevel l = level(e);
        if (l != null) playSuperconduct(l, e.blockPosition());
    }

    public static void playSuperconduct(ServerLevel l, BlockPos pos) {
        if (l == null) return;
        send(l, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.GENERIC_EXPLODE, jitter(2.0f, 0.2f), jitter(1.35f, 0.1f), 28, 14);
        send(l, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.GLASS_BREAK, jitter(1.6f, 0.15f), jitter(0.85f, 0.1f), 14, 6);
        send(l, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.RESPAWN_ANCHOR_CHARGE, jitter(1.5f, 0.15f), jitter(1.6f, 0.2f), 16, 8);
    }

    public static void playFreeze(LivingEntity e) { play(e, SoundEvents.GLASS_PLACE, jitter(1.3f, 0.1f), jitter(1.1f, 0.1f), 14, 6); }

    public static void playShatter(LivingEntity e) { play(e, SoundEvents.GLASS_BREAK, jitter(1.5f, 0.1f), jitter(0.9f, 0.1f), 16, 8); }

    public static void playMelt(LivingEntity e) {
        ServerLevel l = level(e);
        if (l != null) playMelt(l, e.blockPosition());
    }

    public static void playMelt(ServerLevel l, BlockPos pos) { play(l, pos, SoundEvents.FIRE_EXTINGUISH, jitter(1.1f, 0.08f), jitter(1.05f, 0.1f), 16, 8); }

    public static void playVaporize(LivingEntity e) {
        ServerLevel l = level(e);
        if (l != null) playVaporize(l, e.blockPosition());
    }

    public static void playVaporize(ServerLevel l, BlockPos pos) {
        play(l, pos, SoundEvents.FIRE_EXTINGUISH, jitter(1.1f, 0.08f), jitter(1.04f, 0.1f), 16, 8);
        play(l, pos, SoundEvents.BUBBLE_COLUMN_BUBBLE_POP, jitter(0.9f, 0.08f), jitter(1.1f, 0.1f), 10, 5);
    }

    public static void playSwirl(LivingEntity e) { playSwirl(e, ElementType.ANEMO); }

    public static void playSwirl(LivingEntity e, ElementType element) {
        ServerLevel l = level(e);
        if (l == null) return;
        Vec3 c = center(e);
        swirlAt(l, c.x, c.y, c.z, element);
    }

    public static void playSwirl(ServerLevel l, BlockPos pos, ElementType element) {
        if (l == null) return;
        swirlAt(l, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, element);
    }

    private static void swirlAt(ServerLevel l, double x, double y, double z, ElementType element) {
        send(l, x, y, z, SoundEvents.PHANTOM_FLAP, jitter(2.0f, 0.15f), jitter(1.25f, 0.15f), 20, 14);
        send(l, x, y, z, SoundEvents.PLAYER_ATTACK_SWEEP, jitter(1.5f, 0.15f), jitter(1.05f, 0.1f), 12, 8);
        SoundEvent accent = switch (element) {
            case PYRO -> SoundEvents.CAMPFIRE_CRACKLE;
            case HYDRO -> SoundEvents.BUBBLE_COLUMN_BUBBLE_POP;
            case CRYO, FROZEN -> SoundEvents.POWDER_SNOW_STEP;
            case ELECTRO -> SoundEvents.RESPAWN_ANCHOR_CHARGE;
            case DENDRO, QUICKEN -> SoundEvents.AZALEA_LEAVES_BREAK;
            case GEO -> SoundEvents.CALCITE_HIT;
            default -> SoundEvents.PLAYER_ATTACK_SWEEP;
        };
        send(l, x, y, z, accent, jitter(1.1f, 0.1f), jitter(1.18f, 0.12f), 12, 8);
    }

    public static void playCrystallize(LivingEntity e) {
        ServerLevel l = level(e);
        if (l == null) return;
        Vec3 c = center(e);
        crystallizeAt(l, c.x, c.y, c.z);
    }

    public static void playCrystallize(ServerLevel l, BlockPos pos) {
        if (l == null) return;
        crystallizeAt(l, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
    }

    private static void crystallizeAt(ServerLevel l, double x, double y, double z) {
        send(l, x, y, z, SoundEvents.AMETHYST_BLOCK_BREAK, jitter(2.2f, 0.1f), jitter(1.0f, 0.1f), 14, 6);
        send(l, x, y, z, SoundEvents.AMETHYST_CLUSTER_BREAK, jitter(1.8f, 0.1f), jitter(1.1f, 0.1f), 14, 6);
        send(l, x, y, z, SoundEvents.AMETHYST_BLOCK_CHIME, jitter(1.8f, 0.1f), jitter(1.2f, 0.12f), 12, 6);
    }

    public static void playBloom(LivingEntity e) {
        play(e, SoundEvents.BONE_MEAL_USE, jitter(1.2f, 0.08f), jitter(0.86f, 0.1f), 14, 6);
    }

    public static void playBloomBurst(LivingEntity e) {
        ServerLevel l = level(e);
        if (l != null) playBloomBurst(l, e.blockPosition());
    }

    public static void playBloomBurst(ServerLevel l, BlockPos pos) { play(l, pos, SoundEvents.SWEET_BERRY_BUSH_PLACE, jitter(1.3f, 0.1f), jitter(0.76f, 0.1f), 16, 8); }

    public static void playBurgeonTrigger(LivingEntity e) {
        ServerLevel l = level(e);
        if (l != null) playBurgeonTrigger(l, e.blockPosition());
    }

    public static void playBurgeonTrigger(ServerLevel l, BlockPos pos) { play(l, pos, SoundEvents.AZALEA_LEAVES_PLACE, jitter(1.0f, 0.08f), jitter(1.08f, 0.1f), 12, 6); }

    public static void playBurgeonBurst(LivingEntity e) {
        ServerLevel l = level(e);
        if (l != null) playBurgeonBurst(l, e.blockPosition());
    }

    public static void playBurgeonBurst(ServerLevel l, BlockPos pos) {
        play(l, pos, SoundEvents.GENERIC_EXPLODE, jitter(1.4f, 0.1f), jitter(1.02f, 0.1f), 20, 10);
        play(l, pos, SoundEvents.AZALEA_LEAVES_BREAK, jitter(1.0f, 0.1f), jitter(0.95f, 0.1f), 14, 6);
    }

    public static void playHyperbloomTrigger(LivingEntity e) {
        ServerLevel l = level(e);
        if (l != null) playHyperbloomTrigger(l, e.blockPosition());
    }

    public static void playHyperbloomTrigger(ServerLevel l, BlockPos pos) { play(l, pos, SoundEvents.RESPAWN_ANCHOR_CHARGE, jitter(1.1f, 0.08f), jitter(1.32f, 0.12f), 12, 6); }

    public static void playHyperbloomImpact(LivingEntity e) { play(e, SoundEvents.LIGHTNING_BOLT_IMPACT, jitter(1.0f, 0.1f), jitter(1.52f, 0.12f), 16, 8); }
    public static void playHyperbloomImpact(ServerLevel l, BlockPos pos) { play(l, pos, SoundEvents.LIGHTNING_BOLT_IMPACT, jitter(1.0f, 0.1f), jitter(1.52f, 0.12f), 16, 8); }

    public static void playCatalyze(LivingEntity e) { play(e, SoundEvents.SLIME_SQUISH, jitter(1.2f, 0.1f), jitter(1.2f, 0.12f), 12, 6); }

    public static void playAggravate(LivingEntity e) {
        play(e, SoundEvents.SLIME_SQUISH, jitter(1.2f, 0.1f), jitter(0.9f, 0.1f), 12, 6);
        play(e, SoundEvents.BEEHIVE_WORK, jitter(1.2f, 0.1f), jitter(1.8f, 0.1f), 10, 4);
    }

    public static void playSpread(LivingEntity e) {
        play(e, SoundEvents.PHANTOM_FLAP, jitter(1.0f, 0.08f), jitter(1.1f, 0.12f), 12, 8);
        play(e, SoundEvents.AZALEA_LEAVES_BREAK, jitter(1.0f, 0.08f), jitter(1.0f, 0.1f), 12, 6);
    }

    public static void playBurning(LivingEntity e) { play(e, SoundEvents.CAMPFIRE_CRACKLE, jitter(0.9f, 0.1f), jitter(1.0f, 0.1f), 14, 8); }

    public static void playShieldHit(LivingEntity e) { play(e, SoundEvents.SHIELD_BLOCK, jitter(1.0f, 0.1f), jitter(1.0f, 0.1f), 10, 5); }
}