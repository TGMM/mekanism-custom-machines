package com.kaliumstudios.mekanismcustommachines.impl.client;

import com.kaliumstudios.mekanismcustommachines.api.MachineRegistry;
import com.kaliumstudios.mekanismcustommachines.api.definition.ItemChemicalToItemDefinition;
import com.kaliumstudios.mekanismcustommachines.api.definition.ItemToItemDefinition;
import com.kaliumstudios.mekanismcustommachines.api.definition.MachineDefinition;
import com.kaliumstudios.mekanismcustommachines.impl.tile.GenericAdvancedElectricMachineTile;
import com.kaliumstudios.mekanismcustommachines.impl.tile.GenericElectricMachineTile;

import mekanism.client.ClientRegistrationUtil;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

/**
 * Registers screens for all custom machines at client setup time.
 * <p>
 * Iterates {@link MachineRegistry#all()} and dispatches based on the
 * {@link MachineDefinition} subtype to the appropriate Mekanism screen
 * factory (electric, advanced electric, etc.). When a new shape is added in
 * Phase 4, add a matching {@code case} branch here.
 */
public final class ScreenBinder {

    private ScreenBinder() {}

    public static void registerScreens(RegisterMenuScreensEvent event) {
        MachineRegistry.all().forEach(machine -> dispatch(event, machine.definition(), machine.holders().containerType()));
    }

    @SuppressWarnings("unchecked")
    private static void dispatch(RegisterMenuScreensEvent event, MachineDefinition def, ContainerTypeRegistryObject<?> rawType) {
        switch (def) {
            case ItemToItemDefinition ignored ->
                    ClientRegistrationUtil.registerElectricScreen(event,
                            (ContainerTypeRegistryObject<MekanismTileContainer<GenericElectricMachineTile>>) rawType);
            case ItemChemicalToItemDefinition ignored ->
                    ClientRegistrationUtil.registerAdvancedElectricScreen(event,
                            (ContainerTypeRegistryObject<MekanismTileContainer<GenericAdvancedElectricMachineTile>>) rawType);
        }
    }
}
