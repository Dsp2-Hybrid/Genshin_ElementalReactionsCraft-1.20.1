package com.dsp2.genshinerc.client.render.hud.bar.api;

import com.dsp2.genshinerc.common.network.GenshinercNetwork;
import com.dsp2.genshinerc.common.network.sync.packet.SyncElementsPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.network.PacketDistributor;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings({"deprecation", "removal"})
public class ElementManager {
    private static final String NBT_KEY = "GenshinElements";
    private static final Map<String, List<ResourceLocation>> ANIMATION_CACHE = new ConcurrentHashMap<>();

    public static void addElement(LivingEntity entity, ElementType type) {
        if (entity.level().isClientSide()) return;
        List<ElementType> list = readNbt(entity);
        if (list.size() < 3 && !list.contains(type)) {
            list.add(type);
            writeNbt(entity, list);
            sync(entity, list);
        }
    }

    public static void clearElements(LivingEntity entity) {
        if (entity.level().isClientSide()) return;
        entity.getPersistentData().remove(NBT_KEY);
        sync(entity, List.of());
    }

    public static List<ElementType> getElements(LivingEntity entity) {
        if (entity.level().isClientSide()) {
            return ClientElementCache.get(entity.getId());
        }
        return readNbt(entity);
    }

    private static List<ElementType> readNbt(LivingEntity entity) {
        List<ElementType> result = new ArrayList<>();
        String current = entity.getPersistentData().getString(NBT_KEY);
        if (current.isEmpty()) return result;
        for (String s : current.split(",")) {
            try {
                result.add(ElementType.valueOf(s));
            } catch (IllegalArgumentException ignored) {}
        }
        return result;
    }

    private static void writeNbt(LivingEntity entity, List<ElementType> list) {
        entity.getPersistentData().putString(NBT_KEY, String.join(",", list.stream().map(Enum::name).toList()));
    }

    private static void sync(LivingEntity entity, List<ElementType> list) {
        if (!entity.level().isClientSide()) {
            GenshinercNetwork.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity),
                    new SyncElementsPacket(entity.getId(), list));
        }
    }

    public static ResourceLocation getCurrentFrame(ElementType type) {
        if (!type.isAnimated()) {
            return new ResourceLocation("genshinerc", "textures/hud/" + type.getStaticPath());
        }

        List<ResourceLocation> frames = ANIMATION_CACHE.computeIfAbsent(type.getAnimFolder(), folder -> {
            List<ResourceLocation> list = new ArrayList<>();
            try {
                Minecraft.getInstance().getResourceManager().listResources("textures/hud/" + folder, rl -> rl.getPath().endsWith(".png"))
                        .forEach((rl, res) -> list.add(rl));

                list.sort(Comparator.comparing(rl -> {
                    String path = rl.getPath();
                    String fileName = path.substring(path.lastIndexOf('/') + 1, path.lastIndexOf('.'));
                    int lastUnderscore = fileName.lastIndexOf('_');
                    // Fixed the sorting logic bug pointed out by Opus
                    if (lastUnderscore != -1 && lastUnderscore + 1 < fileName.length()) {
                        try {
                            return Integer.parseInt(fileName.substring(lastUnderscore + 1));
                        } catch (NumberFormatException ignored) {}
                    }
                    return 0;
                }));
            } catch (Exception ignored) {}
            return list;
        });

        if (frames.isEmpty()) {
            return new ResourceLocation("genshinerc", "textures/hud/" + type.getStaticPath());
        }

        int index = (int) ((System.currentTimeMillis() / 41.66) % frames.size());
        return frames.get(index);
    }
}