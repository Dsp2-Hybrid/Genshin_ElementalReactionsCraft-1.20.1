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
    private static final String ELEMENT_KEY = "GenshinERC_CrystalDropElement";
    private static final String AMOUNT_KEY = "GenshinERC_CrystalDropAmount";
    private static final String TICKS_KEY = "GenshinERC_CrystalDropTicks";

    private CrystallizeShardDrop() {}

    public static void spawnAt(ServerLevel level, Vec3 position, ElementType element, float amount, int ticks) {
        if (element == null || !Float.isFinite(amount) || amount <= 0f || ticks <= 0) return;

        ItemStack stack = new ItemStack(Items.AMETHYST_SHARD);
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString(ELEMENT_KEY, element.name());
        tag.putFloat(AMOUNT_KEY, amount);
        tag.putInt(TICKS_KEY, ticks);

        ItemEntity shard = new ItemEntity(level, position.x, position.y + 0.35, position.z, stack);
        shard.setPickUpDelay(10);
        level.addFreshEntity(shard);

        int rgb = color(element);
        DustParticleOptions particle = new DustParticleOptions(new Vector3f(
                ((rgb >>> 16) & 255) / 255f,
                ((rgb >>> 8) & 255) / 255f,
                (rgb & 255) / 255f), 1.0f);
        level.sendParticles(particle, position.x, position.y + 0.45, position.z,
                16, 0.28, 0.25, 0.28, 0.06);
        level.playSound(null, net.minecraft.core.BlockPos.containing(position),
                SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.55f,
                1.1f + level.getRandom().nextFloat() * 0.35f);
    }

    @SubscribeEvent
    public static void onPickup(PlayerEvent.ItemPickupEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;

        ItemStack stack = event.getStack();
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains("GenshinERC_CrystalDropElement")
                || !tag.contains("GenshinERC_CrystalDropAmount") || !tag.contains("GenshinERC_CrystalDropTicks")) return;

        ElementType element;
        try {
            element = ElementType.valueOf(tag.getString("GenshinERC_CrystalDropElement"));
        } catch (IllegalArgumentException exception) {
            tag.remove("GenshinERC_CrystalDropElement");
            tag.remove("GenshinERC_CrystalDropAmount");
            tag.remove("GenshinERC_CrystalDropTicks");
            stack.setTag(tag);
            return;
        }

        float amount = tag.getFloat("GenshinERC_CrystalDropAmount");
        int ticks = tag.getInt("GenshinERC_CrystalDropTicks");
        if (Float.isFinite(amount) && amount > 0f && ticks > 0) {
            ShieldManager.add(player, ShieldKind.ELEMENTAL, element, amount, ticks);
        }

        tag.remove("GenshinERC_CrystalDropElement");
        tag.remove("GenshinERC_CrystalDropAmount");
        tag.remove("GenshinERC_CrystalDropTicks");
        stack.setTag(tag);
    }

    private static int color(ElementType element) {
        return switch (element) {
            case PYRO -> 0xFF6936;
            case HYDRO -> 0x27C8FF;
            case ELECTRO -> 0xCC66FF;
            case CRYO, FROZEN -> 0xB4FFFE;
            default -> 0xD5B8FF;
        };
    }
}