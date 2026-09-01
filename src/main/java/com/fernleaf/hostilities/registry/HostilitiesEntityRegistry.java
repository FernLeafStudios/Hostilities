package com.fernleaf.hostilities.registry;

import com.fernleaf.hostilities.Hostilities;
import com.fernleaf.hostilities.client.model.geardian.GeardianModel;
import com.fernleaf.hostilities.client.renderer.GeardianRenderer;
import com.fernleaf.hostilities.server.entity.geardian.Geardian;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class HostilitiesEntityRegistry {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, Hostilities.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<Geardian>> GUARDIAN = ENTITIES.register("geardian",
            () -> EntityType.Builder.of(Geardian::new, MobCategory.MONSTER)
                    .sized(1.4F, 2.7F)
                    .clientTrackingRange(16)
                    .build("geardian")
    );

    public static void register(IEventBus eventBus) {
        ENTITIES.register(eventBus);
    }

    @EventBusSubscriber(modid = Hostilities.MODID)
    public static class AttributesRegister {
        @SubscribeEvent
        public static void registerAttributes(EntityAttributeCreationEvent event) {
            event.put(GUARDIAN.get(), Geardian.createAttributes().build());
        }
    }

    @EventBusSubscriber(modid = Hostilities.MODID, value = Dist.CLIENT)
    public static class ClientRegister {
        @SubscribeEvent
        public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(GUARDIAN.get(), GeardianRenderer::new);
        }

        @SubscribeEvent
        public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
            event.registerLayerDefinition(GeardianModel.LAYER_LOCATION, GeardianModel::createBodyLayer);
        }
    }
}