package com.dsp2.genshinerc.common.core.reaction;

import com.dsp2.genshinerc.common.core.element.ElementType;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.concurrent.ThreadLocalRandom;

public final class ReactionParticles {
    private ReactionParticles() {}

    private static ServerLevel level(LivingEntity e) { return e.level() instanceof ServerLevel s ? s : null; }

    private static int colorOf(ElementType element) {
        return switch (element) {
            case PYRO -> 0xFF6936; case HYDRO -> 0x27C8FF; case ELECTRO -> 0xCC66FF;
            case CRYO, FROZEN -> 0xB4FFFE; case DENDRO, QUICKEN -> 0x66FF55;
            case ANEMO -> 0x66E0C8; case GEO -> 0xE8B84A; default -> 0xFFFFFF;
        };
    }

    static DustParticleOptions dust(ElementType element, float scale) {
        int c = colorOf(element);
        return new DustParticleOptions(new Vector3f(((c >> 16) & 255) / 255f, ((c >> 8) & 255) / 255f, (c & 255) / 255f), scale);
    }

    private static DustParticleOptions gradientDust(ElementType a, ElementType b, float bWeight, float scale) {
        int ca = colorOf(a);
        int cb = colorOf(b);
        float w = Math.max(0f, Math.min(1f, bWeight));
        float r = (((ca >> 16) & 255) * (1f - w) + ((cb >> 16) & 255) * w) / 255f;
        float g = (((ca >> 8) & 255) * (1f - w) + ((cb >> 8) & 255) * w) / 255f;
        float bl = ((ca & 255) * (1f - w) + (cb & 255) * w) / 255f;
        return new DustParticleOptions(new Vector3f(r, g, bl), scale);
    }

    private static ThreadLocalRandom rnd() { return ThreadLocalRandom.current(); }

    private static int jitterCount(int base, float spreadFrac) {
        float f = 1f + (rnd().nextFloat() * 2f - 1f) * spreadFrac;
        return Math.max(1, Math.round(base * f));
    }

    private static double jitterD(double base, double spreadFrac) {
        return base * (1.0 + (rnd().nextDouble() * 2.0 - 1.0) * spreadFrac);
    }

    static void burst(ServerLevel l, Vec3 at, ParticleOptions particle, int count, double radius, double speed) {
        RandomSource r = l.random;
        int actualCount = jitterCount(count, 0.22f);
        double actualRadius = jitterD(radius, 0.18);
        double actualSpeed = jitterD(speed, 0.25);
        for (int i = 0; i < actualCount; i++) {
            double angle = r.nextDouble() * Math.PI * 2.0;
            double lift = (r.nextDouble() - 0.5) * Math.PI;
            double dist = actualRadius * (0.25 + r.nextDouble() * 0.75);
            double spd = actualSpeed * (0.35 + r.nextDouble() * 1.30);
            l.sendParticles(particle,
                    at.x + (r.nextDouble() - 0.5) * actualRadius * 0.35,
                    at.y + (r.nextDouble() - 0.5) * actualRadius * 0.35,
                    at.z + (r.nextDouble() - 0.5) * actualRadius * 0.35,
                    1, Math.cos(angle) * Math.cos(lift) * dist, Math.sin(lift) * dist, Math.sin(angle) * Math.cos(lift) * dist, spd);
        }
    }

    static void ring(ServerLevel l, Vec3 at, ParticleOptions particle, int count, double radius, double rise) {
        RandomSource r = l.random;
        int actualCount = jitterCount(count, 0.2f);
        double actualRadius = jitterD(radius, 0.15);
        double offset = r.nextDouble() * Math.PI * 2.0;
        for (int i = 0; i < actualCount; i++) {
            double angle = offset + i * Math.PI * 2.0 / actualCount;
            double wobble = (r.nextDouble() - 0.5) * actualRadius * 0.30;
            double riseV = rise * (0.45 + r.nextDouble() * 1.10);
            l.sendParticles(particle,
                    at.x + Math.cos(angle) * (actualRadius + wobble), at.y + (r.nextDouble() - 0.5) * 0.55, at.z + Math.sin(angle) * (actualRadius + wobble),
                    1, (r.nextDouble() - 0.5) * 0.10, riseV, (r.nextDouble() - 0.5) * 0.10, 0.012);
        }
    }

