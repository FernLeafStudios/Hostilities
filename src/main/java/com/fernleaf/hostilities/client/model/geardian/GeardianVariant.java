package com.fernleaf.hostilities.client.model.geardian;

import com.fernleaf.fernframe.proprio.model.IModelVariant;
import com.fernleaf.fernframe.proprio.model.ModelVariantRegistry;
import com.fernleaf.fernframe.proprio.model.TextureUtils;
import com.fernleaf.hostilities.Hostilities;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

public enum GeardianVariant implements IModelVariant<GeardianVariant.ModelType> {
    DEFAULT(0, "default", GeardianModel.LAYER_LOCATION, ModelType.GEARDIAN),
    RUSTED(1, "rusted", GeardianModel.LAYER_LOCATION, ModelType.GEARDIAN),
    GOLDEN(2, "golden", GeardianModel.LAYER_LOCATION, ModelType.GEARDIAN);

    private static final Function<Integer, GeardianVariant> LOOKUP =
            ModelVariantRegistry.createLookup(values(), DEFAULT);

    public final int id;
    private final ModelLayerLocation layerLocation;
    private final ModelType modelType;
    private final ResourceLocation textureLocation;

    GeardianVariant(int id, String name, ModelLayerLocation layerLocation, ModelType modelType) {
        this.id = id;
        this.layerLocation = layerLocation;
        this.modelType = modelType;
        this.textureLocation = TextureUtils.buildEntityTexture(Hostilities.MODID, "geardian", "geardian_" + name);
    }

    @Override public int getId() { return this.id; }
    @Override public ModelLayerLocation getLayerLocation() { return this.layerLocation; }
    @Override public ModelType getModelType() { return this.modelType; }
    @Override public ResourceLocation getTextureLocation() { return this.textureLocation; }

    public enum ModelType {
        GEARDIAN
    }

    public static GeardianVariant byId(int id) {
        return LOOKUP.apply(id);
    }
}