package com.dsp2.genshinerc.common.core.element.apply.probe;

import com.dsp2.genshinerc.common.core.element.FrozenControl;

import com.dsp2.genshinerc.GenshinERC;
import com.dsp2.genshinerc.common.config.ServerConfig;
import com.dsp2.genshinerc.common.core.element.ElementType;
import com.dsp2.genshinerc.common.core.reaction.ElementReactions;
import com.dsp2.genshinerc.common.world.block.EleIceBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import com.dsp2.genshinerc.mixin.core.combat.CreeperPoweredAccessor;
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
                && imbue.element() != ElementType.ELECTRO && imbue.element() != ElementType.HYDRO)) {
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
        }
        state.elapsed = sample.elapsed();
        state.tick = now;
        if (imbue.element() == ElementType.ELECTRO) chargeCreepers(level, attacker, sample);
        affectBlocks(level, attacker, imbue, sample, state, canGrief(attacker));
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
                creeper.getEntityData().set(CreeperPoweredAccessor.genshinerc$poweredData(), true);
                break;
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
            switch (imbue.element()) {
                case CRYO -> {
                    if (!grief) break;
                    if (fire && level.removeBlock(pos, false)) {
                        extinguish(level, pos, true);
                    } else if (state.is(Blocks.WATER) && ServerConfig.CRYO_FREEZE_BLOCKS.get()) {
                        EleIceBlock.freeze(level, pos, state.getFluidState().isSource(), imbue.units());
                    }
                }
                case PYRO -> {
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
                case ELECTRO -> {
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
