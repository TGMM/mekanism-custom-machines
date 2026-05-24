package com.kaliumstudios.mekanismcustommachines;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import mekanism.client.ClientRegistrationUtil;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.fml.ModContainer;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(MekanismCustomMachines.MODID)
public class MekanismCustomMachines {
    public static final String MODID = "mekanismcustommachines";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static List<ContainerTypeRegistryObject<MekanismTileContainer<GenericMachineTileEntity>>> screenContainers = new ArrayList<>();

    public MekanismCustomMachines(IEventBus modEventBus, ModContainer modContainer) {
        screenContainers = MekanismCustomMachinesRuntimeTest.RegisterMachineAtRuntime(modEventBus, MODID,
                new MachineParameters("test_chamber", "testing"));

        modEventBus.addListener(MekanismCustomMachines::registerScreens);
    }

    public static void registerScreens(RegisterMenuScreensEvent event) {
        for (ContainerTypeRegistryObject<MekanismTileContainer<GenericMachineTileEntity>> containerType : screenContainers) {
            ClientRegistrationUtil.registerElectricScreen(event, containerType);
        }
    }
}
