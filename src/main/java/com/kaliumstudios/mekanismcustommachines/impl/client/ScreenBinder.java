package com.kaliumstudios.mekanismcustommachines.impl.client;

import com.kaliumstudios.mekanismcustommachines.api.MachineRegistry;
import com.kaliumstudios.mekanismcustommachines.api.definition.ChemicalToChemicalDefinition;
import com.kaliumstudios.mekanismcustommachines.api.definition.ChemicalToItemDefinition;
import com.kaliumstudios.mekanismcustommachines.api.definition.CombinerDefinition;
import com.kaliumstudios.mekanismcustommachines.api.definition.ItemChemicalToItemDefinition;
import com.kaliumstudios.mekanismcustommachines.api.definition.ItemToChemicalDefinition;
import com.kaliumstudios.mekanismcustommachines.api.definition.ItemToItemDefinition;
import com.kaliumstudios.mekanismcustommachines.api.definition.MachineDefinition;
import com.kaliumstudios.mekanismcustommachines.api.definition.SawmillDefinition;
import com.kaliumstudios.mekanismcustommachines.impl.tile.GenericAdvancedElectricMachineTile;
import com.kaliumstudios.mekanismcustommachines.impl.tile.GenericChemicalToChemicalTile;
import com.kaliumstudios.mekanismcustommachines.impl.tile.GenericChemicalToItemTile;
import com.kaliumstudios.mekanismcustommachines.impl.tile.GenericCombinerTile;
import com.kaliumstudios.mekanismcustommachines.impl.tile.GenericElectricMachineTile;
import com.kaliumstudios.mekanismcustommachines.impl.tile.GenericItemToChemicalTile;
import com.kaliumstudios.mekanismcustommachines.impl.tile.GenericSawmillTile;

import mekanism.client.ClientRegistrationUtil;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

/**
 * Registers screens for all custom machines at client setup time.
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
            case CombinerDefinition ignored ->
                    ClientRegistrationUtil.registerScreen(event,
                            (ContainerTypeRegistryObject<MekanismTileContainer<GenericCombinerTile>>) rawType,
                            GenericCombinerScreen::new);
            case SawmillDefinition ignored ->
                    ClientRegistrationUtil.registerScreen(event,
                            (ContainerTypeRegistryObject<MekanismTileContainer<GenericSawmillTile>>) rawType,
                            GenericSawmillScreen::new);
            case ItemToChemicalDefinition ignored ->
                    ClientRegistrationUtil.registerScreen(event,
                            (ContainerTypeRegistryObject<MekanismTileContainer<GenericItemToChemicalTile>>) rawType,
                            GenericItemToChemicalScreen::new);
            case ChemicalToItemDefinition ignored ->
                    ClientRegistrationUtil.registerScreen(event,
                            (ContainerTypeRegistryObject<MekanismTileContainer<GenericChemicalToItemTile>>) rawType,
                            GenericChemicalToItemScreen::new);
            case ChemicalToChemicalDefinition ignored ->
                    ClientRegistrationUtil.registerScreen(event,
                            (ContainerTypeRegistryObject<MekanismTileContainer<GenericChemicalToChemicalTile>>) rawType,
                            GenericChemicalToChemicalScreen::new);
        }
    }
}
