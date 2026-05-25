package com.kaliumstudios.mekanismcustommachines.api.event;

import com.kaliumstudios.mekanismcustommachines.api.MachineRegistry;
import com.kaliumstudios.mekanismcustommachines.api.MekanismCustomMachinesAPI;
import com.kaliumstudios.mekanismcustommachines.api.definition.MachineDefinition;
import com.kaliumstudios.mekanismcustommachines.api.handle.RegisteredMachine;

import net.neoforged.bus.api.Event;

/**
 * Fired on {@link MekanismCustomMachinesAPI#EVENT_BUS} during
 * MekanismCustomMachines's {@code @Mod} constructor, before NeoForge's
 * {@code RegisterEvent}.
 * <p>
 * Subscribe to this event from your own {@code @Mod} constructor (or any code
 * that runs during mod construction) to register custom machines:
 * <pre>{@code
 * public MyMod(IEventBus modEventBus) {
 *     MekanismCustomMachinesAPI.EVENT_BUS.addListener(
 *             RegisterCustomMachinesEvent.class,
 *             event -> event.register(
 *                     ItemToItemDefinition.builder("mymod:my_machine")
 *                             .processName("Processing")
 *                             .energy(200, 10_000)
 *                             .build()
 *             )
 *     );
 * }
 * }</pre>
 * <p>
 * Use the dedicated {@code MekanismCustomMachinesAPI.EVENT_BUS} —
 * <strong>not</strong> {@code NeoForge.EVENT_BUS}. The latter is intentionally
 * shut down during mod construction (and silently drops events posted to it),
 * so the event would never reach any listener.
 * <p>
 * Handlers <em>must</em> be synchronous. Do not defer registration to a later
 * lifecycle phase — the NeoForge deferred-registration window closes shortly
 * after this event fires.
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
