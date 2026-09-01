package com.fernleaf.hostilities.client.renderer;

import com.fernleaf.hostilities.client.model.geardian.GeardianModel;
import com.fernleaf.hostilities.client.model.geardian.GeardianVariant;
import com.fernleaf.hostilities.server.entity.geardian.Geardian;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class GeardianRenderer extends MobRenderer<Geardian, GeardianModel<Geardian>> {

    public GeardianRenderer(EntityRendererProvider.Context context) {
        super(context, new GeardianModel<>(context.bakeLayer(GeardianModel.LAYER_LOCATION)), 0.8F);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull Geardian entity) {
        // If GeardianVariant is added to entity, read from entity.getVariant().getTextureLocation()
        return GeardianVariant.DEFAULT.getTextureLocation();
    }
}