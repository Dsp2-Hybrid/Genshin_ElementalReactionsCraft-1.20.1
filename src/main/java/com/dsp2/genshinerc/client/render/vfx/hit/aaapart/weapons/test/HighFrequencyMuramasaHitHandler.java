package com.dsp2.genshinerc.client.render.vfx.hit.aaapart.weapons.test;

import com.guhao.vix.particles.AAAEffekParticle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@SuppressWarnings({"deprecation", "removal"})
@Mod.EventBusSubscriber(modid = "genshinerc", value = Dist.CLIENT)
public class HighFrequencyMuramasaHitHandler {
    
    private static final ResourceLocation HIT_EFFECT_ID = new ResourceLocation("genshinerc", "hit_fire_c01");

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getSource().getEntity() instanceof Player player) {
            ItemStack mainHandItem = player.getMainHandItem();
            if (isTargetWeapon(mainHandItem)) {
                LivingEntity target = event.getEntity();
                if (target.level() instanceof ClientLevel clientLevel) {
                    double x = target.getX();
                    double y = target.getY() + (target.getBbHeight() * 0.5);
                    double z = target.getZ();
                    
                    AAAEffekParticle particle = new AAAEffekParticle(clientLevel, HIT_EFFECT_ID, x, y, z, 0, 0, 0);
                    Minecraft.getInstance().particleEngine.add(particle);
                }
            }
        }
    }

    private static boolean isTargetWeapon(ItemStack stack) {
        if (stack.isEmpty()) return false;
        String itemId = stack.getDescriptionId();
        String displayName = stack.getHoverName().getString();
        return itemId.contains("high_frequency_muramasa") || displayName.contains("村雨");
    }
}
