package com.dsp2.genshinerc.client.render.entity.elereaction.overlay;

import com.dsp2.genshinerc.common.core.element.ElementAuraManager;
import com.dsp2.genshinerc.common.core.element.ElementType;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.LivingEntity;

public class CryoFlowOverlay<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
    public CryoFlowOverlay(RenderLayerParent<T, M> parent) { super(parent); }
    @Override
    public void render(PoseStack ps, MultiBufferSource buf, int light, T e, float ls, float lsa, float pt, float age, float yaw, float pitch) {
        if (!ElementAuraManager.hasAura(e, ElementType.FROZEN)) return;
    }
}