package com.kaliumstudios.mekanismcustommachines.api.definition;

import java.util.Objects;

import com.kaliumstudios.mekanismcustommachines.api.energy.EnergyProfile;
import com.kaliumstudios.mekanismcustommachines.api.layout.RecipeViewerLayout;

import mekanism.common.registration.impl.SoundEventRegistryObject;
import mekanism.common.registries.MekanismSounds;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

/**
 * Definition for a machine with a single item input and a single item output
 * (analogous to the Enrichment Chamber, Crusher, Energized Smelter).
 * <p>
 * Create instances via {@link Builder}:
 * <pre>{@code
 * ItemToItemDefinition def = ItemToItemDefinition.builder("mymod:my_machine")
 *     .processName("Processing")
 *     .energy(200, 10_000)
 *     .build();
 * }</pre>
 */
public record ItemToItemDefinition(
        ResourceLocation id,
        String processName,
        EnergyProfile energy,
        int baseTicksRequired,
        SoundEventRegistryObject<SoundEvent> sound,
        RecipeViewerLayout viewerLayout
) implements MachineDefinition {

    public ItemToItemDefinition {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(processName, "processName");
        Objects.requireNonNull(energy, "energy");
        Objects.requireNonNull(sound, "sound");
        Objects.requireNonNull(viewerLayout, "viewerLayout");
        if (baseTicksRequired <= 0) throw new IllegalArgumentException("baseTicksRequired must be > 0");
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
        private SoundEventRegistryObject<SoundEvent> sound = MekanismSounds.ENRICHMENT_CHAMBER;
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

        /** Builds the immutable {@link ItemToItemDefinition}. */
        public ItemToItemDefinition build() {
            return new ItemToItemDefinition(id, processName, energy, baseTicksRequired, sound, viewerLayout);
        }
    }
}
