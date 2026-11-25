package com.dsp2.genshinerc.common.world.block.base;

import com.dsp2.genshinerc.common.world.block.EleIceBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "genshinerc")
public final class IceEntityResolver {
    private static final double EPS = 1.0E-4;
    private static final double[] LIFTS = {0.0, 0.5, 1.0};

    private IceEntityResolver() {}

    @SubscribeEvent
    public static void onTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level() instanceof ServerLevel level && entity.isAlive() && !entity.isSpectator()) {
            resolve(level, entity);
        }
    }

    public static void resolveAround(ServerLevel level, BlockPos pos) {
        AABB cell = new AABB(pos);
        for (Entity candidate : level.getEntitiesOfClass(Entity.class, cell.inflate(0.75),
                e -> !e.isRemoved() && !e.isSpectator() && !(e instanceof Projectile)
                        && e.getBoundingBox().intersects(cell))) {
            resolve(level, candidate);
        }
    }

    public static void resolve(ServerLevel level, Entity entity) {
        if (entity.noPhysics || entity.isPassenger()) return;
        AABB box = entity.getBoundingBox();
        double top = Double.NEGATIVE_INFINITY;
        boolean found = false;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int x = Mth.floor(box.minX + EPS); x <= Mth.floor(box.maxX - EPS); x++) {
            for (int y = Mth.floor(box.minY + EPS); y <= Mth.floor(box.maxY - EPS); y++) {
                for (int z = Mth.floor(box.minZ + EPS); z <= Mth.floor(box.maxZ - EPS); z++) {
                    cursor.set(x, y, z);
                    if (!level.hasChunkAt(cursor)) continue;
                    if (level.getBlockState(cursor).getBlock() instanceof EleIceBlock) {
                        found = true;
                        top = Math.max(top, y + 1.0);
                    }
                }
            }
        }
        if (!found) return;

        BlockPos probe = BlockPos.containing(entity.getX(), top + 0.5, entity.getZ());
        boolean surface = !level.getFluidState(probe).is(FluidTags.WATER);

        Vec3 shift = null;
        if (surface) shift = vertical(level, entity, box, top);
        if (shift == null) shift = horizontal(level, entity, box);
        if (shift == null) shift = vertical(level, entity, box, top);
        if (shift == null) shift = new Vec3(0.0, Math.max(0.0625, top - box.minY + 0.001), 0.0);
        relocate(entity, shift);
    }

    private static Vec3 vertical(ServerLevel level, Entity entity, AABB box, double top) {
        double base = Math.max(0.0625, top - box.minY + 0.001);
        for (double lift = base; lift <= 24.0; lift += 0.125) {
            if (free(level, entity, box.move(0.0, lift, 0.0))) return new Vec3(0.0, lift, 0.0);
        }
        return null;
    }

    private static Vec3 horizontal(ServerLevel level, Entity entity, AABB box) {
        for (int ring = 1; ring <= 12; ring++) {
            double distance = ring * 0.25;
            for (int step = 0; step < 16; step++) {
                double angle = step * Math.PI / 8.0;
                double dx = Math.cos(angle) * distance;
                double dz = Math.sin(angle) * distance;
                for (double lift : LIFTS) {
                    if (free(level, entity, box.move(dx, lift, dz))) return new Vec3(dx, lift, dz);
                }
            }
        }
        return null;
    }

    private static boolean free(ServerLevel level, Entity entity, AABB box) {
        return !touchesIce(level, box) && level.noCollision(entity, box);
    }

    private static boolean touchesIce(ServerLevel level, AABB box) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int x = Mth.floor(box.minX + EPS); x <= Mth.floor(box.maxX - EPS); x++) {
            for (int y = Mth.floor(box.minY + EPS); y <= Mth.floor(box.maxY - EPS); y++) {
                for (int z = Mth.floor(box.minZ + EPS); z <= Mth.floor(box.maxZ - EPS); z++) {
                    cursor.set(x, y, z);
                    if (!level.hasChunkAt(cursor)) continue;
                    if (level.getBlockState(cursor).getBlock() instanceof EleIceBlock) return true;
                }
            }
        }
        return false;
    }

    private static void relocate(Entity entity, Vec3 shift) {
        double x = entity.getX() + shift.x;
        double y = entity.getY() + shift.y;
        double z = entity.getZ() + shift.z;
        if (entity instanceof ServerPlayer player && player.connection != null) {
            player.connection.teleport(x, y, z, player.getYRot(), player.getXRot());
        } else {
            entity.setPos(x, y, z);
        }
        Vec3 velocity = entity.getDeltaMovement();
        entity.setDeltaMovement(velocity.x * 0.1, Math.max(0.0, velocity.y), velocity.z * 0.1);
        entity.hasImpulse = true;
        entity.hurtMarked = true;
    }
}