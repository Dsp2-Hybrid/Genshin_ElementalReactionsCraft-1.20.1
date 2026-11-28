package com.dsp2.genshinerc.common.core.reaction;

import com.dsp2.genshinerc.common.network.GenshinercNetwork;
import com.dsp2.genshinerc.common.network.base.element.ReactionSoundPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;

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
        if (level == null) return;
        ReactionSoundPacket packet = new ReactionSoundPacket(event, x, y, z, volume, pitch, clipTicks, fadeTicks);
        GenshinercNetwork.CHANNEL.send(PacketDistributor.NEAR.with(
                () -> new PacketDistributor.TargetPoint(x, y, z, 48.0, level.dimension())), packet);
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
        send(l, c.x, c.y, c.z, SoundEvents.RESPAWN_ANCHOR_CHARGE, jitter(1.1f, 0.1f), jitter(1.4f, 0.1f), 14, 6);
        send(l, c.x, c.y, c.z, SoundEvents.BEACON_AMBIENT, jitter(0.9f, 0.1f), jitter(1.8f, 0.15f), 20, 8);
    }

    public static void playElectroCharged(LivingEntity e) {
        ServerLevel l = level(e);
        if (l == null) return;
        Vec3 c = center(e);
        SoundEvent[] crackles = { SoundEvents.BEEHIVE_WORK, SoundEvents.REDSTONE_TORCH_BURNOUT, SoundEvents.CONDUIT_AMBIENT_SHORT };
        for (int i = 0; i < 2; i++) {
            SoundEvent ev = crackles[rnd().nextInt(crackles.length)];
            send(l, c.x, c.y, c.z, ev, jitter(1.3f, 0.2f), jitter(1.7f, 0.3f), 10, 4);
        }
    }

    public static void playOverload(LivingEntity e) { playOverload(level(e), e.blockPosition()); if (level(e) != null) { Vec3 c = center(e); send(level(e), c.x, c.y, c.z, SoundEvents.RESPAWN_ANCHOR_CHARGE, jitter(1.4f, 0.15f), jitter(0.75f, 0.1f), 16, 8); } }

    public static void playOverload(ServerLevel l, BlockPos pos) {
        if (l == null) return;
        send(l, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.GENERIC_EXPLODE, jitter(2.2f, 0.2f), jitter(0.75f, 0.08f), 40, 20);
        send(l, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.RESPAWN_ANCHOR_CHARGE, jitter(1.6f, 0.15f), jitter(0.7f, 0.1f), 18, 8);
    }

    public static void playSuperconduct(LivingEntity e) { playSuperconduct(level(e), e.blockPosition()); }

    public static void playSuperconduct(ServerLevel l, BlockPos pos) {
        if (l == null) return;
        send(l, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.GENERIC_EXPLODE, jitter(1.9f, 0.2f), jitter(1.35f, 0.1f), 28, 14);
        send(l, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.GLASS_BREAK, jitter(1.1f, 0.15f), jitter(0.85f, 0.1f), 14, 6);
        send(l, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.RESPAWN_ANCHOR_CHARGE, jitter(1.3f, 0.15f), jitter(1.6f, 0.2f), 16, 8);
    }

    public static void playFreeze(LivingEntity e) { play(e, SoundEvents.GLASS_PLACE, jitter(0.9f, 0.1f), jitter(1.1f, 0.1f), 14, 6); }

    public static void playShatter(LivingEntity e) { play(e, SoundEvents.GLASS_BREAK, jitter(1.1f, 0.1f), jitter(0.9f, 0.1f), 16, 8); }

    public static void playMelt(LivingEntity e) { playMelt(level(e), e.blockPosition()); }

    public static void playMelt(ServerLevel l, BlockPos pos) { play(l, pos, SoundEvents.FIRE_EXTINGUISH, jitter(0.75f, 0.08f), jitter(1.05f, 0.1f), 16, 8); }

    public static void playVaporize(LivingEntity e) { playVaporize(level(e), e.blockPosition()); }

    public static void playVaporize(ServerLevel l, BlockPos pos) { play(l, pos, SoundEvents.FIRE_EXTINGUISH, jitter(0.72f, 0.08f), jitter(1.04f, 0.1f), 16, 8); play(l, pos, SoundEvents.BUBBLE_COLUMN_BUBBLE_POP, jitter(0.6f, 0.08f), jitter(1.1f, 0.1f), 10, 5); }

    public static void playSwirl(LivingEntity e) { playSwirl(e, com.dsp2.genshinerc.common.core.element.ElementType.ANEMO); }

    public static void playSwirl(LivingEntity e, com.dsp2.genshinerc.common.core.element.ElementType element) {
        ServerLevel l = level(e);
        if (l == null) return;
        Vec3 c = center(e);
        send(l, c.x, c.y, c.z, SoundEvents.PHANTOM_FLAP, jitter(0.78f, 0.1f), jitter(1.25f, 0.15f), 20, 14);
        SoundEvent accent = switch (element) {
            case PYRO -> SoundEvents.CAMPFIRE_CRACKLE;
            case HYDRO -> SoundEvents.BUBBLE_COLUMN_BUBBLE_POP;
            case CRYO, FROZEN -> SoundEvents.POWDER_SNOW_STEP;
            case ELECTRO -> SoundEvents.RESPAWN_ANCHOR_CHARGE;
            case DENDRO, QUICKEN -> SoundEvents.AZALEA_LEAVES_BREAK;
            case GEO -> SoundEvents.CALCITE_HIT;
            default -> SoundEvents.PLAYER_ATTACK_SWEEP;
        };
        send(l, c.x, c.y, c.z, accent, jitter(0.4f, 0.08f), jitter(1.18f, 0.12f), 12, 8);
    }

    public static void playCrystallize(LivingEntity e) { play(e, SoundEvents.AMETHYST_BLOCK_BREAK, jitter(0.62f, 0.08f), jitter(1.04f, 0.1f), 14, 6); play(e, SoundEvents.AMETHYST_BLOCK_CHIME, jitter(0.5f, 0.08f), jitter(1.2f, 0.12f), 12, 6); }

    public static void playBloom(LivingEntity e) {
        play(e, SoundEvents.BONE_MEAL_USE, jitter(0.66f, 0.08f), jitter(0.86f, 0.1f), 14, 6);
    }
    public static void playBloomBurst(LivingEntity e) { playBloomBurst(level(e), e.blockPosition()); }

    public static void playBloomBurst(ServerLevel l, BlockPos pos) { play(l, pos, SoundEvents.SWEET_BERRY_BUSH_PLACE, jitter(0.8f, 0.1f), jitter(0.76f, 0.1f), 16, 8); }

    public static void playBurgeonTrigger(LivingEntity e) { playBurgeonTrigger(level(e), e.blockPosition()); }
    public static void playBurgeonTrigger(ServerLevel l, BlockPos pos) { play(l, pos, SoundEvents.AZALEA_LEAVES_PLACE, jitter(0.6f, 0.08f), jitter(1.08f, 0.1f), 12, 6); }

    public static void playBurgeonBurst(LivingEntity e) { playBurgeonBurst(level(e), e.blockPosition()); }

    public static void playBurgeonBurst(ServerLevel l, BlockPos pos) { play(l, pos, SoundEvents.GENERIC_EXPLODE, jitter(0.78f, 0.1f), jitter(1.02f, 0.1f), 20, 10); play(l, pos, SoundEvents.AZALEA_LEAVES_BREAK, jitter(0.6f, 0.1f), jitter(0.95f, 0.1f), 14, 6); }

    public static void playHyperbloomTrigger(LivingEntity e) { playHyperbloomTrigger(level(e), e.blockPosition()); }

    public static void playHyperbloomTrigger(ServerLevel l, BlockPos pos) { play(l, pos, SoundEvents.RESPAWN_ANCHOR_CHARGE, jitter(0.58f, 0.08f), jitter(1.32f, 0.12f), 12, 6); }

    public static void playHyperbloomImpact(LivingEntity e) { play(e, SoundEvents.LIGHTNING_BOLT_IMPACT, jitter(0.6f, 0.1f), jitter(1.52f, 0.12f), 16, 8); }
    public static void playHyperbloomImpact(ServerLevel l, BlockPos pos) { play(l, pos, SoundEvents.LIGHTNING_BOLT_IMPACT, jitter(0.6f, 0.1f), jitter(1.52f, 0.12f), 16, 8); }

    public static void playCatalyze(LivingEntity e) { play(e, SoundEvents.SLIME_SQUISH, jitter(0.6f, 0.1f), jitter(1.2f, 0.12f), 12, 6); }

    public static void playAggravate(LivingEntity e) { play(e, SoundEvents.SLIME_SQUISH, jitter(0.7f, 0.1f), jitter(0.9f, 0.1f), 12, 6); }

    public static void playSpread(LivingEntity e) { play(e, SoundEvents.PHANTOM_FLAP, jitter(0.5f, 0.08f), jitter(1.1f, 0.12f), 12, 8); }

    public static void playBurning(LivingEntity e) { play(e, SoundEvents.CAMPFIRE_CRACKLE, jitter(0.55f, 0.1f), jitter(1.0f, 0.1f), 14, 8); }

    public static void playShieldHit(LivingEntity e) { play(e, SoundEvents.SHIELD_BLOCK, jitter(0.7f, 0.1f), jitter(1.0f, 0.1f), 10, 5); }
}