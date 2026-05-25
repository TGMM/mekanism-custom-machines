package com.kaliumstudios.mekanismcustommachines.integration.kubejs.startup;

import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;

/**
 * KubeJS event group exposed to scripts as the {@code MekanismCustomMachines}
 * global binding.
 * <p>
 * Currently contains a single handler, {@code registerMachines}, which scripts
 * use to register custom Mekanism machines during the startup phase. Example:
 * <pre>{@code
 * MekanismCustomMachines.registerMachines(event => {
 *     event.itemToItem('mymod:my_machine', m => {
 *         m.processName('Processing')
 *         m.energy(200, 10000)
 *         m.ticks(200)
 *     })
 * })
 * }</pre>
 * <p>
 * The event is fired by our {@code @Mod} constructor in response to
 * {@link com.kaliumstudios.mekanismcustommachines.api.event.RegisterCustomMachinesEvent}.
 * Because the registration window for blocks / tile-entities closes shortly
 * after that, this must remain a <em>startup</em> handler — never a server
 * handler — and our mod must be loaded after KubeJS (see {@code neoforge.mods.toml}).
 */
public interface MekanismCustomMachinesEvents {

    EventGroup GROUP = EventGroup.of("MekanismCustomMachines");

    EventHandler REGISTER_MACHINES = GROUP.startup("registerMachines", () -> RegisterMachinesKubeEvent.class);
}
