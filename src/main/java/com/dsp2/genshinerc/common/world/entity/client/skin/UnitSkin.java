package com.dsp2.genshinerc.common.world.entity.client.skin;
import java.util.List;
import java.util.Map;
import yesman.epicfight.api.client.model.Mesh.RenderProperties;
import yesman.epicfight.api.client.model.MeshPartDefinition;
import yesman.epicfight.api.client.model.SkinnedMesh;
import yesman.epicfight.api.client.model.VertexBuilder;
public class UnitSkin extends SkinnedMesh {
    public UnitSkin(Map<String, Number[]> arrayMap, Map<MeshPartDefinition, List<VertexBuilder>> parts, SkinnedMesh parent, RenderProperties props) {
        super(arrayMap, parts, parent, props);
    }
}