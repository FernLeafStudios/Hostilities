package com.fernleaf.hostilities.client.renderer;

import com.fernleaf.hostilities.Hostilities;
import com.fernleaf.hostilities.client.model.hounden.HoundenModel;
import com.fernleaf.hostilities.server.entity.hounden.Hounden;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class HoundenRenderer extends MobRenderer<Hounden, HoundenModel<Hounden>> {

    public HoundenRenderer(EntityRendererProvider.Context context) {
        super(context, new HoundenModel<>(context.bakeLayer(HoundenModel.LAYER_LOCATION)), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(Hounden entity) {
        return ResourceLocation.fromNamespaceAndPath(Hostilities.MODID, "textures/entity/hounden/hounden.png");
    }
}