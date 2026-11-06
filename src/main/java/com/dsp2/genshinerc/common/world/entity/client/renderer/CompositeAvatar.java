package com.dsp2.genshinerc.common.world.entity.client.renderer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import yesman.epicfight.api.client.model.Meshes.MeshAccessor;
import yesman.epicfight.api.client.model.SkinnedMesh;
import yesman.epicfight.client.renderer.patched.entity.PatchedLivingEntityRenderer;
import yesman.epicfight.world.capabilities.entitypatch.MobPatch;
public abstract class CompositeAvatar<E extends Mob, P extends MobPatch<E>, M extends HierarchicalModel<E>, R extends LivingEntityRenderer<E, M>, S extends SkinnedMesh>
        extends PatchedLivingEntityRenderer<E, P, M, R, S> {
    public CompositeAvatar(EntityRendererProvider.Context ctx, EntityType<?> type) { super(ctx, type); }
    @Override public abstract MeshAccessor<S> getDefaultMesh();
}