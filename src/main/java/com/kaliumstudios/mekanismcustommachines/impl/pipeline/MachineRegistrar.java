package com.kaliumstudios.mekanismcustommachines.impl.pipeline;

import com.kaliumstudios.mekanismcustommachines.api.definition.MachineDefinition;
import com.kaliumstudios.mekanismcustommachines.impl.registry.RegisteredMachineImpl;

/**
 * Internal SPI for shape-specific machine registration logic.
 * <p>
 * Each implementation handles one recipe-shape family (Item→Item,
 * Item+Chemical→Item, etc.) and produces a {@link RegisteredMachineImpl}
 * containing all resolved deferred holders.
 * <p>
 * New shapes are added by:
 * <ol>
 *   <li>Adding a {@code permits} clause to {@link MachineDefinition} and a new
 *       concrete definition record in {@code api/definition/}.</li>
 *   <li>Implementing this interface for that shape.</li>
 *   <li>Adding a {@code case} branch in {@link RegistrarDispatcher}.</li>
 * </ol>
 */
sealed interface MachineRegistrar<D extends MachineDefinition>
        permits ItemToItemRegistrar, ItemChemicalToItemRegistrar, CombinerRegistrar, SawmillRegistrar {

    RegisteredMachineImpl register(D definition, DeferredBundle bundle);
}
