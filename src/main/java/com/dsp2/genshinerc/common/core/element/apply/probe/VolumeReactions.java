package com.dsp2.genshinerc.common.core.element.apply.probe;

import com.dsp2.genshinerc.common.core.element.FrozenControl;

import com.dsp2.genshinerc.GenshinERC;
import com.dsp2.genshinerc.common.config.ServerConfig;
import com.dsp2.genshinerc.common.core.element.ElementType;
import com.dsp2.genshinerc.common.core.reaction.DendroCoreEntity;
import com.dsp2.genshinerc.common.core.reaction.ElementReactions;
import com.dsp2.genshinerc.common.core.reaction.Reaction;
import com.dsp2.genshinerc.common.core.reaction.WorldElementFields;
import com.dsp2.genshinerc.common.world.block.EleIceBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import com.dsp2.genshinerc.mixin.core.reaction.CreeperPoweredAccessor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.common.Mod;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

import java.util.Optional;
import java.util.Set;
import java.util.WeakHashMap;

@Mod.EventBusSubscriber(modid = GenshinERC.MODID)
public final class VolumeReactions {
    private static final int MAX_POSITIONS = 192;
    private static final int MAX_IGNITE = 8;
    private static final WeakHashMap<LivingEntity, SweepState> SWEEPS = new WeakHashMap<>();

    private static final class SweepState {
        Object animation;
        float phase;
        float elapsed;
        long tick = Long.MIN_VALUE;
        boolean overloaded;
        int ignited;
        boolean steamPlayed;
        boolean geoPlayed;
    }

    private VolumeReactions() {}

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onTick(LivingEvent.LivingTickEvent event) {
        LivingEntity attacker = event.getEntity();
        if (attacker.level().isClientSide() || !attacker.isAlive()) return;
        if (FrozenControl.locked(attacker)) {
            SWEEPS.remove(attacker);
            return;
        }
        ElementImbue.State imbue = ElementImbue.get(attacker);
        if (imbue == null || !Float.isFinite(imbue.units()) || imbue.units() <= 0f
                || (imbue.element() != ElementType.CRYO && imbue.element() != ElementType.PYRO
                && imbue.element() != ElementType.ELECTRO && imbue.element() != ElementType.HYDRO
                && imbue.element() != ElementType.ANEMO && imbue.element() != ElementType.GEO
                && imbue.element() != ElementType.DENDRO)) {
            SWEEPS.remove(attacker);
            return;
        }
        Optional<?> found = EpicFightCapabilities.getUnparameterizedEntityPatch(attacker, LivingEntityPatch.class);
        if (found == null || found.isEmpty()) return;
        SwingVolumeSampler.SwingSample sample = SwingVolumeSampler.sample((LivingEntityPatch<?>) found.get());
        if (sample.volumes().isEmpty() || !(attacker.level() instanceof ServerLevel level)) {
            SWEEPS.remove(attacker);
            return;
        }
        SweepState state = SWEEPS.computeIfAbsent(attacker, ignored -> new SweepState());
        long now = level.getGameTime();
        if (state.animation != sample.animation() || state.phase != sample.phaseStart()
                || sample.elapsed() < state.elapsed || state.tick < now - 1 || state.tick > now) {
            state.animation = sample.animation();
            state.phase = sample.phaseStart();
            state.overloaded = false;
            state.ignited = 0;
            state.steamPlayed = false;
            state.geoPlayed = false;
        }
        state.elapsed = sample.elapsed();
        state.tick = now;
        if (imbue.element() == ElementType.ELECTRO) chargeCreepers(level, attacker, sample);
        affectBlocks(level, attacker, imbue, sample, state, canGrief(attacker));
        affectDendroCores(level, attacker, imbue, sample);
    }

    @SubscribeEvent
    public static void onEpicFightContact(net.minecraftforge.event.entity.living.LivingAttackEvent event) {
        if (!(event.getEntity() instanceof Creeper creeper) || !(creeper.level() instanceof ServerLevel level)
                || !(event.getSource() instanceof yesman.epicfight.world.damagesource.EpicFightDamageSource source)
                || !(source.getEntity() instanceof LivingEntity attacker) || attacker == creeper
                || source.getDirectEntity() != attacker || FrozenControl.locked(attacker)
                || source.getAnimation() == null
                || !(source.getAnimation().get() instanceof yesman.epicfight.api.animation.types.AttackAnimation)) return;
        ElementImbue.State imbue = ElementImbue.get(attacker);
        if (imbue != null && imbue.element() == ElementType.ELECTRO
                && Float.isFinite(imbue.units()) && imbue.units() > 0f) powerCreeper(level, creeper);
    }

