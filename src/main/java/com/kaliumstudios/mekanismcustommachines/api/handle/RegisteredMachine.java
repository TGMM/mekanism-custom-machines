package com.kaliumstudios.mekanismcustommachines.api.handle;

import com.kaliumstudios.mekanismcustommachines.api.definition.MachineDefinition;

import net.minecraft.resources.ResourceLocation;

/**
 * Opaque handle returned when a machine is successfully registered via
 * {@link com.kaliumstudios.mekanismcustommachines.api.MachineRegistry}.
 * <p>
 * Downstream code can store this reference to query the machine's holders
 * after registration is complete.
 */
public interface RegisteredMachine {

    /** The id that was supplied in the {@link MachineDefinition}. */
    ResourceLocation id();

    /** The original definition used to register this machine. */
    MachineDefinition definition();

    /** Read-only view of the NeoForge registry holders for this machine. */
    MachineHolders holders();
}
