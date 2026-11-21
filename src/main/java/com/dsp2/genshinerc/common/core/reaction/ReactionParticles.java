package com.dsp2.genshinerc.common.core.reaction;

import com.dsp2.genshinerc.common.core.element.ElementType;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import org.joml.Vector3f;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

public final class ReactionParticles {
    private ReactionParticles() {}
    private static ServerLevel L(LivingEntity e) { return e.level() instanceof ServerLevel sl ? sl : null; }
    public static void spawnOverload(LivingEntity e) { ServerLevel sl=L(e); if(sl==null)return; sl.sendParticles(ParticleTypes.FLAME,e.getX(),e.getY()+0.5,e.getZ(),20,0.5,0.5,0.5,0.05); sl.sendParticles(ParticleTypes.ELECTRIC_SPARK,e.getX(),e.getY()+0.5,e.getZ(),15,0.5,0.5,0.5,0.1); }
    public static void spawnSuperconduct(LivingEntity e) { ServerLevel sl=L(e); if(sl==null)return; sl.sendParticles(ParticleTypes.SNOWFLAKE,e.getX(),e.getY()+0.5,e.getZ(),15,0.5,0.5,0.5,0.05); sl.sendParticles(ParticleTypes.ELECTRIC_SPARK,e.getX(),e.getY()+0.5,e.getZ(),10,0.5,0.5,0.5,0.08); }
    public static void spawnElectroCharged(LivingEntity e) {
        ServerLevel level = L(e);
        if (level == null) return;
        var center = e.getBoundingBox().getCenter();
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, center.x, center.y, center.z,
                18, 0.38, 0.42, 0.38, 0.12);
        level.sendParticles(ParticleTypes.FALLING_WATER, center.x, center.y, center.z,
                10, 0.34, 0.36, 0.34, 0.07);
        level.sendParticles(dust(ElementType.ELECTRO, 0.9f), center.x, center.y, center.z,
                12, 0.32, 0.32, 0.32, 0.08);
        level.sendParticles(dust(ElementType.HYDRO, 0.75f), center.x, center.y, center.z,
                8, 0.30, 0.28, 0.30, 0.05);
    }
    public static void spawnFreeze(LivingEntity e) { ServerLevel sl=L(e); if(sl==null)return; sl.sendParticles(ParticleTypes.SNOWFLAKE,e.getX(),e.getY()+0.5,e.getZ(),25,0.5,0.5,0.5,0.02); }
    public static void spawnShatter(LivingEntity e) {
        ServerLevel sl = L(e);
        if (sl == null) return;
        var c = e.getBoundingBox().getCenter();
        var chips = new net.minecraft.core.particles.BlockParticleOption(
                ParticleTypes.BLOCK, net.minecraft.world.level.block.Blocks.ICE.defaultBlockState());
        sl.sendParticles(chips, c.x, c.y, c.z, 28, e.getBbWidth() * 0.40, e.getBbHeight() * 0.25, e.getBbWidth() * 0.40, 0.18);
        sl.sendParticles(ParticleTypes.SNOWFLAKE, c.x, c.y, c.z, 12, 0.25, 0.30, 0.25, 0.08);
    }
    public static void spawnMelt(LivingEntity e) { ServerLevel sl=L(e); if(sl==null)return; sl.sendParticles(ParticleTypes.DRIPPING_WATER,e.getX(),e.getY()+1.0,e.getZ(),15,0.3,0.3,0.3,0.05); sl.sendParticles(ParticleTypes.SMOKE,e.getX(),e.getY()+0.5,e.getZ(),8,0.3,0.3,0.3,0.02); }
    public static void spawnVaporize(LivingEntity e) {
        ServerLevel sl = L(e);
        if (sl == null) return;
        var c = e.getBoundingBox().getCenter();
        sl.sendParticles(ParticleTypes.CLOUD, c.x, c.y + 0.15, c.z, 16, 0.35, 0.24, 0.35, 0.065);
        sl.sendParticles(ParticleTypes.FALLING_WATER, c.x, c.y, c.z, 8, 0.30, 0.22, 0.30, 0.04);
    }
    public static void spawnSwirl(LivingEntity e) {
        spawnSwirl(e, ElementType.ANEMO, 1.5);
    }

    public static void spawnSwirl(LivingEntity e, ElementType element, double radius) {
        ServerLevel level = L(e);
        if (level == null) return;
        var center = e.getBoundingBox().getCenter();
        float reach = (float) Math.max(0.35, Math.min(6.0, radius));
        DustParticleOptions color = dust(element, 1.05f);
        DustParticleOptions wind = dust(ElementType.ANEMO, 0.8f);

        level.sendParticles(ParticleTypes.SWEEP_ATTACK, center.x, center.y, center.z, 2, 0.45, 0.16, 0.45, 0);
        level.sendParticles(ParticleTypes.CLOUD, center.x, center.y, center.z, 10, 0.4, 0.18, 0.4, 0.13);
        for (int i = 0; i < 20; i++) {
            double height = i / 19.0;
            double angle = i * Math.PI / 5.0;
            double distance = reach * (0.18 + height * 0.82);
            level.sendParticles(i % 4 == 0 ? wind : color,
                    center.x + Math.cos(angle) * distance, e.getY() + 0.2 + height * e.getBbHeight(),
                    center.z + Math.sin(angle) * distance, 1, 0.06, 0.05, 0.06, 0.02);
        }

        var accent = switch (element) {
            case PYRO -> ParticleTypes.SMALL_FLAME;
            case HYDRO -> ParticleTypes.SPLASH;
            case CRYO, FROZEN -> ParticleTypes.SNOWFLAKE;
            case ELECTRO -> ParticleTypes.ELECTRIC_SPARK;
            default -> ParticleTypes.CLOUD;
        };
        level.sendParticles(accent, center.x, center.y, center.z, 12, reach * 0.32, 0.30, reach * 0.32, 0.07);
    }

    public static void spawnElectroArea(LivingEntity e, double radius) {
        ServerLevel level = L(e);
        if (level == null) return;
        spawnElectroCharged(e);
        if (radius <= 0.0) return;

        var center = e.getBoundingBox().getCenter();
        double reach = Math.min(8.0, radius);
        DustParticleOptions water = dust(ElementType.HYDRO, 0.85f);
        DustParticleOptions electricity = dust(ElementType.ELECTRO, 1.0f);
        double offset = level.getRandom().nextDouble() * Math.PI * 2.0;
        for (int i = 0; i < 32; i++) {
            double angle = offset + i * Math.PI / 16.0;
            double distance = reach * (0.82 + level.getRandom().nextDouble() * 0.18);
            double x = center.x + Math.cos(angle) * distance;
            double z = center.z + Math.sin(angle) * distance;
            double y = e.getY() + 0.18 + (i % 4) * 0.14;
            level.sendParticles(i % 2 == 0 ? electricity : water, x, y, z,
                    1, 0.12, 0.08, 0.12, 0.025);
            if (i % 2 == 0) {
                level.sendParticles(ParticleTypes.ELECTRIC_SPARK, x, y + 0.12, z,
                        1, 0.03, 0.07, 0.03, 0.04);
            }
        }
    }

    private static DustParticleOptions dust(ElementType element, float scale) {
        int rgb = switch (element) {
            case PYRO -> 0xFF6936;
            case HYDRO -> 0x27C8FF;
            case CRYO, FROZEN -> 0xB4FFFE;
            case ELECTRO -> 0xCC66FF;
            default -> 0x66FFCC;
        };
        return new DustParticleOptions(new Vector3f(
                ((rgb >>> 16) & 255) / 255f, ((rgb >>> 8) & 255) / 255f, (rgb & 255) / 255f), scale);
    }
    public static void spawnCrystallize(LivingEntity e) {
        spawnCrystallize(e, ElementType.GEO);
    }

    public static void spawnCrystallize(LivingEntity e, ElementType element) {
        ServerLevel level = L(e);
        if (level == null) return;
        var center = e.getBoundingBox().getCenter();
        level.sendParticles(dust(element, 1.0f), center.x, center.y, center.z,
                18, 0.38, 0.38, 0.38, 0.08);
        level.sendParticles(dust(ElementType.GEO, 0.75f), center.x, center.y, center.z,
                10, 0.32, 0.34, 0.32, 0.06);
        level.sendParticles(ParticleTypes.END_ROD, center.x, center.y, center.z,
                6, 0.30, 0.32, 0.30, 0.04);
    }
    public static void spawnBloom(LivingEntity e) { ServerLevel sl=L(e); if(sl==null)return; sl.sendParticles(ParticleTypes.HAPPY_VILLAGER,e.getX(),e.getY()+0.5,e.getZ(),15,0.5,0.5,0.5,0.05); }
    public static void spawnBurning(LivingEntity e) { ServerLevel sl=L(e); if(sl==null)return; sl.sendParticles(ParticleTypes.SMALL_FLAME,e.getX(),e.getY()+0.5,e.getZ(),3,0.2,0.2,0.2,0.01); }
}