    private static void powerCreeper(ServerLevel level, Creeper creeper) {
        if (!creeper.isAlive() || creeper.isPowered()) return;
        creeper.getEntityData().set(CreeperPoweredAccessor.genshinerc$poweredData(), true);
        var center = creeper.getBoundingBox().getCenter();
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, center.x, center.y, center.z,
                20, 0.35, 0.45, 0.35, 0.11);
    }

    private static void chargeCreepers(ServerLevel level, LivingEntity attacker, SwingVolumeSampler.SwingSample sample) {
        var bounds = sample.volumes().get(0).bounds();
        for (int i = 1; i < sample.volumes().size(); i++) {
            bounds = bounds.minmax(sample.volumes().get(i).bounds());
        }
        for (Creeper creeper : level.getEntitiesOfClass(Creeper.class, bounds,
                target -> target != attacker && target.isAlive() && !target.isPowered())) {
            var hitbox = creeper.getBoundingBox();
            for (SwingVolume volume : sample.volumes()) {
                if (!volume.bounds().intersects(hitbox) || !volume.intersects(hitbox)) continue;
                powerCreeper(level, creeper);
                break;
            }
        }
    }

    private static void affectDendroCores(ServerLevel level, LivingEntity attacker, ElementImbue.State imbue,
                                          SwingVolumeSampler.SwingSample sample) {
        Reaction reaction;
        if (imbue.element() == ElementType.ELECTRO) reaction = Reaction.HYPERBLOOM;
        else if (imbue.element() == ElementType.PYRO) reaction = Reaction.BURGEON;
        else return;

        var volumes = sample.volumes();
        if (volumes.isEmpty()) return;

        net.minecraft.world.phys.AABB bounds = volumes.get(0).bounds();
        for (int i = 1; i < volumes.size(); i++) {
            bounds = bounds.minmax(volumes.get(i).bounds());
        }

        for (DendroCoreEntity core : level.getEntitiesOfClass(DendroCoreEntity.class, bounds,
                entity -> !entity.isRemoved())) {
            for (SwingVolume volume : volumes) {
                if (volume.bounds().intersects(core.getBoundingBox())
                        && volume.intersects(core.getBoundingBox())) {
                    core.detonate(reaction, attacker);
                    break;
                }
            }
        }
    }

    private static void affectBlocks(ServerLevel level, LivingEntity attacker, ElementImbue.State imbue,
                                     SwingVolumeSampler.SwingSample sample, SweepState sweep, boolean grief) {        Set<BlockPos> positions = new java.util.LinkedHashSet<>();
        for (SwingVolume volume : sample.volumes()) {
            if (positions.size() >= MAX_POSITIONS) break;
            BlockPos min = BlockPos.containing(volume.worldCenter.x - volume.radius, volume.worldCenter.y - volume.radius, volume.worldCenter.z - volume.radius);
            BlockPos max = BlockPos.containing(volume.worldCenter.x + volume.radius, volume.worldCenter.y + volume.radius, volume.worldCenter.z + volume.radius);
            for (BlockPos cursor : BlockPos.betweenClosed(min, max)) {
                if (positions.size() >= MAX_POSITIONS) break;
                if (!level.isLoaded(cursor) || !volume.contains(cursor.getX() + 0.5, cursor.getY() + 0.5, cursor.getZ() + 0.5)) continue;
                positions.add(cursor.immutable());
            }
        }
        java.util.List<BlockPos> instantTnt = new java.util.ArrayList<>();
        Vec3 blast = null;
        Vec3 lava = null;
        for (BlockPos pos : positions) {
            BlockState state = level.getBlockState(pos);
            boolean fire = state.is(BlockTags.FIRE);
            boolean snowLayer = state.is(Blocks.SNOW);
            boolean snowBlock = state.is(Blocks.SNOW_BLOCK);
            boolean powderSnow = state.is(Blocks.POWDER_SNOW);
            boolean snowSource = snowLayer || snowBlock || powderSnow;

            switch (imbue.element()) {
                case CRYO -> {
                    if (!grief) break;
                    if (snowSource) {
                        ElementReactions.probeSuperconduct(level, attacker, Vec3.atCenterOf(pos));
                        if (level.removeBlock(pos, false)) {
                            level.playSound(null, pos, SoundEvents.SNOW_BREAK, SoundSource.BLOCKS, 0.8f, 1.15f);
                            level.sendParticles(ParticleTypes.SNOWFLAKE,
                                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                                    18, 0.3, 0.25, 0.3, 0.04);
                        }
                    } else if (fire && level.removeBlock(pos, false)) {
                        extinguish(level, pos, true);
                    } else if (state.is(Blocks.WATER) && ServerConfig.CRYO_FREEZE_BLOCKS.get()) {
                        EleIceBlock.freeze(level, pos, state.getFluidState().isSource(), imbue.units());
                    }
                }
                case PYRO -> {
                    if (snowSource && grief) {
                        if (powderSnow) {
                            level.setBlockAndUpdate(pos, Blocks.WATER.defaultBlockState());
                        } else {
                            level.removeBlock(pos, false);
                        }
                        Vec3 melt = Vec3.atCenterOf(pos);
                        level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH,
                                SoundSource.BLOCKS, 0.85f, 1.35f);
                        level.sendParticles(ParticleTypes.CLOUD,
                                melt.x, melt.y, melt.z, 14, 0.3, 0.25, 0.3, 0.03);
                        level.sendParticles(ParticleTypes.SPLASH,
                                melt.x, melt.y, melt.z, 10, 0.24, 0.2, 0.24, 0.03);
                        break;
                    }
                    if (attacker.isUnderWater() && state.getFluidState().is(Fluids.WATER)) {
                        if (!sweep.steamPlayed) {
                            sweep.steamPlayed = true;
                            Vec3 steam = Vec3.atCenterOf(pos);
                            level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.8f, 1.35f);
                            level.sendParticles(ParticleTypes.CLOUD, steam.x, steam.y, steam.z, 18, 0.32, 0.28, 0.32, 0.025);
                            level.sendParticles(ParticleTypes.WHITE_ASH, steam.x, steam.y, steam.z, 10, 0.24, 0.2, 0.24, 0.02);
                        }
                        break;
                    }
                    if (!grief) break;
                    if (state.is(Blocks.TNT)) {
                        if (level.removeBlock(pos, false)) instantTnt.add(pos);
                    } else if (state.getBlock() instanceof EleIceBlock) {
                        EleIceBlock.melt(level, pos);
                    } else if (sweep.ignited < MAX_IGNITE && ignite(level, pos, state, imbue.units())) {
                        sweep.ignited++;
                    }
                }
                case HYDRO -> {
                    if (grief && fire && level.removeBlock(pos, false)) extinguish(level, pos, false);
                }
                case GEO -> {
                    if (fire && grief && level.removeBlock(pos, false)) {
                        ElementReactions.probeCrystallize(level, attacker, Vec3.atCenterOf(pos), ElementType.PYRO);
                        extinguish(level, pos, false);
                        if (!sweep.geoPlayed) {
                            sweep.geoPlayed = true;
                            level.sendParticles(ParticleTypes.END_ROD, pos.getX() + 0.5, pos.getY() + 0.5,
                                    pos.getZ() + 0.5, 8, 0.25, 0.25, 0.25, 0.03);
                        }
                    }
                }
                case ANEMO -> {
                    if (fire) {
                        WorldElementFields.applyProbeToBlock(level, pos, attacker, ElementType.ANEMO, imbue.units());
                        Vec3 at = Vec3.atCenterOf(pos);
                        level.sendParticles(ParticleTypes.CLOUD, at.x, at.y, at.z, 5, 0.22, 0.18, 0.22, 0.04);
                        level.sendParticles(ParticleTypes.SWEEP_ATTACK, at.x, at.y, at.z, 2, 0.15, 0.1, 0.15, 0);
                    }
                }
                case DENDRO -> {
                    if (state.getBlock() instanceof net.minecraft.world.level.block.CropBlock crop && grief) {
                        crop.randomTick(state, level, pos, level.random);
                        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5, pos.getY() + 0.6,
                                pos.getZ() + 0.5, 2, 0.2, 0.2, 0.2, 0.01);
                    }
                }
                case ELECTRO -> {
                    if ((snowLayer || snowBlock) && grief) {
                        ElementReactions.probeSuperconduct(level, attacker, Vec3.atCenterOf(pos));
                        level.removeBlock(pos, false);
                        Vec3 at = Vec3.atCenterOf(pos);
                        level.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                                at.x, at.y, at.z, 14, 0.3, 0.28, 0.3, 0.08);
                        level.playSound(null, pos, SoundEvents.LIGHTNING_BOLT_IMPACT,
                                SoundSource.PLAYERS, 0.32f, 1.85f);
                    }
                    if (state.getFluidState().is(Fluids.WATER)) {
                        WorldElementFields.startElectroWaterZone(level, pos, attacker);
                        Vec3 at = Vec3.atCenterOf(pos);
                        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, at.x, at.y, at.z, 10,
                                0.35, 0.3, 0.35, 0.08);
                        level.playSound(null, pos, SoundEvents.LIGHTNING_BOLT_IMPACT,
                                SoundSource.PLAYERS, 0.25f, 1.7f);
                    }
                    if (fire) {
                        if (grief) level.removeBlock(pos, false);
                        if (blast == null) blast = Vec3.atCenterOf(pos);
                    } else if (lava == null && (state.is(Blocks.LAVA) || state.getFluidState().is(Fluids.LAVA))) {
                        lava = Vec3.atCenterOf(pos);
                    }
                }
                default -> { }
            }
        }
        for (BlockPos pos : instantTnt) {
            double x = pos.getX() + 0.5;
            double y = pos.getY();
            double z = pos.getZ() + 0.5;
            var tnt = new net.minecraft.world.entity.item.PrimedTnt(level, x, y, z, attacker);
            level.explode(tnt, x, y + tnt.getBbHeight() * 0.0625, z, 4.0f,
                    net.minecraft.world.level.Level.ExplosionInteraction.TNT);
        }
        Vec3 at = blast == null ? lava : blast;
        if (at != null && !sweep.overloaded) {
            sweep.overloaded = true;
            ElementReactions.overloadAt(level, attacker, at);
        }
    }

    private static void extinguish(ServerLevel level, BlockPos pos, boolean cryo) {
        Vec3 at = Vec3.atCenterOf(pos);
        level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.55f, 1.25f);
        level.sendParticles(ParticleTypes.CLOUD, at.x, at.y, at.z, 8, 0.22, 0.18, 0.22, 0.025);
        level.sendParticles(cryo ? ParticleTypes.SNOWFLAKE : ParticleTypes.SPLASH,
                at.x, at.y, at.z, 8, 0.25, 0.18, 0.25, 0.03);
    }
    private static boolean ignite(ServerLevel level, BlockPos pos, BlockState state, float units) {
        if (!level.getGameRules().getBoolean(GameRules.RULE_DOFIRETICK)) return false;
        int flam = ((FireBlock) Blocks.FIRE).getBurnOdds(state);
        if (flam <= 0) return false;
        float required = 3.5f - flam * 0.025f;
        if (units + 1.0e-4f < required) return false;
        if (level.random.nextFloat() > Mth.clamp(0.2f + (units - required) / 4f, 0.2f, 1f)) return false;
        if (state.getBlock() instanceof BushBlock || (state.canBeReplaced() && !(state.getBlock() instanceof BaseFireBlock))) {
            BlockState fire = BaseFireBlock.getState(level, pos);
            if (fire.canSurvive(level, pos)) {
                level.setBlockAndUpdate(pos, fire);
                return true;
            }
        }
        BlockPos above = pos.above();
        if (level.isLoaded(above) && level.getBlockState(above).isAir() && BaseFireBlock.canBePlacedAt(level, above, Direction.UP)) {
            level.setBlockAndUpdate(above, BaseFireBlock.getState(level, above));
            return true;
        }
        return false;
    }

    private static boolean canGrief(LivingEntity attacker) {
        return attacker instanceof net.minecraft.world.entity.player.Player || ForgeEventFactory.getMobGriefingEvent(attacker.level(), attacker);
    }
}
