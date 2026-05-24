package com.kaliumstudios.mekanismcustommachines.api.event;

import com.kaliumstudios.mekanismcustommachines.api.MachineRegistry;
import com.kaliumstudios.mekanismcustommachines.api.definition.MachineDefinition;
import com.kaliumstudios.mekanismcustommachines.api.handle.RegisteredMachine;

import net.neoforged.bus.api.Event;

/**
 * Fired on the NeoForge mod event bus during MekanismCustomMachines's
 * {@code @Mod} constructor, before NeoForge's {@code RegisterEvent}.
 * <p>
 * Subscribe to this event from your own {@code @Mod} constructor (or a static
 * initializer that runs during construction) to register custom machines:
 * <pre>{@code
 * @SubscribeEvent
 * public static void onRegisterMachines(RegisterCustomMachinesEvent event) {
 *     event.register(
 *         ItemToItemDefinition.builder("mymod:my_machine")
 *             .processName("Processing")
 *             .energy(200, 10_000)
 *             .build()
 *     );
 * }
 * }</pre>
 * <p>
 * Handlers <em>must</em> be synchronous. Do not defer registration to a later
 * lifecycle phase — the NeoForge deferred registration window will have closed.
 */
public class RegisterCustomMachinesEvent extends Event {

    public static final String ID = "mekanismcustommachines:register_machines";

    public RegisterCustomMachinesEvent() {}

    /**
     * Registers a machine described by the given {@link MachineDefinition}.
     *
     * @return a {@link RegisteredMachine} handle usable after loading completes
     */
    public RegisteredMachine register(MachineDefinition definition) {
        return MachineRegistry.register(definition);
    }
}
