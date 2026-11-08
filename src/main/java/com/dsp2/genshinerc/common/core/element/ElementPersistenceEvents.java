package com.dsp2.genshinerc.common.core.element;

import com.dsp2.genshinerc.common.core.combat.shield.ShieldManager;
import com.dsp2.genshinerc.common.core.combat.zhenmie.ZhenMieManager;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "genshinerc")
public final class ElementPersistenceEvents {
    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide() || event.isCanceled()) return;
        if (event.getEntity() instanceof LivingEntity living) {
            ElementAuraManager.load(living);
            ShieldManager.load(living);
            ZhenMieManager.load(living);
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (event.isWasDeath()) return;
        Player newPlayer = event.getEntity();
        Player oldPlayer = event.getOriginal();
        newPlayer.getPersistentData().put("GenshinAuras", oldPlayer.getPersistentData().getCompound("GenshinAuras").copy());
        newPlayer.getPersistentData().put("GenshinShields", oldPlayer.getPersistentData().getCompound("GenshinShields").copy());
        newPlayer.getPersistentData().put("GenshinZhenMie", oldPlayer.getPersistentData().getCompound("GenshinZhenMie").copy());
        ElementAuraManager.load(newPlayer);
        ShieldManager.load(newPlayer);
        ZhenMieManager.load(newPlayer);
    }
}