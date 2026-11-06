package com.dsp2.genshinerc.common.world.entity.client.model;
import com.dsp2.genshinerc.common.world.entity.unit.TrainingUnit;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
public class ShellModel extends HierarchicalModel<TrainingUnit> {
    private final ModelPart root;
    public ShellModel(ModelPart root) { this.root = root.getChild("root"); }
    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition part = mesh.getRoot();
        part.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.ZERO);
        return LayerDefinition.create(mesh, 64, 64);
    }
    @Override public ModelPart root() { return root; }
    @Override public void renderToBuffer(PoseStack ps, VertexConsumer vc, int light, int overlay, float r, float g, float b, float a) {
        this.root.render(ps, vc, light, overlay, r, g, b, a);
    }
    @Override public void setupAnim(TrainingUnit e, float limb, float limbAmount, float age, float yaw, float pitch) { }
}