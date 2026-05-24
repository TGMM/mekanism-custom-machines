package com.kaliumstudios.mekanismcustommachines.impl;

import com.kaliumstudios.mekanismcustommachines.api.MachineRegistry;
import com.kaliumstudios.mekanismcustommachines.api.MekanismCustomMachinesAPI;
import com.kaliumstudios.mekanismcustommachines.api.definition.CombinerDefinition;
import com.kaliumstudios.mekanismcustommachines.api.definition.ItemChemicalToItemDefinition;
import com.kaliumstudios.mekanismcustommachines.api.definition.ItemToItemDefinition;
import com.kaliumstudios.mekanismcustommachines.api.energy.EnergyProfile;
import com.kaliumstudios.mekanismcustommachines.api.event.RegisterCustomMachinesEvent;
import com.kaliumstudios.mekanismcustommachines.impl.client.ScreenBinder;
import com.kaliumstudios.mekanismcustommachines.impl.registry.MachineRegistryImpl;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Main mod entry point.
 * <p>
 * Responsibilities:
 * <ol>
 *   <li>Wire {@link MachineRegistryImpl} as the {@link MachineRegistry} delegate.</li>
 *   <li>Fire {@link RegisterCustomMachinesEvent} so other mods (and the built-in
 *       test registration) can register their machines before NeoForge's
 *       {@code RegisterEvent} closes.</li>
 *   <li>Freeze the registry after the event to catch late registrations.</li>
 *   <li>Register the client screen binder.</li>
 * </ol>
 */
@Mod(MekanismCustomMachinesAPI.MODID)
public class MekanismCustomMachines {

    public MekanismCustomMachines(IEventBus modEventBus, ModContainer modContainer) {
        // ── 1. Wire the registry implementation ────────────────────────────
        MachineRegistryImpl registry = new MachineRegistryImpl(modEventBus);
        MachineRegistry.setDelegate(registry);

        // ── 2. Register built-in test machines ─────────────────────────────
        // These demonstrate the library API and validate each pipeline.
        // Remove or move to a dev-only config option before the first release.
        MachineRegistry.register(
                ItemToItemDefinition.builder(MekanismCustomMachinesAPI.MODID + ":test_chamber")
                        .processName("Testing")
                        .energy(EnergyProfile.defaultMachine())
                        .ticks(200)
                        .build());

        MachineRegistry.register(
                ItemChemicalToItemDefinition.builder(MekanismCustomMachinesAPI.MODID + ":test_compressor")
                        .processName("Compressing")
                        .energy(200, 10_000)
                        .maxChemical(10_000)
                        .ticks(200)
                        .build());

        MachineRegistry.register(
                CombinerDefinition.builder(MekanismCustomMachinesAPI.MODID + ":test_combiner")
                        .processName("Combining")
                        .energy(200, 10_000)
                        .ticks(200)
                        .build());

        // ── 3. Fire the public registration event ──────────────────────────
        // Other mods subscribe to RegisterCustomMachinesEvent to add their machines.
        NeoForge.EVENT_BUS.post(new RegisterCustomMachinesEvent());

        // ── 4. Freeze registry to prevent late registrations ───────────────
        registry.freeze();

        // ── 5. Register screen binder (client-side) ────────────────────────
        modEventBus.addListener(ScreenBinder::registerScreens);
    }
}
