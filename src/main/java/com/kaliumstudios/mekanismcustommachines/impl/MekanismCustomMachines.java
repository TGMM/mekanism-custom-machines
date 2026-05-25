package com.kaliumstudios.mekanismcustommachines.impl;

import com.kaliumstudios.mekanismcustommachines.api.MachineRegistry;
import com.kaliumstudios.mekanismcustommachines.api.MekanismCustomMachinesAPI;
import com.kaliumstudios.mekanismcustommachines.api.event.RegisterCustomMachinesEvent;
import com.kaliumstudios.mekanismcustommachines.impl.client.ScreenBinder;
import com.kaliumstudios.mekanismcustommachines.impl.registry.MachineRegistryImpl;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Main mod entry point.
 * <p>
 * Responsibilities:
 * <ol>
 *   <li>Wire {@link MachineRegistryImpl} as the {@link MachineRegistry} delegate.</li>
 *   <li>Fire {@link RegisterCustomMachinesEvent} so other mods — and KubeJS
 *       scripts via the bridge in
 *       {@link com.kaliumstudios.mekanismcustommachines.integration.kubejs.MekanismCustomMachinesKubeJSPlugin} —
 *       can register their machines before NeoForge's {@code RegisterEvent} closes.</li>
 *   <li>Freeze the registry after the event to catch late registrations.</li>
 *   <li>Register the client screen binder.</li>
 * </ol>
 * <p>
 * No machines are registered out of the box. End-to-end demo scripts that
 * exercise every supported shape live in {@code examples/}; copy them into
 * a KubeJS pack to try them.
 */
@Mod(MekanismCustomMachinesAPI.MODID)
public class MekanismCustomMachines {

    public MekanismCustomMachines(IEventBus modEventBus, ModContainer modContainer) {
        MekanismCustomMachinesAPI.LOGGER.info(
                "[MekanismCustomMachines] @Mod constructor — kubejs loaded: {}",
                ModList.get().isLoaded("kubejs"));

        // ── 1. Wire the registry implementation ────────────────────────────
        MachineRegistryImpl registry = new MachineRegistryImpl(modEventBus);
        MachineRegistry.setDelegate(registry);

        // ── 2. Fire the public registration event ──────────────────────────
        // Other mods subscribe to RegisterCustomMachinesEvent (NeoForge game
        // bus) to add their machines. The KubeJS bridge also subscribes here
        // and forwards definitions registered through startup scripts.
        MekanismCustomMachinesAPI.LOGGER.info("[MekanismCustomMachines] posting RegisterCustomMachinesEvent");
        NeoForge.EVENT_BUS.post(new RegisterCustomMachinesEvent());
        MekanismCustomMachinesAPI.LOGGER.info(
                "[MekanismCustomMachines] RegisterCustomMachinesEvent finished — {} machine(s) registered",
                MachineRegistry.all().size());

        // ── 3. Freeze registry to prevent late registrations ───────────────
        registry.freeze();

        // ── 4. Register screen binder (client-side) ────────────────────────
        modEventBus.addListener(ScreenBinder::registerScreens);
    }
}
