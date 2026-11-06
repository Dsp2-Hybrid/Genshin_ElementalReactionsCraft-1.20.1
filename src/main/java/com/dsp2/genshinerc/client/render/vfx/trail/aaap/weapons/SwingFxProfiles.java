package com.dsp2.genshinerc.client.render.vfx.trail.aaap.weapons;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import yesman.epicfight.api.utils.math.Vec3f;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

@SuppressWarnings({"deprecation", "removal"})
public final class SwingFxProfiles {
    public static final ResourceLocation HIT_FIRE_C01 = new ResourceLocation("genshinerc", "hit_fire_c01");

    public static final SwingFxProfile DEFAULT = new SwingFxProfile(
            HIT_FIRE_C01,
            null,
            new Vec3f(0.0F, 0.0F, -0.2F),
            new Vec3f(0.0F, 0.0F, -1.5F),
            6.0F, 4, 0.35F, 0.5F, 0.85F, 0.02F, 3);

    private record Entry(Predicate<ItemStack> matcher, SwingFxProfile profile) {}

    private static final List<Entry> ENTRIES = new CopyOnWriteArrayList<>();

    private SwingFxProfiles() {}

    public static void register(Predicate<ItemStack> matcher, SwingFxProfile profile) {
        ENTRIES.add(new Entry(matcher, profile));
    }

    @Nullable
    public static SwingFxProfile resolve(LivingEntityPatch<?> patch, ItemStack stack) {
        if (stack.isEmpty()) return null;
        for (Entry entry : ENTRIES) {
            if (entry.matcher().test(stack)) return entry.profile();
        }
        return DEFAULT;
    }
}