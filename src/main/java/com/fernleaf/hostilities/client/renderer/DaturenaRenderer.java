package com.fernleaf.hostilities.client.renderer;

import com.fernleaf.hostilities.Hostilities;
import com.fernleaf.hostilities.client.model.daturena.DaturenaModel;
import com.fernleaf.hostilities.server.entity.daturena.Daturena;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class DaturenaRenderer extends MobRenderer<Daturena, DaturenaModel<Daturena>> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Hostilities.MODID, "textures/entity/daturena/daturena.png");

    public DaturenaRenderer(EntityRendererProvider.Context context) {
        super(context, new DaturenaModel<>(context.bakeLayer(DaturenaModel.LAYER_LOCATION)), 1.4F);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull Daturena entity) {
        return TEXTURE;
    }
}