    private static Vec3 center(LivingEntity e) { return e.getBoundingBox().getCenter(); }

    public static void spawnOverload(LivingEntity e) { ServerLevel l = level(e); if (l != null) spawnOverload(l, center(e)); }
    public static void spawnOverload(ServerLevel l, Vec3 at) {
        burst(l, at, ParticleTypes.FLAME, 44, 0.60, 0.18);
        burst(l, at, ParticleTypes.ELECTRIC_SPARK, 36, 0.65, 0.24);
        burst(l, at, ParticleTypes.SMOKE, 18, 0.45, 0.07);
        l.sendParticles(ParticleTypes.EXPLOSION_EMITTER, at.x, at.y, at.z, 1, 0, 0, 0, 0);
    }

    public static void spawnSuperconduct(LivingEntity e) { ServerLevel l = level(e); if (l != null) spawnSuperconduct(l, center(e)); }
    public static void spawnSuperconduct(ServerLevel l, Vec3 at) {
        DustParticleOptions gradient = gradientDust(ElementType.ELECTRO, ElementType.CRYO, 0.6f, 0.95f);
        DustParticleOptions gradientSmall = gradientDust(ElementType.ELECTRO, ElementType.CRYO, 0.65f, 0.72f);
        ring(l, at, gradient, 18, 0.34, 0.035);
        burst(l, at, ParticleTypes.ELECTRIC_SPARK, 16, 0.26, 0.11);
        burst(l, at, gradientSmall, 16, 0.24, 0.06);
        burst(l, at, ParticleTypes.SNOWFLAKE, 10, 0.22, 0.03);
    }

    public static void spawnElectroCharged(LivingEntity e) {
        ServerLevel l = level(e); if (l == null) return;
        Vec3 at = center(e);
        double entityR = Math.max(0.28, Math.min(0.48, e.getBbWidth() * 0.30 + e.getBbHeight() * 0.04));
        burst(l, at, ParticleTypes.ELECTRIC_SPARK, 24, entityR, 0.14);
        burst(l, at, ParticleTypes.FALLING_WATER, 12, entityR * 0.8, 0.06);
        ring(l, at, dust(ElementType.ELECTRO, 0.85f), 14, entityR, 0.025);
        ring(l, at, dust(ElementType.HYDRO, 0.85f), 14, entityR * 0.78, 0.03);
    }

    public static void spawnElectroArea(LivingEntity e, double radius) {
        ServerLevel l = level(e); if (l == null) return;
        spawnElectroCharged(e);
        Vec3 at = center(e);
        double entityR = Math.max(0.28, Math.min(0.48, e.getBbWidth() * 0.30 + e.getBbHeight() * 0.04));
        ring(l, at, dust(ElementType.ELECTRO, 0.9f), 16, entityR, 0.025);
        ring(l, at, dust(ElementType.HYDRO, 0.85f), 14, entityR * 0.78, 0.030);
        ring(l, at, ParticleTypes.ELECTRIC_SPARK, 10, entityR * 0.50, 0.040);
    }

    public static void spawnFreeze(LivingEntity e) {
        ServerLevel l = level(e); if (l == null) return;
        Vec3 at = center(e);
        ring(l, at, ParticleTypes.SNOWFLAKE, 46, 0.82, 0.042);
        burst(l, at, dust(ElementType.CRYO, 1.1f), 24, 0.42, 0.065);
        burst(l, at, ParticleTypes.END_ROD, 8, 0.30, 0.030);
    }

    public static void spawnFreeze(ServerLevel l, Vec3 at) {
        ring(l, at, ParticleTypes.SNOWFLAKE, 40, 0.75, 0.04);
        burst(l, at, dust(ElementType.CRYO, 1.0f), 20, 0.4, 0.06);
    }

