package com.dsp2.genshinerc.client.render.vfx.hit.aaapart.weapons.test;

import mod.chloeprime.aaaparticles.api.common.AAALevel;
import mod.chloeprime.aaaparticles.api.common.ParticleEmitterInfo;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@SuppressWarnings({"deprecation", "removal"})
@Mod.EventBusSubscriber(modid = "genshinerc", value = Dist.CLIENT)
public class HighFrequencyMuramasaHitHandler {

    private static final ResourceLocation HIT_EFFECT_ID = new ResourceLocation("genshinerc", "hit_fire_c01");
    private static final ResourceLocation EFN_MURASAMA_ID = new ResourceLocation("efn", "hf_murasama");

    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) {
            ItemStack mainHandItem = player.getMainHandItem();
            if (isEFNMuramasa(mainHandItem)) {
                Entity target = event.getTarget();
                if (target instanceof LivingEntity livingTarget && player.level() instanceof ClientLevel clientLevel) {
                    double x = livingTarget.getX();
                    double y = livingTarget.getY() + (livingTarget.getBbHeight() * 0.5);
                    double z = livingTarget.getZ();

                    ParticleEmitterInfo info = ParticleEmitterInfo.create(clientLevel, HIT_EFFECT_ID)
                            .position(x, y, z)
                            .scale(1.0f);
                    
                    AAALevel.addParticle(clientLevel, true, info);
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