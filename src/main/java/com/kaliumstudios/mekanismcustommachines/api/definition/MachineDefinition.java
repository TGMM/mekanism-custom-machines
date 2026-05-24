package com.kaliumstudios.mekanismcustommachines.api.definition;

import com.kaliumstudios.mekanismcustommachines.api.energy.EnergyProfile;
import com.kaliumstudios.mekanismcustommachines.api.layout.RecipeViewerLayout;

import mekanism.common.registration.impl.SoundEventRegistryObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

/**
 * Describes a custom Mekanism machine to be registered at mod construction time.
 * <p>
 * Each concrete subtype represents a distinct recipe shape family. Use the
 * nested {@code Builder} on each subtype for fluent construction.
 * <p>
 * All v1 machines are built with {@code MachineBuilder.createMachine()} and
 * therefore support speed, energy, and muffling upgrades via the upgrade tab
 * but cannot be upgraded to factory tiers with a tier installer (v2 addition).
 */
public sealed interface MachineDefinition
        permits ItemToItemDefinition, ItemChemicalToItemDefinition, CombinerDefinition,
                SawmillDefinition, ItemToChemicalDefinition {

    /**
     * Fully-qualified registry id for this machine, e.g. {@code mymod:my_machine}.
     * Used as the block/item/recipe-type registry name.
     */
    ResourceLocation id();

    /**
     * Short name shown in the GUI progress bar tooltip, e.g. {@code "Processing"}.
     */
    String processName();

    /** Energy usage and storage profile. */
    EnergyProfile energy();

    /** Base number of ticks to process one recipe before upgrades are applied. */
    int baseTicksRequired();

    /** Ambient sound played while the machine is running. */
    SoundEventRegistryObject<SoundEvent> sound();

    /** Recipe viewer (JEI/EMI) overlay bounds. */
    RecipeViewerLayout viewerLayout();
}
