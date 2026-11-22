package com.dsp2.genshinerc.common.core.element;

import com.dsp2.genshinerc.GenshinERC;
import com.dsp2.genshinerc.common.core.reaction.ElementReactions;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;
import java.util.WeakHashMap;

@Mod.EventBusSubscriber(modid = GenshinERC.MODID)
public final class NaturalPlantSnowExposure {
    private static final int SAMPLE_INTERVAL = 4;
    private static final int REQUIRED_STANDING_TICKS = 100;
    private static final int AFTER_REACTION_COOLDOWN = 150;
    private static final int EXPOSURE_WINDOW = 80;
    private static final float SNOW_ELEMENT_CAP = 0.5f;
    private static final float PLANT_ELEMENT_CAP = 0.8f;

    private static final Map<LivingEntity, Integer> SNOW_STANDING = new WeakHashMap<>();
    private static final Map<LivingEntity, Integer> PLANT_STANDING = new WeakHashMap<>();
    private static final Map<LivingEntity, Integer> SNOW_COOLDOWN = new WeakHashMap<>();
    private static final Map<LivingEntity, Integer> PLANT_COOLDOWN = new WeakHashMap<>();

    private NaturalPlantSnowExposure() {}

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();

        if (entity.level().isClientSide()) return;
        if (!entity.isAlive() || entity.isSpectator()) return;
        if (entity.tickCount % SAMPLE_INTERVAL != 0) return;

        tickCooldowns(entity);

        BlockPos feet = entity.blockPosition();
        Level level = entity.level();
        BlockState current = level.getBlockState(feet);
        BlockState below = level.getBlockState(feet.below());

        boolean onSnow = isSnow(current) || isSnow(below);
        boolean onPlant = isPlant(level, feet, current)
                || isPlant(level, feet.below(), below);

        boolean blockedByEnvironment = entity.isInWater()
                || entity.isUnderWater()
                || entity.isInLava();

        if (onSnow && !blockedByEnvironment) {
            int time = SNOW_STANDING.getOrDefault(entity, 0) + SAMPLE_INTERVAL;
            SNOW_STANDING.put(entity, time);

            if (time >= REQUIRED_STANDING_TICKS
                    && SNOW_COOLDOWN.getOrDefault(entity, 0) <= 0) {
                float amount = ElementReactions.natural(
                        entity,
                        ElementType.CRYO,
                        SNOW_ELEMENT_CAP);

                if (amount > 0.0f) {
                    SNOW_COOLDOWN.put(entity, AFTER_REACTION_COOLDOWN);
                    SNOW_STANDING.put(entity, 0);
                }
            }
        } else {
            SNOW_STANDING.remove(entity);
        }

        if (onPlant && !blockedByEnvironment) {
            int time = PLANT_STANDING.getOrDefault(entity, 0) + SAMPLE_INTERVAL;
            PLANT_STANDING.put(entity, time);

            if (time >= REQUIRED_STANDING_TICKS
                    && PLANT_COOLDOWN.getOrDefault(entity, 0) <= 0) {
                float amount = ElementReactions.natural(
                        entity,
                        ElementType.DENDRO,
                        PLANT_ELEMENT_CAP);

                if (amount > 0.0f) {
                    PLANT_COOLDOWN.put(entity, AFTER_REACTION_COOLDOWN);
                    PLANT_STANDING.put(entity, 0);
                }
            }
        } else {
            PLANT_STANDING.remove(entity);
        }
    }

    private static void tickCooldowns(LivingEntity entity) {
        int snow = SNOW_COOLDOWN.getOrDefault(entity, 0);
        if (snow > 0) {
            SNOW_COOLDOWN.put(entity, Math.max(0, snow - SAMPLE_INTERVAL));
        }

        int plant = PLANT_COOLDOWN.getOrDefault(entity, 0);
        if (plant > 0) {
            PLANT_COOLDOWN.put(entity, Math.max(0, plant - SAMPLE_INTERVAL));
        }
    }

    private static boolean isSnow(BlockState state) {
        return state.is(Blocks.SNOW)
                || state.is(Blocks.SNOW_BLOCK);
    }

    private static boolean isPlant(Level level, BlockPos pos, BlockState state) {
        if (state.is(Blocks.SUGAR_CANE)
                || state.is(Blocks.TALL_GRASS)
                || state.is(Blocks.GRASS)
                || state.is(Blocks.FERN)
                || state.is(Blocks.LARGE_FERN)
                || state.is(Blocks.VINE)
                || state.is(Blocks.KELP)
                || state.is(Blocks.KELP_PLANT)) {
            return true;
        }

        if (state.getBlock() instanceof CropBlock
                || state.getBlock() instanceof BushBlock) {
            return state.getCollisionShape(level, pos).isEmpty();
        }

        return false;
    }
}