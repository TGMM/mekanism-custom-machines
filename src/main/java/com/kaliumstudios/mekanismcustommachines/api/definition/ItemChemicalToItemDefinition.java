package com.kaliumstudios.mekanismcustommachines.api.definition;

import java.util.Objects;

import com.kaliumstudios.mekanismcustommachines.api.energy.EnergyProfile;
import com.kaliumstudios.mekanismcustommachines.api.layout.RecipeViewerLayout;

import mekanism.common.registration.impl.SoundEventRegistryObject;
import mekanism.common.registries.MekanismSounds;
import mekanism.common.tile.prefab.TileEntityAdvancedElectricMachine;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

/**
 * Definition for a machine with a single item input, a single chemical input
 * and a single item output (analogous to the Osmium Compressor, Purification
 * Chamber, Chemical Injection Chamber).
 * <p>
 * Per-tick chemical consumption is the default Mekanism behaviour ("per-tick
 * usage" enabled on the recipe). Construct via {@link Builder}:
 * <pre>{@code
 * ItemChemicalToItemDefinition def = ItemChemicalToItemDefinition.builder("mymod:my_compressor")
 *     .processName("Compressing")
 *     .energy(200, 10_000)
 *     .maxChemical(10_000)
 *     .build();
 * }</pre>
 */
public record ItemChemicalToItemDefinition(
        ResourceLocation id,
        String processName,
        EnergyProfile energy,
        int baseTicksRequired,
        long maxChemical,
        SoundEventRegistryObject<SoundEvent> sound,
        RecipeViewerLayout viewerLayout
) implements MachineDefinition {

    public ItemChemicalToItemDefinition {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(processName, "processName");
        Objects.requireNonNull(energy, "energy");
        Objects.requireNonNull(sound, "sound");
        Objects.requireNonNull(viewerLayout, "viewerLayout");
        if (baseTicksRequired <= 0) throw new IllegalArgumentException("baseTicksRequired must be > 0");
        if (maxChemical <= 0) throw new IllegalArgumentException("maxChemical must be > 0");
    }

    /** Returns a builder pre-populated with sensible defaults. */
    public static Builder builder(String id) {
        return builder(ResourceLocation.parse(id));
    }

    /** Returns a builder pre-populated with sensible defaults. */
    public static Builder builder(ResourceLocation id) {
        return new Builder(id);
    }

    public static final class Builder {
        private final ResourceLocation id;
        private String processName;
        private EnergyProfile energy = EnergyProfile.defaultMachine();
        private int baseTicksRequired = 200;
        private long maxChemical = TileEntityAdvancedElectricMachine.MAX_GAS;
        private SoundEventRegistryObject<SoundEvent> sound = MekanismSounds.OSMIUM_COMPRESSOR;
        private RecipeViewerLayout viewerLayout = RecipeViewerLayout.ELECTRIC;

        private Builder(ResourceLocation id) {
            this.id = Objects.requireNonNull(id, "id");
            this.processName = id.getPath().replace('_', ' ');
        }

        /** Sets the process name shown in the GUI progress tooltip. */
        public Builder processName(String processName) {
            this.processName = Objects.requireNonNull(processName);
            return this;
        }

        /** Sets the energy usage and storage profile. */
        public Builder energy(EnergyProfile energy) {
            this.energy = Objects.requireNonNull(energy);
            return this;
        }

        /** Sets energy from fixed long values (in FE). */
        public Builder energy(long usageFE, long storageFE) {
            return energy(EnergyProfile.of(usageFE, storageFE));
        }

        /** Sets the base processing duration in ticks (before speed upgrades). */
        public Builder ticks(int ticks) {
            this.baseTicksRequired = ticks;
            return this;
        }

        /** Sets the chemical tank capacity in mB. Defaults to Mekanism's 210 mB. */
        public Builder maxChemical(long capacity) {
            this.maxChemical = capacity;
            return this;
        }

        /** Sets the ambient running sound. */
        public Builder sound(SoundEventRegistryObject<SoundEvent> sound) {
            this.sound = Objects.requireNonNull(sound);
            return this;
        }

        /** Sets the recipe viewer overlay bounds. */
        public Builder viewerLayout(RecipeViewerLayout layout) {
            this.viewerLayout = Objects.requireNonNull(layout);
            return this;
        }

        /** Builds the immutable {@link ItemChemicalToItemDefinition}. */
        public ItemChemicalToItemDefinition build() {
            return new ItemChemicalToItemDefinition(id, processName, energy, baseTicksRequired, maxChemical, sound, viewerLayout);
        }
    }
}