    public static void spawnShatter(LivingEntity e) {
        ServerLevel l = level(e); if (l == null) return;
        Vec3 at = center(e);
        burst(l, at, new BlockParticleOption(ParticleTypes.BLOCK, Blocks.ICE.defaultBlockState()), 42, 0.5, 0.22);
        burst(l, at, ParticleTypes.SNOWFLAKE, 20, 0.45, 0.12);
    }

    public static void spawnMelt(LivingEntity e) {
        ServerLevel l = level(e); if (l == null) return;
        Vec3 at = center(e);
        burst(l, at, ParticleTypes.DRIPPING_WATER, 22, 0.35, 0.07);
        burst(l, at, ParticleTypes.CLOUD, 12, 0.3, 0.04);
    }

    public static void spawnVaporize(LivingEntity e) {
        ServerLevel l = level(e); if (l == null) return;
        Vec3 at = center(e);
        burst(l, at, ParticleTypes.CLOUD, 34, 0.45, 0.1);
        burst(l, at, dust(ElementType.HYDRO, 0.9f), 16, 0.35, 0.06);
    }

    public static void spawnSwirl(LivingEntity e) { spawnSwirl(e, ElementType.ANEMO, 1.5); }
    public static void spawnSwirl(LivingEntity e, ElementType element, double radius) {
        ServerLevel l = level(e); if (l == null) return;
        spawnSwirlAt(l, center(e), element, radius);
    }

    public static void spawnSwirlAt(ServerLevel l, Vec3 at, ElementType element, double radius) {
        double reach = Math.max(0.6, Math.min(2.4, radius * 0.55));
        DustParticleOptions gradientMain = gradientDust(ElementType.ANEMO, element, 0.75f, 1.15f);
        DustParticleOptions gradientRing = gradientDust(ElementType.ANEMO, element, 0.75f, 0.95f);
        burst(l, at, gradientMain, 70, reach, 0.20);
        ring(l, at, gradientRing, 22, reach * 0.82, 0.05);
        ParticleOptions accent = switch (element) {
            case PYRO -> ParticleTypes.FLAME; case HYDRO -> ParticleTypes.SPLASH;
            case CRYO, FROZEN -> ParticleTypes.SNOWFLAKE; case ELECTRO -> ParticleTypes.ELECTRIC_SPARK;
            case DENDRO, QUICKEN -> ParticleTypes.HAPPY_VILLAGER; case GEO -> ParticleTypes.END_ROD;
            default -> ParticleTypes.CLOUD;
        };
        burst(l, at, accent, 14, reach * 0.7, 0.2);
    }

    public static void spawnCrystallize(LivingEntity e) { spawnCrystallize(e, ElementType.GEO); }
    public static void spawnCrystallize(LivingEntity e, ElementType element) {
        ServerLevel l = level(e); if (l == null) return;
        Vec3 at = center(e);
        ring(l, at, dust(element, 1.1f), 32, 0.7, 0.05);
        burst(l, at, dust(ElementType.GEO, 0.85f), 18, 0.35, 0.08);
        burst(l, at, ParticleTypes.END_ROD, 10, 0.3, 0.05);
    }
    public static void spawnCrystallizeAura(ServerLevel l, Vec3 at, ElementType element) {
        ring(l, at, dust(element, 0.8f), 9, 0.55, 0.02);
        burst(l, at, ParticleTypes.END_ROD, 3, 0.2, 0.02);
    }

    public static void spawnBloom(LivingEntity e) {
        ServerLevel l = level(e); if (l == null) return;
        Vec3 at = center(e);
        ring(l, at, dust(ElementType.DENDRO, 1.1f), 26, 0.6, 0.04);
        burst(l, at, ParticleTypes.COMPOSTER, 18, 0.35, 0.06);
    }
    public static void spawnBloomBurst(LivingEntity e) { ServerLevel l = level(e); if (l != null) spawnBloomBurst(l, center(e)); }
    public static void spawnBloomBurst(ServerLevel l, Vec3 at) {
        burst(l, at, dust(ElementType.DENDRO, 1.3f), 36, 0.7, 0.14);
        burst(l, at, ParticleTypes.HAPPY_VILLAGER, 24, 0.55, 0.1);
    }

