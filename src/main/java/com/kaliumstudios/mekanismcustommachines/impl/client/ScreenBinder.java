package com.kaliumstudios.mekanismcustommachines.impl.client;

import com.kaliumstudios.mekanismcustommachines.api.MachineRegistry;
import com.kaliumstudios.mekanismcustommachines.impl.tile.GenericElectricMachineTile;

import mekanism.client.ClientRegistrationUtil;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

/**
 * Registers screens for all custom machines at client setup time.
 * <p>
 * Iterates {@link MachineRegistry#all()} and calls
 * {@link ClientRegistrationUtil#registerElectricScreen} for each machine's
 * container type. This replaces the old {@code screenContainers} static list
 * on the main mod class, and is not tied to any specific tile entity type.
 */
public final class ScreenBinder {

    private ScreenBinder() {}

    @SuppressWarnings("unchecked")
    public static void registerScreens(RegisterMenuScreensEvent event) {
        MachineRegistry.all().forEach(machine -> {
            // All v1 machines use GenericElectricMachineTile or a subtype of
            // TileEntityElectricMachine, so this cast is safe. When additional
            // tile types are added in Phase 4, this dispatch will be updated.
            ContainerTypeRegistryObject<MekanismTileContainer<GenericElectricMachineTile>> containerType =
                    (ContainerTypeRegistryObject<MekanismTileContainer<GenericElectricMachineTile>>)
                    machine.holders().containerType();
            ClientRegistrationUtil.registerElectricScreen(event, containerType);
        });
    }
}
