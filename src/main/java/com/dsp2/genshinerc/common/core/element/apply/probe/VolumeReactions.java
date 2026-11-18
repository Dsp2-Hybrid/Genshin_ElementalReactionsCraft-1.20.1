package com.dsp2.genshinerc.common.core.element.apply.probe;

import com.dsp2.genshinerc.GenshinERC;
import com.dsp2.genshinerc.common.config.ServerConfig;
import com.dsp2.genshinerc.common.core.element.ElementAuraManager;
import com.dsp2.genshinerc.common.core.element.ElementType;
import com.dsp2.genshinerc.common.core.reaction.ElementReactions;
import com.dsp2.genshinerc.common.core.reaction.ReactionTargets;
import com.dsp2.genshinerc.common.world.block.EleIceBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.common.Mod;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.WeakHashMap;

@Mod.EventBusSubscriber(modid = GenshinERC.MODID)
public final class VolumeReactions {
    private static final int MAX_POSITIONS = 192;
    private static final int MAX_IGNITE = 8;
    private static final WeakHashMap<LivingEntity, Integer> ENTITY_PHASE = new WeakHashMap<>();
    private static final WeakHashMap<LivingEntity, Integer> OVERLOAD_PHASE = new WeakHashMap<>();

    private VolumeReactions() {}

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onTick(LivingEvent.LivingTickEvent event) {
        LivingEntity attacker = event.getEntity();
        if (attacker.level().isClientSide() || !attacker.isAlive()) return;
        ElementImbue.State imbue = ElementImbue.get(attacker);
        if (imbue == null) return;
        Optional<?> found = EpicFightCapabilities.getUnparameterizedEntityPatch(attacker, LivingEntityPatch.class);
        if (found == null || found.isEmpty()) return;
        SwingVolumeSampler.SwingSample sample = SwingVolumeSampler.sample((LivingEntityPatch<?>) found.get());
        if (sample.volumes().isEmpty() || !(attacker.level() instanceof ServerLevel level)) return;
        int phaseKey = attacker.getId() * 31 + Float.floatToIntBits(sample.phaseStart()) + imbue.element().ordinal();
        affectEntities(level, attacker, imbue, sample, phaseKey);
        affectBlocks(level, attacker, imbue, sample, canGrief(attacker));
    }

    private static void affectEntities(ServerLevel level, LivingEntity attacker, ElementImbue.State imbue, SwingVolumeSampler.SwingSample sample, int phaseKey) {
        AABB area = null;
        for (SwingVolume volume : sample.volumes()) area = area == null ? volume.bounds() : area.minmax(volume.bounds());
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, area, e -> e != attacker && ReactionTargets.isEnemy(attacker, e))) {
            if (ENTITY_PHASE.getOrDefault(target, Integer.MIN_VALUE) == phaseKey) continue;
            boolean hit = false;
            for (SwingVolume volume : sample.volumes()) if (volume.intersects(target.getBoundingBox())) { hit = true; break; }
            if (!hit) continue;
            ENTITY_PHASE.put(target, phaseKey);
            applyToEntity(target, attacker, imbue);
        }
    }

    private static void applyToEntity(LivingEntity target, LivingEntity attacker, ElementImbue.State imbue) {
        switch (imbue.element()) {
            case CRYO, PYRO -> ElementReactions.trigger(target, attacker, imbue.element(), imbue.units());
            case ELECTRO -> {
                boolean pyro = ElementAuraManager.hasAura(target, ElementType.PYRO);
                boolean cryo = ElementAuraManager.hasAura(target, ElementType.CRYO) || ElementAuraManager.hasAura(target, ElementType.FROZEN);
                if (pyro || cryo) ElementReactions.trigger(target, attacker, ElementType.ELECTRO, imbue.units());
                else if (!ElementAuraManager.hasAura(target, ElementType.HYDRO)) ElementAuraManager.applyUnits(target, ElementType.ELECTRO, imbue.units());
            }
            default -> { }
        }
    }

    private static void affectBlocks(ServerLevel level, LivingEntity attacker, ElementImbue.State imbue, SwingVolumeSampler.SwingSample sample, boolean grief) {
        Set<BlockPos> positions = new HashSet<>();
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
        int ignited = 0;
        boolean overloaded = false;
        for (BlockPos pos : positions) {
            BlockState state = level.getBlockState(pos);
            switch (imbue.element()) {
                case CRYO -> { if (grief && state.is(Blocks.WATER)) EleIceBlock.freeze(level, pos, state.getFluidState().isSource(), imbue.units()); }
                case PYRO -> {
                    if (!grief) break;
                    if (state.getBlock() instanceof EleIceBlock) EleIceBlock.melt(level, pos);
                    else if (ignited < MAX_IGNITE && ignite(level, pos, state, imbue.units())) ignited++;
                }
                case ELECTRO -> {
                    if (!overloaded && heat(state) && overload(level, attacker, imbue, sample, Vec3.atCenterOf(pos))) overloaded = true;
                }
                default -> { }
            }
        }
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

    private static boolean overload(ServerLevel level, LivingEntity attacker, ElementImbue.State imbue, SwingVolumeSampler.SwingSample sample, Vec3 at) {
        int phaseKey = Float.floatToIntBits(sample.phaseStart());
        if (OVERLOAD_PHASE.getOrDefault(attacker, Integer.MIN_VALUE) == phaseKey) return true;
        OVERLOAD_PHASE.put(attacker, phaseKey);
        double radius = ServerConfig.OVERLOAD_RADIUS.get();
        LivingEntity victim = null;
        double best = radius * radius;
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, new AABB(at, at).inflate(radius), e -> ReactionTargets.isEnemy(attacker, e))) {
            double distance = target.distanceToSqr(at);
            if (distance < best) { best = distance; victim = target; }
        }
        if (victim != null) {
            ElementAuraManager.applyUnits(victim, ElementType.ELECTRO, imbue.units());
            ElementReactions.trigger(victim, attacker, ElementType.PYRO, imbue.units());
            return true;
        }
        level.playSound(null, BlockPos.containing(at), SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 0.7f, 0.9f);
        level.sendParticles(ParticleTypes.FLAME, at.x, at.y, at.z, 12, 0.3, 0.3, 0.3, 0.02);
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, at.x, at.y, at.z, 10, 0.3, 0.3, 0.3, 0.08);
        return true;
    }

    private static boolean heat(BlockState state) {
        return state.is(Blocks.LAVA) || state.getFluidState().is(Fluids.LAVA) || state.is(BlockTags.FIRE);
    }

    private static boolean canGrief(LivingEntity attacker) {
        return attacker instanceof net.minecraft.world.entity.player.Player || ForgeEventFactory.getMobGriefingEvent(attacker.level(), attacker);
    }
}
