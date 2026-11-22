package com.dsp2.genshinerc.common.core.reaction;

import com.dsp2.genshinerc.common.core.combat.shield.ShieldKind;
import com.dsp2.genshinerc.common.core.combat.shield.ShieldManager;
import com.dsp2.genshinerc.common.core.element.ElementType;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector3f;

@Mod.EventBusSubscriber(modid = "genshinerc")
public final class CrystallizeShardDrop {
    public static final String ELEMENT_KEY = "GenshinERC_CrystalDropElement";
    public static final String AMOUNT_KEY = "GenshinERC_CrystalDropAmount";
    public static final String TICKS_KEY = "GenshinERC_CrystalDropTicks";
    public static final String COLOR_KEY = "GenshinERC_CrystalDropColor";

    private CrystallizeShardDrop() {}

    public static void spawnAt(
            ServerLevel level,
            Vec3 position,
            ElementType element,
            float amount,
            int ticks) {
        if (level == null) return;
        if (element == null || amount <= 0f || ticks <= 0) return;

        ItemStack stack = new ItemStack(Items.AMETHYST_SHARD);
        CompoundTag tag = stack.getOrCreateTag();

        tag.putString(ELEMENT_KEY, element.name());
        tag.putFloat(AMOUNT_KEY, amount);
        tag.putInt(TICKS_KEY, ticks);
        tag.putInt(COLOR_KEY, color(element));

        ItemEntity shard = new ItemEntity(
                level,
                position.x,
                position.y + 0.35,
                position.z,
                stack);

        shard.setPickUpDelay(10);
        shard.setGlowingTag(true);
        shard.setDeltaMovement(
                (level.random.nextDouble() - 0.5) * 0.08,
                0.12 + level.random.nextDouble() * 0.07,
                (level.random.nextDouble() - 0.5) * 0.08);

        level.addFreshEntity(shard);

        DustParticleOptions particle = particle(element);

        level.sendParticles(
                particle,
                position.x,
                position.y + 0.45,
                position.z,
                36,
                0.30,
                0.28,
                0.30,
                0.08);

        level.sendParticles(
                net.minecraft.core.particles.ParticleTypes.END_ROD,
                position.x,
                position.y + 0.45,
                position.z,
                8,
                0.18,
                0.20,
                0.18,
                0.035);

        level.playSound(
                null,
                net.minecraft.core.BlockPos.containing(position),
                SoundEvents.AMETHYST_BLOCK_RESONATE,
                SoundSource.PLAYERS,
                0.60f,
                1.04f + level.random.nextFloat() * 0.24f);
    }

    @SubscribeEvent
    public static void onPickup(PlayerEvent.ItemPickupEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;

        ItemStack stack = event.getStack();
        CompoundTag tag = stack.getTag();

        if (tag == null || !tag.contains(ELEMENT_KEY)) return;

        ElementType element;

        try {
            element = ElementType.valueOf(tag.getString(ELEMENT_KEY));
        } catch (IllegalArgumentException exception) {
            clearTag(tag);
            stack.setTag(tag);
            return;
        }

        float amount = tag.getFloat(AMOUNT_KEY);
        int ticks = tag.getInt(TICKS_KEY);

        if (Float.isFinite(amount) && amount > 0f && ticks > 0) {
            ShieldManager.add(
                    player,
                    ShieldKind.ELEMENTAL,
                    element,
                    amount,
                    ticks);

            player.level().playSound(
                    null,
                    player.blockPosition(),
                    SoundEvents.AMETHYST_BLOCK_CHIME,
                    SoundSource.PLAYERS,
                    0.72f,
                    1.12f + player.level().random.nextFloat() * 0.24f);
        }

        clearTag(tag);
        stack.setTag(tag);
    }

    private static void clearTag(CompoundTag tag) {
        tag.remove(ELEMENT_KEY);
        tag.remove(AMOUNT_KEY);
        tag.remove(TICKS_KEY);
        tag.remove(COLOR_KEY);
    }

    private static DustParticleOptions particle(ElementType element) {
        int color = color(element);

        return new DustParticleOptions(
                new Vector3f(
                        ((color >> 16) & 255) / 255f,
                        ((color >> 8) & 255) / 255f,
                        (color & 255) / 255f),
                1.0f);
    }

    private static int color(ElementType element) {
        return switch (element) {
            case PYRO -> 0xFF6936;
            case HYDRO -> 0x27C8FF;
            case ELECTRO -> 0xCC66FF;
            case CRYO, FROZEN -> 0xB4FFFE;
            case DENDRO, QUICKEN -> 0x66FF55;
            case ANEMO -> 0x66E0C8;
            case GEO -> 0xE8B84A;
            default -> 0xFFFFFF;
        };
    }
}