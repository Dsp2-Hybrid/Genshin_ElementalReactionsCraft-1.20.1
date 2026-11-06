package com.dsp2.genshinerc.common.world.entity.registry;
import com.dsp2.genshinerc.common.world.entity.combat.TrainingAvatar;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import yesman.epicfight.world.capabilities.provider.EntityPatchProvider;
import yesman.epicfight.world.capabilities.entitypatch.MobPatch;
public class AvatarBinding {
    public static void bind() {
        EntityPatchProvider.putCustomEntityPatch(UnitRegistry.TRAINING.get(), (entity) -> TrainingAvatar::new);
    }
}