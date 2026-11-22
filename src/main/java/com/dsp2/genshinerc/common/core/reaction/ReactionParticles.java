package com.dsp2.genshinerc.common.core.reaction;

import com.dsp2.genshinerc.common.core.element.ElementType;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import org.joml.Vector3f;

public final class ReactionParticles {
    private ReactionParticles() {}

    private static ServerLevel level(LivingEntity entity) {
        return entity.level() instanceof ServerLevel server ? server : null;
    }

    private static DustParticleOptions dust(ElementType element, float scale) {
        int color = switch (element) {
            case PYRO -> 0xFF6936;
            case HYDRO -> 0x27C8FF;
            case ELECTRO -> 0xCC66FF;
            case CRYO, FROZEN -> 0xB4FFFE;
            case DENDRO, QUICKEN -> 0x66FF55;
            case ANEMO -> 0x66E0C8;
            case GEO -> 0xE8B84A;
            default -> 0xFFFFFF;
        };

        return new DustParticleOptions(
                new Vector3f(
                        ((color >> 16) & 255) / 255f,
                        ((color >> 8) & 255) / 255f,
                        (color & 255) / 255f),
                scale);
    }

    public static void spawnOverload(LivingEntity entity) {
        ServerLevel level = level(entity);
        if (level == null) return;

        double x = entity.getX();
        double y = entity.getY() + 0.5;
        double z = entity.getZ();

        level.sendParticles(ParticleTypes.FLAME, x, y, z,
                36, 0.52, 0.50, 0.52, 0.07);
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, x, y, z,
                30, 0.50, 0.50, 0.50, 0.12);
        level.sendParticles(ParticleTypes.SMOKE, x, y + 0.15, z,
                18, 0.38, 0.36, 0.38, 0.035);
        level.sendParticles(ParticleTypes.EXPLOSION,
                x, y, z, 1, 0.1, 0.1, 0.1, 0.0);
    }

    public static void spawnSuperconduct(LivingEntity entity) {
        ServerLevel level = level(entity);
        if (level == null) return;

        double x = entity.getX();
        double y = entity.getY() + 0.5;
        double z = entity.getZ();

        level.sendParticles(ParticleTypes.SNOWFLAKE, x, y, z,
                34, 0.50, 0.50, 0.50, 0.04);
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, x, y, z,
                28, 0.50, 0.50, 0.50, 0.11);
        level.sendParticles(dust(ElementType.CRYO, 1.0f), x, y, z,
                22, 0.38, 0.38, 0.38, 0.06);
        level.sendParticles(dust(ElementType.ELECTRO, 0.9f), x, y, z,
                18, 0.35, 0.35, 0.35, 0.06);
    }

    public static void spawnElectroCharged(LivingEntity entity) {
        ServerLevel level = level(entity);
        if (level == null) return;

        var center = entity.getBoundingBox().getCenter();

        level.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                center.x, center.y, center.z,
                38, 0.40, 0.44, 0.40, 0.16);
        level.sendParticles(ParticleTypes.FALLING_WATER,
                center.x, center.y, center.z,
                26, 0.36, 0.38, 0.36, 0.10);
        level.sendParticles(dust(ElementType.ELECTRO, 0.9f),
                center.x, center.y, center.z,
                22, 0.34, 0.34, 0.34, 0.08);
        level.sendParticles(dust(ElementType.HYDRO, 0.75f),
                center.x, center.y, center.z,
                18, 0.32, 0.30, 0.32, 0.06);
        level.sendParticles(ParticleTypes.END_ROD,
                center.x, center.y, center.z,
                8, 0.26, 0.30, 0.26, 0.035);
    }

    public static void spawnElectroArea(LivingEntity entity, double radius) {
        ServerLevel level = level(entity);
        if (level == null) return;

        spawnElectroCharged(entity);

        var center = entity.getBoundingBox().getCenter();
        double reach = Math.min(8.0, Math.max(0.5, radius));
        double offset = level.getRandom().nextDouble() * Math.PI * 2.0;

        for (int i = 0; i < 48; i++) {
            double angle = offset + i * Math.PI / 24.0;
            double distance = reach * (0.78 + level.getRandom().nextDouble() * 0.22);
            double x = center.x + Math.cos(angle) * distance;
            double z = center.z + Math.sin(angle) * distance;
            double y = entity.getY() + 0.16 + (i % 5) * 0.12;

            level.sendParticles(
                    i % 2 == 0
                            ? dust(ElementType.ELECTRO, 0.95f)
                            : dust(ElementType.HYDRO, 0.78f),
                    x, y, z, 1, 0.12, 0.08, 0.12, 0.025);

            if (i % 2 == 0) {
                level.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                        x, y + 0.12, z, 2, 0.04, 0.08, 0.04, 0.045);
            }
        }
    }

    public static void spawnFreeze(LivingEntity entity) {
        ServerLevel level = level(entity);
        if (level == null) return;

        double x = entity.getX();
        double y = entity.getY() + 0.5;
        double z = entity.getZ();

        level.sendParticles(ParticleTypes.SNOWFLAKE, x, y, z,
                40, 0.52, 0.56, 0.52, 0.035);
        level.sendParticles(dust(ElementType.CRYO, 1.0f), x, y, z,
                24, 0.44, 0.44, 0.44, 0.045);
        level.sendParticles(ParticleTypes.END_ROD, x, y, z,
                8, 0.30, 0.35, 0.30, 0.025);
    }

    public static void spawnShatter(LivingEntity entity) {
        ServerLevel level = level(entity);
        if (level == null) return;

        var center = entity.getBoundingBox().getCenter();
        BlockParticleOption chips = new BlockParticleOption(
                ParticleTypes.BLOCK,
                Blocks.ICE.defaultBlockState());

        level.sendParticles(chips, center.x, center.y, center.z,
                42, entity.getBbWidth() * 0.50, entity.getBbHeight() * 0.30,
                entity.getBbWidth() * 0.50, 0.20);
        level.sendParticles(ParticleTypes.SNOWFLAKE, center.x, center.y, center.z,
                24, 0.34, 0.40, 0.34, 0.09);
        level.sendParticles(dust(ElementType.CRYO, 1.0f),
                center.x, center.y, center.z,
                18, 0.30, 0.35, 0.30, 0.06);
    }

    public static void spawnMelt(LivingEntity entity) {
        ServerLevel level = level(entity);
        if (level == null) return;

        double x = entity.getX();
        double y = entity.getY() + 1.0;
        double z = entity.getZ();

        level.sendParticles(ParticleTypes.DRIPPING_WATER, x, y, z,
                26, 0.34, 0.34, 0.34, 0.06);
        level.sendParticles(ParticleTypes.SMOKE, x, y - 0.2, z,
                14, 0.32, 0.30, 0.32, 0.025);
        level.sendParticles(ParticleTypes.CLOUD, x, y, z,
                12, 0.28, 0.25, 0.28, 0.035);
    }

    public static void spawnVaporize(LivingEntity entity) {
        ServerLevel level = level(entity);
        if (level == null) return;

        var center = entity.getBoundingBox().getCenter();

        level.sendParticles(ParticleTypes.CLOUD,
                center.x, center.y + 0.15, center.z,
                30, 0.38, 0.28, 0.38, 0.075);
        level.sendParticles(ParticleTypes.FALLING_WATER,
                center.x, center.y, center.z,
                18, 0.32, 0.25, 0.32, 0.05);
        level.sendParticles(dust(ElementType.HYDRO, 0.85f),
                center.x, center.y, center.z,
                14, 0.30, 0.26, 0.30, 0.05);
    }

    public static void spawnSwirl(LivingEntity entity) {
        spawnSwirl(entity, ElementType.ANEMO, 1.5);
    }

    public static void spawnSwirl(
            LivingEntity entity,
            ElementType element,
            double radius) {
        ServerLevel level = level(entity);
        if (level == null) return;

        var center = entity.getBoundingBox().getCenter();
        float reach = (float) Math.max(0.35, Math.min(6.0, radius));

        DustParticleOptions color = dust(element, 1.05f);
        DustParticleOptions wind = dust(ElementType.ANEMO, 0.82f);

        level.sendParticles(ParticleTypes.SWEEP_ATTACK,
                center.x, center.y, center.z,
                4, 0.45, 0.18, 0.45, 0.0);
        level.sendParticles(ParticleTypes.CLOUD,
                center.x, center.y, center.z,
                22, 0.42, 0.20, 0.42, 0.15);

        for (int i = 0; i < 42; i++) {
            double height = i / 41.0;
            double angle = i * Math.PI / 10.5;
            double distance = reach * (0.16 + height * 0.84);

            level.sendParticles(
                    i % 4 == 0 ? wind : color,
                    center.x + Math.cos(angle) * distance,
                    entity.getY() + 0.2 + height * entity.getBbHeight(),
                    center.z + Math.sin(angle) * distance,
                    1, 0.07, 0.06, 0.07, 0.025);
        }

        var accent = switch (element) {
            case PYRO -> ParticleTypes.SMALL_FLAME;
            case HYDRO -> ParticleTypes.SPLASH;
            case CRYO, FROZEN -> ParticleTypes.SNOWFLAKE;
            case ELECTRO -> ParticleTypes.ELECTRIC_SPARK;
            case DENDRO, QUICKEN -> ParticleTypes.HAPPY_VILLAGER;
            case GEO -> ParticleTypes.END_ROD;
            default -> ParticleTypes.CLOUD;
        };

        level.sendParticles(accent,
                center.x, center.y, center.z,
                18, reach * 0.34, 0.32, reach * 0.34, 0.08);
    }

    public static void spawnCrystallize(LivingEntity entity) {
        spawnCrystallize(entity, ElementType.GEO);
    }

    public static void spawnCrystallize(
            LivingEntity entity,
            ElementType element) {
        ServerLevel level = level(entity);
        if (level == null) return;

        var center = entity.getBoundingBox().getCenter();

        level.sendParticles(dust(element, 1.0f),
                center.x, center.y, center.z,
                38, 0.40, 0.40, 0.40, 0.11);
        level.sendParticles(dust(ElementType.GEO, 0.78f),
                center.x, center.y, center.z,
                22, 0.34, 0.36, 0.34, 0.07);
        level.sendParticles(ParticleTypes.END_ROD,
                center.x, center.y, center.z,
                12, 0.30, 0.34, 0.30, 0.045);
    }

    public static void spawnBloom(LivingEntity entity) {
        ServerLevel level = level(entity);
        if (level == null) return;

        double x = entity.getX();
        double y = entity.getY() + 0.5;
        double z = entity.getZ();

        level.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                x, y, z, 38, 0.52, 0.52, 0.52, 0.07);
        level.sendParticles(ParticleTypes.COMPOSTER,
                x, y, z, 28, 0.40, 0.38, 0.40, 0.06);
        level.sendParticles(dust(ElementType.DENDRO, 1.0f),
                x, y, z, 20, 0.34, 0.34, 0.34, 0.055);
    }

    public static void spawnBurning(LivingEntity entity) {
        ServerLevel level = level(entity);
        if (level == null) return;

        double x = entity.getX();
        double y = entity.getY() + 0.5;
        double z = entity.getZ();

        level.sendParticles(ParticleTypes.SMALL_FLAME,
                x, y, z, 18, 0.28, 0.34, 0.28, 0.03);
        level.sendParticles(ParticleTypes.LAVA,
                x, y, z, 10, 0.22, 0.28, 0.22, 0.04);
        level.sendParticles(ParticleTypes.SMOKE,
                x, y + 0.18, z, 12, 0.24, 0.34, 0.24, 0.02);
    }
}