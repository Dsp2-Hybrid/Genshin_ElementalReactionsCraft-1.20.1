package com.dsp2.genshinerc.common.world.entity.client.renderer;
import com.dsp2.genshinerc.common.world.entity.client.model.ShellModel;
import com.dsp2.genshinerc.common.world.entity.client.skin.Skins;
import com.dsp2.genshinerc.common.world.entity.client.skin.UnitSkin;
import com.dsp2.genshinerc.common.world.entity.combat.TrainingAvatar;
import com.dsp2.genshinerc.common.world.entity.unit.TrainingUnit;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.EntityType;
import yesman.epicfight.api.client.model.Meshes.MeshAccessor;
public class TrainingAvatarRenderer extends CompositeAvatar<TrainingUnit, TrainingAvatar, ShellModel, UnitShellRenderer, UnitSkin> {
    public TrainingAvatarRenderer(EntityRendererProvider.Context ctx, EntityType<?> type) { super(ctx, type); }
    @Override public MeshAccessor<UnitSkin> getDefaultMesh() { return Skins.UNIT; }
}