    public static void spawnBurgeonTrigger(LivingEntity e) { ServerLevel l = level(e); if (l != null) spawnBurgeonTrigger(l, center(e)); }
    public static void spawnBurgeonTrigger(ServerLevel l, Vec3 at) {
        burst(l, at, ParticleTypes.FLAME, 20, 0.3, 0.08);
        burst(l, at, dust(ElementType.PYRO, 1.0f), 12, 0.25, 0.05);
    }
    public static void spawnBurgeonBurst(LivingEntity e) { ServerLevel l = level(e); if (l != null) spawnBurgeonBurst(l, center(e)); }
    public static void spawnBurgeonBurst(ServerLevel l, Vec3 at) {
        burst(l, at, ParticleTypes.FLAME, 40, 0.65, 0.2);
        burst(l, at, ParticleTypes.LAVA, 14, 0.4, 0.1);
        burst(l, at, ParticleTypes.SMOKE, 18, 0.5, 0.06);
    }

    public static void spawnHyperbloomTrigger(LivingEntity e) { ServerLevel l = level(e); if (l != null) spawnHyperbloomTrigger(l, center(e)); }
    public static void spawnHyperbloomTrigger(ServerLevel l, Vec3 at) {
        burst(l, at, ParticleTypes.ELECTRIC_SPARK, 22, 0.3, 0.14);
        burst(l, at, dust(ElementType.ELECTRO, 1.0f), 12, 0.25, 0.06);
    }
    public static void spawnHyperbloomImpact(LivingEntity e) { ServerLevel l = level(e); if (l != null) spawnHyperbloomImpact(l, center(e)); }
    public static void spawnHyperbloomImpact(ServerLevel l, Vec3 at) {
        burst(l, at, ParticleTypes.ELECTRIC_SPARK, 30, 0.45, 0.18);
        burst(l, at, dust(ElementType.DENDRO, 1.1f), 16, 0.35, 0.08);
        ring(l, at, ParticleTypes.END_ROD, 12, 0.5, 0.04);
    }

    public static void spawnCatalyze(LivingEntity e) {
        ServerLevel l = level(e); if (l == null) return;
        Vec3 at = center(e);
        ring(l, at, dust(ElementType.QUICKEN, 1.0f), 22, 0.55, 0.03);
        burst(l, at, ParticleTypes.COMPOSTER, 14, 0.3, 0.05);
    }
    public static void spawnAggravate(LivingEntity e) {
        ServerLevel l = level(e); if (l == null) return;
        Vec3 at = center(e);
        burst(l, at, ParticleTypes.ELECTRIC_SPARK, 26, 0.4, 0.16);
        burst(l, at, dust(ElementType.QUICKEN, 1.05f), 16, 0.3, 0.07);
    }
    public static void spawnSpread(LivingEntity e) {
        ServerLevel l = level(e); if (l == null) return;
        Vec3 at = center(e);
        ring(l, at, dust(ElementType.DENDRO, 1.15f), 24, 0.65, 0.04);
        burst(l, at, ParticleTypes.SPORE_BLOSSOM_AIR, 18, 0.4, 0.06);
    }

    public static void spawnBurning(LivingEntity e) {
        ServerLevel l = level(e); if (l == null) return;
        Vec3 at = center(e);
        burst(l, at, ParticleTypes.SMALL_FLAME, 16, 0.3, 0.05);
        burst(l, at, ParticleTypes.SMOKE, 10, 0.25, 0.03);
    }

    public static void spawnShieldHit(LivingEntity e, ElementType element) {
        ServerLevel l = level(e); if (l == null) return;
        Vec3 at = center(e);
        ring(l, at, dust(element == null ? ElementType.GEO : element, 1.0f), 18, 0.8, 0.03);
        burst(l, at, ParticleTypes.CRIT, 6, 0.25, 0.04);
    }
}