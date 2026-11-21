package com.dsp2.genshinerc.common.core.reaction;

import com.dsp2.genshinerc.common.core.combat.shield.ShieldKind;
import com.dsp2.genshinerc.common.core.combat.shield.ShieldManager;
import com.dsp2.genshinerc.common.core.element.ElementType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
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

    private CrystallizeShardPickup() {}

    public static void spawn(Player owner, ElementType element, float amount, int ticks) {
        if (!(owner.level() instanceof ServerLevel level) || amount <= 0f || element == null) return;

        ItemStack stack = new ItemStack(Items.AMETHYST_SHARD);
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString(ELEMENT_KEY, element.name());
        tag.putFloat(AMOUNT_KEY, amount);
        tag.putInt(TICKS_KEY, ticks);

        ItemEntity shard = new ItemEntity(level, owner.getX(), owner.getY() + 0.6, owner.getZ(), stack);
        shard.setDefaultPickUpDelay();
        shard.setPickUpDelay(10);
        shard.setUnlimitedLifetime();
        level.addFreshEntity(shard);
    }

    @SubscribeEvent
    public static void onPickup(PlayerEvent.ItemPickupEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;

        ItemStack stack = event.getStack();
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains("GenshinERC_CrystallizeElement")
                || !tag.contains("GenshinERC_CrystallizeAmount") || !tag.contains("GenshinERC_CrystallizeTicks")) return;

        ElementType element;
        try {
            element = ElementType.valueOf(tag.getString("GenshinERC_CrystallizeElement"));
        } catch (IllegalArgumentException exception) {
            tag.remove("GenshinERC_CrystallizeElement");
            tag.remove("GenshinERC_CrystallizeAmount");
            tag.remove("GenshinERC_CrystallizeTicks");
            stack.setTag(tag);
            return;
        }

        float amount = tag.getFloat("GenshinERC_CrystallizeAmount");
        int ticks = tag.getInt("GenshinERC_CrystallizeTicks");
        if (Float.isFinite(amount) && amount > 0f && ticks > 0) {
            ShieldManager.add(player, ShieldKind.ELEMENTAL, element, amount, ticks);
        }

        tag.remove("GenshinERC_CrystallizeElement");
        tag.remove("GenshinERC_CrystallizeAmount");
        tag.remove("GenshinERC_CrystallizeTicks");
        stack.setTag(tag);
    }
}