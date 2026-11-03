package com.dsp2.genshinerc.client.render.vfx.hit.aaap.weapons.test;

import com.dsp2.genshinerc.common.network.GenshinercNetwork;
import com.dsp2.genshinerc.common.network.packet.vfx.aaap.S2CHitEffectPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;

@SuppressWarnings({"deprecation", "removal"})
@Mod.EventBusSubscriber(modid = "genshinerc")
public class HighFrequencyMuramasaHitHandler {

    private static final ResourceLocation HIT_EFFECT_ID = new ResourceLocation("genshinerc", "hit_fire_c01");
    private static final ResourceLocation EFN_MURASAMA_ID = new ResourceLocation("efn", "hf_murasama");

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.isCanceled() || event.getAmount() <= 0) return;
        
        Entity sourceEntity = event.getSource().getEntity();
        if (sourceEntity instanceof Player player) {
            ItemStack mainHandItem = player.getMainHandItem();
            if (isEFNMuramasa(mainHandItem)) {
                LivingEntity target = event.getEntity();
                
                if (!player.level().isClientSide()) {
                    double x = target.getX();
                    double y = target.getY() + (target.getBbHeight() * 0.5);
                    double z = target.getZ();
                    
                    GenshinercNetwork.CHANNEL.send(
                            PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> target),
                            new S2CHitEffectPacket(x, y, z, HIT_EFFECT_ID)
                    );
                }
            }
        }
    }

    private static boolean isEFNMuramasa(ItemStack stack) {
        if (stack.isEmpty()) return false;
        ResourceLocation regName = ForgeRegistries.ITEMS.getKey(stack.getItem());
        return EFN_MURASAMA_ID.equals(regName);
    }
}