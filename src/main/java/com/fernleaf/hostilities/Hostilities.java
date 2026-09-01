package com.fernleaf.hostilities;

import com.fernleaf.hostilities.registry.HostilitiesEntityRegistry;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod(Hostilities.MODID)
public class Hostilities {
    public static final String MODID = "hostilities";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Hostilities(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);
        HostilitiesEntityRegistry.register(modEventBus);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("Initializing Hostilities...");
    }
}