package com.dsp2.genshinerc.client.render.vfx.feature.light;

import com.dsp2.genshinerc.common.core.element.ElementType;
import com.dsp2.genshinerc.common.core.reaction.CrystallizeShardDrop;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Mod.EventBusSubscriber(
        modid = "genshinerc",
        value = Dist.CLIENT,
        bus = Mod.EventBusSubscriber.Bus.MOD)
public final class CrystallizeShardClientVisuals {
    private static final Map<UUID, AaapTransientLights.Handle> LIGHTS = new HashMap<>();
    private static long lastParticleTick;

    private CrystallizeShardClientVisuals() {}

    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        event.register((stack, tintIndex) -> {
            if (tintIndex != 0 || !stack.hasTag()) return -1;
            if (!stack.getTag().contains(CrystallizeShardDrop.COLOR_KEY)) return -1;
            return stack.getTag().getInt(CrystallizeShardDrop.COLOR_KEY);
        }, net.minecraft.world.item.Items.AMETHYST_SHARD);
    }

    @Mod.EventBusSubscriber(
            modid = "genshinerc",
            value = Dist.CLIENT,
            bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static final class ClientEvents {
        private ClientEvents() {}

        @SubscribeEvent
        public static void tick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) return;

            Minecraft minecraft = Minecraft.getInstance();
            ClientLevel level = minecraft.level;

            if (level == null || minecraft.isPaused()) {
                releaseAll();
                return;
            }

            Set<UUID> visible = new HashSet<>();
            long gameTime = level.getGameTime();
            boolean particlesThisTick = gameTime - lastParticleTick >= 3L;

            if (particlesThisTick) lastParticleTick = gameTime;

            for (Entity entity : level.entitiesForRendering()) {
                if (!(entity instanceof ItemEntity item)) continue;

                ItemStack stack = item.getItem();
                if (!isCrystal(stack)) continue;

                UUID id = item.getUUID();
                visible.add(id);

                int rgb = stack.getTag().getInt(CrystallizeShardDrop.COLOR_KEY);
                int lifetime = Math.max(
                        1,
                        stack.getTag().getInt(CrystallizeShardDrop.LIFETIME_KEY));
                float lifeRatio = 1f - Math.min(1f, Math.max(0f, item.tickCount / (float) lifetime));
                float brightness = 0.12f + 0.88f * lifeRatio;
                float size = 0.75f + 0.55f * lifeRatio;

                AaapTransientLights.Handle handle = LIGHTS.get(id);

                if (handle == null || !handle.alive()) {
                    handle = AaapTransientLights.attach(
                            lightSpec(rgb, brightness),
                            item.getX(),
                            item.getY() + 0.25,
                            item.getZ(),
                            size * brightness);
                    if (handle != null) LIGHTS.put(id, handle);
                } else {
                    handle.moveTo(item.getX(), item.getY() + 0.25, item.getZ());
                    handle.setSize(size * brightness);
                }

                if (particlesThisTick && lifeRatio > 0.01f) {
                    level.addParticle(
                            dust(rgb, 0.75f + lifeRatio * 0.65f),
                            item.getX() + (level.random.nextDouble() - 0.5) * 0.22,
                            item.getY() + 0.18 + level.random.nextDouble() * 0.24,
                            item.getZ() + (level.random.nextDouble() - 0.5) * 0.22,
                            (level.random.nextDouble() - 0.5) * 0.006,
                            0.012 + lifeRatio * 0.025,
                            (level.random.nextDouble() - 0.5) * 0.006);
                }
            }

            LIGHTS.entrySet().removeIf(entry -> {
                if (visible.contains(entry.getKey())) return false;
                entry.getValue().release(0.35f);
                return true;
            });
        }

        @SubscribeEvent
        public static void onUnload(LevelEvent.Unload event) {
            if (event.getLevel().isClientSide()) releaseAll();
        }

        private static boolean isCrystal(ItemStack stack) {
            return stack.is(net.minecraft.world.item.Items.AMETHYST_SHARD)
                    && stack.hasTag()
                    && stack.getTag().contains(CrystallizeShardDrop.ELEMENT_KEY)
                    && stack.getTag().contains(CrystallizeShardDrop.COLOR_KEY);
        }

        private static AaapTransientLights.Spec lightSpec(int rgb, float brightness) {
            return new AaapTransientLights.Spec(
                    ((rgb >> 16) & 255) / 255f,
                    ((rgb >> 8) & 255) / 255f,
                    (rgb & 255) / 255f,
                    0.72f * brightness,
                    1.9f,
                    false);
        }

        private static DustParticleOptions dust(int rgb, float scale) {
            return new DustParticleOptions(
                    new Vector3f(
                            ((rgb >> 16) & 255) / 255f,
                            ((rgb >> 8) & 255) / 255f,
                            (rgb & 255) / 255f),
                    scale);
        }

        private static void releaseAll() {
            for (AaapTransientLights.Handle handle : LIGHTS.values()) {
                handle.release(0.25f);
            }
            LIGHTS.clear();
        }
    }
}