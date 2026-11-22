package com.dsp2.genshinerc.common.core.reaction;

import com.dsp2.genshinerc.common.core.combat.shield.ShieldKind;
import com.dsp2.genshinerc.common.core.combat.shield.ShieldManager;
import com.dsp2.genshinerc.common.core.element.ElementType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "genshinerc")
public final class CrystallizeShardPickup {
    private static final String ELEMENT_KEY = "GenshinERC_CrystallizeElement";
    private static final String AMOUNT_KEY = "GenshinERC_CrystallizeAmount";
    private static final String TICKS_KEY = "GenshinERC_CrystallizeTicks";
    private static final String COLOR_KEY = "GenshinERC_CrystallizeColor";

    private CrystallizeShardPickup() {}

    public static void spawn(
            Player owner,
            ElementType element,
            float amount,
            int ticks) {
        if (!(owner.level() instanceof ServerLevel level)) return;
        if (element == null || amount <= 0f || ticks <= 0) return;

        ItemStack stack = new ItemStack(Items.AMETHYST_SHARD);
        CompoundTag tag = stack.getOrCreateTag();

        tag.putString(ELEMENT_KEY, element.name());
        tag.putFloat(AMOUNT_KEY, amount);
        tag.putInt(TICKS_KEY, ticks);
        tag.putInt(COLOR_KEY, color(element));

        ItemEntity shard = new ItemEntity(
                level,
                owner.getX(),
                owner.getY() + 0.6,
                owner.getZ(),
                stack);

        shard.setPickUpDelay(10);
        shard.setGlowingTag(true);
        shard.setDeltaMovement(
                (level.random.nextDouble() - 0.5) * 0.08,
                0.12 + level.random.nextDouble() * 0.06,
                (level.random.nextDouble() - 0.5) * 0.08);

        level.addFreshEntity(shard);
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