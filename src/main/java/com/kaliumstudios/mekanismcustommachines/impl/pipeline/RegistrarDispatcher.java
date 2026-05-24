package com.kaliumstudios.mekanismcustommachines.impl.pipeline;

import com.kaliumstudios.mekanismcustommachines.api.definition.CombinerDefinition;
import com.kaliumstudios.mekanismcustommachines.api.definition.ItemChemicalToItemDefinition;
import com.kaliumstudios.mekanismcustommachines.api.definition.ItemToItemDefinition;
import com.kaliumstudios.mekanismcustommachines.api.definition.MachineDefinition;
import com.kaliumstudios.mekanismcustommachines.impl.registry.RegisteredMachineImpl;

/**
 * Routes a {@link MachineDefinition} to the correct shape-specific
 * {@link MachineRegistrar} implementation.
 * <p>
 * To add a new recipe shape, add a {@code case} branch here and a corresponding
 * {@link MachineRegistrar} implementation.
 */
public final class RegistrarDispatcher {

    private RegistrarDispatcher() {}

    public static RegisteredMachineImpl dispatch(MachineDefinition definition, DeferredBundle bundle) {
        return switch (definition) {
            case ItemToItemDefinition d -> ItemToItemRegistrar.INSTANCE.register(d, bundle);
            case ItemChemicalToItemDefinition d -> ItemChemicalToItemRegistrar.INSTANCE.register(d, bundle);
            case CombinerDefinition d -> CombinerRegistrar.INSTANCE.register(d, bundle);
        };
    }
}
