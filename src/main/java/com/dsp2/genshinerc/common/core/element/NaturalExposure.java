package com.dsp2.genshinerc.common.core.element;

import com.dsp2.genshinerc.GenshinERC;
import com.dsp2.genshinerc.common.config.ServerConfig;
import com.dsp2.genshinerc.common.core.reaction.ElementReactions;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Drowned;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.entity.monster.MagmaCube;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = GenshinERC.MODID)
public final class NaturalExposure {
    private static final int STEP = 4;
    private static final int NEAR_STEP = 40;

    private NaturalExposure() {}

    @SubscribeEvent
    public static void onTick(LivingEvent.LivingTickEvent event) {
        LivingEntity e = event.getEntity();
        if (e.level().isClientSide() || !e.isAlive() || e instanceof ArmorStand) return;
        if (!ServerConfig.NATURAL_ENABLED.get()) return;
        int phase = e.tickCount + e.getId();
        if (e instanceof Slime && phase % ServerConfig.PERIODIC_INTERVAL_TICKS.get() == 0) periodic(e);
        if (phase % STEP != 0) return;
        exposure(e);
        if (phase % NEAR_STEP == 0) nearSwamp(e);
    }

    private static void periodic(LivingEntity e) {
        ElementType type = e instanceof MagmaCube ? ElementType.PYRO : ElementType.DENDRO;
        if (ElementAuraManager.hasAura(e, type)) return;
        float rest = ElementReactions.natural(e, type, GaugeTier.WEAK.units());
        if (rest > 0f) ElementAuraManager.apply(e, type, rest, Math.max(1, Math.round((2.5f * rest + 7f) * 20f)));
    }

    private static void exposure(LivingEntity e) {
        Level level = e.level();
        float weak = 1.0f;
        float strong = 2.0f;
        BlockPos feet = e.blockPosition();
        BlockPos eye = BlockPos.containing(e.getEyePosition());
        boolean aquatic = e instanceof WaterAnimal || e instanceof Drowned || e instanceof Guardian;

        if (e.isInWater()) {
            if (!aquatic) {
                if (isSwamp(level, feet)) apply(e, ElementType.DENDRO, weak, ServerConfig.SWAMP_WATER_SECONDS.get(), STEP);
                else apply(e, ElementType.HYDRO, strong, ServerConfig.WATER_SECONDS.get(), STEP);
            }
        } else if (level.isRainingAt(eye)) {
            apply(e, ElementType.HYDRO, weak, ServerConfig.RAIN_SECONDS.get(), STEP);
        } else if (snowingAt(level, eye)) {
            apply(e, ElementType.CRYO, weak, ServerConfig.SNOW_SECONDS.get(), STEP);
        }

        if (level.getBlockState(feet).is(Blocks.POWDER_SNOW)) apply(e, ElementType.CRYO, strong, ServerConfig.POWDER_SNOW_SECONDS.get(), STEP);

        if (e.isInLava()) {
            apply(e, ElementType.PYRO, strong, ServerConfig.LAVA_SECONDS.get(), STEP);
        } else if (e.isOnFire() || level.getBlockState(feet).is(BlockTags.FIRE)) {
            if (level.getBlockState(feet).is(Blocks.SOUL_FIRE)) apply(e, ElementType.PYRO, strong, ServerConfig.SOUL_FIRE_SECONDS.get(), STEP);
            else apply(e, ElementType.PYRO, weak, ServerConfig.FIRE_SECONDS.get(), STEP);
        }
    }

    private static void nearSwamp(LivingEntity e) {
        if (e.isInWater()) return;
        Level level = e.level();
        double radius = ServerConfig.SWAMP_RADIUS.get();
        double d = nearestSwamp(level, e.blockPosition(), radius);
        if (d < 0.0) return;
        double min = ServerConfig.SWAMP_NEAR_MIN_SECONDS.get();
        double max = ServerConfig.SWAMP_NEAR_MAX_SECONDS.get();
        double seconds = min + (max - min) * Mth.clamp(d / radius, 0.0, 1.0);
        apply(e, ElementType.DENDRO, 1.0f, seconds, NEAR_STEP);
    }

    private static double nearestSwamp(Level level, BlockPos origin, double radius) {
        if (isSwamp(level, origin)) return 0.0;
        for (int ring = 1; ring <= 4; ring++) {
            double dist = radius * ring / 4.0;
            for (int i = 0; i < 8; i++) {
                double angle = i * (Math.PI / 4.0);
                BlockPos p = origin.offset((int) Math.round(Math.cos(angle) * dist), 0, (int) Math.round(Math.sin(angle) * dist));
                if (isSwamp(level, p)) return dist;
            }
        }
        return -1.0;
    }

    private static boolean isSwamp(Level level, BlockPos pos) { var biome = level.getBiome(pos); return biome.is(Biomes.SWAMP) || biome.is(Biomes.MANGROVE_SWAMP); }
    private static boolean snowingAt(Level level, BlockPos pos) { return level.isRaining() && level.canSeeSky(pos) && level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, pos).getY() <= pos.getY() && level.getBiome(pos).value().getPrecipitationAt(pos) == Biome.Precipitation.SNOW; }
    private static boolean immune(LivingEntity e, ElementType type) { return (type == ElementType.PYRO && e instanceof MagmaCube) || (type == ElementType.DENDRO && e instanceof Slime && !(e instanceof MagmaCube)); }

    private static void apply(LivingEntity e, ElementType type, float cap, double seconds, int step) {
        if (immune(e, type)) return;
        int ticks = Math.max(step, (int) Math.round(seconds * 20.0));
        float add = cap * step / ticks;
        float rest = ElementReactions.natural(e, type, add);
        ElementAuraManager.hold(e, type, rest, cap, step);
    }
}